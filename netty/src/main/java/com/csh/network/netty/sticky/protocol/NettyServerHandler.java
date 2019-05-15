package com.csh.network.netty.sticky.protocol;

import java.nio.charset.Charset;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class NettyServerHandler extends ChannelHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		String message = msg.toString();
		System.out.println("服务端接收到的协议信息是：" + message);
		message = ProtocolParser.parse(message);
		if (null == message) {
			System.out.println("客户端的请求失败");
			return;
		}
		System.out.println(" 来自客户端的信息是：" + message);
		String line = "Server Message";
		line = ProtocolParser.transferTo(line);
		System.out.println("服务端发送协议到客户端：" + line);
		ctx.writeAndFlush(Unpooled.copiedBuffer(line.getBytes(Charset.forName("UTF-8"))));
	}

	/**
	 * 异常处理逻辑
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("服务端异常");
		cause.printStackTrace();
		ctx.close();
	}

	static class ProtocolParser {
		public static String parse(String message) {
			String[] temp = message.split("HEADBODY");
			temp[0] = temp[0].substring(4);
			temp[1] = temp[1].substring(0, temp[1].length() - 4);
			int length = Integer.parseInt(temp[0].substring(temp[0].indexOf(":") + 1));
			System.out.println("截取的是：" + length + " = " + temp[0].substring(temp[0].indexOf(":") + 1));
			if (length != temp[1].length()) {
				return null;
			}
			return temp[1];
		}

		public static String transferTo(String message) {
			message = "HEADcontent-length:" + message.length() + "HEADBODY" + message + "BODY";
			return message;
		}
	}
}
