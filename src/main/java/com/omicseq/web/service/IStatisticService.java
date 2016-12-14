package com.omicseq.web.service;

import java.util.List;

import com.omicseq.bean.Paginator;
import com.omicseq.common.SourceType;
import com.omicseq.domain.StatisticInfo;
import com.omicseq.store.criteria.StatisticCriteria;

public interface IStatisticService {

    List<StatisticInfo> initInfo(SourceType source);

    List<StatisticInfo> findByCriteria(StatisticCriteria criteria,Paginator paginator);

    void exec(Integer sampleId);

    void check(Boolean all,SourceType source);

    void check(Integer sampleId);

}
