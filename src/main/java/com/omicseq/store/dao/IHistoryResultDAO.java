package com.omicseq.store.dao;

import com.omicseq.domain.HistoryResult;

public interface IHistoryResultDAO {
    
    void create(HistoryResult result);
    void update(HistoryResult result);
    HistoryResult findById(String id);
}
