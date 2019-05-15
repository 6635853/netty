package com.csh.network.netty.sticky.protocol;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * netty解决粘包粘包问题-》协议方案
 * 
 * @author Administrator
 * @date 2019年5月13日
 * @package com.csh.network.netty.sticky.protocol
 */
public class NettyClientHandler extends ChannelHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		String message = msg.toString();
		System.out.println(" 客户端 接收到的协议内容是：" + message);
		message = ProtocolParser.parse(message);
	}

	static class ProtocolParser {
		public static String parse(String message) {
			String[] temp = message.split("HEADBODY");
			temp[0] = temp[0].substring(4);
			temp[1] = temp[1].substring(0, temp[1].length() - 4);
			int length = Integer.parseInt(temp[0].substring(temp[0].indexOf(":") + 1));
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
