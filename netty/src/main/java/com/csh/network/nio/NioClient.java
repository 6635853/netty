package com.csh.network.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

/**
 * Nio 模型客户端
 * 
 * @author Administrator
 * @date 2019年5月5日
 * @package com.csh.network.nio
 */
public class NioClient {

	public static void main(String[] args) {
		InetSocketAddress address = new InetSocketAddress("localhost", 9999);
		SocketChannel channel = null;
		// 定义缓存
		ByteBuffer buffer = ByteBuffer.allocate(1024);

		try {
			// 开启通道
			channel = SocketChannel.open();
			// 链接远程服务器
			channel.connect(address);
			Scanner input = new Scanner(System.in);
			while (true) {
				System.out.println(" 请输入要往服务器发送的信息： ");
				String line = input.nextLine();
				if (line.equals("exit")) {
					break;
				}
				// 将控制台的输入数据写到缓存中
				buffer.put(line.getBytes("UTF-8"));
				buffer.flip();// 重置缓存游标
				channel.write(buffer);// 将数据发送给服务器
				buffer.clear();// 清空缓存数据

				int readContent = channel.read(buffer);
				if (readContent == -1) {
					break;
				}
				// 重置缓存游标
				buffer.flip();
				byte[] datas = new byte[buffer.remaining()];
				// 读取数据
				buffer.get(datas);
				System.out.println(" 从服务器返回回来的数据是：" + new String(datas, "UTF-8"));
				// 清空缓存
				buffer.clear();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != channel) {
				try {
					channel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
