package com.csh.network.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Bio 模型
 * 
 * @author Administrator
 * @date 2019年5月5日
 * @package com.csh.network.bio
 */
public class BioServer {

	public static void main(String[] args) {
		int port = genPort(args);
		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
			System.out.println(" server started !");
			while (true) {
				Socket socket = server.accept();// 监听
				new Thread(new Handler(socket)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (server != null) {
				try {
					server.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// 内部类主要处理socket的通道事情
	static class Handler implements Runnable {
		Socket socket = null;

		public Handler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			BufferedReader reader = null;
			PrintWriter writer = null;
			try {
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));// 输入流
				writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));// 打印
				String readerContent = null;
				while (true) {
					System.out.println(" server reader... start ");
					if ((readerContent = reader.readLine()) == null) {
						break;
					}
					writer.println(" server recive:" + readerContent);
					writer.flush();
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (writer != null) {
						writer.close();
					}
					if (reader != null) {
						reader.close();
					}
					if (socket != null) {
						socket.close();
					}
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}

	}

	/**
	 * 端口号
	 * 
	 * @param args
	 * @return
	 */
	private static int genPort(String[] args) {
		if (args.length > 0) {
			try {
				return Integer.parseInt(args[0]);
			} catch (Exception e) {
				return 9999;
			}
		} else {
			return 9999;
		}
	}
}
