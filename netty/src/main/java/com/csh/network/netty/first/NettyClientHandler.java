package com.csh.network.netty.first;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 实现ChannelHandlerAdapter
 * 
 * @author Administrator
 * @date 2019年5月13日
 * @package com.csh.network.netty.first
 */
public class NettyClientHandler extends ChannelHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buffer = (ByteBuf) msg;
		byte[] tempDatas = new byte[buffer.readableBytes()];
		buffer.readBytes(tempDatas);
		System.out.println(" 从客户端发来的消息是：" + new String(tempDatas, "UTF-8"));
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println(" 客户端异常了 ");
		ctx.close();
	}
}
