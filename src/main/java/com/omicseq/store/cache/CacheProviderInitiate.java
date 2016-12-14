package com.omicseq.store.cache;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.configuration.CacheClient;
import com.omicseq.configuration.CacheConfig;
import com.omicseq.configuration.ConfigsHelper;

/**
 * @author Min.Wang
 *
 */
public class CacheProviderInitiate {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private static CacheProviderInitiate cacheProviderInitiate = new CacheProviderInitiate();
	public static CacheProviderInitiate getInstance() {
		return cacheProviderInitiate;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
    public boolean init() {
		CacheConfig  cacheConfig = ConfigsHelper.getCacheConfig();
		if (CollectionUtils.isNotEmpty(cacheConfig.getCacheClientList())) {
			for (CacheClient cacheClient : cacheConfig.getCacheClientList()) {
				String className = cacheClient.getClassName();
				try {
					Class clazz = Class.forName(className);
					Object object = clazz.newInstance();
					Method method = clazz.getMethod("init", CacheClient.class);
					method.invoke(object, cacheClient);
					if ("distributed".equalsIgnoreCase(cacheClient.getType())) {
					    if(logger.isDebugEnabled()){
                            logger.debug("CacheProvider is {} ",object);
                        }
						CacheProviderFactory.setCacheProvider((ICacheProvider)object);
					} 
					if ("local".equalsIgnoreCase(cacheClient.getType())) {
					    if(logger.isDebugEnabled()){
					        logger.debug("LocalCacheProvider is {} ",object);
					    }
						CacheProviderFactory.setLocalCacheProvider((ICacheProvider)object);
					}
				} catch (ClassNotFoundException e) {
					logger.error(" init cache provider failed : class name : " + className, e);
					return false;
				} catch (InstantiationException e) {
					logger.error(" init instance for class : " + className + " failed " , e);
					return false;
				} catch (IllegalAccessException e) {
					logger.error(" init instance for class : " + className + " failed " , e);
					return false;
				} catch (NoSuchMethodException e) {
					logger.error(" don't find method init, class: " + className, e);
					return false;
				} catch (InvocationTargetException e) {
					logger.error(" invoke init failed , class: " + className, e);
					return false;
				}
			}
		}
		
		return true;
	}
	
}
