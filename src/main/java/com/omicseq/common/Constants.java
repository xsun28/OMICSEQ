package com.omicseq.common;

import com.omicseq.core.PropertiesHolder;

public class Constants {

    /**
     * 基因总数
     */
    public static final Integer GENE_COUNT = Integer.valueOf(44805);
    /**
     * 数据导入表名后缀
     */
    public static String STAT_SUFFIX = PropertiesHolder.get(PropertiesHolder.COMM, "stat.suffix","");

}
