package com.tc.netty.chat.processor;

import com.alibaba.fastjson.JSONObject;
import com.sun.xml.internal.ws.util.StringUtils;
import com.tc.netty.chat.protocol.IMDecoder;
import com.tc.netty.chat.protocol.IMEncoder;
import com.tc.netty.chat.protocol.IMMessage;
import com.tc.netty.chat.protocol.IMP;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author taosh
 * @create 2020-01-09 13:58
 */
public class MsgProcessorSingle {
    //netty现成的容器 Set          解决线程安全、性能问题
    private static ChannelGroup onlineUsers = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    //定义一些扩展属性
    public static final AttributeKey<String> NICK_NAME = AttributeKey.valueOf("nickName");
    public static final AttributeKey<String> IP_ADDR = AttributeKey.valueOf("ipAddr");
    public static final AttributeKey<JSONObject> ATTRS = AttributeKey.valueOf("attrs");
    public static final AttributeKey<String> FROM = AttributeKey.valueOf("from");

    //自定义解码器
    private IMDecoder decoder = new IMDecoder();
    //自定义编码器
    private IMEncoder encoder = new IMEncoder();

    public void porcess(Channel client, IMMessage msg){
        porcess(client, encoder.encode(msg));
    }

    /**
     * 获取用户昵称
     * @param client
     * @return
     */
    public String getNickName(Channel client){
        return client.attr(NICK_NAME).get();
    }

    /**
     * 获取地址
     * @param client
     * @return
     */
    public String getAddress(Channel client){
        return client.remoteAddress().toString().replaceFirst("/", "");
    }

    /**
     * 获取系统时间
     * @return
     */
    private Long sysTime(){
        return System.currentTimeMillis();
    }

    /**
     * 获取扩展属性
     * @param client
     * @return
     */
    public JSONObject getAttrs(Channel client){
        return client.attr(ATTRS).get();
    }

    private void setAttrs(Channel client, String key, Object value){
        try {
            JSONObject json = client.attr(ATTRS).get();
            json.put(key, value);
            client.attr(ATTRS).set(json);
        }catch ( Exception e ){
            JSONObject json = new JSONObject();
            json.put(key, value);
            client.attr(ATTRS).set(json);
        }
    }

    public void porcess(Channel client, String msg){
        //统一处理
        IMMessage request = decoder.decode(msg);
        if( null == request ){
            return;
        }

        if( request.getCmd().equals(IMP.LOGIN.getName()) ){
            //昵称, 保存用户IP地址,端口
            client.attr(NICK_NAME).getAndSet(request.getSender());
            client.attr(IP_ADDR).getAndSet(getAddress(client));
            //终端类型
            client.attr(FROM).getAndSet(request.getTerminal());
            //用户保存在统一的容器中，给所有在线的用户推送消息
            onlineUsers.add(client);

            //通知所有在线用户，xxx上线了
            for( Channel channel : onlineUsers){
                boolean isSelf = (channel == client);
                if( !isSelf ){
                    request = new IMMessage(IMP.SYSTEM.getName(), sysTime(), onlineUsers.size(), getNickName(client)+"已加入聊天室!");
                }else {
                    //自己上线
                    request = new IMMessage(IMP.SYSTEM.getName(), sysTime(), onlineUsers.size(), "成功加入聊天室!");
                }

                //如果终端是控制台，开始推送消息IMMessage
                if( "Console".equals(channel.attr(FROM).get()) ){
                    channel.writeAndFlush(request);
                    continue;
                }

                String content = encoder.encode(request);
                channel.writeAndFlush(new TextWebSocketFrame(content));
            }
        }else if( request.getCmd().equals(IMP.CHAT.getName()) ){
            for (Channel channel : onlineUsers) {
                boolean isSelf = (channel == client);

                request.setSender(getNickName(client));
                request.setTime(sysTime());

                //如果终端是控制台，开始推送消息IMMessage
                if( "Console".equals(channel.attr(FROM).get()) & !isSelf){
                    if( request.getReceiver() != ""){
                        if(request.getReceiver().equals(getNickName(channel)) ) {
                            channel.writeAndFlush(request);
                            break;
                        }
                    }
                    channel.writeAndFlush(request);
                    continue;
                }

                String content = encoder.encode(request);
                channel.writeAndFlush(new TextWebSocketFrame(content));
            }
        }else if( request.getCmd().equals(IMP.FLOWER.getName()) ){
            JSONObject attrs = getAttrs(client);
            long startTime = sysTime();
            if( null != attrs ){
                long endTime = attrs.getLongValue("lastFlowerTime");
                //10s内不允许重复刷
                int seconds = 10;
                long sub = endTime - startTime;
                if( sub < seconds * 1000 ){
                    request.setSender("you");
                    request.setCmd(IMP.SYSTEM.getName());
                    request.setContent("您鲜花送得太频繁了,请在"+(seconds- Math.round(sub/1000))+"秒后再试");

                    String content = encoder.encode(request);
                    client.writeAndFlush(new TextWebSocketFrame(content));
                    return;
                }
            }

            //正常送花
            for (Channel channel : onlineUsers) {
                if( channel == client ){
                    request.setSender("you");
                    request.setContent("你送给聊天室一朵鲜花");
                    setAttrs(channel, "lastFlowerTime", startTime);
                }else {
                    request.setSender(getNickName(client));
                    request.setContent(getNickName(client)+"送给聊天室一朵鲜花");
                }
                request.setTime(sysTime());

                String content = encoder.encode(request);
                channel.writeAndFlush(new TextWebSocketFrame(content));
            }
        }
    }
}
