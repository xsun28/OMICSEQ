package com.omicseq.common;

import java.util.HashMap;
import java.util.Map;

public enum SortType {
	ASC(1),
	DESC(-1);
	
	private static Map<String,SortType> nameMap = new HashMap<String,SortType>();
    static {
        for (SortType sourceType : SortType.values()) {
            nameMap.put(sourceType.name(), sourceType);
        }
    }
	public static SortType parse(String name) {
        return nameMap.get(name);
    }
	
	private int value;
	private SortType(int value) {
		this.value = value;
    }
	
	public int value() {
		return value;
	}
}
