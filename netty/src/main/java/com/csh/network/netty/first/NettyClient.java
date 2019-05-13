package com.csh.network.netty.first;

import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * netty的客户端
 * 
 * @author Administrator
 * @date 2019年5月10日
 * @package com.csh.network.netty.first
 */
public class NettyClient {

	private EventLoopGroup group = null;// 处理请求和处理服务端响应的线程组
	private Bootstrap bootstrap = null;// 客户端启动相关配置

	public NettyClient() {
		init();
	}

	/**
	 * 初始化
	 */
	public void init() {
		group = new NioEventLoopGroup();
		bootstrap = new Bootstrap();
		// 绑定线程组
		bootstrap.group(group);
		bootstrap.channel(NioSocketChannel.class);
	}

	/**
	 * 请求逻辑
	 * 
	 * @param host
	 * @param port
	 * @param handlers
	 * @return
	 */
	public ChannelFuture doRequest(String host, int port, final ChannelHandler... handlers) {
		try {
			this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(handlers);
				}
			});
			// 建立连接
			ChannelFuture future = this.bootstrap.connect(host, port).sync();
			return future;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void release() {
		this.group.shutdownGracefully();
	}

	public static void main(String[] args) {
		NettyClient client = null;
		ChannelFuture future = null;
		try {
			client = new NettyClient();
			future = client.doRequest("localhost", 9999, new NettyClientHandler());
			Scanner s = null;
			while (true) {
				s = new Scanner(System.in);
				System.out.println(" 发送返回给服务端的消息 ");
				String line = s.nextLine();
				if ("exit".equals(line)) {
					future.channel().writeAndFlush(Unpooled.copiedBuffer(line.getBytes("UTF-8")))
							.addListener(ChannelFutureListener.CLOSE);
					break;
				}
				future.channel().writeAndFlush(Unpooled.copiedBuffer(line.getBytes("UTF-8")));
				TimeUnit.SECONDS.sleep(1);
			}
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
