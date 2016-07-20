package com.xyq.fs.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import com.xyq.fs.resource.R;

public class ReadFileUtil {

	private static ExcelUtils eu = new ExcelUtils();
	private static WordIndexUtil wu = new WordIndexUtil();
	private static PptUtil pt = new PptUtil();

	/**
	 * 读取一个path，不需要过滤验证
	 * 
	 * @param path
	 * @return
	 */
	public static Document readPath(Path path) {

		Document doc = new Document();
		String suffix = getFileTypeSuffix(path);
		String content = null;
		if (suffix != null) {
			/**
			 * 因为这里涉及到了zip包里面的文件
			 */
			if (Files.exists(path)) {
				switch (suffix) {
				case "doc":
					content = wu.readWord(path);
					break;
				case "docx":
					content = wu.readWord(path);
					break;
				case "xls":
					content = eu.readExcel(path);
					break;
				case "xlsx":
					content = eu.readExcel(path);
					break;
				case "ppt":
					content = pt.readPPT(path);
					break;
				case "pptx":
					content = pt.readPPTX(path);
					break;
				default:
					String charsetName = null;
					if (isReadAbleFile(suffix)) {
						charsetName = MyFilesUtil.getCharsetName(path);
						if ("UTF-16LE".equals(charsetName)) {
							charsetName = "GBK";
						}
						try {
							content = FileUtils.readFileToString(path.toFile(),
									charsetName);
						} catch (IOException e) {
						}
					}
					break;
				}
			}
		}
		// 存类型
		doc.add(new StringField(R.INDEX_NAMES.FILE_TYPE, suffix, Store.NO));

		if (content != null) {
			doc.add(new TextField(R.INDEX_NAMES.CONTENT, content, Store.NO));
		}
		// 存分词路径TextField
		doc.add(new TextField(R.INDEX_NAMES.FILE_PATH, path.toString(),
				Store.NO));

		doc.add(new StringField(R.INDEX_NAMES.REAL_PATH, path.toString(),
				Store.YES));

		// 存时间
		doc.add(new StringField(R.INDEX_NAMES.FILE_TIME, path.toFile()
				.lastModified() + "", Store.YES));

		// 存父类类型
		doc.add(new StringField(R.INDEX_NAMES.PARENT_PATH, path.toString(),
				Store.YES));
		Path parent = path.getParent();
		while (parent != null) {
			doc.add(new StringField(R.INDEX_NAMES.PARENT_PATH, parent
					.toString(), Store.YES));
			parent = parent.getParent();
		}
		return doc;
	}

	public static String getFileTypeSuffix(Path path) {

		String filePath = path.toString().toLowerCase();
		String suffix;
		int lastToken = filePath.lastIndexOf(".");
		if (lastToken != -1) {
			suffix = filePath.substring(lastToken + 1);
			return suffix;
		}
		return null;
	}

	/**
	 * 是否为普通可读文本
	 * 
	 * @param suffix
	 * @return
	 */
	public static boolean isReadAbleFile(String suffix) {

		return R.TYPES.INDEX_TYPES_MAP.get("普通文档").contains(suffix);
	}
	
	public static void main(String[] args) {
		
		readPath(Paths.get("d:\\11.pptx"));
	}
}
