package com.csh.network.netty.serializ;

import com.csh.network.util.SerializableFactoryMarshalling;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * netty序列化对象
 * 
 * @author Administrator
 * @date 2019年5月13日
 * @package com.csh.network.netty.serializ
 */
public class NettyServer {

	// 监听线程组，负责监听客户端的请求
	private EventLoopGroup acceptOrGroup;
	// 处理客户端相关的操作
	private EventLoopGroup clientOrGroup;
	private ServerBootstrap bootstrap;// 启动服务相关配置

	public NettyServer() {
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		acceptOrGroup = new NioEventLoopGroup();
		clientOrGroup = new NioEventLoopGroup();
		bootstrap = new ServerBootstrap();
		bootstrap.group(acceptOrGroup, clientOrGroup);
		// 设置通讯模式为nio
		bootstrap.channel(NioServerSocketChannel.class);
		// 设定缓冲区
		bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
		bootstrap.option(ChannelOption.SO_SNDBUF, 16 * 1024).option(ChannelOption.SO_RCVBUF, 16 * 24)
				.option(ChannelOption.SO_KEEPALIVE, true);
	}

	public ChannelFuture doAccept(int port, final ChannelHandler... handlers) throws InterruptedException {
		bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(SerializableFactoryMarshalling.buildMarshallingDecoder());
				ch.pipeline().addLast(SerializableFactoryMarshalling.buildMarshallingEncoder());
				ch.pipeline().addLast(handlers);
			}
		});
		ChannelFuture future = bootstrap.bind(port).sync();
		return future;
	}

	public void release() {
		this.acceptOrGroup.shutdownGracefully();
		this.clientOrGroup.shutdownGracefully();
	}

	public static void main(String[] args) {
		ChannelFuture future = null;
		NettyServer server = null;
		try {
			server = new NettyServer();
			future = server.doAccept(9999, new NettyServerHandler());
			System.out.println(" 服务端已经准备好了 ");
			future.channel().closeFuture().sync();
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

			if (null != server) {
				server.release();
			}
		}
	}
}
