package com.xyq.fs.base;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.xyq.fs.util.MyFilesUtil;

/***
 * 本类封装了基本的writer和reader信息，自动写入硬盘
 * 
 * @author xyq
 * 
 */
@SuppressWarnings("deprecation")
public class MyDirectory {

	private static String dirName = "lucenedirs";
	private static Directory dir;

	private static IndexWriter writer;

	private static IndexWriterConfig writerConfig;

	/**
	 * 这个read主要是为了全盘索引用的
	 */
	private static IndexReader reader;

	/**
	 * 这个是自定义过滤表，没有过滤任何词汇，保证完全可搜索
	 */
	private static Set<String> stopWords = new HashSet<>();
	private static CharArraySet cs = new CharArraySet(stopWords, true);
	private static Analyzer analyzer = new StandardAnalyzer(cs);

	/**
	 * 初始化
	 */
	static {
		stopWords.add("★");
		try {
			dir = FSDirectory.open(Paths.get(MyFilesUtil
					.getAbsPathByName(dirName)));
			IndexWriter.isLocked(dir);
			writerConfig = new IndexWriterConfig(analyzer);

			// 128M自动提交
			writerConfig.setRAMBufferSizeMB(128.0);

			writer = new IndexWriter(dir, writerConfig);
			writer.commit();
			reader = getRTIndexReader();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 注意：增删改操作都主动抛出了异常，这样可以在业务逻辑环境下使用日志记录出错原因
	 */

	/**
	 * 添加一篇文档
	 * 
	 * @param doc
	 * @throws IOException
	 */
	public static void addDocument(Document doc) throws IOException {

		writer.addDocument(doc);
	}

	/**
	 * 删除一系列文档
	 * 
	 * @param query
	 * @throws IOException
	 */
	public static void deleteDocuments(Query query) throws IOException {

		writer.deleteDocuments(query);

	}

	/**
	 * 删除一系列文档
	 * 
	 * @param field
	 * @param value
	 * @throws IOException
	 */
	public static void deleteDocuments(String field, String value)
			throws IOException {

		writer.deleteDocuments(new Term(field, value));
	}

	/**
	 * 更新一篇文档
	 * 
	 * @param field
	 * @param value
	 * @param doc
	 * @throws IOException
	 */
	public static void updateDocument(String field, String value, Document doc)
			throws IOException {

		writer.updateDocument(new Term(field, value), doc);
	}

	/**
	 * writer提交后，也要更新reader，此方法只要在程序结束时调用即可
	 */
	public static void commit() {

		try {
			if (writer.isOpen()) {
				writer.commit();
				reader = getIndexReader();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 这个获取到的reader是为了在全盘索引下尽量使用同一个reader
	 */
	public static IndexReader getIndexReader() {

		return reader;
	}

	/**
	 * 这个获取到的永远是最新的reader，是为了做实时检索而使用的
	 * 
	 * @return
	 */
	public static IndexReader getRTIndexReader() {

		try {
			return DirectoryReader.open(writer, true, true);
		} catch (IOException e) {
			return reader;
		}
	}

	/**
	 * 强制刷新reader
	 * 
	 * @throws IOException
	 */
	public static void refreshIndexreader() throws IOException {

		reader = getRTIndexReader();
	}

	/**
	 * 获取所有数据量
	 * 
	 * @return
	 */
	public static int getIndexTotalCount() {

		return writer.numDocs();
	}

	/**
	 * 查询
	 * 
	 * @return
	 * @throws IOException
	 */
	public static ScoreDoc[] search(IndexSearcher searcher, Query query,
			ScoreDoc after, int queryNum) throws IOException {

		ScoreDoc[] sdocs = null;
		TopDocs top = null;
		if (after == null) {
			top = searcher.search(query, queryNum);
		} else {
			top = searcher.searchAfter(after, query, queryNum);
		}
		if (top != null)
			sdocs = top.scoreDocs;
		return sdocs;
	}

	/**
	 * 关闭资源
	 */
	public static void closeAll() {

		try {
			writer.commit();
			writer.close();
			reader.close();
			dir.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {

	}
}
