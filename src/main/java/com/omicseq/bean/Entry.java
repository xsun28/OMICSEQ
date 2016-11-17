package com.omicseq.bean;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Entry<K, V> {
    private K k;
    private V v;

    public Entry() {
    }

    public Entry(K k) {
        this.k = k;
    }

    public Entry(K k, V v) {
        this.k = k;
        this.v = v;
    }

    public void setValue(V v) {
        this.v = v;
    }

    public V getValue() {
        return this.v;
    }

    public void setKey(K k) {
        this.k = k;
    }

    public K getKey() {
        return this.k;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
