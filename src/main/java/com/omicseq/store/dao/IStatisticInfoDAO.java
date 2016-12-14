package com.omicseq.store.dao;

import java.util.List;

import com.omicseq.bean.Paginator;
import com.omicseq.domain.StatisticInfo;
import com.omicseq.store.criteria.StatisticCriteria;

public interface IStatisticInfoDAO extends IGenericDAO<StatisticInfo> {

    List<StatisticInfo> findByCriteria(StatisticCriteria criteria, Paginator paginator);

    List<StatisticInfo> findUnProcessed(String serverIP, Boolean input, int start, int limit);

    StatisticInfo getBySampleId(Integer sampleId);

}
