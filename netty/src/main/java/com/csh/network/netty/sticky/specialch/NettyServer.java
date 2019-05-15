package com.csh.network.netty.sticky.specialch;

import java.nio.charset.Charset;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * 解决netty粘包粘包问题的第二种方法-》使用固定的分隔符号
 * 
 * @author Administrator
 * @date 2019年5月13日
 * @package com.csh.network.netty.sticky.specialch
 */
public class NettyServer {

	private EventLoopGroup acceptOrGroup = null;// 监听线程组，负责监听客户端请求
	private EventLoopGroup clientOrGroup = null;// 客户端线程组，负责处理客户端的数据通讯

	private ServerBootstrap serverBootstrap = null;

	public NettyServer() {
		init();
	}

	private void init() {
		// 初始化
		acceptOrGroup = new NioEventLoopGroup();
		clientOrGroup = new NioEventLoopGroup();
		serverBootstrap = new ServerBootstrap();
		// 绑定
		serverBootstrap.group(acceptOrGroup, clientOrGroup);
		// 设置通讯模型为Nio
		serverBootstrap.channel(NioServerSocketChannel.class);
		// 设置缓冲区大小
		serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
		//
		serverBootstrap.option(ChannelOption.SO_SNDBUF, 16 * 1024).option(ChannelOption.SO_RCVBUF, 16 * 1024)
				.option(ChannelOption.SO_KEEPALIVE, true);

	}

	public ChannelFuture doAccept(int port) throws InterruptedException {
		serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				// 设置一个固定的分隔符
				ByteBuf buffer = Unpooled.copiedBuffer("$$".getBytes());
				ChannelHandler[] handlers = new ChannelHandler[3];
				// 处理固定结束标记符号的Handler。这个Handler没有@Sharable注解修饰，
				// 必须每次初始化通道时创建一个新对象
				// 使用特殊符号分隔处理数据粘包问题，也要定义每个数据包最大长度。netty建议数据有最大长度。
				handlers[0] = new DelimiterBasedFrameDecoder(1024, buffer);
				handlers[1] = new StringDecoder(Charset.forName("UTF-8"));
				handlers[2] = new NettyServerHandler();
				ch.pipeline().addLast(handlers);
			}
		});
		// 建立连接
		ChannelFuture future = this.serverBootstrap.bind(port).sync();
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

			future = server.doAccept(9999);
			System.out.println("server started.");
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
