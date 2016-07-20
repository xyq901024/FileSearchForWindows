package com.xyq.fs.dao;

import java.nio.file.Path;

import java.util.Set;

public interface ScanDao {

	/**
	 * 全盘索引
	 * @return 
	 */
	public int scanSystem();

	/**
	 * 指定目录索引
	 * 
	 * @param dir
	 * @return 
	 */
	public int scanDirs(Set<Path> dirs);
}
