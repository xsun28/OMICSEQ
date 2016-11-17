package com.omicseq.domain;

import java.util.List;

import com.omicseq.annotation.NonPersistent;

/**
 * 
 * 文件信息
 * 
 * @author zejun.du
 */
public class FileInfo extends BaseDomain {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 关联sample对象
     */
    private Integer sampleId;
    /**
     * 网站来源
     */
    private Integer source;
    /**
     * url 网络文件地址
     */
    private String url;
    /**
     * 本地文件所在服务器
     */
    private String serverIp;
    /**
     * 本地文件路径
     */
    private String path;
    /**
     * 文件大小
     */
    private Long length;
    /**
     * 文件最后修改时间
     */
    private Long lastModified;

    /**
     * 记录分块下载文件的数据信息
     */
    private List<Chunk> chunks;
    /**
     * 是否支持断点续传
     */
    @NonPersistent
    private Boolean resume = Boolean.TRUE;
    /**
     * 文件状态
     */
    private Integer state = 0;

    /**
     * 优先级 从大到小排序
     */
    private Integer priority = 0;

    public Integer getSampleId() {
        return sampleId;
    }

    public void setSampleId(Integer sampleId) {
        this.sampleId = sampleId;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    public List<Chunk> getChunks() {
        return chunks;
    }

    public void setChunks(List<Chunk> chunks) {
        this.chunks = chunks;
    }

    public Boolean getResume() {
        return resume;
    }

    public void setResume(Boolean resume) {
        this.resume = resume;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public boolean comare(FileInfo info) {
        Long last = null == lastModified ? 0 : lastModified;
        Long len = null == length ? 0 : length;
        if (null == info.lastModified) {
            info.lastModified = lastModified;
        }
        // 如果文件有更新
        if (!last.equals(info.lastModified) || !len.equals(info.length)) {
            // 删除分块信息,重置文件大小及更新时间
            this.chunks = null;
            this.length = info.length;
            this.lastModified = info.lastModified;
            return false;
        } else {
            return true;
        }
    }
}
