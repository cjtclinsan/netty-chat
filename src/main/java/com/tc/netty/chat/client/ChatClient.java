package com.tc.netty.chat.client;

import com.tc.netty.chat.client.handler.ChatClientHandler;
import com.tc.netty.chat.protocol.IMDecoder;
import com.tc.netty.chat.protocol.IMEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author taosh
 * @create 2020-01-09 9:34
 */
public class ChatClient {
//    private ChatClientHandler clientHandler;
//    private String host;
//    private int port;
//
//    public ChatClient(String nickName) {
//        this.clientHandler = new ChatClientHandler(nickName);
//    }
//
//    public void connect(String host, int port){
//        this.host = host;
//        this.port = port;
//
//        EventLoopGroup workerGroup = new NioEventLoopGroup();
//        try {
//            Bootstrap bootstrap = new Bootstrap();
//            bootstrap.group(workerGroup)
//                    .channel(NioSocketChannel.class)
//                    .option(ChannelOption.SO_KEEPALIVE, true)
//                    .handler(new ChannelInitializer<SocketChannel>() {
//                        @Override
//                        protected void initChannel(SocketChannel socketChannel) throws Exception {
//                            socketChannel.pipeline().addLast(new IMDecoder());
//                            socketChannel.pipeline().addLast(new IMEncoder());
//                            socketChannel.pipeline().addLast(clientHandler);
//                        }
//                    });
//            ChannelFuture f = bootstrap.connect(this.host, this.port).sync();
//            f.channel().closeFuture().sync();
//        } catch ( Exception e ){
//            e.printStackTrace();
//        } finally {
//          workerGroup.shutdownGracefully();
//        }
//    }
//
//    public static void main(String[] args) {
//        new ChatClient("沈").connect("127.0.0.1", 10080);
//
//        String url = "http://localhost:10080/images/a.png";
//        System.out.println(url.toLowerCase().matches(".*\\.(gif|png|jpg)$"));
//    }
}
