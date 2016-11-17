package com.omicseq.configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Min.Wang
 *
 */
public class TableGroupMappingConfig {

	private List<TableGroupMapping> tableGroupMappingList;

	public List<TableGroupMapping> getTableGroupMappingList() {
		return tableGroupMappingList;
	}

	public void setTableGroupMappingList(List<TableGroupMapping> tableGroupMappingList) {
		this.tableGroupMappingList = tableGroupMappingList;
	}
	
	public void addTableGroupMapping(TableGroupMapping tableGroupMapping) {
		if (tableGroupMappingList == null) {
			tableGroupMappingList = new ArrayList<TableGroupMapping>();
		}
		tableGroupMappingList.add(tableGroupMapping);
	}

	@Override
	public String toString() {
		return "TableGroupMappingConfig [tableGroupMappingList=" + tableGroupMappingList + "]";
	}
	
	
}
