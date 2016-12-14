package com.omicseq.configuration;

/**
 * @author Min.Wang
 *
 */
public class TableGroupMapping {

	private String tableName;
	private String groupName;
	private String dbName;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public TableGroupMapping() {
		super();
	}
	
	public TableGroupMapping(String tableName, String groupName, String dbName) {
		super();
		this.tableName = tableName;
		this.groupName = groupName;
		this.dbName = dbName;
	}

	@Override
	public String toString() {
		return "TableGroupMapping [tableName=" + tableName + ", groupName=" + groupName
				+ ", dbName=" + dbName + "]";
	}
	

}
