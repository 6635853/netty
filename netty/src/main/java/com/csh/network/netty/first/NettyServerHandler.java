package com.csh.network.netty.first;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class NettyServerHandler extends ChannelHandlerAdapter {

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("server exceptionCaught method run...");
		ctx.close();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// 获取读取的数据，是一个缓存
		ByteBuf readBuffer = (ByteBuf) msg;
		// 创建一个字节数组，用于保存缓存中的数据
		byte[] tempDatas = new byte[readBuffer.readableBytes()];// 长度为缓存可读的长度
		readBuffer.readBytes(tempDatas);// 将缓存数据读取到字节数组中
		String message = new String(tempDatas, "UTF-8");
		System.out.println(" 客户端发送过来的数据是：" + message);
		if ("exit".equals(message)) {
			ctx.close();
			return;
		}
		String line = "服务端发往客户端的";
		//写操作自动释放缓存
		ctx.writeAndFlush(Unpooled.copiedBuffer(message.getBytes("UTF-8")));
		//如果调用的是write方法，不会刷新缓存，缓存中的数据不会发送到客户端，必须再次调用flush方法
		//ctx.write(Unpooled.copiedBuffer(line.getBytes("UTF-8")));
		//ctx.flush();
	}

}
