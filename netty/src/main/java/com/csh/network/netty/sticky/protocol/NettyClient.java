package com.csh.network.netty.sticky.protocol;

import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

/**
 * Netty粘包粘包问题的解决第三种方案-》协议
 * 
 * @author Administrator
 * @date 2019年5月13日
 * @package com.csh.network.netty.sticky.protocol
 */
public class NettyClient {

	private EventLoopGroup group = null;// 处理情求和处理服务端响应的线程组

	private Bootstrap bootstrap = null;

	public NettyClient() {
		init();
	}

	private void init() {
		group = new NioEventLoopGroup();
		bootstrap=new Bootstrap();
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
	 * @throws InterruptedException
	 */
	public ChannelFuture doRequest(String host, int port, final ChannelHandler... handlers)
			throws InterruptedException {
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new StringDecoder(Charset.forName("UTF-8")));
				ch.pipeline().addLast(handlers);
			}
		});
		// 建立连接
		ChannelFuture future = bootstrap.connect(host, port).sync();
		return future;
	}

	public void release() {
		this.group.shutdownGracefully();
	}

	public static void main(String[] args) throws InterruptedException {
		NettyClient client = null;
		ChannelFuture future = null;
		try {
			client = new NettyClient();
			future = client.doRequest("localhost", 9999, new NettyClientHandler());
			Scanner input = null;
			while (true) {
				input = new Scanner(System.in);
				System.out.println("发送消息到服务端：");
				String line = input.nextLine();
				line = NettyClientHandler.ProtocolParser.transferTo(line);
				System.out.println("客户端发送的协议内容是：" + line);
				future.channel().writeAndFlush(Unpooled.copiedBuffer(line.getBytes(Charset.forName("UTF-8"))));
				TimeUnit.SECONDS.sleep(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != future) {
				future.channel().closeFuture().sync();
			}
			if (null != client) {
				client.release();
			}
		}
	}
}
