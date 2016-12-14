package com.omicseq.store.dao;

import java.util.List;

import com.omicseq.domain.SearchHistory;

/** 
* 类名称：ISearchHistoryDAO 
* 类描述： 用户搜索记录数据层
* 
* 
* 创建人：Liangxiaoyan
* 创建时间：2014-4-16 下午3:28:32 
* @version 
* 
*/
public interface ISearchHistoryDAO {
    
    void create(SearchHistory history);
    
    List<SearchHistory> findAll(Integer userId);
    
    SearchHistory findByKeyword(String keyword, Integer integer);
    
    void update(SearchHistory history);
    
}
