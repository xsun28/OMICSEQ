package com.omicseq.domain;

public class HashDB extends BaseDomain {
    private static final long serialVersionUID = 1L;
    private String key;
    private String value;
    private Long expiry;

    public HashDB() {

    }

    public HashDB(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    public HashDB(String key, String value, Long expiry) {
        this.key = key;
        this.value = value;
        this.expiry= expiry;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getExpiry() {
        return expiry;
    }

    public void setExpiry(Long expiry) {
        this.expiry = expiry;
    }

}
