package com.omicseq.store.cache;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeoutException;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.utils.AddrUtil;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.yanf4j.core.impl.StandardSocketOption;
import com.omicseq.concurrent.ThreadTaskPoolsFactory;
import com.omicseq.configuration.CacheClient;
import com.omicseq.configuration.CacheConfig;
import com.omicseq.configuration.CacheServer;
import com.omicseq.domain.FileInfo;
import com.omicseq.exception.WrappedInterruptedException;

/**
 * @author Min.Wang
 *
 */
public class XMemcachedProvider implements ICacheProvider {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected MemcachedClient mcc = null;
	
	protected int lifecycle = 86400; // 1 day
	
	private boolean initialize = false;
	
	private CacheClient memCachedConfig;

	@Override
	public CacheClient getConfig() {
		return memCachedConfig;
	}
	
	public void init(CacheClient m) {
		// grab an instance of our connection pool
		if (mcc == null) {
			synchronized (this) {
				if (mcc == null) {
					try {
						if (logger.isDebugEnabled()) {
							logger.debug("Using the configuration to initialize mem cached client:"
									+ m);
						}
						memCachedConfig = m;

						StringBuffer servers = new StringBuffer();
						for (CacheServer s : m.getCacheServerList()) {
							servers.append(s.getIp() + ":" + s.getPort() + " ");
						}

						MemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(servers.toString()));
						builder.setConnectionPoolSize(m.getMaxConn());
						builder.setSocketOption(StandardSocketOption.SO_RCVBUF, 128 * 1024); // set receive buffer as 32K, default is 16K
						builder.setSocketOption(StandardSocketOption.SO_SNDBUF, 64 * 1024); // set send buffer as 16K, default is 8K
						builder.setSocketOption(StandardSocketOption.TCP_NODELAY, m.isNagle()); // enable nagle
						builder.getConfiguration().setStatisticsServer(false); // disable connection statistic
						builder.getConfiguration().setReadThreadCount((int) (m.getCacheServerList().size() * 2));
						builder.getConfiguration().setSessionIdleTimeout(10000);
						builder.setCommandFactory(new BinaryCommandFactory());//use binary protocol 
						mcc = builder.build();
						mcc.setConnectTimeout(m.getSocketConnectTO());
						mcc.setOpTimeout(1000L);
						mcc.setMergeFactor(30);
						mcc.setOptimizeMergeBuffer(false); // close the buffer merge
						int initiaServerSize = mcc.getAvailableServers().size();
						if (m.isCompress()) {
							mcc.getTranscoder().setCompressionThreshold(m.getCompressThreshold());
						} else {
							mcc.getTranscoder().setCompressionThreshold(Integer.MAX_VALUE);
						}
						mcc.setSanitizeKeys(false);
						lifecycle = m.getLifecycle();

						if (initiaServerSize > 0) {
							initialize = true;
							if (logger.isInfoEnabled()) {
								logger.info("memcached client is initialized successfully");
							}
						}
					} catch (IOException e) {
						mcc = null;
						logger.error(" initial xmemcache client failed ", e);
					}
				}
			}
		}
	}

	@Override
	public boolean isInitialized() {
		return (mcc != null) && initialize;
	}

	
	
	
	/**
	 * Put data to cache using given key and default expiry time.
	 * @param key
	 * @param value
	 */
	@Override
	public boolean set(String key, Object value) {
		return set(key, value, lifecycle);
	}

	/**
	 * Put data to cache using given key and given expiry time
	 * @param key
	 * @param value
	 * @param expirySeconds
	 */
	@Override
	public boolean set(String key, Object value, long expirySeconds) {
		try {
			boolean result;
			if (expirySeconds > 0) {
				result = mcc.set(key, (int) expirySeconds, value);
			} else {
				// using default expiry time
				result = mcc.set(key, lifecycle, value);
			}
			return result;
		} catch (TimeoutException e) {
			logger.error("Set to xmemcached time out", e);
			return false;
		} catch (MemcachedException e) {
			logger.error("Set to xmemcached failed", e);
			return false;
		} catch (InterruptedException e) {
			logger.error(" exception happen ", e);
			throw new WrappedInterruptedException(e);
		} catch (Exception e) {
			logger.error("exception happen ", e);
			return false;
		}
	}

	/**
	 * Get data from cache by given key.
	 * @param key a key to get cached data.
	 * @return
	 */
	public Object get(String key) {
		try {
			return mcc.get(key, 2000);
		} catch (TimeoutException e) {
			logger.error("Get data by key " + key + " timed out", e);
			if (logger.isDebugEnabled()) {
				logger.debug("Stack for key(" + key + "):\n");
			}
			return null;
		} catch (MemcachedException e) {
			logger.error("Get data by key " + key + " failed", e);
			if (logger.isDebugEnabled()) {
				logger.debug("Stack for key(" + key + "):\n");
			}
			return null;
		} catch (InterruptedException e) {
			throw new WrappedInterruptedException(e);
		}

	}

	/**
	 * Get multiple data from cache by given keys, which is more efficient
	 * than looping the keys one by one. 
	 * @param keys array of keys to get cached data. 
	 * @return
	 */
	public Object[] get(String[] keys) {
		try {
			Map<String, Object> data = mcc.get(Arrays.asList(keys), 2000 + keys.length * 10);
			Object[] res = new Object[keys.length];
			for (int i = 0; i < keys.length; i++) {
				res[i] = data.get(keys[i]);
			}
			return res;
		} catch (TimeoutException e) {
			logger.error("Get multiple data for " + keys.length + " keys(" + keys[0]
					+ "...) timed out", e);
			if (logger.isDebugEnabled()) {
				logger.debug("Stack for keys(" + keys[0] + "...):\n");
			}
			return new Object[keys.length];
		} catch (MemcachedException e) {
			logger.error("Get multiple data for " + keys.length + " keys(" + keys[0]
					+ "...) failed", e);
			if (logger.isDebugEnabled()) {
				logger.debug("Stack for keys(" + keys[0] + "...):\n");
			}
			return new Object[keys.length];
		} catch (InterruptedException e) {
			throw new WrappedInterruptedException(e);
		}

	}

	/**
	 * Check if the given key exists in cache 
	 * @param key a key to check cached data.
	 * @return
	 */
	public boolean exists(String key) {
		try {
			return mcc.get(key) != null;
		} catch (TimeoutException e) {
			logger.error("Check exists by key " + key + " timed out", e);
			return false;
		} catch (MemcachedException e) {
			logger.error("Check exists by key " + key + " failed", e);
			return false;
		} catch (InterruptedException e) {
			throw new WrappedInterruptedException(e);
		}
	}

	/**
	 * Delete cached data by given key
	 * @param key a key to delete cached data.
	 * @return
	 */
	public boolean delete(String key) {
		try {
			return mcc.delete(key);
		} catch (TimeoutException e) {
			logger.error("delete by key " + key + " timed out", e);
			if (logger.isDebugEnabled()) {
				logger.debug("Stack for key(" + key + "):\n");
			}
			return false;
		} catch (MemcachedException e) {
			logger.error("delete by key " + key + " failed", e);
			if (logger.isDebugEnabled()) {
				logger.debug("Stack for key(" + key + "):\n");
			}
			return false;
		} catch (InterruptedException e) {
			throw new WrappedInterruptedException(e);
		}
	}

	/**
	 * Clean all cached data
	 */
	public void flushAll() {
		try {
			mcc.flushAll();
		} catch (TimeoutException e) {
			logger.error("Flush all timed out ", e);
		} catch (MemcachedException e) {
			logger.error("Flush all failed ", e);
		} catch (InterruptedException e) {
			throw new WrappedInterruptedException(e);
		}
	}

	public void flush(String keyGroup) {
		if (StringUtils.isBlank(keyGroup)) {
			flushAll();
			return;
		}
		try {
			keyGroup = keyGroup.toLowerCase();
			// 1st key is server name, 2nd key is itemname:number:field
			Map<InetSocketAddress, Map<String, String>> itemsByServerMap = mcc.getStats();
			for (InetSocketAddress s : itemsByServerMap.keySet()) {
				Map<String, String> dumpMap = itemsByServerMap.get(s);
				for (String key : dumpMap.keySet()) {
					if (key != null && key.startsWith(keyGroup)) {
						if (logger.isDebugEnabled()) {
							logger.debug("Deleting cache by key(" + key + ") from server " + s);
						}
						mcc.delete(key);
					}
				}
			}

		} catch (TimeoutException e) {
			logger.error("Flush cache by key group" + keyGroup + " timed out ", e);
		} catch (MemcachedException e) {
			logger.error("Flush cache by key group" + keyGroup + " failed ", e);
		} catch (InterruptedException e) {
			throw new WrappedInterruptedException(e);
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		if (mcc != null) {
			try {
				mcc.shutdown();
			} catch (IOException e) {
				logger.error(" shut down failed ", e);
			}
		}
	}

	@Override
	public boolean asySet(final String key, final Object value) {
		// TODO Auto-generated method stub
		 FutureTask<Boolean> task = new FutureTask<Boolean>(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				// TODO Auto-generated method stub
				return set(key, value);
			}
		 });
		ThreadTaskPoolsFactory.getThreadTaskPoolsExecutor().run(task);
		
		return true;
	}

	@Override
	public boolean asySet(final String key, final Object value, final long expirySeconds) {
		// TODO Auto-generated method stub
		 FutureTask<Boolean> task = new FutureTask<Boolean>(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				// TODO Auto-generated method stub
				return set(key, value);
			}
		 });
		ThreadTaskPoolsFactory.getThreadTaskPoolsExecutor().run(task);
		return true;
	}

}	
