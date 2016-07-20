package com.xyq.fs.file.monitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.search.IndexSearcher;

import com.xyq.fs.base.MyDirectory;
import com.xyq.fs.dao.FsDao;
import com.xyq.fs.dao.FsDaoImpl;
import com.xyq.fs.dao.ScanDao;
import com.xyq.fs.dao.ScanDaoImpl;
import com.xyq.fs.resource.R;
import com.xyq.fs.util.ReadFileUtil;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyListener;

/**
 * 文件目录监听,直接所有盘符
 */
public class FileDirMonitor {

	private FsDao dao = new FsDaoImpl();
	private ScanDao sDao = new ScanDaoImpl();
	private IndexSearcher searcher = new IndexSearcher(
			MyDirectory.getRTIndexReader());
	private Set<String> dirs;

	Logger logger = Logger.getLogger(FileDirMonitor.class);

	public FileDirMonitor(Set<String> dirs) {

		this.dirs = dirs;
	}

	/**
	 * 一个JNotify测试实例
	 * 
	 * @throws Exception
	 */
	public void sample() throws Exception {

		try {
			logger.info("实时监控程序开始运行...");
			System.out.println("实时索引系统启动..");

			// 被监听事件
			int mask = JNotify.FILE_CREATED // 文件创建
					| JNotify.FILE_DELETED // 文件删除
					| JNotify.FILE_MODIFIED // 文件修改
					| JNotify.FILE_RENAMED; // 文件改名

			// 是否监听子目录
			boolean watchSubtree = true;
			List<Integer> watchList = new ArrayList<>();
			Map<Integer, String> watchDirMap = new HashMap<>();
			// 添加监听目录
			for (String dir : dirs) {
				int watchID = JNotify.addWatch(dir, mask, watchSubtree,
						new Listener(dir));
				watchList.add(watchID);
				watchDirMap.put(watchID, dir);

			}
			// 这里主线程需要休眠一段时间，否则则主线程结束的话，监听时间就会结束
			/***
			 * 
			 */
			try {
				while (true) {
					if (R.TOKENS.MONITOR_NUM.get() == 1)
						break;
				}
				R.TOKENS.MONITOR_NUM.set(0);
			} catch (Exception e) {
				e.printStackTrace();
				// 休眠被唤醒时，该捕获异常，不该额外处理，继续后续操作
				System.err.println("主线程被打断");
			} finally {
				// 移除监听
				for (int watchID1 : watchList) {
					boolean res = JNotify.removeWatch(watchID1);
					if (!res) {
						System.err.println(watchDirMap.get(watchID1)
								+ "监听目录移除监听失败");
					}

				}
				System.out.println("退出");
				logger.info("实时监控程序退出...");
			}
		} catch (Exception e) {
			logger.error("实时监控程序崩溃.." + e.getMessage());
		}
	}

	/**
	 * 文件监听器,作为内部类
	 */
	class Listener extends Thread implements JNotifyListener {

		public Listener(String filePath) {

			System.out.println(filePath + "开始被监控...");
		}

		/**
		 * 监听文件重命名事件
		 * 
		 * @param wd
		 *            被监听目录ID
		 * @param rootPath
		 *            被监听目录
		 * @param oldName
		 *            被改名前文件名
		 * @param newName
		 *            被改名后文件名
		 * 
		 * 
		 */
		public void fileRenamed(int wd, String rootPath, String oldName,
				String newName) {

			// 这里一定要新文件，旧文件因为已经删了
			Path newFile = Paths.get(rootPath, newName);
			Path oldFile = Paths.get(rootPath, oldName);

			// 删除旧索引
			try {
				dao.deleteDocuments(oldFile);
			} catch (IOException e) {
				logger.error(oldFile + "索引删除失败," + e.getMessage());
			}
			// 添加新索引,如果修改的是文件夹的名字，那么就要重新索引其子类文件（夹）
			if (Files.isDirectory(newFile)) {
				Set<Path> dirs = new HashSet<>();
				dirs.add(newFile);
				sDao.scanDirs(dirs);
			} else
				try {
					dao.updateDocument(newFile, ReadFileUtil.readPath(newFile));
					System.out.println(oldName + "重命名为:" + newFile);
				} catch (IOException e) {
					logger.error(oldFile + "索引更新失败," + e.getMessage());
				}
		}

		/**
		 * 该方法监听文件修改事件
		 * 
		 * @param wd
		 *            被监听目录ID
		 * @param rootPath
		 *            被监听目录
		 * @param name
		 *            被修改文件名
		 */

		/**
		 * 注意：JNotify存在一次文件修改，触发多次fileModified方法的BUG，
		 * 该方法可以用来修复一次文件修改可能会触发多个fileModified方法，
		 * 从而减少没有必要的资源重新加载。但是由于t变量是类内共享变量，所 以注意线程安全，尽量避免共用Listener导致错误
		 * 
		 * 
		 * 
		 * 
		 */
		Map<String, Long> fileToken = new HashMap<>();

		/**
		 * 文件被修改,修复同一个文件至少重复走2次bug，如果一个文件不是一个内容可索引的类型，那么它 在当前的索引字段下，是无需重复索引的。
		 * 判断文件是否为过滤
		 */
		public synchronized void fileModified(int wd, String rootPath,
				String name) {

			Path file = Paths.get(rootPath, name);
			String suffix = ReadFileUtil.getFileTypeSuffix(file);
			// 不是被过滤的,不是文件夹,是指定监控类型
			if (!R.FILTERS.isFilterFile(file) && !Files.isDirectory(file)
					&& R.TYPES.MYTYPES.contains(suffix)) {
				Long old = fileToken.get(file.toFile().getAbsolutePath());
				// 必须要新建一个基础类long的对象，否则要么挂掉，要么不对
				long now = file.toFile().lastModified();
				if (old == null || old != now) {
					if (dao.isNeedIndex(searcher, file)) {
						try {
							dao.updateDocument(file,
									ReadFileUtil.readPath(file));
							System.out.println(file + "修改成功");
						} catch (IOException e) {
							logger.error(file + "更新失败," + e.getMessage());
						}
						fileToken.put(file.toFile().getAbsolutePath(), now);
					}
				}
			}
		}

		/**
		 * 监听文件删除事件
		 * 
		 * @param wd
		 *            被监听目录ID
		 * @param rootPath
		 *            被监听目录
		 * @param name
		 *            被删除文件名 注意：如果被删除的对象是一个目录。那么还必须将该目录下的所有文件一并删除 ，如果是改名事假下的
		 * 
		 */
		public void fileDeleted(int wd, String rootPath, String name) {

			Path file = Paths.get(rootPath, name);
			String suffix = ReadFileUtil.getFileTypeSuffix(file);
			// 不在过滤列表内同时是指定监控类型，或者是文件夹，就可以删除
			if ((!R.FILTERS.isFilterFile(file) && R.TYPES.MYTYPES
					.contains(suffix)) || Files.isDirectory(file)) {
				try {
					dao.deleteDocuments(file);
					System.out.println(file + "删除成功");
				} catch (IOException e) {
					logger.error(file + "删除失败," + e.getMessage());
				}
			}
		}

		/**
		 * 监听文件创建事件 如果是文件夹，或者是指定类型
		 * 
		 * @param wd
		 *            被监听目录ID
		 * @param rootPath
		 *            被监听目录
		 * @param name
		 *            被创建文件名
		 */
		public void fileCreated(int wd, String rootPath, String name) {

			Path file = Paths.get(rootPath, name);
			String suffix = ReadFileUtil.getFileTypeSuffix(file);
			// 是文件夹或者不在过滤范围内并且是指定类型
			if (Files.isDirectory(file) || !R.FILTERS.isFilterFile(file)
					&& R.TYPES.MYTYPES.contains(suffix)) {
				try {
					dao.updateDocument(file, ReadFileUtil.readPath(file));
					System.out.println(file + "添加成功");
				} catch (IOException e) {
					logger.error(file + "添加失败," + e.getMessage());
				}
			}
		}

		/**
		 * 错误打印
		 * 
		 * @param msg
		 *            错误信息
		 */
		void print(String msg) {
			System.err.println(" " + msg);
		}
	}

	/**
	 * 测试主程序 11
	 * 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// FileDirMonitor fd = new FileDirMonitor();
		// fd.sample();
	}

}
