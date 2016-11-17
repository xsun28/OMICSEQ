package com.omicseq.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.omicseq.domain.MiRNARank;
import com.omicseq.domain.MiRNASample;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ImiRNARankDAO;
import com.omicseq.store.dao.ImiRNASampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class Test2 {
	private static IGeneRankDAO geneRankDao = DAOFactory.getDAO(IGeneRankDAO.class);
	

	public static void main(String[] args) {
//		java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00000");  
//		for(int sampleId = 65010 ;sampleId<65084;sampleId++){
//			SmartDBObject query = new SmartDBObject();
//			query.put("sampleId", sampleId);
//			query.put("etype", 15);
//			query.addSort("tssTesCount", SortType.DESC);
//			List<GeneRank> ranks = geneRankDao.find(query);
//			for(GeneRank gr : ranks){
//				GeneRank rank = new GeneRank();
//				rank.setCreatedTimestamp(gr.getCreatedTimestamp());
//				rank.setEtype(15);
//				rank.setSource(1);
//				rank.setMixturePerc(Double.parseDouble(df.format((double)(ranks.indexOf(gr)+1)/27639)));
//				rank.setTotalCount(27639);
//				rank.setTssTesCount(gr.getTssTesCount());
//				rank.setSampleId(sampleId);
//				rank.setGeneId(gr.getGeneId());
//				geneRankDao.update(rank);
//			} 
//		}
		ImiRNARankDAO rankDao = DAOFactory.getDAO(ImiRNARankDAO.class);
		ImiRNASampleDAO sampleDao = DAOFactory.getDAO(ImiRNASampleDAO.class);
		List<MiRNASample> sampleList = sampleDao.find(new SmartDBObject("deleted", 0));
		List<MiRNASample> sampleNewList = new ArrayList<MiRNASample>();
		List<MiRNARank> ranks = rankDao.find(new SmartDBObject("miRNAId",1));
		Map<Integer,Integer> map = new HashMap<Integer, Integer>();
		for(MiRNARank rank : ranks){
			map.put(rank.getMiRNASampleId(), rank.getTotalCount());
		}
		for(MiRNASample sample : sampleList){
			MiRNASample sampleNew = new MiRNASample();
			sampleNew.setMiRNASampleId(sample.getMiRNASampleId());
			sampleNew.setDeleted(0);
			sampleNew.setSource(1);
			sampleNew.setEtype(14);
			sampleNew.setCreateTimeStamp(sample.getCreateTimeStamp());
			sampleNew.setCell(sample.getCell());
			sampleNew.setLab(sample.getLab());
			sampleNew.setFactor(sample.getFactor());
			sampleNew.setBarCode(sample.getBarCode());
			sampleNew.setDescription(sample.getDescription());
			sampleNew.setUrl(sample.getUrl());
			int totalCount = map.get(sample.getMiRNASampleId());
			sampleNew.setTotalCount(totalCount);
			sampleNewList.add(sampleNew);
		}
		sampleDao.create(sampleNewList);
	}

}
