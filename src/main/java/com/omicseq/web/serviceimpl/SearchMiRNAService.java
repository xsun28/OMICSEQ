package com.omicseq.web.serviceimpl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.omicseq.bean.GeneItem;
import com.omicseq.bean.SampleItem;
import com.omicseq.bean.SampleResult;
import com.omicseq.common.SortType;
import com.omicseq.domain.MiRNA;
import com.omicseq.domain.MiRNARank;
import com.omicseq.domain.MiRNASample;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.ImiRNADAO;
import com.omicseq.store.dao.ImiRNARankDAO;
import com.omicseq.store.dao.ImiRNASampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MiRNADAOImpl;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.web.service.ISearchMiRNAService;

@Service
public class SearchMiRNAService implements ISearchMiRNAService {
	private static ImiRNASampleDAO miSampleDAO = DAOFactory.getDAO(ImiRNASampleDAO.class);
	//private static ImiRNADAO miDAO = DAOFactory.getDAO(ImiRNADAO.class);
	private static ImiRNADAO miDAO = DAOFactory.getDAOByTableType(ImiRNADAO.class, "new");
	private static ImiRNARankDAO miRankDAO = DAOFactory.getDAO(ImiRNARankDAO.class);
	@Autowired
	private SampleSearchServiceHelper sampleSearchServiceHelper;
	@Override
	public SampleResult searchMiRNA(String query, List<String> sourceList,
			List<String> etypeList, SortType sortType, Integer start,
			Integer limit) {
		query = query.toLowerCase();
		MiRNA  miRNA = miDAO.findByName(query);
		if(miRNA == null){
			return new SampleResult();
		}
        int miRNAId = miRNA.getMiRNAId();
        SmartDBObject obj = new SmartDBObject();
        obj.put("source",1);
        obj.put("etype",14);
        obj.put("miRNAId", miRNAId);
        SmartDBObject q = new SmartDBObject("$lte", 0.01);
        obj.put("mixtureperc", q);
        int count = miRankDAO.count(miRNAId);
        int total_all = miRankDAO.count_all(miRNAId);
        obj.addSort("mixtureperc", sortType);
        List<MiRNARank> miRankList = miRankDAO.find(obj, start, limit);
        SampleResult sampleResult = new SampleResult();
        List<SampleItem> sampleItemList = new ArrayList<SampleItem>();
        sampleResult.setSampleItemList(sampleItemList);
        for(MiRNARank mr : miRankList){
        	SmartDBObject q1 = new SmartDBObject("miRNASampleId", mr.getMiRNASampleId());
        	List<MiRNASample> msList = miSampleDAO.find(q1);
        	MiRNASample ms = msList.get(0);
        	Sample sample = new Sample();
        	sample.setCell(ms.getCell());
        	sample.setCreateTiemStamp(ms.getCreateTimeStamp());
        	sample.setLab(ms.getLab());
        	sample.setDeleted(0);
        	sample.setEtype(ms.getEtype());
        	sample.setSource(ms.getSource());
        	sample.setFactor(ms.getFactor());
        	sample.setSampleId(ms.getMiRNASampleId());
        	sample.setDescription(ms.getDescription());
        	sample.setUrl(ms.getUrl());
        	SampleItem item = new SampleItem(sample,null,mr.getTotalCount(),mr.getMixtureperc(),null,null,false,null,null,null);
        	sampleItemList.add(item);
        }
        GeneItem geneItem = new GeneItem();
        geneItem.setGeneId(miRNAId);
        geneItem.setGeneSymbol(miRNA.getMiRNAName());
        geneItem.setUsedForQuery(true);
        List<GeneItem> geneItemList = new ArrayList<GeneItem>();
        geneItemList.add(geneItem);
        sampleResult.setGeneItemList(geneItemList);
        sampleResult.setTotal(count);
        sampleResult.setTotal_all(total_all);
		return sampleResult;
	}
	@Override
	public List<MiRNA> search(String query) {
		int size = 10;
//		SmartDBObject q = new SmartDBObject("$regex",".*"+query);
//		SmartDBObject q1 = new SmartDBObject("name",q);
//		List<MiRNA> miRNAList = miDAO.find(q1);
		List<MiRNA> finalList = new ArrayList<MiRNA>();
//		if(CollectionUtils.isNotEmpty(miRNAList))
//		{
//			finalList.addAll(miRNAList);
//		}
		query = query.toLowerCase();
		
		finalList.addAll(miDAO.fuzzyQuery("miRNAName", query, 10));
		return finalList;
	}
	@Override
	public List<String> searchTopMiRNA(int miRNASampleId , int size){
		SmartDBObject query  = new SmartDBObject("miRNASampleId",miRNASampleId);
		query.addSort("mixtureperc", SortType.ASC);
		List<String> miRNANames = new ArrayList<String>();
		List<MiRNARank> miRanks = miRankDAO.find(query,0,size);
		for(MiRNARank mi : miRanks){
			List<MiRNA> m = miDAO.find(new SmartDBObject("miRNAId",mi.getMiRNAId()));
			miRNANames.add(m.get(0).getMiRNAName());
		}
		return miRNANames;
	} 
}
