package com.omicseq.store.cache;

import com.omicseq.configuration.CacheClient;

/**
 * @author Min.Wang
 *
 */
public interface ICacheProvider {

	CacheClient getConfig();

	void init(CacheClient cacheClient);

	boolean isInitialized();
	

	boolean asySet(String key, Object value);
	
	boolean set(String key, Object value);
	
	boolean asySet(String key, Object value, long expirySeconds);

	boolean set(String key, Object value, long expirySeconds);
	

	Object get(String key);

	Object[] get(String[] keys);

	boolean exists(String key);

	boolean delete(String key);

	/**
	 * Clean all cached data
	 */
	void flushAll();

	void close();

}
