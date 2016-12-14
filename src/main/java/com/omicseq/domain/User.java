package com.omicseq.domain;

/** 
* 类名称：User 
* 类描述： 用户管理系统用户实体
* 
* 创建人：Liangxiaoyan
* @version 
* 
*/
public class User extends BaseDomain {
	
    /** 
    *serialVersionUID 
    */ 
    private static final long serialVersionUID = 7068954633086978688L;
    
    private Integer userId;
    private String name;
    private String password;
    private Boolean deleted = false;
    private String Experiments;
    private String cellType;
    private String source;
    private String email;
    private String company;
    private String department;
    private String lab;
    private String companyType;
    private Integer pageSize;
    private String source_mouse;
    private String Experiments_mouse;
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
	private String idCardAndPhone;
    
    private Integer userType; //0:普通注册用户 1:公用账户2:账户禁用除gene的其他Tab和页面数据下载功能
    
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public Boolean getDeleted() {
        return deleted;
    }
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
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
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getCompany() {
        return company;
    }
    public void setCompany(String company) {
        this.company = company;
    }
    public String getCompanyType() {
        return companyType;
    }
    public void setCompanyType(String companyType) {
        this.companyType = companyType;
    }
    public String getDepartment() {
        return department;
    }
    public void setDepartment(String department) {
        this.department = department;
    }
    public String getLab() {
        return lab;
    }
    public void setLab(String lab) {
        this.lab = lab;
    }
    public Integer getPageSize() {
        return pageSize;
    }
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
	public Integer getUserType() {
		return userType;
	}
	public void setUserType(Integer userType) {
		this.userType = userType;
	}
	public String getIdCardAndPhone() {
		return idCardAndPhone;
	}
	public void setIdCardAndPhone(String idCardAndPhone) {
		this.idCardAndPhone = idCardAndPhone;
	}
    
}
