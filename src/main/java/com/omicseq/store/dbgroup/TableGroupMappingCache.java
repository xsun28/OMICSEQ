package com.omicseq.store.dbgroup;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.omicseq.common.DBGroupName;
import com.omicseq.common.DBName;
import com.omicseq.configuration.ConfigsHelper;
import com.omicseq.configuration.TableGroupMapping;
import com.omicseq.configuration.TableGroupMappingConfig;

/**
 * @author Min.Wang
 *
 */
public class TableGroupMappingCache {

	private static ConcurrentMap<String, TableGroupMapping> tableGroupMappingMap = new ConcurrentHashMap<String, TableGroupMapping>();
	private static TableGroupMapping defaultTableGroupMapping = new TableGroupMapping("", DBGroupName.manage.name(), DBName.manage.name());
	static {
		TableGroupMappingConfig  groupMappingConfig = ConfigsHelper.getTableGroupMappingConfig();
		if (CollectionUtils.isNotEmpty(groupMappingConfig.getTableGroupMappingList())) {
			for (TableGroupMapping tableGroupMapping : groupMappingConfig.getTableGroupMappingList()) {
				tableGroupMappingMap.put(tableGroupMapping.getTableName().toLowerCase(), tableGroupMapping);
			}
		}
	}
	
	private static TableGroupMappingCache  instance  = new TableGroupMappingCache();
	
	public static TableGroupMappingCache getInstance() {
		return instance;
	}
	
	public TableGroupMapping getTableGroupMapping(String tableName) {
		if (StringUtils.isBlank(tableName)) {
			return null;
		}
		
		TableGroupMapping tableGroupMapping = tableGroupMappingMap.get(tableName);
		if (tableGroupMapping == null) {
			return defaultTableGroupMapping;
		} else {
			return tableGroupMapping;
		}
	}
	
}
