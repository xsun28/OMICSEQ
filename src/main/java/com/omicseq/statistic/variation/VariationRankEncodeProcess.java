package com.omicseq.statistic.variation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.common.SourceType;
import com.omicseq.core.SampleCache;
import com.omicseq.core.VariationGeneCache;
import com.omicseq.domain.StatisticInfo;
import com.omicseq.domain.StatisticResult;
import com.omicseq.domain.VariationRank;
import com.omicseq.robot.message.FileInfoConsumer;
import com.omicseq.store.dao.IStatisticInfoDAO;
import com.omicseq.store.dao.IVariationRankDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.MiscUtils;

public class VariationRankEncodeProcess {
	private static IVariationRankDAO variationRankDAO = DAOFactory.getDAO(IVariationRankDAO.class);
//	private static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, Constants.STAT_SUFFIX);
	private static IStatisticInfoDAO statisticInfoDAO = DAOFactory.getDAO(IStatisticInfoDAO.class);
	
	private static Logger logger = LoggerFactory.getLogger(FileInfoConsumer.class);
	
	public static void main(String[] args) {
		SampleCache.getInstance().doInit();
		VariationGeneCache.getInstance().doInit();
		proc();
	}
	
	public static void proc() {
		Integer sampleId = 0;
		String filePath = "";
		try {
			IVariationRankStatistic proc = VariationStatisticFactory.get(SourceType.ENCODE);
			String serverIP = MiscUtils.getServerIP();
			SmartDBObject query = new SmartDBObject();
			query.put("serverIp", serverIP);
			query.put("source", 2);
			List<StatisticInfo> files = statisticInfoDAO.find(query);
			for(StatisticInfo si: files)
			{
				filePath = si.getPath();
				sampleId = si.getSampleId();
				StatisticResult result = proc.computeRank(filePath, sampleId);
				List<VariationRank> rankList = result.getVariationRank();
				variationRankDAO.removeBySampleId(sampleId);
				variationRankDAO.create(rankList);
				logger.debug("processed: " + sampleId + " file: " + filePath);
			}
		} catch (Exception e) {
			logger.debug("fail: " + sampleId + " file: " + filePath);
			e.printStackTrace();
		}
	}

}
