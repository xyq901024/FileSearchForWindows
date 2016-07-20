package com.xyq.fs.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;

public class PptUtil {

	// 直接抽取ppt97-2003的全部内容 ppt
	public String readPPT(Path path) {

		InputStream is = null;
		PowerPointExtractor extractor = null;
		String text = null;
		try {
			is = new FileInputStream(path.toString());
			extractor = new PowerPointExtractor(is);
			text = extractor.getText();
			extractor.close();
		} catch (Exception e) {
			System.out.println("getTextFromPPT IO错误" + path.toString());
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
		}
		return text;
	}

	/**
	 * 读取pptx
	 * 
	 * @param path
	 * @return
	 */
	public String readPPTX(Path path) {

		String text = null;
		InputStream is = null;
		XMLSlideShow slide = null;
		try {
			is = new FileInputStream(path.toString());
			slide = new XMLSlideShow(is);
			XSLFPowerPointExtractor extractor = new XSLFPowerPointExtractor(
					slide);
			text = extractor.getText();
			extractor.close();
		} catch (FileNotFoundException e) {
			System.out.println("没有找到指定路径" + path.toString());
		} catch (IOException e) {
			System.out.println("getTextFromPPT2007 IO错误" + path.toString());
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e1) {
				}
			try {
				if (slide != null)
					slide.close();
			} catch (IOException e) {
			}
		}
		return text;
	}

	public static void main(String[] args) {
		
		PptUtil pt = new PptUtil();
		// System.out.println(readPPT(Paths.get("d:\\11.ppt")));
		System.out.println(pt.readPPTX(Paths.get("d:\\11.pptx")));
	}
}