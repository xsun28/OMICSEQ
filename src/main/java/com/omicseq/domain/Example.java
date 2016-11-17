package com.omicseq.domain;

import java.util.Date;

import com.omicseq.annotation.NonPersistent;


/**
 * @author Min.Wang
 *
 */
public class Example extends  BaseDomain {
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Long id;
	private String name;
	private Integer age;
	private Date birthday;
	private Integer type;
	
	@NonPersistent
	private String password;
	

	public String getName() {
    	return name;
    }

	public void setName(String name) {
    	this.name = name;
    }

	public Integer getAge() {
    	return age;
    }

	public void setAge(Integer age) {
    	this.age = age;
    }

	public Date getBirthday() {
    	return birthday;
    }

	public void setBirthday(Date birthday) {
    	this.birthday = birthday;
    }

	public String getPassword() {
    	return password;
    }

	public void setPassword(String password) {
    	this.password = password;
    }

	public Integer getType() {
    	return type;
    }

	public void setType(Integer type) {
    	this.type = type;
    }

	public Long getId() {
    	return id;
    }

	public void setId(Long id) {
    	this.id = id;
    }

	@Override
	public String toString() {
		return "Example [id=" + id + ", name=" + name + ", age=" + age + ", birthday=" + birthday
				+ ", type=" + type + ", password=" + password + "]";
	}
	
	
	
}
