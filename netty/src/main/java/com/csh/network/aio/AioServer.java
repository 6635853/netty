package com.csh.network.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Aio 模型
 * 
 * @author Administrator
 * @date 2019年5月5日
 * @package com.csh.network.aio
 */
public class AioServer {

	private ExecutorService service;// 线程池
	private AsynchronousServerSocketChannel serverChannel;// 通道

	public ExecutorService getService() {
		return service;
	}

	public void setService(ExecutorService service) {
		this.service = service;
	}

	public AsynchronousServerSocketChannel getServerChannel() {
		return serverChannel;
	}

	public void setServerChannel(AsynchronousServerSocketChannel serverChannel) {
		this.serverChannel = serverChannel;
	}

	public AioServer(int port) {
		init(port);
	}

	// 初始化方法
	private void init(int port) {
		System.out.println(" 服务端已经准备好了，端口是：" + port + "...");
		int threadNum = Runtime.getRuntime().availableProcessors();// CPU核数
		try {
			// 创建线程池
			service = Executors.newFixedThreadPool(threadNum);
			// 开启通道服务
			serverChannel = AsynchronousServerSocketChannel.open();
			// 绑定端口
			serverChannel.bind(new InetSocketAddress(port));
			System.out.println("服务端已经启动成功：");
			// 开启监听
			serverChannel.accept(this, new AIOServerHandler());
			try {
				TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new AioServer(9999);
	}
}
