package com.omicseq.bean;

public class HistoryResultValue {
    private Integer sampleId;
    private Integer rank;
    private Integer total;
    private String percentileFormat;
    private String dataType;
    
    public Integer getSampleId() {
        return sampleId;
    }
    public void setSampleId(Integer sampleId) {
        this.sampleId = sampleId;
    }
    public Integer getRank() {
        return rank;
    }
    public void setRank(Integer rank) {
        this.rank = rank;
    }
    public Integer getTotal() {
        return total;
    }
    public void setTotal(Integer total) {
        this.total = total;
    }
    public String getPercentileFormat() {
        return percentileFormat;
    }
    public void setPercentileFormat(String percentileFormat) {
        this.percentileFormat = percentileFormat;
    }
    public String getDataType() {
        return dataType;
    }
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    
}
