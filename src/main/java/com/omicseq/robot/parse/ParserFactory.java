package com.omicseq.robot.parse;

import java.util.HashMap;
import java.util.Map;

import com.omicseq.common.SourceType;

public class ParserFactory {

    private static final Map<SourceType, IParser> cache = new HashMap<SourceType, IParser>(3);
    static {
        cache.put(SourceType.TCGA, new TCGAParser());
    }

    public static IParser get(SourceType source) {
        return cache.get(source);
    }
}
