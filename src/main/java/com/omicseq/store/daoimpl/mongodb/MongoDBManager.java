package com.omicseq.store.daoimpl.mongodb;


import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.util.MapUtils;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.omicseq.configuration.ConfigsHelper;
import com.omicseq.configuration.DBGroup;
import com.omicseq.configuration.DBGroupConfig;
import com.omicseq.exception.DataAccessException;

/**
 * @author Min.Wang
 *
 */
public class MongoDBManager {
	protected Logger logger = LoggerFactory.getLogger(getClass());
	private static volatile MongoDBManager instance;
	private static ConcurrentMap<String, MongoClient> clientMap = new ConcurrentHashMap<String, MongoClient>();
	private static ConcurrentMap<String, String> userNameMap = new ConcurrentHashMap<String, String>();
	private static ConcurrentMap<String, String> passWordMap = new ConcurrentHashMap<String, String>();
	private static MongoClient client;
	private static String username;
	private static String password;
	
    private MongoDBManager() {
    }

    public static MongoDBManager getInstance() {
	    if (instance == null) {
		    synchronized (MongoDBManager.class) {
			    if (instance == null) {
				    instance = new MongoDBManager();
				    init();
			    }
		    }
	    }
	    return instance;
    }

	private static void init() {
		try {
			DBGroupConfig dbGroupConfig = ConfigsHelper.getDbGroupConfig();
			List<DBGroup> dbGroupList = dbGroupConfig.getDbGroupList();
			Validate.notNull(dbGroupList, " db group don't been configed");
			for (DBGroup dbGroup : dbGroupList) {
				String servers = dbGroup.getServers();
				String[] arr = servers.split("\\ *,\\ *");
				
				if (arr.length == 1) {
					String[] serverPortArr = arr[0].split(":");
					client = new MongoClient(serverPortArr[0], Integer.parseInt(serverPortArr[1]));
				} else {
					List<ServerAddress> serverList = new ArrayList<ServerAddress>();
					for (String server : arr) {
						String[] serverPortArr = server.split(":");
						ServerAddress sa = new ServerAddress(serverPortArr[0], Integer.parseInt(serverPortArr[1]));
						serverList.add(sa);
					}
					client = new MongoClient(serverList);
				}
				clientMap.putIfAbsent(dbGroup.getName().toLowerCase(), client);
				userNameMap.putIfAbsent(dbGroup.getName().toLowerCase(), dbGroup.getUserName());
				passWordMap.putIfAbsent(dbGroup.getName().toLowerCase(), dbGroup.getPassWord());
			}
			
        } catch (UnknownHostException e) {
        	throw new DataAccessException("Failed to init MongoDB client", e);
        }
    }
	
	/**
	 * A MongoDB client with internal connection pooling. For most applications, you should have one MongoClient instance for the entire JVM. 
	 * @return
	 */
	private MongoClient getClient() {
		return client;
	}
	
	public List<String> getDatabaseNames() {
		DB admin = client.getDB("admin");
		if (StringUtils.isNotBlank(username)) {
			admin.authenticate(username, password.toCharArray());
		}
		return client.getDatabaseNames();
	}
	
	public DB getDB(String dbName) {
		return client.getDB(dbName);
	}
	
	public DBCollection getCollection(String dbName, String collectionName) {
		DB db = getClient().getDB(dbName);
		if (StringUtils.isNotBlank(username)) {
			db.authenticate(username, password.toCharArray());
		}
		return db.getCollection(collectionName);
	}
	
	public DBCollection getCollection(String groupName, String dbName, String collectionName) {
		
		DB db = clientMap.get(groupName).getDB(dbName);
		String userName = userNameMap.get(groupName);
		String passWord = passWordMap.get(groupName); 
		String key = (groupName + "_" + dbName + "_db").intern();
		synchronized(key) {
			if (StringUtils.isNotBlank(userName) && !db.isAuthenticated()) {
				db.authenticate(userName, passWord.toCharArray());
				
			}
		}
		return db.getCollection(collectionName);
	}
	
	/**
	 * 关闭所有的连接
	 */
	public void close() {
		if (MapUtils.isEmpty(clientMap)) {
			for (String client : clientMap.keySet()) {
				clientMap.get(client).close();
			}
		}
	}
	
}
