package com.omicseq.concurrent;

/**
 * @author Min.Wang
 *
 */
public class ThreadTaskPoolsFactory {

	public static IThreadTaskPoolsExecutor getThreadTaskPoolsExecutor() {
		return ThreadTaskPoolsExecutor.getInstance();
	}
	
}
