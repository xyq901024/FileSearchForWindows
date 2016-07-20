package com.xyq.fs.dao;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;

import com.xyq.fs.entity.Page;

public interface FsDao {

	public void addDocuments(List<Document> dList);

	public void deleteDocuments(Path path) throws IOException;

	public void updateDocuments(Map<String, Document> map);

	public void updateDocument(Path path, Document doc) throws IOException;

	public Page<Document> search(Page<Document> page, boolean useRtReader)
			throws IOException;

	public boolean isNeedIndex(IndexSearcher searcher, Path path);

	/**
	 * 索引自检（删除废弃索引）
	 */
	public void indexSelfCheck();

	public double getSearchTime(long startTime, long endTime);
}
