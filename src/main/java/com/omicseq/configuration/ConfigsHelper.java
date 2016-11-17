package com.omicseq.configuration;

import com.omicseq.utils.DigesterUtils;

/**
 * @author Min.Wang
 *
 */
public class ConfigsHelper {

	private static DBGroupConfig dbGroupConfig = null;
	private final static  String dbGroupConfigPath = "dbgroup.xml";
	private final static String dbGroupConfigRulePath = "dbgroup-rule.xml";
	
	private static TableGroupMappingConfig tableGroupMappingConfig = null;
	private final static  String tableGroupMappingConfigPath = "tablegroupmapping.xml";
	private final static String tableGroupMappingConfigRulePath = "tablegroupmapping-rule.xml";
	
	
	private final static  String cacheConfigPath = "cacheconfig.xml";
	private final static String cachedconfigRule = "cachedconfig-rule.xml";
	private static CacheConfig cacheConfig = null;
	
	static {
		dbGroupConfig = DigesterUtils.parseResource(dbGroupConfigPath, dbGroupConfigRulePath, DBGroupConfig.class);
		tableGroupMappingConfig = DigesterUtils.parseResource(tableGroupMappingConfigPath, tableGroupMappingConfigRulePath, TableGroupMappingConfig.class);
		cacheConfig = DigesterUtils.parseResource(cacheConfigPath, cachedconfigRule, CacheConfig.class);
	}

	public static DBGroupConfig getDbGroupConfig() {
		return dbGroupConfig;
	}

	public static TableGroupMappingConfig getTableGroupMappingConfig() {
		return tableGroupMappingConfig;
	}
	
	public static CacheConfig getCacheConfig() {
		return cacheConfig;
	}
	
	public static void main(String[] args) {
		System.out.println(ConfigsHelper.getDbGroupConfig().toString());
		System.out.println(ConfigsHelper.getTableGroupMappingConfig().toString());
		System.out.println(ConfigsHelper.getCacheConfig().toString());
	}

	
	
	


	
	
}
