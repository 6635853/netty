package com.csh.network.netty.sticky.fixedlen;

import java.nio.charset.Charset;

import javax.swing.tree.FixedHeightLayoutCache;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * 解决粘包粘包问题的第一种方式-》 定长数据流
 * 
 * @author Administrator
 * @date 2019年5月13日
 * @package com.csh.network.netty.sticky.fixedLength
 */
public class NettyServer {

	private EventLoopGroup acceptOrGroup = null;// 负责监听客户端请求的线程组
	private EventLoopGroup clientOrGroup = null;// 负责处理客户端请求
	private ServerBootstrap serverBootstarp = null;// 服务启动配置相关信息

	public NettyServer() {
		init();
	}

	private void init() {
		// 初始化线程组
		acceptOrGroup = new NioEventLoopGroup();
		clientOrGroup = new NioEventLoopGroup();
		serverBootstarp = new ServerBootstrap();
		// 绑定线程组
		serverBootstarp.group(acceptOrGroup, clientOrGroup);
		// 设置通讯模型 为Nio
		serverBootstarp.channel(NioServerSocketChannel.class);
		// 设置缓冲区大小 ChannelOption.SO_SNDBUF-》发送时 ChannelOption.SO_RCVBUF-》接收时
		// SO_KEEPALIVE保持连接有效
		serverBootstarp.option(ChannelOption.SO_SNDBUF, 16 * 1024).option(ChannelOption.SO_RCVBUF, 16 * 1024)
				.option(ChannelOption.SO_KEEPALIVE, true);
	}

	/**
	 * 监听逻辑
	 * 
	 * @param port
	 * @return
	 */
	public ChannelFuture doAccept(int port) {
		try {
			serverBootstarp.childHandler(new ChannelInitializer<Channel>() {

				@Override
				protected void initChannel(Channel ch) throws Exception {
					ChannelHandler[] handlers = new ChannelHandler[3];
					// 定长的Handler，通过构造参数设置消息长度（单位是字节）
					handlers[0] = new FixedLengthFrameDecoder(5);
					handlers[1] = new StringDecoder(Charset.forName("UTF-8"));
					handlers[2] = new NettyServerHandler();
					ch.pipeline().addLast(handlers);
				}
			});
			// 建立连接
			ChannelFuture future = serverBootstarp.bind(port).sync();
			return future;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 释放链接
	 */
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
			System.out.println(" 服务端已经准备好了 ");
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != future) {
				try {
					future.channel().closeFuture().sync();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
			if (null != server) {
				server.release();
			}
		}
	}
}
