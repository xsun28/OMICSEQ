package com.omicseq.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.exception.ResourceIOException;

/**
 * @author Min.Wang
 *
 */
public class ResourceLoadUtils {

	private static Logger logger = LoggerFactory.getLogger(ResourceLoadUtils.class);
    
    /*
     * (non-Javadoc)
     * @see com.sears.tec.util.IResourceLoader#load(java.lang.String)
     */
    public static String load(String path)  {
        byte[] bytes = loadBinary(path);
        return new String(bytes);
    }

    public static byte[] loadBinary(String path) {
        InputStream is = ResourceLoadUtils.class.getClassLoader().getResourceAsStream(path);
        if (is == null) {
            throw new ResourceIOException("Can not find resource file " + path);
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder result = new StringBuilder();
        String line = null;
        try {
	        while ((line = reader.readLine()) != null) {
	        	result.append(line).append("\n");
	        }
	        return result.toString().getBytes();
        } catch (IOException e) {
        	throw new ResourceIOException(e.getMessage(), e);
        }
    }
    
    private static byte[] loadBinary2(String path) {
        InputStream is = ResourceLoadUtils.class.getClassLoader().getResourceAsStream(path);
        if (is == null) {
            throw new ResourceIOException("Can not find resource file " + path);
        }
        try {
            byte[] finalBytes = null;
            int start = 0;
            int available;
            if (logger.isDebugEnabled()) {
            	logger.debug("is = " + is);
            }
            while ((available = is.available()) > 0) {
                byte[] readBytes = new byte[available];
                if (logger.isDebugEnabled()) {
                	logger.debug("path = " + path);
                	logger.debug("available = " + available);
                	logger.debug("start = " + start);
                	logger.debug("readBytes.length = " + readBytes.length);
                }
                int readCount = is.read(readBytes, start, available);
                if (finalBytes == null) {
                    finalBytes = readBytes;
                } else {
                    byte[] newBytes = new byte[finalBytes.length + readCount];
                    System.arraycopy(finalBytes, 0, newBytes, 0, finalBytes.length);
                    System.arraycopy(readBytes, 0, newBytes, start, readCount);
                    finalBytes = newBytes;
                }
                start += readCount;
            }
            return finalBytes;
        } catch (IOException e) {
            throw new ResourceIOException("Read resource file " + path + " failed", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // Do nothing
            }
        }
    }
	
}
