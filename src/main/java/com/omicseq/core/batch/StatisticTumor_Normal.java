package com.omicseq.core.batch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SortType;
import com.omicseq.common.SourceType;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.domain.SummaryTrackData;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.ISummaryTrackDataDao;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateUtils;

public class StatisticTumor_Normal {
	protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	protected static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	protected static ISummaryTrackDataDao summaryDao = DAOFactory.getDAO(ISummaryTrackDataDao.class);
	protected static ISampleDAO dao = DAOFactory.getDAO(ISampleDAO.class);
	
	public static void main(String[] args) {
		StatisticTumor_Normal st = new StatisticTumor_Normal();
//		String[] cancerTypes = {"ACC","BLCA", "BRCA", "CESC", "COAD", "DLBC", "ESCA", "GBM", "HNSC", "KICH", "KIRC", "KIRP",
//	        		"LAML", "LGG", "LIHC", "LUAD", "LUSC", "OV", "PAAD", "READ", "SARC", "SKCM", "STAD", "THCA", "UCEC", "UCS"};
		String[] cancerTypes = {"PRAD"};
		for(int i=0 ;i < cancerTypes.length; i++)
		{
			String cancerType = cancerTypes[i];
			st.createSummaryData(cancerType, ExperimentType.CVN.getValue());
//			st.createSummaryData(cancerType, ExperimentType.RNA_SEQ.getValue());
//			st.createSummaryData(cancerType, ExperimentType.METHYLATION.getValue());
		}
	}

	private void createSummaryData(String cancerType, Integer etype) {
		SmartDBObject query = new SmartDBObject("cell", new SmartDBObject("$regex", "TCGA-"+cancerType.toLowerCase()+"-normal"));
    	query.put("source", 1);
    	query.put("etype", etype);
    	query.put("deleted", 0);
    	List<Sample> sampleList = sampleDAO.find(query);
    	
    	for(int i = 0; i<sampleList.size(); i++) {
    		Sample s = sampleList.get(i);
    		String sampleCode = s.getSampleCode();
    		if(sampleCode != null && !"".equals(sampleCode))
    		{
    			String[] arr = sampleCode.split("-");
    			SmartDBObject query2 = new SmartDBObject("cell", new SmartDBObject("$regex", "TCGA-"+cancerType.toLowerCase()+"-tumor"));
    	    	query2.put("source", 1);
    	    	query2.put("sampleCode", new SmartDBObject("$regex", arr[0]+"-"+arr[1]+"-"+arr[2]));
    	    	query2.put("deleted", 0);
    	    	query2.put("etype", etype);
    	    	
    	    	List<Sample> sampleList2 = sampleDAO.find(query2);
    	    	if(sampleList2 != null && sampleList2.size() >0)
    	    	{
    	    		Sample sample = sampleList2.get(0);
    	    		
    	    		compare(sample.getSampleId(), s.getSampleId(), 1, etype, s.getCell().replace("-normal", ""), sample.getSampleCode()+"/"+s.getSampleCode());
    	    	}
    		}
    	}
    	
//    	createSample(sampleList, cancerType);
	}

	private void compare(Integer sampleId1, Integer sampleId2, Integer source, Integer etype, String cell, String sampleCode) {
		
		SmartDBObject query = new SmartDBObject();
		query.put("sampleId", sampleId1);
		query.addSort("geneId", SortType.ASC);
		List<GeneRank> geneRanks_tumor = geneRankDAO.find(query);
		
		query.put("sampleId", sampleId2);
		List<GeneRank> geneRanks_normal = geneRankDAO.find(query);
		
		int i =0;
//		String data  = "geneId	sampleId	sampleCode	tumorCount	normalCount	cell \n";
//		File file = new File("./logs/"+sampleId1+"_diff.txt");
		
		List<SummaryTrackData> summaryList = new ArrayList<SummaryTrackData>();
		
		int normalSize = geneRanks_normal.size();
		for(GeneRank g : geneRanks_tumor) {
			
			if(i < normalSize)
			{
				SummaryTrackData summary = new SummaryTrackData();
				
				summary.setGeneId(g.getGeneId());
				summary.setSampleId_tumor_normal(sampleId1+"_"+sampleId2);
				summary.setSampleCode(sampleCode);
				summary.setSource(source);
				summary.setEtype(etype);
				summary.setCellType(cell);
				
				summary.setNormalCount(geneRanks_normal.get(i).getTssTesCount());
				summary.setTumorCount(g.getTssTesCount());
				summary.setTumorDiffNormalCount(g.getTssTesCount() - geneRanks_normal.get(i).getTssTesCount());
				
				summaryList.add(summary);
			}
			i++;
		}
		try {
			summaryDao.create(summaryList);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void createSample(List<Sample> sampleList, String cacerType) {
		
		create(cacerType , sampleList, "-normal average");
		create(cacerType , sampleList, "-tumor average");
		create(cacerType , sampleList, "-tumor std dev");
		create(cacerType , sampleList, "-normal std dev");
		create(cacerType , sampleList, "-matched tumor/normal diff");
		create(cacerType , sampleList, "-total tumor/normal diff");
	}

	private static void create(String cacerType, List<Sample> sampleList, String tumorOrNormal) {
		Sample sampleNew = new Sample();
		
		sampleNew.setCreateTiemStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
		sampleNew.setSource(SourceType.TCGA.value());
		sampleNew.setEtype(ExperimentType.SUMMARY_TRACK.value());
		sampleNew.setDeleted(0);
		String inputSampleIds = "";
		for(Sample s : sampleList)
		{
			inputSampleIds += s.getSampleId()+",";
		}
		sampleNew.setInputSampleIds(inputSampleIds);
		int sampleId = dao.getSequenceId(SourceType.TCGA);
		sampleNew.setSampleId(sampleId);
		sampleNew.setCell("TCGA-" + cacerType.toLowerCase() + tumorOrNormal);
		sampleNew.setSettype("Methylation");
		sampleNew.setLab("Serendi");
		sampleDAO.create(sampleNew);
	}

}
