package com.csh.network.netty.sticky.fixedlen;

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
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * 解决粘包粘包问题的第一种方式->定长数据流客户端
 * 
 * @author Administrator
 * @date 2019年5月13日
 * @package com.csh.network.netty.sticky.fixedlen
 */
public class NettyClient {

	private EventLoopGroup clientGroup = null;// 处理情求和处理服务端响应的线程组
	private Bootstrap bootstrap = null;// 配置启动相关信息

	public NettyClient() {
		init();
	}

	private void init() {
		clientGroup = new NioEventLoopGroup();
		bootstrap = new Bootstrap();
		// 绑定线程组
		bootstrap.group(clientGroup);
		// 设置通讯模式为Nio
		bootstrap.channel(NioSocketChannel.class);
	}

	/**
	 * 处理请求逻辑
	 * 
	 * @param port
	 * @param port
	 * @return
	 */
	public ChannelFuture doRequest(String host, int port) {
		try {
			this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelHandler[] handlers = new ChannelHandler[3];
					handlers[0] = new FixedLengthFrameDecoder(3);
					// 字符串解码器，会自动处理channelRead方法的msg参数，将ByteBuf类型的数据转换成字符串对象
					handlers[1] = new StringDecoder(Charset.forName("UTF-8"));
					handlers[2] = new NettyClientHandler();
					ch.pipeline().addLast(handlers);
				}
			});
			// 建立连接
			ChannelFuture future = this.bootstrap.connect(host, port).sync();
			return future;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void release() {
		this.clientGroup.shutdownGracefully();
	}

	public static void main(String[] args) throws InterruptedException {
		NettyClient client = null;
		ChannelFuture future = null;
		try {
			client = new NettyClient();
			future = client.doRequest("localhost", 9999);
			Scanner input = null;
			while (true) {
				input = new Scanner(System.in);
				System.out.println(" 开始发送消息到服务端：");
				String line = input.nextLine();
				byte[] bs = new byte[5];
				byte[] temp = line.getBytes("UTF-8");
				if (temp.length <= 5) {
					for (int i = 0; i < temp.length; i++) {
						bs[i] = temp[i];
					}
				}
				future.channel().writeAndFlush(Unpooled.copiedBuffer(bs));
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
