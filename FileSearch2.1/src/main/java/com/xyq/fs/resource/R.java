package com.xyq.fs.resource;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.xyq.fs.util.MyFilesUtil;

public class R {

	/**
	 * 索引字段名称配置
	 * 
	 * @author xyq
	 * 
	 */
	public static class INDEX_NAMES {

		public static String REAL_PATH = "real_path";
		public static String FILE_TYPE = "file_type";
		public static String FILE_PATH = "file_path";
		public static String CONTENT = "content";
		public static String PARENT_PATH = "parent_path";
		public static String FILE_TIME = "file_time";

	}

	/**
	 * 屏幕尺寸
	 * 
	 * @author xyq
	 * 
	 */
	public static class DIMENSIONS {
		static Dimension screensize = Toolkit.getDefaultToolkit()
				.getScreenSize();
		public static int WIDTH = (int) screensize.getWidth();
	}

	/**
	 * 各种标记配置
	 * 
	 * @author xyq,当要索引的时候，先查看
	 * 
	 */
	public static class TOKENS {

		// 索引开始标记，如果索引正在进行中
		private static AtomicInteger INDEX_TOKEN = new AtomicInteger(0);

		/**
		 * 索引运行时，由索引任务操控，索引任务开始时设为1，结束时设为0
		 */
		public static AtomicInteger INDEX_STATU = new AtomicInteger(0);

		/**
		 * 索引任务运行时不断的检测标记,当他为1时，表示立即停止索引任务。
		 */
		public static AtomicInteger INDEX_STOP = new AtomicInteger(0);

		/**
		 * 文件监控观察标记
		 */
		public static AtomicInteger MONITOR_NUM = new AtomicInteger(0);
		public static AtomicInteger MONITOR_STOP = new AtomicInteger(0);

		public static void INDEX_BEGIN() {

			if (INDEX_STATU.get() == 0) {
				INDEX_TOKEN.set(0);
				INDEX_STOP.set(0);
				// 索引开始时文件监控关闭
				MONITOR_NUM.set(1);
			}
		}
	}

	/**
	 * 查询使用的字段配置
	 * 
	 * @author xyq
	 * 
	 */
	public static class SEARCHS {
		/**
		 * 跨域查询的基本字段
		 */
		public static String[] QUERIES_INDEX_NAMES = { INDEX_NAMES.FILE_PATH,
				INDEX_NAMES.CONTENT };

		/**
		 * 需要查询的文件类型
		 */
		public static Set<String> QUERIES_FILE_TYPES = new HashSet<>();

		/**
		 * 需要查询的父类路径
		 */
		public static Set<String> QUERIES_PARENT_PATH = new HashSet<>();

		/**
		 * 需要索引的父类路径
		 */
		public static Set<Path> INDEX_PARENT_PATH = new HashSet<>();

		/**
		 * 查询次数
		 */
		public static AtomicInteger SEARCH_NUM = new AtomicInteger();

	}

	/**
	 * 文件类型配置
	 * 
	 * @author xyq
	 * 
	 */
	public static class TYPES {

		public static String ALL_FILE_TYPES = "{图         片:['jpg','png', 'jpeg',  'gif', 'ico', 'bpm', 'psd', 'pic', 'svg', 'eps', 'cdr', 'ai', 'ps', 'wmf'],"
				+ "音        频:['wav', 'aif', 'mp3', 'ram', 'wma', 'amr', 'aac', 'ogg', 'flac', 'mid','au'],"
				+ "视        频:['avi', 'mpg', 'mov', 'swf', 'mp4', 'rm', 'rmvb','mkg', 'dvi', 'flv', '3gp', 'wmv', 'vcd', 'svcd', 'dvd', 'mkv'],"
				+ "可    执行:['zip', 'tar', 'gz', 'rar','jar','iso', 'exe', 'bak' ,'torrent'],"
				+ "办公文档:['ppt', 'pptx', 'doc', 'docx', 'wps', 'xls', 'xlsx', 'vsd', 'vsdx', 'dot', 'pdf', 'rtf', 'dwg', 'dxf'],"
				+ "普通文档:['txt', 'java', 'php', 'py', 'js', 'html','shtml', 'm', 'h', 'mm', 'cpp', 'log','md', 'c', 'vc', 'css','jsp','xml','sql']}";

		public static String[] ROOT_TYPES = { "图         片", "办公文档", "普通文档",
				"可    执行", "视        频", "音        频" };

		/**
		 * 这是为了JDK7全盘扫描做的
		 */
		public static String INDEX_TYPES = "";

		/**
		 * 全盘监控的类型
		 */
		public static Set<String> MYTYPES = new HashSet<>();
		/**
		 * 解析过的类型map
		 */
		public static Map<String, Set<String>> INDEX_TYPES_MAP = new HashMap<>();

		/**
		 * 加载文件类型
		 */
		private static void loadTypes() {

			INDEX_TYPES += "*.{";
			JSONObject jsonObj = new JSONObject(ALL_FILE_TYPES);
			for (String rootType : ROOT_TYPES) {
				JSONArray jsonArray = jsonObj.getJSONArray(rootType);
				Set<String> set = new HashSet<>();
				for (int i = 0; i < jsonArray.length(); i++) {
					String ft = jsonArray.getString(i);
					set.add(ft);
					INDEX_TYPES += ft + ",";
					MYTYPES.add(ft);
				}
				INDEX_TYPES_MAP.put(rootType, set);
			}
			INDEX_TYPES = INDEX_TYPES.substring(0, INDEX_TYPES.length() - 2)
					+ "}";
		}

		static {
			loadTypes();
		}
	}

	public static class FILTERS {

		public static Set<String> FILTER_DIRS = new HashSet<>();
		private static String FILTER_FILE = "filter\\filter.txt";
		static {
			loadFilters();
		}

		/**
		 * 加载过滤文件
		 */
		private static void loadFilters() {

			Path path = Paths.get(MyFilesUtil.getAbsPathByName(FILTER_FILE));
			if (Files.exists(path)) {
				try {
					List<String> list = FileUtils.readLines(path.toFile(),
							"utf-8");
					for (String s : list) {
						FILTER_DIRS.add(s);
					}
					FILTER_DIRS.add(MyFilesUtil.getAbsPathByName(""));
					FILTER_DIRS.add(System.getProperty("user.home")
							+ "\\AppData");
					FILTER_DIRS.add(System.getProperty("user.home")
							+ "\\Documents");
					FILTER_DIRS.add(System.getProperty("user.home")
							+ "\\Public");
					FILTER_DIRS.add(System.getProperty("user.home")
							+ "\\Default");

					FILTER_DIRS.add("lucenedirs");
					FILTER_DIRS.add(".metadata");
					FILTER_DIRS.add("myeclipse2014\\configuration");

				} catch (IOException e) {

				}
			}
		}

		/**
		 * 是否在过滤列表内
		 * 
		 * @param path
		 * @return
		 */
		public static boolean isFilterFile(Path path) {

			if (path.toString().indexOf(":\\$") != -1)
				return true;
			for (String s : FILTER_DIRS) {
				if (path.startsWith(s) || path.toString().indexOf(s) != -1) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * 文件监控配置
	 * 
	 * @author xyq
	 * 
	 */
	public static class FILE_MONITOR {

		public static Set<String> MONITOR_DIRS = new HashSet<>();
	}

	public static class THREADS {

		public static int CPU_NUM = Runtime.getRuntime().availableProcessors();
	}

	public static void main(String[] args) {

		System.out.println(DIMENSIONS.WIDTH);
	}

}
