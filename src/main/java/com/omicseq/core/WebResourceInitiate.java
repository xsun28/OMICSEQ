package com.omicseq.core;

import java.util.ArrayList;
import java.util.List;

import com.omicseq.common.MessageTopic;
import com.omicseq.concurrent.ThreadTaskPoolsFactory;
import com.omicseq.message.MemMessageTool;
import com.omicseq.robot.message.FileInfoConsumer;
import com.omicseq.robot.message.FileInfoProducer;
import com.omicseq.store.cache.CacheProviderInitiate;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;

/**
 * 
 * 初始化web工程需要的资源 
 * @author Min.Wang
 */
public class WebResourceInitiate {
	private static WebResourceInitiate instance  = new WebResourceInitiate();
	// init items
	private static List<IInitializeable> initializeableItemList = new ArrayList<IInitializeable>();
	static {
		initializeableItemList.add(MiRNASampleCache.getInstance());
		initializeableItemList.add(GeneCache.getInstance());
		initializeableItemList.add(TxrRefCache.getInstance());
		initializeableItemList.add(SampleCache.getInstance());
		initializeableItemList.add(AntibodyCache.getInstance());
		initializeableItemList.add(MouseGeneCache.getInstance());
		initializeableItemList.add(MouseTxrRefCache.getInstance());
		//initializeableItemList.add(GeneRankCount.getInstance());
	}
	
	public static WebResourceInitiate getInstance() {
		return instance;
	}
	
	public void init() {
		// init mongodb
		MongoDBManager.getInstance();
		// init memcached
		CacheProviderInitiate.getInstance().init();
		// init thread pool
		ThreadTaskPoolsFactory.getThreadTaskPoolsExecutor().init();
		
		// subscribe message topic.
        MemMessageTool tool = MemMessageTool.getInstance();
        tool.subscribe(MessageTopic.down, FileInfoProducer.class, FileInfoConsumer.getInstance());
        // run container
        tool.runContainer();
		
		// load cache
		for (IInitializeable iInitializeable : initializeableItemList) {
			iInitializeable.init();
		}
	}
	
	public static void main(String[] args) {
		Long start = System.currentTimeMillis();
		WebResourceInitiate.getInstance().init();
		System.out.println(" loading time : " + String.valueOf(System.currentTimeMillis() - start));
		GeneCache geneCache = GeneCache.getInstance();
		System.out.println(" using geen cache : "+geneCache);
	}
	
}
