package com.xyq.fs.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;

public class Page<T> {
	/** 当前第几页(从1开始计算) */
	private int currentPage;
	/** 每页显示几条 */
	private int pageSize = 20;
	/** 总记录数 */
	private int totalRecord;
	/** 总页数 */
	private int totalPage;
	/** 分页数据集合[用泛型T来限定集合元素类型] */
	private Collection<T> items;
	/** 当前显示起始索引(从零开始计算) */
	private int startIndex;
	/** 当前显示结束索引(从零开始计算) */
	private int endIndex;
	/** 一组最多显示几个页码[比如Google一组最多显示10个页码] */
	private int groupSize;

	/** 左边偏移量 */
	private int leftOffset = 5;
	/** 右边偏移量 */
	private int rightOffset = 4;
	/** 当前页码范围 */
	private String[] pageRange;

	/** 分页数据 */
	private List<Document> docList = new ArrayList<>();
	/** 上一页最后一个ScoreDoc对象 */
	private ScoreDoc afterDoc;

	/** 上一页最后一个ScoreDoc对象的Document对象ID */
	private int afterDocId;

	private String queryStr;

	private Map<Integer, ScoreDoc> preSdoc = new HashMap<>();

	private double searchTime;

	public double getSearchTime() {
		return searchTime;
	}

	public void setSearchTime(double searchTime) {
		this.searchTime = searchTime;
	}

	public Map<Integer, ScoreDoc> getPreSdoc() {
		return preSdoc;
	}

	public void setPreSdoc(Map<Integer, ScoreDoc> preSdoc) {
		this.preSdoc = preSdoc;
	}

	public void setRangeIndex() {

		int groupSize = getGroupSize();
		int totalPage = getTotalPage();
		if (totalPage < 2) {
			startIndex = 0;
			endIndex = totalPage - startIndex;
		}

		else {
			int currentPage = getCurrentPage();
			// 如果页组数大于总页数，那么起始就是0到总页数-1
			if (groupSize >= totalPage) {
				startIndex = 0;
				endIndex = totalPage - startIndex - 1;
			}
			// 如果页组数不大于总数（说明有多页），那么起始就当前页-5，
			else {
				int leftOffset = getLeftOffset();
				// 中间索引等于左偏移量+1 = 6
				int middleOffset = getMiddleOffset();
				if (-1 == middleOffset) {
					startIndex = 0;
					endIndex = groupSize - 1;
				}
				// 如果当前页小于等于左偏移量，起始就是0，（实际当前页面是1到6）
				else if (currentPage <= leftOffset) {
					startIndex = 0;
					endIndex = groupSize - 1;
				}
				// 一旦当前页超过了6，那么起始就是当前页-偏移量= 2（下标就是2-1）
				else {
					startIndex = currentPage - leftOffset - 1;
					// 如果到了最后一页，也就是当前实际总数当前页组数不足10，那么最后一个索引就是最大页数
					if (currentPage + rightOffset > totalPage) {
						endIndex = totalPage - 1;
					}
					// 否则就是当前页-1
					else {
						endIndex = currentPage + rightOffset - 1;
					}
				}
			}
		}
	}

	/**
	 * 获取当前页,如果当前页大于0，如果当前页大于总页数（边际），那么当前页就是总页数
	 * 
	 * @return
	 */
	public int getCurrentPage() {
		if (currentPage <= 0) {
			currentPage = 1;
		} else {
			int totalPage = getTotalPage();
			if (totalPage > 0 && currentPage > getTotalPage()) {
				currentPage = totalPage;
			}
		}
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getPageSize() {
		if (pageSize <= 0) {
			pageSize = 20;
		}
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotalRecord() {
		return totalRecord;
	}

	public void setTotalRecord(int totalRecord) {
		this.totalRecord = totalRecord;
	}

	public int getTotalPage() {
		int totalRecord = getTotalRecord();
		if (totalRecord == 0) {
			totalPage = 0;
		} else {
			int pageSize = getPageSize();
			totalPage = totalRecord % pageSize == 0 ? totalRecord / pageSize
					: (totalRecord / pageSize) + 1;
		}
		return totalPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}

	public int getGroupSize() {
		if (groupSize <= 0) {
			groupSize = 10;
		}
		return groupSize;
	}

	public void setGroupSize(int groupSize) {
		this.groupSize = groupSize;
	}

	public int getLeftOffset() {
		leftOffset = getGroupSize() / 2;
		return leftOffset;

	}

	public void setLeftOffset(int leftOffset) {
		this.leftOffset = leftOffset;
	}

	public int getRightOffset() {
		int groupSize = getGroupSize();
		if (groupSize % 2 == 0) {
			rightOffset = (groupSize / 2) - 1;
		} else {
			rightOffset = groupSize / 2;
		}
		return rightOffset;
	}

	public void setRightOffset(int rightOffset) {
		this.rightOffset = rightOffset;
	}

	/** 中心位置索引[从1开始计算] */
	public int getMiddleOffset() {
		int groupSize = getGroupSize();
		int totalPage = getTotalPage();
		if (groupSize >= totalPage) {
			return -1;
		}
		return getLeftOffset() + 1;
	}

	/***
	 * 获取一组页面数字（索引+1）的范围，
	 * 
	 * @return
	 */
	public String[] getPageRange() {

		setRangeIndex();
		int size = endIndex - startIndex + 1;
		if (size <= 0) {
			return new String[0];
		}
		if (totalPage == 1) {
			return new String[] { "1" };
		}
		pageRange = new String[size];
		for (int i = 0; i < size; i++) {
			pageRange[i] = (startIndex + i + 1) + "";
		}
		return pageRange;
	}

	public void setPageRange(String[] pageRange) {
		this.pageRange = pageRange;
	}

	public Collection<T> getItems() {
		return items;
	}

	public void setItems(Collection<T> items) {
		this.items = items;
	}

	public List<Document> getDocList() {
		return docList;
	}

	public void setDocList(List<Document> docList) {
		this.docList = docList;
	}

	public ScoreDoc getAfterDoc() {
		setAfterDocId(afterDocId);
		return afterDoc;
	}

	public void setAfterDoc(ScoreDoc afterDoc) {
		this.afterDoc = afterDoc;
	}

	public int getAfterDocId() {
		return afterDocId;
	}

	public void setAfterDocId(int afterDocId) {
		this.afterDocId = afterDocId;
		if (null == afterDoc) {
			this.afterDoc = new ScoreDoc(afterDocId, 1.0f);
		}
	}

	//
	// public String[] getQueries() {
	// return queries;
	// }
	//
	// public void setQueries(String[] queries) {
	// this.queries = queries;
	// }

	public String getQueryStr() {
		return queryStr;
	}

	public void setQueryStr(String queryStr) {
		this.queryStr = queryStr;
	}

	public Page() {
	}

	public Page(int currentPage, int pageSize) {
		this.currentPage = currentPage;
		this.pageSize = pageSize;
	}

	public Page(int currentPage, int pageSize, Collection<T> items) {
		this.currentPage = currentPage;
		this.pageSize = pageSize;
		this.items = items;
	}

	public Page(int currentPage, int pageSize, Collection<T> items,
			int groupSize) {
		this.currentPage = currentPage;
		this.pageSize = pageSize;
		this.items = items;
		this.groupSize = groupSize;
	}

	public Page(int currentPage, int pageSize, int groupSize, int afterDocId) {
		this.currentPage = currentPage;
		this.pageSize = pageSize;
		this.groupSize = groupSize;
		this.afterDocId = afterDocId;
	}

	public static void main(String[] args) {
		Collection<Integer> items = new ArrayList<Integer>();
		int totalRecord = 201;
		for (int i = 0; i < totalRecord; i++) {
			items.add(new Integer(i));
		}
		Page<Integer> page = new Page<Integer>(1, 10, items, 10);
		page.setTotalRecord(totalRecord);
		int totalPage = page.getTotalPage();
		for (int i = 0; i < totalPage; i++) {
			page.setCurrentPage(i + 1);
			String[] pageRange = page.getPageRange();
			System.out.println("当前第" + page.currentPage + "页");
			for (int j = 0; j < pageRange.length; j++) {
				System.out.print(pageRange[j] + "  ");
			}
			System.out.println("\n");
		}
	}
}
