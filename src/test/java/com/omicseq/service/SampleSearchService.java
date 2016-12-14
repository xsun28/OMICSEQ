package com.omicseq.service;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.omicseq.bean.SampleResult;
import com.omicseq.common.SortType;
import com.omicseq.core.WebResourceInitiate;
import com.omicseq.web.service.ISampleSearchService;
import com.omicseq.web.serviceimpl.SampleSearchServiceImpl;

public class SampleSearchService {

    private static ISampleSearchService service;
    
    public static void main(String[] args) {
        //ISampleDAO dao = DAOFactory.getDAO(ISampleDAO.class);
        //Sample sample = dao.getBySampleId(869);
        //SampleItem item = new SampleItem(sample, 500, 2000);
        //System.out.println(item);
    }
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        WebResourceInitiate.getInstance().init();
        service = new SampleSearchServiceImpl();
    }

    @Test
    public void test() {
        SampleResult result = service.searchSample("klk3", null, null, SortType.ASC, 0, 10);
        Assert.assertTrue(CollectionUtils.isNotEmpty(result.getSampleItemList())
                || CollectionUtils.isNotEmpty(result.getGeneItemList()));
        System.out.println(result.getSampleItemList().get(0).getGeoUrl());
        System.out.println(result.getSampleItemList().get(0).getDownLoadUrl());
        System.out.println(result.getSampleItemList().get(0).getMetaData());
        System.out.println(result.getSampleItemList().get(0).getPubMedUrl());
    }

    @Test
    public void testQuery2() {
        SampleResult result = service.searchSample("KLK3", null, null, SortType.ASC, 0, 10);
        Assert.assertTrue(CollectionUtils.isNotEmpty(result.getSampleItemList())
                || CollectionUtils.isNotEmpty(result.getGeneItemList()));
    }
    
    @Test
    public void testQuery3() {
        SampleResult result = service.searchSample("NM_001005484", null, null, SortType.ASC, 0, 10);
        Assert.assertTrue(CollectionUtils.isNotEmpty(result.getSampleItemList())
                || CollectionUtils.isNotEmpty(result.getGeneItemList()));
    }
    
}
