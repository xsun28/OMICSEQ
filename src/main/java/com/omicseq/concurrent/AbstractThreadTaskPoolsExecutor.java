package com.omicseq.concurrent;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractThreadTaskPoolsExecutor implements IThreadTaskPoolsExecutor {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected static String THREAD_POOL_FILE = "threadpool.properties";

	protected final ConcurrentMap<String, Integer[]> taskPoolLimitMap = new ConcurrentHashMap<String, Integer[]>();
	protected static final Integer[] DEFAULT_POOL_SIZE = new Integer[] { 3, 10 };

    protected boolean init = false;

    public void init() {
        if (init) {
            return;
        }
        internalInitialize();
        init = true;
    }
	
	private void internalInitialize() {
		// initialize some pool limits
		InputStream is = AbstractThreadTaskPoolsExecutor.class.getClassLoader().getResourceAsStream(THREAD_POOL_FILE);
		if (is != null) {
			// load to properties
			Properties props = new Properties();
			try {
				props.load(is);
			} catch (IOException e) {
				logger.warn("Loading " + THREAD_POOL_FILE
						+ " failed, using default configurations for thread pools", e);
			}

			// parse properties
			Enumeration<?> names = props.propertyNames();
			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				Integer[] limits = parsePoolLimits(props.getProperty(name));
				if (limits != null) {
					taskPoolLimitMap.put(name, limits);
					if (logger.isDebugEnabled()) {
						logger.debug("Applied limit for thread pool " + name + ": "
								+ StringUtils.join(limits, ","));
					}
				}
			}
			try {
				is.close();
			} catch (IOException e) {
			}
		}

		// initialize all the thread pools
		for (String name : taskPoolLimitMap.keySet()) {
			// force to initializing pool.
			createTaskPool(name, true);
		}
	}
	
	protected abstract void createTaskPool(String name, boolean createIfNotExist);

	private Integer[] parsePoolLimits(String limitString) {
		if (StringUtils.isBlank(limitString)) {
			return null;
		}
		String[] limitArr = StringUtils.split(limitString.trim(), ",");
		try {
			Integer[] limits = new Integer[2];
			limits[0] = Integer.valueOf(limitArr[0].trim());
			if (limitArr.length > 1) {
				limits[1] = Integer.valueOf(limitArr[1].trim());
			} else {
				limits[1] = limits[0];
			}
			return limits;
		} catch (NumberFormatException e) {
			logger.warn("The configuration(" + limitString + ") for thread pool is invalid", e);
			return null;
		}
	}

	
}
