package com.omicseq.store.daoimpl.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.omicseq.bean.Paginator;
import com.omicseq.common.SortType;
import com.omicseq.common.SourceType;
import com.omicseq.common.StatisticInfoStatus;
import com.omicseq.domain.StatisticInfo;
import com.omicseq.store.criteria.StatisticCriteria;
import com.omicseq.store.dao.IStatisticInfoDAO;
import com.omicseq.store.helper.MongodbHelper;

public class StatisticInfoDAO extends GenericMongoDBDAO<StatisticInfo> implements IStatisticInfoDAO {

    @Override
    public List<StatisticInfo> findByCriteria(StatisticCriteria criteria, Paginator paginator) {
        SmartDBObject query = new SmartDBObject();
        if (StringUtils.isNoneBlank(criteria.getServerIp())) {
            query.put("serverIp", criteria.getServerIp());
        }
        if (null != criteria.getSource()) {
            query.put("source", criteria.getSource());
        } else if (CollectionUtils.isNotEmpty(criteria.getSources())) {
            query.put("$in", MongodbHelper.in(criteria.getSources()));
        }
        if (null != criteria.getState()) {
            query.put("state", criteria.getState());
        }
        if (StringUtils.isNotBlank(criteria.getFileName())) {
            String fname = criteria.getFileName() + "$";
            query.put("path", java.util.regex.Pattern.compile(fname));
        }
        if (null != paginator) {
            String sort = paginator.getSort();
            if (StringUtils.isNotBlank(sort)) {
                String dir = paginator.getDir();
                SortType sortType = StringUtils.isNotBlank(dir) ? SortType.valueOf(dir.toUpperCase()) : SortType.ASC;
                query.addSort(sort, sortType);
            }
            return super.find(query, paginator.getStartIndex(), paginator.getPageSize());
        } else {
            return super.find(query);
        }
    }

    @Override
    public List<StatisticInfo> findUnProcessed(String server, Boolean input, int start, int limit) {
        if (StringUtils.isBlank(server)) {
            return new ArrayList<StatisticInfo>(0);
        }
        SmartDBObject query = new SmartDBObject("serverIp", server);
        if (Boolean.TRUE.equals(input)) {
            query.put("priority", 99);
        }
        query.put("state", MongodbHelper.in(StatisticInfoStatus.DEFAULT.value(), StatisticInfoStatus.FAILED.value(), StatisticInfoStatus.UNCHECKED.value()));
//        query.put("state", StatisticInfoStatus.DEFAULT.value());
        //限制解析arrayexpress csv
        query.put("path", new SmartDBObject("$regex",".csv"));
        query.put("source", SourceType.ArrayExpress.getValue());
        query.addSort("priority", SortType.DESC);
        query.addSort("sampleId", SortType.ASC);
        query.addSort("state", SortType.ASC);
        return super.find(query, start, limit);
    }

    @Override
    public StatisticInfo getBySampleId(Integer sampleId) {
        return findOne(new SmartDBObject("sampleId", sampleId));
    }

}
