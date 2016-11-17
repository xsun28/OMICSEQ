package com.omicseq.utils;

import java.io.InputStream;
import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.common.Charsets;
import com.omicseq.exception.OmicSeqException;

/**
 * 
 * 
 * @author zejun.du
 */
public class HttpClients {
    private static Logger logger = LoggerFactory.getLogger(HttpClients.class);
    private static CloseableHttpClient client = org.apache.http.impl.client.HttpClients.createDefault();

    public static String encode(String value) {
        try {
            return URLEncoder.encode(value, "utf-8");
        } catch (Exception e) {
            return value;
        }
    }

    public static String get(String url) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("http get string from {}", url);
            }
            HttpGet get = new HttpGet(url);
            CloseableHttpResponse res = client.execute(get);
            InputStream input = res.getEntity().getContent();
            Header encode = res.getEntity().getContentEncoding();
            return IOUtils.toString(input, null == encode ? Charsets.UTF_8.name() : encode.getValue());
        } catch (Exception e) {
            throw new OmicSeqException(e.getMessage(), e);
        }
    }

}
