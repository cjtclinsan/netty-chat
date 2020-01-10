package com.tc.netty.chat.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.msgpack.MessagePack;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author taosh
 * @create 2020-01-09 10:30
 */
public class IMDecoder extends ByteToMessageDecoder {
    /**解析IM,请求内容的正则*/
    private Pattern pattern = Pattern.compile("^\\[(.*)\\](\\s\\-\\s(.*))?");

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //获取可读字节数
        final int length = in.readableBytes();
        final byte[] array = new byte[length];
        String content = new String(array, in.readerIndex(), length);

        //空消息不解析
        if( !(null == content || "".equals(content.trim())) ){
            if( !IMP.isIMP(content) ){
                ctx.channel().pipeline().remove(this);
                return;
            }
        }

        in.getBytes(in.readerIndex(), array, 0, length);
        out.add(new MessagePack().read(array, IMMessage.class));
        in.clear();
    }

    /**
     * 字符串解析成自定义的即时通讯信息
     * @param msg
     * @return
     */
    public IMMessage decode(String msg){
        if( null == msg || "".equals(msg.trim()) ){
            return null;
        }

        Matcher m = pattern.matcher(msg);
        String header = "";
        String content = "";
        if(m.matches()){
            header = m.group(1);
            content = m.group(3);
        }

        String [] heards = header.split("\\]\\[");
        long time = Long.parseLong(heards[1]);

        String nickName = heards[2];
        String receiver = heards[3];

        //昵称最多十个字
        nickName = nickName.length() < 10?nickName : nickName.substring(0, 9);

        if( msg.startsWith("[" + IMP.LOGIN.getName() + "]") ){
            return new IMMessage(heards[0], heards[4], time, nickName, receiver);
        }else if(msg.startsWith("[" + IMP.CHAT.getName() + "]")){
            return new IMMessage(heards[0],time,nickName, receiver, content);
        }else if(msg.startsWith("[" + IMP.FLOWER.getName() + "]")){
            return new IMMessage(heards[0],heards[4],time,nickName, receiver);
        }else{
            return null;
        }
    }
}
