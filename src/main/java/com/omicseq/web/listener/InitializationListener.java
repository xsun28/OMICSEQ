package com.omicseq.web.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.core.WebResourceInitiate;
import com.omicseq.core.WebResourceReleaser;

/**
 * @author Min.Wang
 *
 */
public class InitializationListener implements ServletContextListener {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			WebResourceInitiate.getInstance().init();
		} catch (Exception e) {
			logger.error(" init failed ", e);
		}
		
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		WebResourceReleaser.getInstance().release();
	}

}
