package com.omicseq.configuration;

import java.util.ArrayList;
import java.util.List;

import com.omicseq.common.CacheType;

/**
 * @author Min.Wang
 *
 */
public class CacheClient {

	private String type = CacheType.memcached.name();
	private String name;
	private boolean disabled = false;
	private String className = "com.sears.wishbook.cache.JavaMemcachedManager";
	private int initConn = 5;
	private int minConn = 5;
	private int maxConn = 250;
	private int maxIdle = 21600000;
	private int maintSleep = 30;
	private boolean nagle = false;
	private int socketTO = 3000;
	private int socketConnectTO = 0;
	private int lifecycle = 86400; // 1 day
	private boolean compress = false;
	private int compressThreshold = 65536;
	
	private List<CacheServer> cacheServerList;

	public List<CacheServer> getCacheServerList() {
		return cacheServerList;
	}

	public void setCacheServerList(List<CacheServer> cacheServerList) {
		this.cacheServerList = cacheServerList;
	}

	public void addCacheServer(CacheServer cacheServer) {
		if (cacheServerList == null) {
			this.cacheServerList = new ArrayList<CacheServer>();
		}
		this.cacheServerList.add(cacheServer);
	}
	

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public int getInitConn() {
		return initConn;
	}

	public void setInitConn(int initConn) {
		this.initConn = initConn;
	}

	public int getMinConn() {
		return minConn;
	}

	public void setMinConn(int minConn) {
		this.minConn = minConn;
	}

	public int getMaxConn() {
		return maxConn;
	}

	public void setMaxConn(int maxConn) {
		this.maxConn = maxConn;
	}

	public int getMaxIdle() {
		return maxIdle;
	}

	public void setMaxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
	}

	public int getMaintSleep() {
		return maintSleep;
	}

	public void setMaintSleep(int maintSleep) {
		this.maintSleep = maintSleep;
	}

	public boolean isNagle() {
		return nagle;
	}

	public void setNagle(boolean nagle) {
		this.nagle = nagle;
	}

	public int getSocketTO() {
		return socketTO;
	}

	public void setSocketTO(int socketTO) {
		this.socketTO = socketTO;
	}

	public int getSocketConnectTO() {
		return socketConnectTO;
	}

	public void setSocketConnectTO(int socketConnectTO) {
		this.socketConnectTO = socketConnectTO;
	}

	public int getLifecycle() {
		return lifecycle;
	}

	public void setLifecycle(int lifecycle) {
		this.lifecycle = lifecycle;
	}

	public boolean isCompress() {
		return compress;
	}

	public void setCompress(boolean compress) {
		this.compress = compress;
	}

	public int getCompressThreshold() {
		return compressThreshold;
	}

	public void setCompressThreshold(int compressThreshold) {
		this.compressThreshold = compressThreshold;
	}

	@Override
	public String toString() {
		return "CacheClient [type=" + type + ", name=" + name + ", disabled=" + disabled
				+ ", className=" + className + ", initConn=" + initConn + ", minConn=" + minConn
				+ ", maxConn=" + maxConn + ", maxIdle=" + maxIdle + ", maintSleep=" + maintSleep
				+ ", nagle=" + nagle + ", socketTO=" + socketTO + ", socketConnectTO="
				+ socketConnectTO + ", lifecycle=" + lifecycle + ", compress=" + compress
				+ ", compressThreshold=" + compressThreshold + "]";
	}
	

}
