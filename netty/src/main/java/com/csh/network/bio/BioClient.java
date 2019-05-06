package com.csh.network.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * 客户端
 * 
 * @author Administrator
 * @date 2019年5月5日
 * @package com.csh.network.bio
 */
public class BioClient {

	public static void main(String[] args) {
		String host = null;// 主机
		int port = 0;
		if (args.length > 2) {
			host = args[0];
			port = Integer.parseInt(args[1]);
		} else {
			host = "127.0.0.1";
			port = 9999;
		}

		Socket socket = null;
		BufferedReader reader = null;
		PrintWriter writer = null;
		Scanner input = new Scanner(System.in);
		try {
			socket = new Socket(host, port);// 启动一个socket
			String message = null;
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			writer = new PrintWriter(socket.getOutputStream(), true);
			while (true) {
				System.out.println("请输入你要输入的内容：");
				message = input.nextLine();
				if (message.equals("exit")) {
					break;
				}
				writer.println(message);
				writer.flush();
				System.out.println(reader.readLine());
			}
		} catch (UnknownHostException e) {
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
				if (writer != null) {
					writer.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

}
