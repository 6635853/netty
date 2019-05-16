package com.csh.network.netty.heatbeat;

import com.csh.network.util.SerializableFactoryMarshalling;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 心跳监测
 * 
 * @author Administrator
 * @date 2019年5月15日
 * @package com.csh.network.netty.heatbeat
 */
public class NettyServer {

	private EventLoopGroup acceptOrGroup = null;// 监听线程组，监听客户端请求
	private EventLoopGroup clientOrGroup = null;// 负责处理与客户端的数据通讯
	private ServerBootstrap serverBootstrap = null;

	public NettyServer() {
		init();
	}

	private void init() {
		acceptOrGroup = new NioEventLoopGroup();
		clientOrGroup = new NioEventLoopGroup();
		serverBootstrap = new ServerBootstrap();
		// 绑定线程组
		serverBootstrap.group(acceptOrGroup, clientOrGroup);
		// 设置通讯模式为NIO
		serverBootstrap.channel(NioServerSocketChannel.class);
		// 设置缓存区
		serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
		// SO_SNDBUF发送缓冲区，SO_RCVBUF接收缓冲区，SO_KEEPALIVE开启心跳监测（保证连接有效）
		serverBootstrap.option(ChannelOption.SO_SNDBUF, 16 * 1024).option(ChannelOption.SO_RCVBUF, 16 * 1024)
				.option(ChannelOption.SO_KEEPALIVE, true);
	}

	/**
	 * 监听请求
	 * 
	 * @param port
	 * @return
	 * @throws InterruptedException
	 */
	public ChannelFuture doAccept(int port) throws InterruptedException {
		serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(SerializableFactoryMarshalling.buildMarshallingDecoder());
				ch.pipeline().addLast(SerializableFactoryMarshalling.buildMarshallingEncoder());
				ch.pipeline().addLast(new NettyServerHandler());
			}
		});
		// 绑定连接
		return serverBootstrap.bind(port).sync();
	}

	public void release() {
		this.acceptOrGroup.shutdownGracefully();
		this.clientOrGroup.shutdownGracefully();
	}

	public static void main(String[] args) {
		ChannelFuture future = null;
		NettyServer server = new NettyServer();
		try {
			future = server.doAccept(9999);
			System.out.println(" 服务端已经准备好了 ");
			future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (null != future) {
				try {
					future.channel().closeFuture().sync();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (null != server) {
				server.release();
			}
		}
	}

}
