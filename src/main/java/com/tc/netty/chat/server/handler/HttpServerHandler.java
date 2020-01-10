package com.tc.netty.chat.server.handler;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author taosh
 * @create 2020-01-09 13:46
 */
@Slf4j
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    //获取class路径
    private URL baseurl = HttpServerHandler.class.getResource("");
    private final String webroot = "webroot";

    private File getResource(String fileName) throws URISyntaxException {
        String basePath = baseurl.toURI().toString();
        int start = basePath.indexOf("/classes");
        basePath = (basePath.substring(0, start)+"/" + "classes/").replaceAll("/+", "/");

        String path = basePath + webroot + "/" +fileName;

        path = !path.contains("file:") ? path : path.substring(5);
        path = path.replaceAll("//", "/");

        return new File(path);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        //请求一个url，相当于读取一个静态资源文件
        String uri = request.getUri();

        RandomAccessFile file = null;

        try {
            String page = uri.equals("/") ? "chat.html" : uri;
            file = new RandomAccessFile(getResource(page), "r");
        } catch ( Exception e ){
            ctx.fireChannelRead(request.retain());
            return;
        }

        //构建一个Http response 对象往外写内容
        HttpResponse response = new DefaultFullHttpResponse(request.getProtocolVersion(),
                HttpResponseStatus.OK);
        String contextType = "text/html;";
        if(uri.endsWith(".css")){
            contextType = "text/css;";
        }else if(uri.endsWith(".js")){
            contextType = "text/javascript;";
        }else if(uri.toLowerCase().matches(".*\\.(jpg|png|gif)$")){
            String ext = uri.substring(uri.lastIndexOf("."));
            contextType = "image/" + ext;
        }

        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, contextType+" charset=utf-8");

        //在页面上支持websocket，设置为长连接
        boolean keepAlive = HttpHeaders.isKeepAlive(request);

        if( keepAlive ){
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, file.length());
            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        ctx.write(response);

        ctx.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));

        ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        if( !keepAlive ){
            future.addListener(ChannelFutureListener.CLOSE);
        }

        file.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        Channel client = ctx.channel();
        log.info("Client:"+client.remoteAddress()+"异常");
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}
