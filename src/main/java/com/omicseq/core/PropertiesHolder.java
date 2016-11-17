package com.omicseq.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesHolder {
    public final static String FILES = "files";
    public final static String COMM = "comm";
    private static Logger logger = LoggerFactory.getLogger(PropertiesHolder.class);
    private static Map<String, Map<String, String>> cache = new HashMap<String, Map<String, String>>(1);
    static {
        try {
            ClassLoader cl = PropertiesHolder.class.getClassLoader();
            URL url = cl.getResource("properties");
            File file = new File(url.toURI());
            if (file.isDirectory()) {
                File[] files = file.listFiles(new FilenameFilter() {

                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".properties");
                    }
                });
                for (File f : files) {
                    String name = StringUtils.lowerCase(FilenameUtils.getBaseName(f.getName()));
                    Map<String, String> map = cache.get(name);
                    if (null == map) {
                        map = new HashMap<String, String>(5);
                        cache.put(name, map);
                    }
                    Properties prop = new Properties();
                    prop.load(new FileInputStream(f));
                    Set<Entry<Object, Object>> entrySet = prop.entrySet();
                    for (Entry<Object, Object> entry : entrySet) {
                        map.put(entry.getKey().toString(), entry.getValue().toString());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Load properties failed!", e);
        }
    }

    public static String get(String name, String key) {
        return get(name, key, StringUtils.EMPTY);
    }

    public static String get(String name, String key, String def) {
        Map<String, String> map = cache.get(StringUtils.lowerCase(name));
        if (null == map) {
            return def;
        }
        return map.containsKey(key) ? StringUtils.trimToEmpty(map.get(key)) : def;
    }
}
