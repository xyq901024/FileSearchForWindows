package com.xyq.fs.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;

public class ZipUtil {

	public static List<Path> previewZipFileNames(String file) throws Exception {

		ZipFile zipFile = new ZipFile(file);
		zipFile.setFileNameCharset("GBK");
		@SuppressWarnings("rawtypes")
		List headersList = zipFile.getFileHeaders();
		List<Path> fileNames = new ArrayList<>();
		for (int i = 0; i < headersList.size(); i++) {
			FileHeader subHeader = (FileHeader) headersList.get(i);
			//System.out.println(file + "/" + subHeader.getFileName());
			fileNames.add(Paths.get(file + "/" + subHeader.getFileName()));
			if (i > 100)
				break;
		}
		return fileNames;
	}

	public static void main(String[] args) throws Exception {

		previewZipFileNames("d:\\11.rar");
	}
}
