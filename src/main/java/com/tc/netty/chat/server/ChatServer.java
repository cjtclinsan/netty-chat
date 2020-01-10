package com.tc.netty.chat.server;

import com.tc.netty.chat.protocol.IMDecoder;
import com.tc.netty.chat.protocol.IMEncoder;
import com.tc.netty.chat.server.handler.HttpServerHandler;
import com.tc.netty.chat.server.handler.TerminalServerHandler;
import com.tc.netty.chat.server.handler.WebSocketServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author taosh
 * @create 2020-01-09 9:34
 */
@Slf4j
public class ChatServer {
    private int port = 10080;

    public void start(int port){
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup  workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();

                            /**解析自定义协议*/

                            //Inbound
                            pipeline.addLast(new IMDecoder());
                            //Outbound
                            pipeline.addLast(new IMEncoder());
                            //处理直接发送IMMessage对象的IDE控制台
                            pipeline.addLast(new TerminalServerHandler());

                            /**解析Http请求*/
                            pipeline.addLast(new HttpServerCodec());
                            //将同一个http请求或者响应的多个消息对象变成一个fullHttpRequest完整的消息对象
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                            //用于处理大数据流，比如1G的文件，如果直接传输肯定JVM内存溢出
                            pipeline.addLast(new ChunkedWriteHandler());
                            pipeline.addLast(new HttpServerHandler());

                            /**解析webSocket请求*/
                            pipeline.addLast(new WebSocketServerProtocolHandler("/im"));
                            pipeline.addLast(new WebSocketServerHandler());
                        }
                    });
            ChannelFuture f = bootstrap.bind(this.port).sync();
            log.info("服务已启动，监听端口:"+this.port);
            f.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public void start(){
        start(this.port);
    }

    public static void main(String[] args) {
        if( args.length > 0 ){
            new ChatServer().start(Integer.valueOf(args[0]));
        }else {
            new ChatServer().start();
        }
    }
}
