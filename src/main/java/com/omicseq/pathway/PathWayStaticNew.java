package com.omicseq.pathway;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.omicseq.common.SourceType;
import com.omicseq.core.SampleCache;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.IPathWayDAO;
import com.omicseq.store.dao.IPathWaySampleDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class PathWayStaticNew {
	protected static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	private static IPathWayDAO pathWayDao = DAOFactory.getDAO(IPathWayDAO.class);
	private static IPathWaySampleDAO pathWaySampleDao = DAOFactory.getDAO(IPathWaySampleDAO.class);
	protected static DBCollection  collection;
	protected static SampleCache sampleCahe = SampleCache.getInstance();
	protected static List<Integer> sampleIds;
	
	static {
		try {
			Mongo mongo = new Mongo("112.25.20.155", 27017);
			DB db = mongo.getDB("manage");
			db.authenticate("root", "seqjava".toCharArray());
			collection = db.getCollection("sampleOfGeneReadCount");
			
			sampleCahe.doInit();
			
			sampleIds = sampleCahe.getSampleIds();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		
		List<PathWay> pathWayList = pathWayDao.find(new SmartDBObject());
//		List<PathWay> pathWayList = new ArrayList<PathWay>();
//		pathWayList.add(pathWayDao.findOne("TOMLINS_PROSTATE_CANCER_UP"));
//		pathWayList.add(pathWayDao.findOne("HWANG_PROSTATE_CANCER_MARKERS"));
		for(PathWay pw : pathWayList) {
//			String fileName = "./logs/pathwayRead_"+ pw.getPathwayName() +".txt";
//			File file = new File(fileName);
//			try {
//				file.createNewFile();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			
//			String content = "sampleId,totalReadCount,pathwayReadcount,numY/numX,result\n";
//			try {
//				FileWriter writer0 = new FileWriter(fileName, true);
//				writer0.write(content);
//				writer0.close();
//			} catch (IOException e1) {
//				e1.printStackTrace();
//			}
			
			List<PathWaySample> psList = new ArrayList<PathWaySample>();
			List<Integer> pathGeneIdList = new ArrayList<Integer>();
			String geneIds = pw.getGeneIds();
			String[] geneIdArray = geneIds.split(",");
			for(int i=0; i<geneIdArray.length; i++)
			{
				if(geneIdArray[i] != null && !"null".equals(geneIdArray[i])) {
					pathGeneIdList.add(Integer.parseInt(geneIdArray[i]));
				}
			}
			for(Integer sampleId : sampleIds)
			{
				Sample sample = sampleCahe.get(sampleId);
				if(sample.getEtype() == 0 || sample.getSource() == SourceType.GEO.getValue())
				{
					continue;
				}
				String genRanks = null;
				SmartDBObject queryRank = new SmartDBObject("sampleId", sampleId);
				try {
					DBObject  object = collection.findOne(queryRank);
					if(object != null)
					{
						genRanks = object.get("geneId_count").toString();
					}else {
						continue;
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				
				if(genRanks == null)
				{
					continue;
				}
				
				try {
					List<GeneRank> genRankList = new ArrayList<GeneRank>();
					String[] geneId_Rank = genRanks.split(",");
					String[] values;
					for(int i=0; i<geneId_Rank.length; i++)
					{
						GeneRank geneRank = new GeneRank();
						values = geneId_Rank[i].split("=");
						if("".equals(values[0]) || "".equals(values[1]) || "非数字".equals(values[1]))
						{
							continue;
						}
						Integer geneId = Integer.parseInt(values[0]);
						Double rank = Double.parseDouble(values[1]);

						geneRank.setGeneId(geneId);
						geneRank.setTssTesCount(rank);
						
						genRankList.add(geneRank);
					}
					
					double totalX = 0.0;
					double totalY = 0.0;
					int totalNum = genRankList.size();
					int pathNumIn = 0;
					for(GeneRank geneRank : genRankList) {
						totalX += Math.abs(geneRank.getTssTesCount());
						Integer geneId = geneRank.getGeneId();
						if(pathGeneIdList.contains(geneId))
						{
							totalY += Math.abs(geneRank.getTssTesCount());
							pathNumIn++;
						}
					}
					if(pathNumIn == 0 || totalX == 0)
					{
						continue;
					}
					double result = (totalY/totalX)*(totalNum/pathNumIn);
					if("非数字".equals(result))
					{
						continue;
					}
					PathWaySample ps = new PathWaySample();
					ps.setPathId(pw.getPathId());
					ps.setSampleId(sampleId);
					ps.setPathWayName(pw.getPathwayName());
					ps.setAvgA(totalY/totalX);
					ps.setB(result);
					ps.setSource(sample.getSource());
					ps.setEtype(sample.getEtype());
					psList.add(ps);
					
//		            try {
//		            	content = sampleId + "," + totalX  + "," + totalY + "," + pathNumIn+"/" + totalNum + "," + result + "\n";
//		            	FileWriter writer = new FileWriter(fileName, true);
//						writer.write(content);
//						writer.close();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
		          
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
			
			//根据rank值排序
    		Collections.sort(psList, new Comparator<PathWaySample>() {
    			@Override
    			public int compare(PathWaySample o1, PathWaySample o2) {
    				if(null != o1.getB() && null != o2.getB())
    				{
    					return o1.getB().compareTo(o2.getB()) * (-1);
    				}
    				return 0;
    			}
    		});
    		
    		java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00000");
    		
    		for(PathWaySample ps : psList)
    		{
    			ps.setRank(Double.parseDouble(df.format((double)(psList.indexOf(ps)+1)/psList.size())));
    		}
    		
    		pathWaySampleDao.delete(new SmartDBObject("pathId", pw.getPathId()));
    		
    		pathWaySampleDao.create(psList);
    		
    		pathWayDao.updateStatus(pw.getPathId(), (short)1);
    		
    		System.out.println("runned pathId: " + pw.getPathId());
		}
		
	}

}
