package com.csh.network.netty.serializ;

import com.csh.network.util.GzipUtils;
import com.csh.network.util.RequestMessage;
import com.csh.network.util.ResponseMessage;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class NettyServerHandler extends ChannelHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println(" 来自客户端的消息是：" + msg.getClass().getName() + "; 消息是：" + msg.toString());
		if (msg instanceof RequestMessage) {
			RequestMessage request = (RequestMessage) msg;
			byte[] attachment = GzipUtils.unzip(request.getAttachment());
			System.out.println(new String(attachment));
		}

		ResponseMessage response = new ResponseMessage(0L, "测试 响应");
		ctx.writeAndFlush(response);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("服务端异常");
		cause.printStackTrace();
		ctx.close();
	}
}
