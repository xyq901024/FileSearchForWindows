package com.xyq.fs.util;

import java.io.*;

import org.apache.log4j.Logger;



public class Load {

	static Logger logger = Logger.getLogger(Load.class);
	static {
		try {
			String arch = System.getProperty("os.arch");
			String dllName = "";
			if ("amd64".equals(arch)) {
				dllName = "jnotify_64bit";
			} else
				dllName = "jnotify";
			InputStream in = Load.class.getClassLoader().getResourceAsStream(
					dllName + ".dll");
			File dll = File.createTempFile(dllName, ".dll");
			FileOutputStream out = new FileOutputStream(dll);

			int i;
			byte[] buf = new byte[1024];
			while ((i = in.read(buf)) != -1) {
				out.write(buf, 0, i);
			}

			in.close();
			out.close();
			dll.deleteOnExit();

			System.load(dll.toString());//
			new File(MyFilesUtil.getAbsPathByName("rtindextoken//exit.txt"))
					.delete();
			logger.info("加载DLL文件成功" + dll.getAbsolutePath());
		} catch (Exception e) {
			logger.error("加载DLL文件失败" + e.getMessage());
			System.err.println("load jni error!");
		}
	}

	public static void init() {
	}

	public static void main(String[] args) {

		init();
	}

}
