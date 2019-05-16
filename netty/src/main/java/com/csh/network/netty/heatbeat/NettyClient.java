package com.csh.network.netty.heatbeat;

import com.csh.network.util.SerializableFactoryMarshalling;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 心跳监测
 * 
 * @author Administrator
 * @date 2019年5月16日
 * @package com.csh.network.netty.heatbeat
 */
public class NettyClient {

	private EventLoopGroup group = null;// 处理请求和服务端响应的
	private Bootstrap bootstrap = null;

	public NettyClient() {
		init();
	}

	private void init() {
		group = new NioEventLoopGroup();
		bootstrap = new Bootstrap();
		// 绑定线程组
		bootstrap.group(group);
		bootstrap.channel(NioSocketChannel.class);
	}

	public ChannelFuture doRequest(String host, int port) throws InterruptedException {
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(SerializableFactoryMarshalling.buildMarshallingDecoder());
				ch.pipeline().addLast(SerializableFactoryMarshalling.buildMarshallingEncoder());
				ch.pipeline().addLast(new NettyClientHandler());
			}
		});
		return bootstrap.connect(host, port).sync();
	}

	public void release() {
		this.group.shutdownGracefully();
	}

	public static void main(String[] args) throws InterruptedException {
		NettyClient client = new NettyClient();
		ChannelFuture future = client.doRequest("localhost", 9999);
		if (future != null) {
			future.channel().closeFuture().sync();
		}
		if (client != null) {
			client.release();
		}
	}
}
