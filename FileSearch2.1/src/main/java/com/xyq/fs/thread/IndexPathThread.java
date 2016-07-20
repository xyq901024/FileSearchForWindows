package com.xyq.fs.thread;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import org.apache.lucene.document.Document;

import com.xyq.fs.util.ReadFileUtil;

public class IndexPathThread implements Callable<Document>{
   
	private Path path;
	
	public IndexPathThread(Path path){
		
		this.path = path;
	}
	@Override
	public Document call() throws Exception {
		
		return ReadFileUtil.readPath(path);
		
	}

}
