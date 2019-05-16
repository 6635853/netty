package com.csh.network.netty.timer;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.csh.network.util.RequestMessage;
import com.csh.network.util.SerializableFactoryMarshalling;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.WriteTimeoutHandler;

/**
 * 定时断线重连
 * 
 * @author Administrator
 * @date 2019年5月15日
 * @package com.csh.network.netty.timer
 */
public class NettyClient {

	// 处理请求和处理服务端响应的线程组
	private EventLoopGroup group = null;
	private Bootstrap bootstrap = null;
	private ChannelFuture future = null;

	public NettyClient() {
		init();
	}

	private void init() {
		group = new NioEventLoopGroup();
		bootstrap = new Bootstrap();
		// 绑定线程组
		bootstrap.group(group);
		// 设置通讯模式为NIO
		bootstrap.channel(NioSocketChannel.class);
	}

	public void setHandlers() {
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(SerializableFactoryMarshalling.buildMarshallingDecoder());
				ch.pipeline().addLast(SerializableFactoryMarshalling.buildMarshallingEncoder());
				ch.pipeline().addLast(new WriteTimeoutHandler(3));
				ch.pipeline().addLast(new NettyClientHandler());
			} 
		});
	}

	public ChannelFuture getChannelFuture(String host, int port) throws InterruptedException {
		if (future == null) {
			future = this.bootstrap.connect(host, port).sync();
		}
		if (future.channel().isActive()) {
			future = this.bootstrap.connect(host, port).sync();
		}
		return future;
	}

	public void release() {
		this.group.shutdownGracefully();
	}

	public static void main(String[] args) {
		NettyClient client = null;
		ChannelFuture future = null;
		try {
			client = new NettyClient();
			client.setHandlers();
			future = client.getChannelFuture("localhost", 9999);
			for (int i = 0; i < 3; i++) {
				RequestMessage msg = new RequestMessage(new Random().nextLong(), "test" + i, new byte[0]);
				future.channel().writeAndFlush(msg);
				TimeUnit.SECONDS.sleep(2);
			}
			TimeUnit.SECONDS.sleep(5);
			// 断开之后再次连接
			future = client.getChannelFuture("localhost", 9999);
			RequestMessage msg = new RequestMessage(new Random().nextLong(), "test", new byte[0]);
			future.channel().writeAndFlush(msg);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != future) {
				try {
					future.channel().closeFuture().sync();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (null != client) {
				client.release();
			}
		}
	}
}
