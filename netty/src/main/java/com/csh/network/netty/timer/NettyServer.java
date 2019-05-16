package com.csh.network.netty.timer;

import com.csh.network.util.SerializableFactoryMarshalling;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

/**
 * 定时断线重连 Timer
 * 
 * @author Administrator
 * @date 2019年5月15日
 * @package com.csh.network.netty.timer
 */
public class NettyServer {

	private EventLoopGroup acceptOrGroup = null;// 负责监听客户端请求的线程组
	private EventLoopGroup clientOrGroup = null;// 负责处理与客户端数据通讯的线程组
	private ServerBootstrap bootstrap = null;

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
		// 绑定线程组
		bootstrap.group(acceptOrGroup, clientOrGroup);
		// 设置通讯模式为NIO
		bootstrap.channel(NioServerSocketChannel.class);
		// 设置缓冲区大小
		bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
		//
		bootstrap.option(ChannelOption.SO_SNDBUF, 16 * 1024).option(ChannelOption.SO_RCVBUF, 16 * 1024)
				.option(ChannelOption.SO_KEEPALIVE, true);
	}

	public ChannelFuture doAccept(int port) throws InterruptedException {
		bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(SerializableFactoryMarshalling.buildMarshallingDecoder());
				ch.pipeline().addLast(SerializableFactoryMarshalling.buildMarshallingEncoder());
				// 定义一个定时短线处理器，当多长时间内没有任何可读数据自动断开连接
				// 构造参数，就是间隔时长，默认单位是秒
				// 自定义间隔时长单位，new ReadTimeoutHandler(long times, TimeUnit unit);
				ch.pipeline().addLast(new ReadTimeoutHandler(3));
				ch.pipeline().addLast(new NettyServerHandler());
			}
		});
		// 绑定连接
		ChannelFuture future = bootstrap.bind(port).sync();
		return future;
	}

	public void release() {
		this.acceptOrGroup.shutdownGracefully();
		this.clientOrGroup.shutdownGracefully();
	}

	public static void main(String[] args) {
		NettyServer server = null;
		ChannelFuture future = null;
		try {
			server = new NettyServer();
			future = server.doAccept(9999);
			System.out.println("客户端已经准备好了");
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
