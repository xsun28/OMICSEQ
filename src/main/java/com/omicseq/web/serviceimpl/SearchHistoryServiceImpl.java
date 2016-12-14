package com.omicseq.web.serviceimpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.omicseq.bean.HistoryResultValue;
import com.omicseq.bean.SampleItem;
import com.omicseq.bean.SampleResult;
import com.omicseq.common.ExperimentType;
import com.omicseq.common.SortType;
import com.omicseq.concurrent.ThreadTaskPoolsFactory;
import com.omicseq.core.GeneCache;
import com.omicseq.core.SampleCache;
import com.omicseq.domain.CacheGeneRank;
import com.omicseq.domain.HistoryResult;
import com.omicseq.domain.Sample;
import com.omicseq.domain.SearchHistory;
import com.omicseq.domain.User;
import com.omicseq.store.dao.IHistoryResultDAO;
import com.omicseq.store.dao.ISearchHistoryDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.utils.JSONUtils;
import com.omicseq.web.service.ISearchHistoryService;

@Service
public class SearchHistoryServiceImpl implements ISearchHistoryService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ISearchHistoryDAO searchHistoryDAO = DAOFactory.getDAO(ISearchHistoryDAO.class);
    private IHistoryResultDAO historyResultDAO = DAOFactory.getDAO(IHistoryResultDAO.class);
    @Autowired
    private SampleSearchServiceHelper sampleSearchServiceHelper;

    @Override
    public List<SearchHistory> findAll(Integer userId, String keyword) {
        logger.debug("find history by keyword:{}, and user[id]:{}", keyword, userId);
        List<SearchHistory> list = searchHistoryDAO.findAll(userId);
        if (StringUtils.isNotBlank(keyword)) {
            List<SearchHistory> result = new ArrayList<SearchHistory>();
            for (SearchHistory history : list) {
                if (StringUtils.containsIgnoreCase(history.getKeyword(), keyword)) {
                    result.add(history);
                }
            }
            return result;
        }
        return list;
    }

    @Override
    public SearchHistory findByKeyword(String keyword, User user) {
        return searchHistoryDAO.findByKeyword(keyword, user.getUserId());
    }

    @Override
    public String saveOrUpdateSearchHistory(SearchHistory history) {
        // 1.保存查询条件
        if (StringUtils.isBlank(history.get_id())) {
            // save
            searchHistoryDAO.create(history);
        } else {
            // update
            searchHistoryDAO.update(history);
        }
        // 2.保存查询结果
        saveOrUpdateResult(history);
        return history.get_id();
    }

    /**
     * 保存或更新搜索结果
     * 
     * @param history
     */
    private void saveOrUpdateResult(final SearchHistory history) {
        FutureTask<Boolean> task = new FutureTask<Boolean>(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                Integer geneId = history.getGeneId();
                List<Integer> sourceList = sampleSearchServiceHelper.toSourceTypies(history.getSource());
                List<Integer> etypeList = sampleSearchServiceHelper.toEtypies(history.getExperiments());
                List<CacheGeneRank> list = sampleSearchServiceHelper.searchSampleByGeneId(geneId, sourceList,
                        etypeList, SortType.ASC, (double)0.01, 0, 100);
                List<HistoryResultValue> rs = new ArrayList<HistoryResultValue>(100);
                for (CacheGeneRank gr : list) {
                    Integer sampleId = gr.getSampleId();
                    Sample sample = SampleCache.getInstance().getSampleById(sampleId);
                    HistoryResultValue value = new HistoryResultValue();
                    value.setSampleId(sampleId);
                    DecimalFormat df = new DecimalFormat("0.000");
                    Integer total = 0;
                    ExperimentType experimentType = ExperimentType.parse(sample.getEtype());
                    String dataType = null == experimentType ? "" : experimentType.getDesc();
                    if (Double.compare(gr.getMixturePerc(), gr.getTss5kPerc()) == 0) {
                        total = GeneCache.getInstance().getTss5kTotal();
                        dataType = dataType.equalsIgnoreCase(ExperimentType.RNA_SEQ.getDesc()) ? dataType : dataType + "(P)";
                    }else{
                        total = GeneCache.getInstance().getTssTesTotal();
                        dataType = dataType.equalsIgnoreCase(ExperimentType.RNA_SEQ.getDesc()) ? dataType : dataType + "(B)";
                    }
                    value.setPercentileFormat(df.format(gr.getMixturePerc()*100));
                    value.setDataType(dataType);
                    value.setTotal(total);
                    
                    BigDecimal bigDecimal = new BigDecimal(gr.getMixturePerc() * total).setScale(0, BigDecimal.ROUND_HALF_UP);
                    value.setRank(bigDecimal.intValue());
                    rs.add(value);
                }
                String json = JSONUtils.to(rs);
                HistoryResult result = new HistoryResult();
                result.set_id(history.get_id());
                result.setValue(json);
                historyResultDAO.update(result);
                return Boolean.TRUE;
            }
        });
        ThreadTaskPoolsFactory.getThreadTaskPoolsExecutor().run(task);

    }

    @Override
    public void createHistoryResult(HistoryResult result) {
        historyResultDAO.create(result);
    }

    @Override
    public void updateHistoryResult(HistoryResult result) {
        historyResultDAO.update(result);
    }

    @Override
    public void updateSearchHistory(SearchHistory searchHistory) {
        searchHistoryDAO.update(searchHistory);
    }

    @Override
    public SampleResult searchSample(String historyId, List<String> sourceList, List<String> etypeList,
            SortType sortType, Integer start, Integer pageSize) {
        SampleResult result = new SampleResult();
        HistoryResult historyResult = historyResultDAO.findById(historyId);
        if (historyResult != null) {
            try {
                List<SampleItem> sampleItemList = new ArrayList<SampleItem>();
                String value = historyResult.getValue();
                List<HistoryResultValue> resultValues = new ArrayList<HistoryResultValue>();
                if (StringUtils.isNotBlank(value)) {
                    // 构造sample数据
                    TypeReference<List<HistoryResultValue>> type = new TypeReference<List<HistoryResultValue>>() {
                    };
                    List<HistoryResultValue> temp = JSONUtils.from(value, type);
                    if ((start + pageSize) > temp.size()) {
                        resultValues = temp.subList(start, temp.size());
                    }else{
                        resultValues = temp.subList(start, start + pageSize);
                    }
                    for (HistoryResultValue hv : resultValues) {
                        Sample sample = SampleCache.getInstance().getSampleById(hv.getSampleId());
                        SampleItem item = new SampleItem(sample, hv.getRank(), hv.getTotal(), null, null, hv.getPercentileFormat(), true, hv.getDataType(), null, null);
                        sampleItemList.add(item);
                    }
                    result.setSampleItemList(sampleItemList);
                    result.setTotal(temp.size());
                }
            } catch (Exception e) {
                logger.error("file to json faile", e);
            }
        }
        return result;
    }



}
