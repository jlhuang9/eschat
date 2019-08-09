package com.hcq.eschat.web.netty;

import com.hcq.eschat.core.listen.Init;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TcpService implements Init {

    @Value("${netty.service.port}")
    private int servicePort;


    @Autowired
    private ServerHandler serverHandler;

    @Override
    public void init() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(serverHandler);
                    }
                });
        bind(serverBootstrap, servicePort);
    }

    private void bind(ServerBootstrap serverBootstrap, int port) {
        serverBootstrap
                .bind(port)
                .addListener(future -> {
                    if(future.isSuccess()) {
                        System.out.println("服务端：端口【"+port+"】绑定成功！");
                    }else {
                        System.out.println("服务端：端口【"+port+"】绑定失败，尝试绑定【"+(port+1)+"】！");
                        bind(serverBootstrap, port+1);
                    }
                });
    }

}
