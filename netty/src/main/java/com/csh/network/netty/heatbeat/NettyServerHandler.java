package com.csh.network.netty.heatbeat;

import java.util.ArrayList;
import java.util.List;

import com.csh.network.util.HeatbeatMessage;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

@Sharable
public class NettyServerHandler extends ChannelHandlerAdapter {

	private static List<String> credentials = new ArrayList<>();
	private static final String HEATBEAT_SUCCESS = "SERVER_RETURN_HEATBEAT_SUCCESS";

	public NettyServerHandler() {
		credentials.add("10.221.153.223_CSH");
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof String) {
			this.checkCredential(ctx, msg.toString());
		} else if (msg instanceof HeatbeatMessage) {
			this.readHeatbeatMessage(ctx, msg);
		} else {
			ctx.writeAndFlush("wrong message").addListener(ChannelFutureListener.CLOSE);
		}
	}

	private void readHeatbeatMessage(ChannelHandlerContext ctx, Object msg) {
		HeatbeatMessage message = (HeatbeatMessage) msg;
		System.out.println(message);
		System.out.println("=======================================");
		ctx.writeAndFlush("receive heatbeat message");
	}

	private void checkCredential(ChannelHandlerContext ctx, String credential) {
		System.out.println("1."+credential);
		System.out.println(credentials);
		if (credentials.contains(credential)) {
			ctx.writeAndFlush(HEATBEAT_SUCCESS);
		} else {
			ctx.writeAndFlush("no credential contains").addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("服务端异常");
		ctx.close();
	}
}
