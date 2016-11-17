package com.omicseq.configuration;

import java.util.ArrayList;
import java.util.List;

public class DBGroupConfig {

	private List<DBGroup> dbGroupList;

	public List<DBGroup> getDbGroupList() {
		return dbGroupList;
	}

	public void setDbGroupList(List<DBGroup> dbGroupList) {
		this.dbGroupList = dbGroupList;
	}
	
	public void addDbGroup(DBGroup dbGroup) {
		if (dbGroupList == null) {
			dbGroupList = new ArrayList<DBGroup>();
		}
		dbGroupList.add(dbGroup);
	}

	@Override
	public String toString() {
		return "DBGroupConfig [dbGroupList=" + dbGroupList + "]";
	}
	
	
	
}
