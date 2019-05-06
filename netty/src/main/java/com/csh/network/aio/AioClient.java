package com.csh.network.aio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * Aio 的客户端
 * 
 * @author Administrator
 * @date 2019年5月6日
 * @package com.csh.network.aio
 */
public class AioClient {

	private AsynchronousSocketChannel channel;

	public AioClient(String host, int port) {
		init(host, port);
	}

	/**
	 * 初始化
	 * 
	 * @param port
	 */
	private void init(String hostname, int port) {
		// 开启通道
		try {
			channel = AsynchronousSocketChannel.open();
			// 发起请求，建立连接
			channel.connect(new InetSocketAddress(hostname, port));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 写数据
	 * 
	 * @param line
	 */
	public void write(String line) {
		try {
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			buffer.put(line.getBytes("UTF-8"));
			buffer.flip();// 复位
			channel.write(buffer);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 读数据
	 */
	public void read() {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		try {
			// 其中get()方法是阻塞的，应为read()方法是异步的，为了演示read()方法此处需要get方法配合，实际开发中不需要get
			channel.read(buffer).get();
			buffer.flip();
			System.out.println(" 从客户端返回 回来的数据是" + new String(buffer.array(), "UTF-8"));
		} catch (InterruptedException | ExecutionException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void doDestory() {
		if (null != channel) {
			try {
				channel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		AioClient client = new AioClient("localhost", 9999);
		System.out.println("开始发送数据到服务端");
		Scanner input = new Scanner(System.in);
		String line = input.nextLine();
		client.write(line);
		client.read();
		client.doDestory();
	}
}
