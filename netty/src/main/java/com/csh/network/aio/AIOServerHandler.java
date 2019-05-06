package com.csh.network.aio;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Scanner;

public class AIOServerHandler implements CompletionHandler<AsynchronousSocketChannel, AioServer> {

	/**
	 * 业务逻辑的处理，请求到来，并且监听成功， 一定要实现的逻辑，未下一次客户端的请求开启监听，accept()方法的调用
	 * result参数：是和客户端直接建立关联的通道，该通道中有请求的所有相关数据， 如：读取的缓存数据以及等待返回的缓存数据
	 */
	@Override
	public void completed(AsynchronousSocketChannel result, AioServer attachment) {
		// 处理下一次的客户端请求，类似于递归
		attachment.getServerChannel().accept(attachment, this);
		// 进行下一步读取数据的操作
		doRead(result);
	}

	@Override
	public void failed(Throwable exc, AioServer attachment) {
		exc.printStackTrace();
	}

	/**
	 * 读取数据
	 * 
	 * @param result
	 */
	private void doRead(AsynchronousSocketChannel channel) {
		// 创建一个缓存对象
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		/**
		 * 异步操作： read(Buffer destination, A attachment,CompletionHandler<Integer, ?
		 * super A> handler) destination - 目的地，是处理客户端传递数据的中专缓存，可以不适用 attachment -
		 * 处理客户端传递数据的对象，通常使用buffer处理 handler - 处理逻辑
		 */
		channel.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {

			@Override
			public void completed(Integer result, ByteBuffer attachment) {
				try {
					System.out.println("数据的容量：" + attachment.capacity());
					// 将数据服务
					attachment.flip();
					System.out.println("从客户端发送来的数据是：" + new String(attachment.array(), "UTF-8"));
					// 进行写数据
					doWrite(channel);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void failed(Throwable exc, ByteBuffer attachment) {
				exc.printStackTrace();
			}
		});
	}

	/**
	 * 写数据
	 * 
	 * @param channel
	 */
	private void doWrite(AsynchronousSocketChannel channel) {
		// 创建一个bytebuffer对象
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		System.out.println("服务端开始返回数据到客户端：");
		Scanner input=new Scanner(System.in);
		String line=input.nextLine();
		try {
			buffer.put(line.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		// 此处必须复位
		buffer.flip();
		channel.write(buffer);
		// result.write(buffer).get(); // 调用get代表服务端线程阻塞，等待写操作完成
	}
}
