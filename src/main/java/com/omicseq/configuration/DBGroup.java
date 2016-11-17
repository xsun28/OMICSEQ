package com.omicseq.configuration;

import com.omicseq.common.DbType;

/**
 * @author Min.Wang
 *
 */
public class DBGroup {

	private String name;
	private String type = DbType.mongodb.name();
	private String servers;
	private String userName;
	private String passWord;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getServers() {
		return servers;
	}

	public void setServers(String servers) {
		this.servers = servers;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	@Override
	public String toString() {
		return "DBGroup [name=" + name + ", type=" + type + ", servers=" + servers + ", userName="
				+ userName + ", passWord=" + passWord + "]";
	}
	
	

}
