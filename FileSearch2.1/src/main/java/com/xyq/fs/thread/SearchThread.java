package com.xyq.fs.thread;

import org.apache.lucene.document.Document;
import com.xyq.fs.entity.Page;
import com.xyq.fs.login.SearchWindow;
import com.xyq.fs.service.FSService;

/**
 * 查询线程，为主面板减压
 * 
 * @author xyq
 * 
 */
public class SearchThread extends Thread {

	private FSService fs = new FSService();
	private Page<Document> page;

	public SearchThread(Page<Document> page) {

		this.page = page;
	}

	public void run() {

		fs.search(page, true);
		if (page.getDocList().size() == 0) {
			SearchWindow.actiontarget.setText("关键词:  \"" + page.getQueryStr()
					+ "\"  未搜索到任何文件!!!");
		}
	}
}
