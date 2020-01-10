package com.tc.netty.chat.server.handler;

import com.tc.netty.chat.processor.MsgProcessorSingle;
import com.tc.netty.chat.protocol.IMMessage;
import com.tc.netty.chat.processor.MsgProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author taosh
 * @create 2020-01-09 13:46
 */
@Slf4j
public class TerminalServerHandler extends SimpleChannelInboundHandler<IMMessage> {
//    private MsgProcessor processor = new MsgProcessor();
    private MsgProcessorSingle processor = new MsgProcessorSingle();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IMMessage msg) throws Exception {
        processor.porcess(ctx.channel(), msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        log.info("Socket Client:与客户端断开连接:"+cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }

}
