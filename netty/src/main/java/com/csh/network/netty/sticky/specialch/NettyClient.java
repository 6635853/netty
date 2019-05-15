package com.csh.network.netty.sticky.specialch;

import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * 解决netty粘包粘包问题的第二种方案-》固定分隔符
 * 
 * @author Administrator
 * @date 2019年5月13日
 * @package com.csh.network.netty.sticky.specialch
 */
public class NettyClient {

	private EventLoopGroup group = null;
	private Bootstrap bootstrap = null;

	public NettyClient() {
		init();
	}

	private void init() {
		group = new NioEventLoopGroup();
		bootstrap = new Bootstrap();
		// 绑定线程组
		bootstrap.group(group);
		// 设定通讯模式为Nio
		bootstrap.channel(NioSocketChannel.class);
	}

	/**
	 * 请求逻辑
	 * 
	 * @param host
	 * @param port
	 * @return
	 * @throws InterruptedException
	 */
	public ChannelFuture doRequest(String host, int port) throws InterruptedException {
		this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ByteBuf buffer = Unpooled.copiedBuffer("$$".getBytes());
				ChannelHandler[] handlers = new ChannelHandler[3];
				handlers[0] = new DelimiterBasedFrameDecoder(1024, buffer);
				handlers[1] = new StringDecoder(Charset.forName("UTF-8"));
				handlers[2] = new NettyClientHandler();
				ch.pipeline().addLast(handlers);
			}
		});
		// 建立连接
		ChannelFuture future = this.bootstrap.connect(host, port).sync();
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
			future = client.doRequest("localhost", 9999);
			Scanner input = null;
			while (true) {
				input = new Scanner(System.in);
				System.out.println("开始发送消息到服务端：");
				String line = input.nextLine();
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
