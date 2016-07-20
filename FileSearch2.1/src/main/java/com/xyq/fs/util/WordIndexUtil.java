package com.xyq.fs.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class WordIndexUtil {

	/**
	 * 通过XWPFWordExtractor访问XWPFDocument的内容
	 * 
	 * @throws IOException
	 * 
	 * @throws Exception
	 */

	@SuppressWarnings("resource")
	public String readDocx(InputStream is) throws IOException {

		XWPFWordExtractor extractor;
		String txt = null;
		XWPFDocument docx = new XWPFDocument(is);
		extractor = new XWPFWordExtractor(docx);
		txt = extractor.getText();

		return txt;
	}

	private String readDoc(InputStream is) throws IOException {

		HWPFDocument doc;
		String txt = null;
		doc = new HWPFDocument(is);
		txt = doc.getDocumentText();
		return txt;
	}

	public String readWord(Path path) {

		String txt = null;
		InputStream is = null;
		try {
			is = new FileInputStream(path.toString());
			if (path.toString().endsWith(".doc")) {
				txt = readDoc(is);
			} else if (path.toString().endsWith(".docx")) {
				txt = readDocx(is);
			}
		} catch (Exception e) {
			System.out.println(path.toString() + "无法读取");
		}finally{
			close(is);
		}
		return txt;
	}

	/**
	 * 关闭输入流
	 * 
	 * @param is
	 */
	private void close(InputStream is) {

		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception {


	

	}
}
