package com.xyq.fs.thread;

import com.xyq.fs.base.MyDirectory;

public class CommitThread extends Thread{

	public void run(){
		
		MyDirectory.commit();
	}
}
