package com.csh.network.bio.threadpool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池的Bio服务器
 * 
 * @author Administrator
 * @date 2019年5月5日
 * @package com.csh.network.bio.threadpool
 */
public class BioServer {

	public static void main(String[] args) {
		int port = genPort(args);

		ServerSocket server = null;
		ExecutorService service = Executors.newFixedThreadPool(10);
		try {
			server = new ServerSocket(port);
			System.out.println(" Server Started! ");
			while (true) {
				Socket socket = server.accept();
				service.execute(new Handler(socket));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
				writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
				String readerMessage = null;
				while (true) {
					System.out.println(" server reader... ");
					if ((readerMessage = reader.readLine()) == null) {
						break;
					}
					System.out.println(readerMessage);
					writer.println(readerMessage);
					writer.flush();
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				socket = null;
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				reader = null;
				if (writer != null) {
					writer.close();
				}
				writer = null;
			}

		}

	}

	private static int genPort(String[] args) {
		if (args.length > 2) {
			return Integer.valueOf(args[0]);
		} else
			return 9999;
	}
}
