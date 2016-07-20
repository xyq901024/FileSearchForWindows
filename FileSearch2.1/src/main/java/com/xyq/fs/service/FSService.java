package com.xyq.fs.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;

import com.xyq.fs.dao.FsDao;
import com.xyq.fs.dao.FsDaoImpl;
import com.xyq.fs.dao.ScanDao;
import com.xyq.fs.dao.ScanDaoImpl;
import com.xyq.fs.entity.Page;
import com.xyq.fs.login.ResultWindow;
import com.xyq.fs.resource.R;
import com.xyq.fs.thread.CommitThread;
import com.xyq.fs.thread.FileMonitorThread;

public class FSService {

	private FsDao dao = new FsDaoImpl();
	private ScanDao sDao = new ScanDaoImpl();
	private static TimerTask timerTask = null;
	public static Timer timer;

	Logger logger = Logger.getLogger(FSService.class);

	/**
	 * 批量添加索引
	 * 
	 * @param dList
	 */
	public void addDocuments(List<Document> dList) {

		dao.addDocuments(dList);
	}

	/**
	 * 删除索引
	 * 
	 * @param path
	 */
	public void deleteDocuments(Path path) {

		try {
			dao.deleteDocuments(path);
		} catch (IOException e) {
			logger.error(path + "删除失败," + e.getMessage());
		}
	}

	/**
	 * 批量更新
	 * 
	 * @param map
	 */
	public void updateDocuments(Map<String, Document> map) {

		dao.updateDocuments(map);
	}

	/**
	 * 分页查询
	 * 
	 * @param page
	 * @return
	 */
	public Page<Document> search(Page<Document> page, boolean useRtReader) {

		try {
			page = dao.search(page, useRtReader);
			if (page.getDocList().size() > 0)
				new ResultWindow(page).setVisible(true);
		} catch (IOException e) {
			page.setDocList(new ArrayList<Document>());
			logger.error(page.getQueryStr() + "查询失败," + e.getMessage());
		}
		return page;
	}

	/**
	 * 打开实时监控
	 */
	public void openFileMonitor() {

		new FileMonitorThread().start();

	}

	/**
	 * 关闭实时监控
	 */
	public void closeFileMonitor() {

		R.TOKENS.MONITOR_NUM.set(1);

	}

	public int openIndexSystem() {

		R.TOKENS.INDEX_BEGIN();
		if (R.SEARCHS.INDEX_PARENT_PATH.size() > 0) {
			return sDao.scanDirs(R.SEARCHS.INDEX_PARENT_PATH);
		} else
			return sDao.scanSystem();
	}

	/**
	 * 关闭全盘索引
	 */
	public void closeIndexSystem() {

		R.TOKENS.INDEX_STOP.set(1);
		new CommitThread().start();
	}

	/**
	 * 索引自检
	 */
	public void indexSelfCheck() {

		if (timerTask == null) {
			timerTask = new TimerTask() {
				@Override
				public void run() {
					dao.indexSelfCheck();
				}
			};
		}
		if (timer == null) {
			timer = new Timer();
			// 系统启动3分钟后运行，每小时运行一次
			timer.schedule(timerTask, 1000 * 60 * 3, 1000 * 60 * 60 * 1);
		}
	}
}
