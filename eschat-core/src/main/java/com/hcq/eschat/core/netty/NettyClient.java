package com.hcq.eschat.core.netty;

import com.hcq.eschat.core.util.IpUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class NettyClient {

    private AtomicInteger tempId = new AtomicInteger(0);

    private final Bootstrap bootstrap = new Bootstrap();
    private final Lock lockChannelTables = new ReentrantLock();
    private Map<String, ChannelWrapper> attrChaneelMap = new ConcurrentHashMap<>();
    private final EventLoopGroup eventLoopGroupWorker;

    public NettyClient() {
        this.eventLoopGroupWorker = new NioEventLoopGroup(1, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyClientThread_%d", this.threadIndex.getAndIncrement()));
            }
        });
        this.bootstrap.group(this.eventLoopGroupWorker).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();


                    }

                });
    }

    public int createId() {
        return tempId.getAndIncrement();
    }

    static class ChannelWrapper {
        private final ChannelFuture channelFuture;

        public ChannelWrapper(ChannelFuture channelFuture) {
            this.channelFuture = channelFuture;
        }

        public boolean isOK() {
            return this.channelFuture.channel() != null && this.channelFuture.channel().isActive();
        }

        public boolean isWritable() {
            return this.channelFuture.channel().isWritable();
        }

        private Channel getChannel() {
            return this.channelFuture.channel();
        }

        public ChannelFuture getChannelFuture() {
            return channelFuture;
        }
    }

    protected Channel getChannel(String attr) {
        ChannelWrapper channelWrapper = attrChaneelMap.get(attr);
        if (channelWrapper != null && channelWrapper.isOK()) {
            return channelWrapper.getChannel();
        }
        return CreateChannel(attr);
    }

    protected Channel CreateChannel(String attr) {
        ChannelWrapper channelWrapper = attrChaneelMap.get(attr);
        if (channelWrapper != null && channelWrapper.isOK()) {
            return channelWrapper.getChannel();
        }
        if (lockChannelTables.tryLock()) {
            try {
                ChannelWrapper channelWrapper2 = attrChaneelMap.get(attr);
                if (channelWrapper2 != null && channelWrapper.isOK()) {
                    return channelWrapper2.getChannel();
                }
                ChannelFuture connect = this.bootstrap.connect(IpUtils.string2SocketAddress(attr));
                ChannelWrapper newChannelWrapper = new ChannelWrapper(connect);
                attrChaneelMap.put(attr, newChannelWrapper);
                return newChannelWrapper.getChannel();
            }finally {
                lockChannelTables.unlock();
            }
        }
    }

}
