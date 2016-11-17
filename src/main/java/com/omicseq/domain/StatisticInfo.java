package com.omicseq.domain;

public class StatisticInfo extends BaseDomain {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    /**
     * sampleId
     */
    private Integer sampleId;
    /**
     * 
     */
    private Integer source;
    /**
     * 文件路径
     */
    private String path;
    /**
     * 文件所在服务器Ip
     */
    private String serverIp;
    /**
     * 优先级 从大到小排序
     */
    private Integer priority = 0;
    /**
     * 状态
     */
    private Integer state;

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

}
