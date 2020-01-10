package com.tc.netty.chat.server.handler;

import com.tc.netty.chat.processor.MsgProcessor;
import com.tc.netty.chat.processor.MsgProcessorSingle;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

/**
 * @author taosh
 * @create 2020-01-09 13:46
 */
@Slf4j
public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
//    private MsgProcessor processor = new MsgProcessor();
    private MsgProcessorSingle processor = new MsgProcessorSingle();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        processor.porcess(ctx.channel(), msg.text());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        String address = processor.getAddress(ctx.channel());
        log.info("WebSocket Client:"+address+"异常");

        cause.printStackTrace();
        ctx.close();
    }
}
