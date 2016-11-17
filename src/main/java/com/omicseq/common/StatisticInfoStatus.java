package com.omicseq.common;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * 
 * @author zejun.du
 */
public enum StatisticInfoStatus {
    DEFAULT(0), //
    PROCESSING(1), // 处理中
    FAILED(3), // 处理失败
    UNCHECKED(4),//未校验通过的
    PROCESSED(99), //
    ;
    private static Map<Integer, StatisticInfoStatus> valueMap = new HashMap<Integer, StatisticInfoStatus>(3);
    static {
        for (StatisticInfoStatus item : StatisticInfoStatus.values()) {
            valueMap.put(item.value(), item);
        }
    }
    private Integer state;

    StatisticInfoStatus(Integer state) {
        this.state = state;
    }

    public Integer value() {
        return this.state;
    }

    public static boolean isProcessed(Integer state) {
        return PROCESSED.value().equals(state);
    }

    public static StatisticInfoStatus parse(Integer state) {
        return valueMap.get(state);
    }
}
