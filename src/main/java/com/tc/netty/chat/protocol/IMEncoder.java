package com.tc.netty.chat.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.MessagePack;

/**
 * 自定义IM协议编码器
 * @author taosh
 * @create 2020-01-09 10:07
 */
public class IMEncoder extends MessageToByteEncoder<IMMessage> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, IMMessage message, ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes(new MessagePack().write(message));
    }

    public String encode(IMMessage msg){
        if( null == msg ){
            return "";
        }

        String prex = "[" + msg.getCmd() + "][" + msg.getTime() + "]";
        if( IMP.LOGIN.getName().equals(msg.getCmd()) ||
            IMP.FLOWER.getName().equals(msg.getCmd())){
            prex += ("["+ msg.getSender() +"][" + msg.getReceiver()+"]["+msg.getTerminal()+"]");
        }else if( IMP.CHAT.getName().equals(msg.getCmd()) ){
            prex += ("["+ msg.getSender() +"][" + msg.getReceiver()+"]");
        }else if( IMP.SYSTEM.getName().equals(msg.getCmd()) ){
            prex += ("["+ msg.getOnline() +"]");
        }

        if( !(null == msg.getContent() || "".equals(msg.getContent())) ){
            prex += (" - " + msg.getContent());
        }

        return prex;
    }
}
