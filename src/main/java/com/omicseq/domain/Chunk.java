package com.omicseq.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 
 * 分块下载信息
 * 
 * @author zejun.du
 */
public class Chunk implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public Chunk() {

    }

    public Chunk(Long start, Long end) {
        this.start = start;
        this.end = end;
    }

    /**
     * 分块开始位置
     */
    private Long start;
    /**
     * 已完成位置
     */
    private Long postion;
    /**
     * 分块结束位置
     */
    private long end;

    public long getBegin() {
        return Math.max(null == start ? 0 : start, null == postion ? 0 : postion);
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getPostion() {
        return postion;
    }

    public void setPostion(Long postion) {
        this.postion = postion;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
