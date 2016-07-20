package com.xyq.fs.dao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;

import com.xyq.fs.base.MyDirectory;
import com.xyq.fs.entity.Page;
import com.xyq.fs.resource.R;
import com.xyq.fs.util.Arith;
import com.xyq.fs.util.ReadFileUtil;

/**
 * 只提供索引的增删改查
 * 
 * @author xyq
 * 
 */
public class FsDaoImpl implements FsDao {

	Logger logger = Logger.getLogger(FsDaoImpl.class);

	/**
	 * 批量添加索引
	 */
	@Override
	public void addDocuments(List<Document> dList) {

		for (Document doc : dList) {
			try {
				MyDirectory.addDocument(doc);
			} catch (IOException e) {
				logger.error("无法添加" + doc.get(R.INDEX_NAMES.REAL_PATH) + ","
						+ e.getMessage());
			}
		}
	}

	/**
	 * 删除索引
	 * 
	 * @throws IOException
	 * 
	 */
	@Override
	public void deleteDocuments(Path path) throws IOException {

		MyDirectory.deleteDocuments(new TermQuery(new Term(
				R.INDEX_NAMES.PARENT_PATH, path.toString())));
	}

	/**
	 * 批量更新索引
	 * 
	 * @param map
	 */
	@Override
	public void updateDocuments(Map<String, Document> map) {

		for (String filePath : map.keySet()) {
			try {
				MyDirectory.updateDocument(R.INDEX_NAMES.REAL_PATH, filePath,
						map.get(filePath));
			} catch (IOException e) {
				logger.error("无法更新" + filePath + "," + e.getMessage());
			}
		}
	}

	/**
	 * 分页查询
	 */
	@Override
	public Page<Document> search(Page<Document> page, boolean useRtReader)
			throws IOException {

		long begin = new Date().getTime();
		ScoreDoc[] scoreDocs = null;
		// 构建基本查询，路径+内容
		BooleanQuery.Builder bQuery = new BooleanQuery.Builder();
		for (String queryStr : page.getQueryStr().split(" ")) {
			BooleanQuery.Builder query2 = new BooleanQuery.Builder();
			String[] queryStrToArray = queryStrToArray(queryStr.toLowerCase());
			for (int y = 0; y < R.SEARCHS.QUERIES_INDEX_NAMES.length; y++) {
				Query query3 = new PhraseQuery(0,
						R.SEARCHS.QUERIES_INDEX_NAMES[y], queryStrToArray);
				query2.add(query3, BooleanClause.Occur.SHOULD);
			}
			bQuery.add(query2.build(), BooleanClause.Occur.MUST);
		}
		// 加入查询文件类型
		for (String type : R.SEARCHS.QUERIES_FILE_TYPES) {
			bQuery.add(new TermQuery(new Term(R.INDEX_NAMES.FILE_TYPE, type)),
					BooleanClause.Occur.MUST);
		}
		// 加入查询路径
		for (String parentPath : R.SEARCHS.QUERIES_PARENT_PATH) {
			bQuery.add(new TermQuery(new Term(R.INDEX_NAMES.PARENT_PATH,
					parentPath)), BooleanClause.Occur.MUST);
		}
		IndexReader reader = null;
		if (useRtReader)
			reader = MyDirectory.getRTIndexReader();
		else
			reader = MyDirectory.getIndexReader();
		IndexSearcher searcher = new IndexSearcher(reader);

		/**
		 * 如果是第一次查询，那么就要全查一次
		 */
		if (page.getCurrentPage() == 1 && page.getPreSdoc().get(1) == null) {
			scoreDocs = MyDirectory.search(searcher, bQuery.build(), null,
					Integer.MAX_VALUE);
			if (scoreDocs != null)
				page.setTotalRecord(scoreDocs.length);
			scoreDocs = MyDirectory.search(searcher, bQuery.build(), null,
					page.getPageSize());
		} else
			scoreDocs = MyDirectory.search(searcher, bQuery.build(), page
					.getPreSdoc().get(page.getCurrentPage() - 1), page
					.getPageSize());
		page.setDocList(new ArrayList<Document>());
		if (scoreDocs != null) {
			for (ScoreDoc doc : scoreDocs) {
				page.getDocList().add(searcher.doc(doc.doc));
			}
			if (scoreDocs.length > 0) {
				page.setAfterDoc(scoreDocs[scoreDocs.length - 1]);
				page.getPreSdoc().put(page.getCurrentPage(),
						scoreDocs[scoreDocs.length - 1]);
			}
		}
		long end = new Date().getTime();
		page.setSearchTime(getSearchTime(begin, end));
		return page;
	}

	private String[] queryStrToArray(String queryStr) {

		String[] queryStrArray = new String[queryStr.length()];
		for (int i = 0; i < queryStrArray.length; i++) {
			queryStrArray[i] = queryStr.charAt(i) + "";
		}
		return queryStrArray;
	}

	/**
	 * 更新一篇文档
	 * 
	 * @throws IOException
	 */
	@Override
	public void updateDocument(Path path, Document doc) throws IOException {

		MyDirectory.updateDocument(R.INDEX_NAMES.REAL_PATH, path.toString(),
				doc);
	}

	/**
	 * 是否需要索引(缓存),时间一致，文件夹，不可读文件都不需要再次缓存，前提是索引已经存在
	 */
	@Override
	public boolean isNeedIndex(IndexSearcher searcher, Path path) {

		Query query = new TermQuery(new Term(R.INDEX_NAMES.REAL_PATH,
				path.toString()));
		try {
			ScoreDoc[] scoreDocs = MyDirectory.search(searcher, query, null, 1);
			if (scoreDocs != null && scoreDocs.length > 0) {
				for (ScoreDoc sdoc : scoreDocs) {
					Document doc = searcher.doc(sdoc.doc);
					if ((path.toFile().lastModified() + "").equals(doc
							.get(R.INDEX_NAMES.FILE_TIME))
							|| !isReadAbleFile(path))
						return false;
				}
			} else
				return true;

		} catch (IOException e) {
			return true;
		}
		return true;
	}

	/**
	 * 这个方法要验证不是目录，不是office系列，不是可读文本
	 * 
	 * @param file
	 * @return
	 */
	private boolean isReadAbleFile(Path file) {

		if (Files.isDirectory(file))
			return false;
		String suffix = null;
		String filePath = file.toString().toLowerCase();
		int lastindex = filePath.lastIndexOf(".");
		if (lastindex != -1) {
			suffix = filePath.substring(lastindex + 1);
			if (!"doc".equals(suffix) && !"docx".equals(suffix)
					&& !"xls".equals(suffix) && !"xlsx".equals(suffix)
					&& !ReadFileUtil.isReadAbleFile(suffix))
				return false;
		} else
			return false;
		return true;
	}

	/**
	 * 如果正在索引，就等待
	 */
	@Override
	public void indexSelfCheck() {

		while (true) {
			if (R.TOKENS.INDEX_STATU.get() == 0)
				break;
			else {
				System.out.println("暂停中。。");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		Set<Path> delSet = new HashSet<>();
		System.out.println("索引自检开始...");
		IndexReader reader = MyDirectory.getRTIndexReader();
		IndexSearcher searcher = new IndexSearcher(reader);
		int maxNum = reader.numDocs();
		int delNum = 0;
		for (int i = 0; i < maxNum; i++) {
			Path path = null;
			Document doc;
			try {
				doc = searcher.doc(i);
				path = Paths.get(doc.get(R.INDEX_NAMES.REAL_PATH));
				if (!Files.exists(path)) {
					delSet.add(path);
				}
			} catch (IOException e) {
				logger.error(path + "无法查询," + e.getMessage());
			}
		}

		for (Path path : delSet) {
			try {
				deleteDocuments(path);
				delNum++;
			} catch (IOException e) {
				logger.error(path + "删除失败," + e.getMessage());
			}
		}

		MyDirectory.commit();
		System.out.println("本次索引自检共删除:" + delNum + "条无效索引");
		logger.info("本次索引自检共删除:" + delNum + "条无效索引");
	}

	/**
	 * 获取查询时间
	 */
	@Override
	public double getSearchTime(long startTime, long endTime) {

		long time = endTime - startTime;
		return Arith.div((time % (1000 * 60)), 1000, 2);
	}

	public static void main(String[] args) throws InterruptedException {

		FsDao dao = new FsDaoImpl();
		long a = new Date().getTime();
		long b = new Date().getTime();
		dao.getSearchTime(a, b);
	}

}
