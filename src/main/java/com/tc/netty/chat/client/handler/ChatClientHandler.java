package com.tc.netty.chat.client.handler;

import com.tc.netty.chat.protocol.IMMessage;
import com.tc.netty.chat.protocol.IMP;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 聊天客户端逻辑实现
 * @author taosh
 * @create 2020-01-09 18:10
 */
@Slf4j
public class ChatClientHandler extends SimpleChannelInboundHandler<IMMessage> {
    private ChannelHandlerContext ctx;
    private String nickName;
    private String acceptName;

    public ChatClientHandler(String nickName, String acceptName) {
        this.nickName = nickName;
        this.acceptName = acceptName;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, IMMessage msg) throws Exception {
        IMMessage message = msg;
        System.out.println( (null == message.getSender()? "" : (message.getSender()+":")) + removeHtmlTag(message.getContent()));
    }

    private Object removeHtmlTag(String htmlStr) {
        //定义script的正则表达式
        String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>";
        //定义style的正则表达式
        String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>";
        //定义HTML标签的正则表达式
        String regEx_html = "<[^>]+>";

        Pattern p_script = Pattern.compile(regEx_script,Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        //过滤script标签
        htmlStr = m_script.replaceAll("");

        Pattern p_style = Pattern.compile(regEx_style,Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        //过滤style标签
        htmlStr = m_style.replaceAll("");

        Pattern p_html = Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        //过滤html标签
        htmlStr = m_html.replaceAll("");

        //返回文本字符串
        return htmlStr.trim();
    }

    /**
     * 启动客户端控制台
     */
    private void session(){
        new Thread(){
            @Override
            public void run(){
                System.out.println(nickName+",你好，请在控制台输入对话内容:");
                IMMessage msg = null;
                Scanner scanner = new Scanner(System.in);
                do {
                    if( scanner.hasNext() ){
                        String input = scanner.nextLine();
                        if( "exit".equals(input) ){
                            msg = new IMMessage(IMP.LOGIN.getName(), "Console",
                                    System.currentTimeMillis(), nickName, acceptName);
                        }else {
                            msg = new IMMessage(IMP.CHAT.getName(), System.currentTimeMillis(),
                                    nickName, acceptName, input);
                        }
                    }
                }while ( sendMsg(msg) );
                scanner.close();
            }
        }.start();
    }

    /**
     * tcp链路建立成功后调用
     * @param ctx
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx){
        this.ctx = ctx;
        IMMessage message = new IMMessage(IMP.LOGIN.getName(), "Console",
                System.currentTimeMillis(), this.nickName, this.acceptName);
        sendMsg(message);
        log.info("成功连接服务器，已执行登录");
        session();
    }

    /**
     * 发送消息
     * @param msg
     * @return
     */
    private boolean sendMsg(IMMessage msg) {
        ctx.channel().writeAndFlush(msg);
        System.out.println("请继续输入对话...");
        return msg.getCmd().equals(IMP.LOGOUT) ? false : true;
    }

    /**
     * 发生异常时调用
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.info("与服务器断开连接:"+cause.getMessage());
        ctx.close();
    }

}
