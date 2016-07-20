package com.xyq.fs.thread;

import org.apache.log4j.Logger;

import com.xyq.fs.file.monitor.FileDirMonitor;
import com.xyq.fs.resource.R;


public class FileMonitorThread extends Thread {

	Logger logger = Logger.getLogger(FileMonitorThread.class);
    
    public void run(){
    	
    	// 实时监控开启必须在索引结束后,不能多开实时监控
    			if (R.TOKENS.MONITOR_NUM.get() == 1)
    				return ;
    			try {
    				new FileDirMonitor(R.FILE_MONITOR.MONITOR_DIRS).sample();
    			
    			} catch (Exception e) {
    				logger.error("文件监控启动失败," + e.getMessage());
    			}
    }
}
