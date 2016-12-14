package com.omicseq.statistic.variation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.omicseq.domain.Sample;
import com.omicseq.domain.StatisticInfo;
import com.omicseq.domain.VariationRank;
import com.omicseq.store.dao.IStatisticInfoDAO;
import com.omicseq.store.dao.IVariationRankDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class ModifyVariationRankAddOrderNo {
	
	private static IVariationRankDAO variationRankDAO = DAOFactory.getDAO(IVariationRankDAO.class);
	private static IStatisticInfoDAO statisticInfoDAO = DAOFactory.getDAO(IStatisticInfoDAO.class);
	public static void main(String[] args) {
		Integer [] serverIp = {157};
		for(Integer ip : serverIp){
			SmartDBObject query = new SmartDBObject();
			query.put("serverIp", new SmartDBObject("$regex","112.25.20."+ip));
			query.put("source", 2);
			List<StatisticInfo> list = statisticInfoDAO.find(query);
			for(StatisticInfo s : list){
				Integer sampleId = s.getSampleId();
				
				SmartDBObject query2 = new SmartDBObject();
				query2.put("sampleId", sampleId);
				List<VariationRank> vrList = variationRankDAO.find(query2);
				if(vrList != null && !vrList.isEmpty()) {
					for(VariationRank vr : vrList) {
						Double mixturePerc = vr.getMixturePerc();
						Integer totalCount = vr.getTotalCount();
						
						Integer orderNo = (int) Math.round(mixturePerc * totalCount);
						if(orderNo.equals(0)) {
							orderNo = 1;
						}
						vr.setOrderNo(orderNo);
					}
					
					variationRankDAO.removeBySampleId(sampleId);
					
					variationRankDAO.create(vrList);
				}
			}
		}
	}

}
