package com.omicseq.utils;


import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.omicseq.exception.ResourceIOException;


/**
 * parser xml to object using digester
 * @author Min.Wang
 *
 */
public class DigesterUtils {
	
	private static Logger logger = LoggerFactory.getLogger(ResourceLoadUtils.class);
	
	/* (non-Javadoc)
	 * @see com.sears.grip.serviceimpl.simpleapi.parser.IParser#parse(java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public static <T> T  parse(String input, String digesterRuleFilePath, Class<T> z) {
		try {
			String rule= ResourceLoadUtils.load(digesterRuleFilePath);
			Digester digester = DigesterLoader.createDigester(new InputSource(new ByteArrayInputStream(rule.getBytes())));
			
			digester.setValidating(false);
			
			return (T) digester.parse(new InputSource(new ByteArrayInputStream(input.getBytes())));
		} catch (ResourceIOException e) {
			logger.error(" file " + digesterRuleFilePath + " don't be found ", e);
			return null;
		} catch (IOException e) {
		    logger.error(" parser " + input + " failed ", e);
            return null;
		} catch (SAXException e) {
			logger.error(" parser " + input + " failed ", e);
			return null;
		}
	}
	
	public static <T> T parseResource(String inputFilePath, String digesterRuleFilePath, Class<T> z) {
		try {
			String input = ResourceLoadUtils.load(inputFilePath);
			return parse(input, digesterRuleFilePath, z);
		} catch (ResourceIOException e) {
			logger.error(" file " + inputFilePath + " don't be found ", e);
			return null;
		}
	}
	
}

