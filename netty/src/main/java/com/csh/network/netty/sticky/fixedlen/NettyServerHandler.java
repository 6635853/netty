package com.csh.network.netty.sticky.fixedlen;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 
 * @author Administrator
 * @date 2019年5月13日
 * @package com.csh.network.netty.sticky.fixedlen
 */
public class NettyServerHandler extends ChannelHandlerAdapter {

	/**
	 * 业务逻辑
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		String message = msg.toString();
		System.out.println(" 来自客户端的消息：" + message.trim());
		String line = "OK";
		ctx.writeAndFlush(Unpooled.copiedBuffer(line.getBytes("UTF-8")));
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println(" 服务端异常 ");
		ctx.close();
	}
}
