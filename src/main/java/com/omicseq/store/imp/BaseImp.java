package com.omicseq.store.imp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.concurrent.ThreadTaskPoolsFactory;
import com.omicseq.store.cache.CacheProviderInitiate;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.utils.DateTimeUtils;

public abstract class BaseImp {
    static {
        // init mongodb
        MongoDBManager.getInstance();
        // init memcached
        CacheProviderInitiate.getInstance().init();
        // init thread pool
        ThreadTaskPoolsFactory.getThreadTaskPoolsExecutor().init();
    }
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected boolean showLine = false;

    void impl(String file) throws IOException {
        logger.debug("imp file is {} ", file);
        DateTime dt = DateTime.now();
        InputStream is = null;
        BufferedReader reader = null;
        try {
            is = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
            String line = reader.readLine();
            while (line != null) {
                if (showLine) {
                    logger.debug("line:" + line);
                }
                process(line);
                line = reader.readLine();
            }
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(is);
            logger.debug("imp csv file used " + DateTimeUtils.diff(dt, DateTime.now()));
        }
    }

    public static String[] split(String line) {
        List<String> list = new ArrayList<String>(5);
        while (true) {
            int pos = line.indexOf(",");
            if (line.startsWith("\"")) {
                int end = line.indexOf("\"", 1);
                pos = line.indexOf(",", end);
            }
            String item = pos == -1 ? line : line.substring(0, pos);
            String str = item;
            if ("\"".equals(str) || "\'".equals(str) || StringUtils.trimToNull(str) == null) {
                str = "";
            } else if (str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"') {
                str = str.substring(1, str.length() - 1);
            }
            list.add(StringUtils.trimToNull(str));
            if (item.equals(line)) {
                break;
            }
            line = line.substring(item.length() + 1);
        }
        return list.toArray(new String[] {});
    }

    void process(String line) {
        doProcess(split(line));
    }

    void doProcess(String[] lines) {

    }

    Integer toInteger(String str) {
        if (StringUtils.isNotBlank(str)) {
            return Integer.valueOf(str);
        }
        return null;
    }

    Double toDouble(String str) {
        if (StringUtils.isNotBlank(str)) {
            return Double.valueOf(str);
        }
        return null;
    }
}
