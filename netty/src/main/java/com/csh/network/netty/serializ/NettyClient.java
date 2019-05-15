package com.csh.network.netty.serializ;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.csh.network.util.GzipUtils;
import com.csh.network.util.RequestMessage;
import com.csh.network.util.SerializableFactoryMarshalling;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * netty序列化对象
 * 
 * @author Administrator
 * @date 2019年5月15日
 * @package com.csh.network.netty.serializ
 */
public class NettyClient {

	// 处理请求和处理服务端响应的线程组
	private EventLoopGroup group = null;
	// 服务启动相关配置
	private Bootstrap bootstrap = null;

	public NettyClient() {
		init();
	}

	private void init() {
		group = new NioEventLoopGroup();
		bootstrap = new Bootstrap();
		// 绑定线程组
		bootstrap.group(group);
		// 设置通讯模式为NIO
		bootstrap.channel(NioSocketChannel.class);
	}

	public ChannelFuture doRequest(String host, int port, final ChannelHandler... handlers)
			throws InterruptedException {
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(SerializableFactoryMarshalling.buildMarshallingDecoder());
				ch.pipeline().addLast(SerializableFactoryMarshalling.buildMarshallingEncoder());
				ch.pipeline().addLast(handlers);
			}
		});
		// 链接
		ChannelFuture future = bootstrap.connect(host, port).sync();
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
			future = client.doRequest("localhost", 9999, new NettyClientHandler());
			String attachment = "test attachment";
			byte[] attbuf = attachment.getBytes();
			attbuf = GzipUtils.zip(attbuf);
			RequestMessage msg = new RequestMessage(new Random().nextLong(), "测试", attbuf);
			future.channel().writeAndFlush(msg);
			TimeUnit.SECONDS.sleep(1);
			future.addListener(ChannelFutureListener.CLOSE);
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
