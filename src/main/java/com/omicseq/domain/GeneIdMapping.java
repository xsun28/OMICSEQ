package com.omicseq.domain;

public class GeneIdMapping extends BaseDomain {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private Integer newId;
    private Integer oldId;
    // start_end
    private String range;

    public Integer getNewId() {
        return newId;
    }

    public void setNewId(Integer newId) {
        this.newId = newId;
    }

    public Integer getOldId() {
        return oldId;
    }

    public void setOldId(Integer oldId) {
        this.oldId = oldId;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }
    

}
