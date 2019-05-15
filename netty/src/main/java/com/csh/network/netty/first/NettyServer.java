package com.csh.network.netty.first;

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
 * Netty 开发的服务端
 * 
 * @author Administrator
 * @date 2019年5月6日
 * @package com.csh.network.netty.first
 */
public class NettyServer {

	// 监听线程组，负责监听客户端请求
	private EventLoopGroup acceptOrGroup = null;
	// 客户端线程组，处理与客户端的数据通讯
	private EventLoopGroup clientOrGroup = null;
	// 启动服务相关信息
	private ServerBootstrap bootstrap = null;

	public NettyServer() {
		init();
	}

	/**
	 * 初始化一些基本数据
	 */
	public void init() {
		// 初始化线程组，如果不传递参数，默认构建的线程组中的线程数量是CPU核心数
		acceptOrGroup = new NioEventLoopGroup();
		clientOrGroup = new NioEventLoopGroup();
		// 初始化服务配置
		bootstrap = new ServerBootstrap();
		// 进行线程组的绑定
		bootstrap.group(acceptOrGroup, clientOrGroup);
		// 设定 通讯模式为nio，同步非阻塞
		bootstrap.channel(NioServerSocketChannel.class);
		// 设定缓存区大小，缓存区的单位是字节
		bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
		// SO_SNDBUF发送缓冲区，SO_RCVBUF接收缓存区，SO_KEEPALIVE开启心跳检测，保证链接有效
		bootstrap.option(ChannelOption.SO_SNDBUF, 16 * 1024).option(ChannelOption.SO_RCVBUF, 16 * 1024)
				.option(ChannelOption.SO_KEEPALIVE, true);

	}

	/**
	 * 监听处理逻辑
	 * 
	 * @param port
	 * @param acceptOrHandler
	 * @return
	 * @throws InterruptedException
	 */
	public ChannelFuture doAccept(int port, final ChannelHandler... acceptOrHandler) throws InterruptedException {
		/**
		 * childHandler是bootstrap的独有方法，是用于提供处理对象的， 可以一次性增加若干个处理逻辑，类似于责任链的处理方式
		 * 增加A，B两个处理逻辑，在处理客户端请求数据的时候，根据A->B的顺序依次处理 ChannelInitializer - 用于提供处理器的一个模型对象
		 * 其中定义了处理方法，initChannel()方法，该方法用于初始化处理逻辑责任链
		 * 可以保证服务端的bootstrap只初始化依次处理器，尽量提供处理罗的重点
		 */
		bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(acceptOrHandler);
			}
		});
		// bind()方法 - 绑定监听端口的，ServerBootstrap可以绑定多个监听端口，多次调用dind方法即可
		// sync()方法 - 开始监听逻辑返回一个ChannelFuture对象，返回结果代表的是监听成功后的一个对应的未来结果
		// 可以使用ChannelFuture实现后续的服务和客户端的交互
		ChannelFuture future = bootstrap.bind(port).sync();
		return future;
	}

	/**
	 * 释放资源 shutdownGraceFully()方法是一个安全关闭的方法，可以保证不放弃任何一个已经连接的客户端
	 */
	public void release() {
		this.acceptOrGroup.shutdownGracefully();
		this.clientOrGroup.shutdownGracefully();
	}

	public static void main(String[] args) {
		ChannelFuture future = null;
		NettyServer server = new NettyServer();
		try {
			future = server.doAccept(9999, new NettyServerHandler());
			System.out.println(" 服务端已经准备好：");
			// 关闭链接
			future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
