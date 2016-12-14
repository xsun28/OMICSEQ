package com.omicseq.store.cache;

import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.configuration.CacheClient;

/**
 * 
 * 
 * @author zejun.du
 */
public class EhcacheProvider implements ICacheProvider {
    private static final String DEFAULT_CACHE_NAME = "_default_";
    protected static Logger logger = LoggerFactory.getLogger(EhcacheProvider.class);
    private CacheManager cm = null;
    private Cache cache = null;
    private CacheClient cacheConfig;
    private boolean initialize = false;

    @Override
    public CacheClient getConfig() {
        return cacheConfig;
    }

    @Override
    public void init(CacheClient cacheConfig) {
        try {
            if (null == cm) {
                synchronized (this) {
                    this.cacheConfig = cacheConfig;
                    cm = CacheManager.create();
                    cache = new Cache(DEFAULT_CACHE_NAME, Integer.MAX_VALUE, false, false, 0, 0);
                    cm.addCache(cache);
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private Cache getCache() {
        return cache;
    }

    @Override
    public boolean isInitialized() {
        return initialize;
    }

    @Override
    public boolean asySet(String key, Object value) {
        return set(key, value, Long.MAX_VALUE);
    }

    @Override
    public boolean set(String key, Object value) {
        return asySet(key, value);
    }

    @Override
    public boolean asySet(String key, Object value, long expirySeconds) {
        int timeToIdleSeconds= Long.valueOf(expirySeconds).intValue();
        if (getCache().isKeyInCache(key)) {
            getCache().get(key).setTimeToIdle(timeToIdleSeconds);
        } else {
            Element el = new Element(key, value, timeToIdleSeconds, 0);
            getCache().put(el);
        }
        return getCache().isKeyInCache(key);
    }

    @Override
    public boolean set(String key, Object value, long expirySeconds) {
        return asySet(key, value, expirySeconds);
    }

    @Override
    public Object get(String key) {
        Element el = getCache().get(key);
        return null == el ? null : el.getObjectValue();
    }

    @Override
    public Object[] get(String[] keys) {
        List<Object> rs = new ArrayList<Object>(5);
        for (String key : keys) {
            Object obj = get(key);
            if (null != obj) {
                rs.add(obj);
            }
        }
        return rs.toArray();
    }

    @Override
    public boolean exists(String key) {
        return null != get(key);
    }

    @Override
    public boolean delete(String key) {
        Cache cache = getCache();
        return cache.remove(key);
    }

    @Override
    public void flushAll() {
        // do nothing
    }

    @Override
    public void close() {
        cm.clearAll();
        cm.shutdown();
    }
}
