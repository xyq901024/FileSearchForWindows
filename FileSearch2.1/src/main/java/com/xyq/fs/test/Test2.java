package com.xyq.fs.test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.document.Document;
import org.junit.Test;

import com.xyq.fs.base.MyDirectory;
import com.xyq.fs.dao.ScanDao;
import com.xyq.fs.dao.ScanDaoImpl;
import com.xyq.fs.entity.Page;
import com.xyq.fs.resource.R;
import com.xyq.fs.service.FSService;
import com.xyq.fs.util.ReadFileUtil;

public class Test2 {

	private FSService fs = new FSService();

	/**
	 * 添加字段使用更新模式，可以避免重复添加问题
	 */
	@Test
	public void testAddDocuments() {
 
		Path p1 = Paths.get("d:/@aa！!==++ ;.txt");
		Document doc = ReadFileUtil.readPath(p1);
		Path p2 = Paths.get("d:/@aa--+！【1】》{1《!{} ; (2).txt");
		Document doc2 = ReadFileUtil.readPath(p2);
		Map<String,Document> map = new HashMap<>();
		map.put(p1.toString(), doc);
		map.put(p2.toString(), doc2);
		fs.updateDocuments(map);
		MyDirectory.commit();
		MyDirectory.closeAll();
	}
	
	@Test
	public void testSearch(){
		
		Page<Document> page = new Page<>();
		page.setCurrentPage(1);
		String queryStr= "";
		page.setQueryStr(queryStr);
		page = fs.search(page,true);
		System.out.println(page.getDocList().get(0).get(R.INDEX_NAMES.REAL_PATH));
	}
	
	/**
	 * 测试全盘索引
	 */
	@Test
	public void testScanSystem(){
		
		ScanDao dao = new ScanDaoImpl();
		dao.scanSystem();
	}
	public static void main(String[] args) {
		
         String s = "C:/Users/xyq/AndroidStudioProjects/MyApplication/app/build/intermediates/res/merged/debug/values-nl";
         System.out.println(s.getBytes().length);
	}
}
