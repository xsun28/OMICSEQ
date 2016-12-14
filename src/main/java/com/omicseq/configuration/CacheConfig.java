package com.omicseq.configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Min.Wang
 *
 */
public class CacheConfig {

	private List<CacheClient> cacheClientList;

	public void addCacheClient(CacheClient cacheClient) {
		if (cacheClientList == null) {
			cacheClientList = new ArrayList<CacheClient>();
		}
		cacheClientList.add(cacheClient);
	}

	public List<CacheClient> getCacheClientList() {
		return cacheClientList;
	}

	public void setCacheClientList(List<CacheClient> cacheClientList) {
		this.cacheClientList = cacheClientList;
	}

	@Override
	public String toString() {
		return "CacheConfig [cacheClient=" + cacheClientList + ", cacheServerList=" 
				+ "]";
	}
	
}
