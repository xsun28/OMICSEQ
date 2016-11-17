package com.omicseq.web.common;

import java.util.HashMap;
import java.util.Map;

public enum CompanyType {
    UNIVERSITIES(1, "Universities"), 
    RESEARCH_INSTITUTES(2, "Research Institutes"), 
    BIOMEDICAL_ENTERPRISE(3, "Biomedical Enterprise"),
    GOVERNMENT(4, "Government Departments"), 
    OTHERS(5, "Others");
    
    private static Map<Integer,CompanyType> valueMap = new HashMap<Integer,CompanyType>();
    static {
        for (CompanyType companyType : CompanyType.values()) {
            valueMap.put(companyType.getValue(), companyType);
        }
    }

    private static Map<Integer,String> descMap = new HashMap<Integer,String>();
    static {
        for (CompanyType companyType : CompanyType.values()) {
            descMap.put(companyType.getValue(), companyType.getDesc());
        }
    }
    
    public static Map<Integer, String> getDescMap() {
        return descMap;
    }

    private Integer value;
    private String desc;
    
    private CompanyType(String desc){
        this.desc = desc;
    }
    CompanyType(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static CompanyType parse(Integer value) {
        return valueMap.get(value);
    }
    
    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}
