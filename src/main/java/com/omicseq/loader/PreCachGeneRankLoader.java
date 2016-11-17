package com.omicseq.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.common.SortType;
import com.omicseq.concurrent.ThreadTaskPoolsFactory;
import com.omicseq.domain.CacheGeneRank;

/**
 * @author Min.Wang
 *
 */
public class PreCachGeneRankLoader  implements IGeneRankLoader {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Integer cacheSize = 200;
	//final private CacheGeneRankLoader  preLoadcacheGeneRankLoader = new CacheGeneRankLoader();
	private CacheGeneRankLoader  directLoadcacheGeneRankLoader = new CacheGeneRankLoader();
	
	public PreCachGeneRankLoader() {
		super();
		//preLoadcacheGeneRankLoader.setAsySet(false);
	}

	@Override
	public List<CacheGeneRank> load(final Integer geneId, final List<Integer> sourceList, final List<Integer> etypeList, final SortType sortType, final Double mixturePerc, final Integer start, final Integer limit) {
		// compute the load start and limit.
		Integer cacheStart = (start / cacheSize) * cacheSize;	
		List<CacheGeneRank> cacheGeneRankList = new ArrayList<CacheGeneRank>();
		if(limit == null){
			cacheGeneRankList = directLoadcacheGeneRankLoader.load(geneId, sourceList, etypeList, sortType, mixturePerc, cacheStart, null);
		}
		else{
			cacheGeneRankList = directLoadcacheGeneRankLoader.load(geneId, sourceList, etypeList, sortType, mixturePerc, cacheStart, cacheSize);
		}
		
		
		final Integer relativeStart = start % cacheSize;
		List<CacheGeneRank> finalCacheGeneRankList = new ArrayList<CacheGeneRank>();
		
		if(limit == null) {
			for (int i = 0; i < cacheGeneRankList.size(); i++) {
				if ((relativeStart + i) < cacheGeneRankList.size()) {
					finalCacheGeneRankList.add(cacheGeneRankList.get(relativeStart + i));
				}
			}
		}
		else{
			for (int i = 0; i < limit; i++) {
				if ((relativeStart + i) < cacheGeneRankList.size()) {
					finalCacheGeneRankList.add(cacheGeneRankList.get(relativeStart + i));
				}
			}
		}
		/*
		// pre load
		if ((relativeStart + limit) == cacheSize) {
			try {
				FutureTask<Boolean> task = new FutureTask<Boolean>(new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						// TODO Auto-generated method stub
						directLoadcacheGeneRankLoader.load(geneId, sourceList, etypeList, sortType, start, limit);
						return true;
					}
				});
				
				ThreadTaskPoolsFactory.getThreadTaskPoolsExecutor().run(task);
			} catch (Exception e) {
				logger.error(" exception happen on pre loading ", e);
			}
		}
		*/
		return finalCacheGeneRankList;
	}
	
	
	
	
	
	
}
