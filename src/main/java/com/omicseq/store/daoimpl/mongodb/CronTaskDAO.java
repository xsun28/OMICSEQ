package com.omicseq.store.daoimpl.mongodb;

import org.apache.commons.lang3.StringUtils;

import com.omicseq.domain.CronTask;
import com.omicseq.store.dao.ICronTaskDAO;

public class CronTaskDAO extends GenericMongoDBDAO<CronTask> implements ICronTaskDAO {

    @Override
    public CronTask get(String name, String launchServer) {
        if (StringUtils.isBlank(name) || StringUtils.isBlank(launchServer)) {
            return null;
        } else {
            SmartDBObject query = new SmartDBObject("name", name);
            query.put("launchServer", launchServer);
            return findOne(query);
        }
    }

}
