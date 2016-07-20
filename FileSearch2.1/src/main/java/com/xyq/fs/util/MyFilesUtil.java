package com.xyq.fs.util;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.ParsingDetector;
import info.monitorenter.cpdetector.io.UnicodeDetector;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class MyFilesUtil {

	/***
	 * 这是获取文件路径的方法
	 */
	private static CodepageDetectorProxy detector;
	static {
		detector = CodepageDetectorProxy.getInstance();
		detector.add(new ParsingDetector(false));
		detector.add(JChardetFacade.getInstance());// 需要第三方JAR包:antlr.jar、chardet.jar.
		detector.add(ASCIIDetector.getInstance());
		detector.add(UnicodeDetector.getInstance());
	}

	public static String getCharsetName(Path path) {

		Charset charset = null;
		try {
			charset = detector.detectCodepage(path.toFile().toURI().toURL());
		} catch (Exception e) {

		}
		String charsetName = "GBK";
		if (charset != null) {
			if (charset.name().equals("US-ASCII")) {
				charsetName = "ISO_8859_1";
			} else if (charset.name().startsWith("UTF")) {
				charsetName = charset.name();// 例如:UTF-8,UTF-16BE.
			}
		}
		return charsetName;
	}

	public static String getAbsPathByName(String fileName) {

		String path = null;
		path = System.getProperty("user.dir") + "\\conf\\" + fileName;
		return path;
	}




	public void copeFile(final String FilePath) {

		Transferable t = new Transferable() {
			// 返回对象一定是要一个集合
			@Override
			public Object getTransferData(DataFlavor flavor)
					throws UnsupportedFlavorException, IOException {
				String[] p = { FilePath };
				List<File> l = new ArrayList<>();
				for (String str : p) {
					l.add(new File(str));
				}
				return l;
			}

			@Override
			public DataFlavor[] getTransferDataFlavors() {

				DataFlavor[] d = new DataFlavor[] { DataFlavor.javaFileListFlavor };

				return d;
			}

			@Override
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				boolean b = DataFlavor.javaFileListFlavor.equals(flavor);

				return b;
			}
		};
		// Put the selected files into the system clipboard
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(t, null);

		// 返回指定flavor类型的数据

	}
}
