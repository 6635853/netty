package com.csh.network.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * Nio 模型
 * 
 * @author Administrator
 * @date 2019年5月5日
 * @package com.csh.network.nio
 */
public class NioServer implements Runnable {

	private Selector selector;// 多路复用选择器，主要是用来轮询Socket
	private ByteBuffer readerBuffer = ByteBuffer.allocate(1024);// 读的缓存
	private ByteBuffer writerBuffer = ByteBuffer.allocate(1024);// 写缓存

	public static void main(String[] args) {
		new Thread(new NioServer(9999)).start();
	}

	public NioServer(int port) {
		init(port);
	}

	/**
	 * 初始化方法
	 * 
	 * @param port
	 */
	public void init(int port) {
		System.out.println(" Server Starting at port " + port + " ...");
		try {
			// 开启多路复用选择器
			this.selector = Selector.open();
			// 创建Socket服务器
			ServerSocketChannel socketChannel = ServerSocketChannel.open();
			socketChannel.configureBlocking(false);// 设置为非阻塞
			socketChannel.bind(new InetSocketAddress(port));// 绑定端口
			/*
			 * register(Selector, int) int - 状态编码 OP_ACCEPT ： 连接成功的标记位。 OP_READ ： 可以读取数据的标记
			 * OP_WRITE ： 可以写入数据的标记 OP_CONNECT ： 连接建立后的标记
			 */
			socketChannel.register(this.selector, SelectionKey.OP_ACCEPT);// 注册当前服务通道状态
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 主要处理逻辑
	 */
	@Override
	public void run() {
		while (true) {
			try {
				// 选择一个多路复用选择器
				this.selector.select();// 此方法为阻塞方法
				Set<SelectionKey> selectionKey = this.selector.selectedKeys();
				Iterator<SelectionKey> keys = selectionKey.iterator();
				while (keys.hasNext()) {
					// 拿出一个key进行操作
					SelectionKey key = keys.next();
					// 一定要将本次的操作进行删除，否则会出现重复操作的现象
					keys.remove();
					// 如果key是有值的
					if (key.isValid()) {
						// 阻塞状态
						if (key.isAcceptable()) {
							// 如果是监听事件
							accept(key);
						}
						if (key.isReadable()) {
							// 读取数据
							reader(key);
						}
						if (key.isWritable()) {
							// 写数据
							writer(key);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 写数据
	 * 
	 * @param key
	 */
	private void writer(SelectionKey key) {
		this.writerBuffer.clear();
		// 获取通道
		SocketChannel socketChannel = (SocketChannel) key.channel();
		Scanner input = new Scanner(System.in);
		System.out.println(" 服务器返回回去的数据是：");
		String line = input.nextLine();
		try {
			// 将内容写到缓存中
			writerBuffer.put(line.getBytes("UTF-8"));
			writerBuffer.flip();
			socketChannel.write(writerBuffer);
			socketChannel.register(this.selector, SelectionKey.OP_READ);// 读数据
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 读取数据
	 * 
	 * @param key
	 */
	private void reader(SelectionKey key) {
		this.readerBuffer.clear();// 清空缓存
		// 获取通道
		SocketChannel socketChannel = (SocketChannel) key.channel();
		try {
			int readLength = socketChannel.read(readerBuffer);
			if (readLength == -1) {
				key.channel().close();
				key.cancel();// 关闭链接
				return;
			}
			/*
			 * flip， NIO中最复杂的操作就是Buffer的控制。
			 * Buffer中有一个游标。游标信息在操作后不会归零，如果直接访问Buffer的话，数据有不一致的可能。
			 * flip是重置游标的方法。NIO编程中，flip方法是常用方法。
			 */
			this.readerBuffer.flip();//
			// 字节数组，保存具体数据的
			byte[] datas = new byte[readerBuffer.remaining()];
			readerBuffer.get(datas);// 将数据读取到字节数组中
			System.out.println("从 " + socketChannel.getRemoteAddress() + " 客户端接收到的数据是： " + new String(datas, "UTF-8"));
			// 注册通道标记为写
			socketChannel.register(this.selector, SelectionKey.OP_WRITE);
		} catch (IOException e) {
			e.printStackTrace();
			try {
				key.channel().close();
				key.cancel();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * 监听事件
	 * 
	 * @param key
	 * @throws IOException
	 */
	private void accept(SelectionKey key) {
		try {
			// 此通道为init方法中注册到selector上的serverSocketChannel
			ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
			// 阻塞方法，当客户端发起请求后返回，此通道和客户端一致
			SocketChannel socketChannel = serverChannel.accept();
			socketChannel.configureBlocking(false);
			// 为对应的客户端的通道标记状态，此通道为读取数据的
			socketChannel.register(this.selector, SelectionKey.OP_READ);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
