package com.omicseq.core;

import com.omicseq.concurrent.ThreadTaskPoolsFactory;
import com.omicseq.message.MemMessageTool;
import com.omicseq.store.cache.CacheProviderFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;

/**
 * 释放web 工程的所有资源，包含数据库连接资源，缓存连接资源等。 
 * @author Min.Wang
 */
public class WebResourceReleaser {

	private static WebResourceReleaser instance  = new WebResourceReleaser();
	
	public static WebResourceReleaser getInstance() {
		return instance;
	}
	
	
	
	public void release() {
		// close mongodb
		MongoDBManager.getInstance().close();
		// close thread pool
		ThreadTaskPoolsFactory.getThreadTaskPoolsExecutor().close();
		// close cache provider
		if (CacheProviderFactory.getCacheProvider() != null) {
			CacheProviderFactory.getCacheProvider().flushAll();
			CacheProviderFactory.getCacheProvider().close();
			System.out.println("system is shutdown, all cache flushed!");
		}
		//stop container
		MemMessageTool.getInstance().stop();
	}
	
}
