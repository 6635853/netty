package com.csh.network.netty.sticky.protocol;

import java.nio.charset.Charset;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

/**
 * 解决netty粘包粘包问题的第三种方式->协议 协议格式 -》 HEADcontent-length:xxxxHEADBODYxxxxxxBODY
 * 
 * @author Administrator
 * @date 2019年5月13日
 * @package com.csh.network.netty.sticky.protocol
 */
public class NettyServer {

	private EventLoopGroup acceptOrGroup = null;// 监听线程组，监听客户端请求
	private EventLoopGroup clientOrGroup = null;// 客户端线程组，负责处理与客户端的数据通讯
	private ServerBootstrap bootstrap = null;// 启动相关配置

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
		// 设置通讯模式为Nio
		bootstrap.channel(NioServerSocketChannel.class);
		// 设定缓冲区大小
		bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
		//
		bootstrap.option(ChannelOption.SO_SNDBUF, 16 * 1024).option(ChannelOption.SO_RCVBUF, 16 * 1024)
				.option(ChannelOption.SO_KEEPALIVE, true);
	}

	/**
	 * 监听逻辑
	 * 
	 * @param port
	 * @param acceptOrHandlers
	 * @return
	 * @throws InterruptedException
	 */
	public ChannelFuture doAccept(int port, final ChannelHandler... acceptOrHandlers) throws InterruptedException {
		bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new StringDecoder(Charset.forName("UTF-8")));
				ch.pipeline().addLast(acceptOrHandlers);
			}
		});
		//绑定
		ChannelFuture future = bootstrap.bind(port).sync();
		return future;
	}

	public void release() {
		this.acceptOrGroup.shutdownGracefully();
		this.clientOrGroup.shutdownGracefully();
	}

	public static void main(String[] args) throws InterruptedException {
		ChannelFuture future = null;
		NettyServer server = null;
		try {
			server = new NettyServer();
			future = server.doAccept(9999, new NettyServerHandler());
			System.out.println(" 服务端已经准备好了：");
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != future) {
				future.channel().closeFuture().sync();
			}
			if (null != server) {
				server.release();
			}
		}
	}
}
