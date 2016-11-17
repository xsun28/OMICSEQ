package com.omicseq.domain;

import java.util.Date;

/** 
* 类名称：SearchHistory 
* 类描述： 
* 用户搜索记录实体
* 
* 创建人：Liangxiaoyan
* 创建时间：2014-4-16 下午3:22:49 
* @version 
* 
*/
public class SearchHistory extends BaseDomain{

    /** 
    *serialVersionUID 
    */ 
    private static final long serialVersionUID = -5442977914496548709L;
    
    private Integer userId;
    private String keyword;
    private Date createDate;
    private String Experiments;
    private String cellType;
    private String source;
    private Integer geneId;
    private String Experiments_mouse; 
    private String source_mouse;
    private String genome; //物种基因库  Human,house Mouse
    
    public String getGenome() {
		return genome;
	}
	public void setGenome(String genome) {
		this.genome = genome;
	}
	public String getExperiments_mouse() {
		return Experiments_mouse;
	}
	public void setExperiments_mouse(String experiments_mouse) {
		Experiments_mouse = experiments_mouse;
	}
	public String getSource_mouse() {
		return source_mouse;
	}
	public void setSource_mouse(String source_mouse) {
		this.source_mouse = source_mouse;
	}
	public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public String getKeyword() {
        return keyword;
    }
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    public Date getCreateDate() {
        return createDate;
    }
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
    public String getExperiments() {
        return Experiments;
    }
    public void setExperiments(String experiments) {
        Experiments = experiments;
    }
    public String getCellType() {
        return cellType;
    }
    public void setCellType(String cellType) {
        this.cellType = cellType;
    }
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public Integer getGeneId() {
        return geneId;
    }
    public void setGeneId(Integer geneId) {
        this.geneId = geneId;
    }

}
