package com.omicseq.core;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCache<K, V> implements IInitializeable {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    // key is genename, value is gene
    private ConcurrentMap<K, V> cache = new ConcurrentHashMap<K, V>();
    protected boolean lazy = false;

    public AbstractCache() {
    }

    public AbstractCache(boolean lazy) {
        this.lazy = lazy;
    }

    @Override
    public void init() {
        if (lazy) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Init All data cache ");
        }
        doInit();
    }

    public synchronized void refresh() {
        cache.clear();
        init();
    }

    public Integer size() {
        return cache.size();
    }

    protected Collection<V> values() {
        return cache.values();
    }

    public void put(K key, V value) {
        if (null != value) {
            cache.put(key, value);
        }
    }

    public V get(K key) {
        if (lazy && !isContainsKey(key)) {
            String lock = String.format("%s_%s", getClass().getName(), key).intern();
            synchronized (lock) {
                if (logger.isDebugEnabled()) {
                    logger.debug("lazy load data by:" + key);
                }
                V val = lazyLoad(key);
                put(key, val);
            }
        }
        return cache.get(key);
    }

    public boolean isContainsKey(K key) {
        return cache.containsKey(key);
    }

    abstract void doInit();

    abstract V lazyLoad(K key);
}
