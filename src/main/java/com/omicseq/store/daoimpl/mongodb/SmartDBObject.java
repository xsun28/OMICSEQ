package com.omicseq.store.daoimpl.mongodb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import com.omicseq.common.SortType;

/**
 * @author Min.Wang
 * 
 */
public class SmartDBObject extends BasicDBObject {
    private static final long serialVersionUID = 1L;

    private List<BasicDBObject> sortList = new ArrayList<BasicDBObject>();
    private List<String> returnFieldList = new ArrayList<String>();

    public SmartDBObject() {
    }

    public SmartDBObject(String key, Object value) {
        super(key, value);
    }

    public void addSort(String key, SortType sortType) {
        sortList.add(new BasicDBObject(key, sortType.value()));
    }

    public List<BasicDBObject> getSortList() {
        return sortList;
    }

    public void setSortList(List<BasicDBObject> sortList) {
        this.sortList = sortList;
    }

    public void addReturnFields(String... fields) {
        if (ArrayUtils.isNotEmpty(fields)) {
            addReturnFields(Arrays.asList(fields));
        }
    }

    public void addReturnFields(List<String> fields) {
        if (CollectionUtils.isNotEmpty(fields)) {
            returnFieldList.addAll(fields);
        }
    }

    public List<String> getReturnFieldList() {
        return returnFieldList;
    }

    public void setReturnFieldList(List<String> returnFieldList) {
        this.returnFieldList = returnFieldList;
    }

    /**
     * not in
     * 
     * @param string
     * @param names
     */
    public void notIn(String key, Object... values) {
        BasicDBList list = new BasicDBList();
        for (Object obj : values) {
            list.add(obj);
        }
        this.put(key, new SmartDBObject("$nin", list));
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (CollectionUtils.isNotEmpty(returnFieldList)) {
            buf.append("fields:");
            JSON.serialize(returnFieldList, buf);
            buf.append(",");
        }
        if (buf.length() != 0) {
            buf.append("query:");
        }
        JSON.serialize(this, buf);
        if (CollectionUtils.isNotEmpty(sortList)) {
            buf.append(",sort:");
            JSON.serialize(sortList, buf);
        }
        return buf.toString();
    }
}
