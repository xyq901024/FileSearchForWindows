package com.xyq.fs.base;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class BaseBootUp {

	private static final int PORT = 20520;
	public static ServerSocket SERVER = null;

	
	 static class BindThread extends Thread {

		public void run() {
			try {
				SERVER.accept();
			} catch (IOException e) {
			}
		}
	}
	/**
	 * 判断程序是否启动
	 * 
	 * @return
	 * @throws IOException
	 */
	public static boolean bindPort() {
		// 创建socket,连接20520端口
		try {
			SERVER = new ServerSocket();
			SERVER.bind(new InetSocketAddress("127.0.0.1", PORT));
			new BindThread().start();
		} catch (IOException e) {
			return false;
		}
		return true;
	}



	public static void main(String[] args) throws IOException {

		bindPort();
	}
}
