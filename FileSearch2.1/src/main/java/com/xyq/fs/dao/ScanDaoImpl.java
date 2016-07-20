package com.xyq.fs.dao;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javafx.concurrent.Task;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;

import com.xyq.fs.base.MyDirectory;
import com.xyq.fs.login.SearchWindow;
import com.xyq.fs.resource.R;
import com.xyq.fs.thread.FileMonitorThread;
import com.xyq.fs.thread.IndexPathThread;

/**
 * 边扫描边索引
 * 
 * @author xyq
 * 
 */
public class ScanDaoImpl implements ScanDao {

	private FsDao fsDao = new FsDaoImpl();

	@Override
	public int scanSystem() {

		Iterable<Path> roots = FileSystems.getDefault().getRootDirectories();
		Set<Path> dirs = new HashSet<>();
		for (Path root : roots) {
			dirs.add(root);
		}
		return scanDirs(dirs);
	}

	Task<Void> task = new Task<Void>() {

		@Override
		protected Void call() throws Exception {

			return null;
		}
	};

	@Override
	public int scanDirs(Set<Path> dirs) {
		
		ScanUtil su  = new ScanUtil(dirs);
		su.setDaemon(true);
		su.setPriority(3);
		su.start();
		return 1;
	}

	/**
	 * 这个是后台启动索引线程
	 * 
	 * @author xyq
	 * 
	 */
	class ScanUtil extends Thread {

		private Set<Path> dirs;

		public ScanUtil(Set<Path> dirs) {

			this.dirs = dirs;
		}

		ExecutorService pool = Executors.newFixedThreadPool(R.THREADS.CPU_NUM);
		IndexSearcher searcher = new IndexSearcher(MyDirectory.getRTIndexReader());

		@Override
		public void run() {

			// 索引任务开始标记
			R.TOKENS.INDEX_STATU.set(1);
			for (Path dir : dirs) {
				try {
					System.out.println("开始扫描:" + dir);
					Files.walkFileTree(dir, new SimpleScan());
					System.out.println(dir + "扫描完毕");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				pool.shutdown();
				pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// 扫描结束后要记得commit一下
			MyDirectory.commit();
			// 主界面索引文字清除
			SearchWindow.actiontarget.setText("");
			// 索引任务结束标记
			R.TOKENS.INDEX_STATU.set(0);
			R.TOKENS.MONITOR_NUM.set(0);
			//重新开启文件监控服务
				if("停止监控".equals(SearchWindow.btn5.getText())){
					new FileMonitorThread().start();
				}
				SearchWindow.pb.setVisible(false);
				SearchWindow.indexingTxt.setText("");
		}

		/**
		 * 这个方法必须要能控制全盘索引结束
		 */

		class SimpleScan extends SimpleFileVisitor<Path> {

			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {

				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc)
					throws IOException {
				// 如果错误信息中包含X:\System Volume Information，这是表示系统的隐藏盘，是不能读的
				// System.out.println(exc.getMessage());
				return FileVisitResult.CONTINUE;
			}

			/**
			 * 访问文件夹之前，对于需要过滤的就跳过
			 */
			@Override
			public FileVisitResult preVisitDirectory(Path dir,
					BasicFileAttributes attrs) {

				// 立即终止扫描
				if (R.TOKENS.INDEX_STOP.get() == 1) {
					R.TOKENS.INDEX_STATU.set(0);
					return FileVisitResult.TERMINATE;
				}
				// 跳过过滤掉的文件夹
				if (R.FILTERS.isFilterFile(dir)) {
					System.out.println(dir + "整个文件夹跳过");
					return FileVisitResult.SKIP_SUBTREE;
				}
				Map<Path, Future<Document>> fMap = new HashMap<Path, Future<Document>>();
				// 开始扫描
				DirectoryStream<Path> pathsFilter;
				try {
					pathsFilter = Files.newDirectoryStream(dir,
							R.TYPES.INDEX_TYPES);
					for (Path file : pathsFilter) {
						if (fsDao.isNeedIndex(searcher, file)) {
							System.out.println(file + "需要重新索引");
							fMap.put(file,
									pool.submit(new IndexPathThread(file)));
						}
					}

					for (Path file : fMap.keySet()) {
						try {
							fsDao.updateDocument(file, fMap.get(file).get());
						} catch (InterruptedException | ExecutionException e) {
							System.out.println(file + "索引任务失败");
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				return FileVisitResult.CONTINUE;
			}
		}
	}

	public static void main(String[] args) {

		ScanDao dao = new ScanDaoImpl();
		dao.scanSystem();
	}
}
