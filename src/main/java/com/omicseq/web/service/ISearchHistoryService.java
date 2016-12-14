package com.omicseq.web.service;

import com.omicseq.bean.SampleResult;
import com.omicseq.common.SortType;
import com.omicseq.domain.HistoryResult;
import com.omicseq.domain.SearchHistory;
import com.omicseq.domain.User;

import java.util.List;

public interface ISearchHistoryService {
    
    List<SearchHistory> findAll(Integer userId, String keyword);
    
    SearchHistory findByKeyword(String keyword, User user);
    
    String saveOrUpdateSearchHistory(SearchHistory history);
    
    void createHistoryResult(HistoryResult result);

    void updateHistoryResult(HistoryResult result);

    void updateSearchHistory(SearchHistory searchHistory);
    
    SampleResult searchSample(String query, List<String> sourceList, List<String> etypeList, SortType sortType, Integer start, Integer limit);

}
