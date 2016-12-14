package com.omicseq.web.common;

import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * 类名称：WebServiceResult 类描述： Web Service 返回结果
 * 
 * 
 * 创建人：Duzj 创建时间：2012-4-9 下午3:47:39
 * 
 * @version
 * 
 */
public class WebServiceResult {
    // 结果
    private boolean result = true;
    // 数据
    private Object data;
    // 编码,可设置错误编码...
    private String code;
    // 消息
    private String message;
    // 总记录数
    private Integer totalRecords;
    // 最后的记录数
    private Integer lastRecored;
    // 当前数据记录数
    private Integer size;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
        if (data instanceof Object[] || data instanceof Iterable<?> || data instanceof Map<?, ?>) {
            this.size = CollectionUtils.size(data);
        }
    }

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public Integer getLastRecored() {
        return lastRecored;
    }

    public void setLastRecored(Integer lastRecored) {
        this.lastRecored = lastRecored;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
