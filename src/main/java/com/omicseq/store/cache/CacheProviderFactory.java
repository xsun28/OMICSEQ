package com.omicseq.store.cache;

/**
 * @author Min.Wang
 *
 */
public class CacheProviderFactory {

	private static ICacheProvider cacheProvider;
	private static ICacheProvider localCacheProvider;
	
    public static ICacheProvider getCacheProvider() {
        return null == cacheProvider && null != localCacheProvider ? localCacheProvider : cacheProvider;
    }

	public static void setCacheProvider(ICacheProvider cacheProvider) {
		CacheProviderFactory.cacheProvider = cacheProvider;
	}

	public static ICacheProvider getLocalCacheProvider() {
		return localCacheProvider;
	}

	public static void setLocalCacheProvider(ICacheProvider localCacheProvider) {
		CacheProviderFactory.localCacheProvider = localCacheProvider;
	}
	
	
}
