package com.asd.template.plugin.monitor;

import java.io.PrintWriter;

import com.jcraft.jsch.SftpProgressMonitor;

/**
 * sftp文件传输监视器
 */
public class MySftpProgressMonitor implements SftpProgressMonitor {

	private PrintWriter pw = new PrintWriter(System.out);
	private long maxSize;
	private long currentSize;
	private long timeStart;
	private long currentTime;

	/**
	 * 
	 * */
	public void init(int op, String src, String dest, long max) {
		pw.print(String.format("file from %s --- to --> %s", src, dest));
		pw.println(String.format("file_size: %s k", max / 1024));
		pw.flush();
		long time = System.currentTimeMillis();
		this.timeStart = time;
		this.currentTime = time;
		this.maxSize = max;
	}

	/**
	 * 传输过程中，每1秒打印一次传输进度
	 */
	public boolean count(long count) {
		currentSize += count;
		long time = System.currentTimeMillis();
		if (time - currentTime >= 1000) {
			currentTime = time;
			pw.println(String.format("progress: ===> %s%% , time_used: ===> %ss", (currentSize * 100 / maxSize),
					(timeStart - currentTime) / 1000));
			pw.flush();
		}
		return true;
	}

	/**
	 * 传输完毕
	 */
	public void end() {
		pw.println(String.format("success ! --  totle_time:%ss", (System.currentTimeMillis() - timeStart) / 1000));
		pw.flush();
	}
}
