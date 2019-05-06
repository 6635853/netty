package com.csh.network.nio;

import java.nio.ByteBuffer;

/**
 * 测试ByteBuffer
 * Buffer的应用固定逻辑
 * 写操作
 * 1、clear()
 * 2、put() -> 写操作
 * 3、flip()->重置游标
 * 4、SocketChannel.write(buffer);->将缓存数据发送到网络的另一端
 * 5、clear()
 * 
 * 读数据操作
 * 1、clear()
 * 2、SocketChannel.read(buffer)->从网络中读取数据
 * 3、buffer.flip()->重置游标
 * 4、bugger.get()->读取数据
 * 5、buffer.clear()
 * @author Administrator
 * @date 2019年5月5日
 * @package com.csh.network.nio
 */
public class TestBuffer {

	public static void main(String[] args) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		byte[] temp = new byte[] { 3, 2, 1 };
		// 写数据之前：java.nio.HeapByteBuffer[pos=0 lim=8 cap=8]
		// pos游标的位置 ，lim - 限制数量 ， cap- 最大容量
		System.out.println("写数据之前：" + buffer);
		buffer.put(temp);// 将字节数组写入到缓存中
		// 写数据之后：java.nio.HeapByteBuffer[pos=3 lim=8 cap=8]
		// pos - 游标为3 ，lim - 限制为8 ，cap - 容量8
		System.out.println("写数据之后：" + buffer);
		buffer.flip();//
		// 重置游标之后：java.nio.HeapByteBuffer[pos=0 lim=3 cap=8]
		// 使用flip方法重置以后，游标为0，限制为3，容量8
		System.out.println("重置游标之后：" + buffer);
		// System.out.println(buffer.get());//get()->无参，获取的是当前游标指向的位置的数据
		// get(int index)->获取的是指定位置的数据
		// buffer.clear();// 清空buffer pos=0,lim=cap
		for (int i = 0; i < buffer.remaining(); i++) {
			System.out.println(buffer.remaining());
			int data = buffer.get(i);
			System.out.println(i + " = " + data);
		}
	}
}
