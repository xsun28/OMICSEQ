package com.omicseq.core.summary;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SortType;
import com.omicseq.common.SourceType;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.MiRNA;
import com.omicseq.domain.MiRNARank;
import com.omicseq.domain.MiRNASample;
import com.omicseq.domain.Sample;
import com.omicseq.domain.SummaryTrackData;
import com.omicseq.robot.process.SymbolReader;
import com.omicseq.store.dao.ImiRNADAO;
import com.omicseq.store.dao.ImiRNARankDAO;
import com.omicseq.store.dao.ImiRNASampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateUtils;

public class MiRNASummary {
	private static ImiRNASampleDAO miSampleDAO = DAOFactory.getDAO(ImiRNASampleDAO.class);
	private static ImiRNADAO miRNADAO = DAOFactory.getDAOByTableType(ImiRNADAO.class, "new");
	private static ImiRNARankDAO miRankDAO = DAOFactory.getDAO(ImiRNARankDAO.class);
	
	private static List<MiRNA> miRNAIds = new ArrayList<MiRNA>();
	
	public static Map<Integer, List<SummaryTrackData>> summaryMap = new HashMap<Integer, List<SummaryTrackData>>();
	
	public static Map<Integer, List<SummaryTrackData>> summaryTumorMap = new HashMap<Integer, List<SummaryTrackData>>();
	
	public static void main(String[] args) {
//		String[] cancerTypes = {"ACC","BLCA", "BRCA", "CESC", "COAD", "DLBC", "ESCA", "GBM", "HNSC", "KICH", "KIRC", "KIRP",
//		"LAML", "LGG", "LIHC", "LUAD", "LUSC", "OV", "PAAD", "READ", "SARC", "SKCM", "STAD", "THCA", "UCEC", "UCS"};
		String[] cancerTypes = {"DLBC", "GBM", "HNSC", "KICH", "LAML", "LGG", "OV", "SARC", "UCS"};
		
//		miRNAIds = miRNADAO.find(new SmartDBObject());
		MiRNASummary st = new MiRNASummary();
		for(int i=0 ;i < cancerTypes.length; i++)
		{
			String cancerType = cancerTypes[i];
//			st.summary(cancerType.toLowerCase());
//			st.summaryTumor(cancerType.toLowerCase());
			
			st.removeRank(cancerType.toLowerCase());
		}
	}

	private void removeRank(String cancerType) {
		String normal_average = "normal average";
		String normal_std_dev = "normal std dev";
		String matched_tumor_normal_diff = "matched tumor/normal diff";
		Integer sampleId_normal_average = miSampleDAO.find(new SmartDBObject("cell", "TCGA-" + cancerType + "-" + normal_average)).get(0).getMiRNASampleId();
		Integer sampleId_normal_std_dev = miSampleDAO.find(new SmartDBObject("cell", "TCGA-" + cancerType + "-" + normal_std_dev)).get(0).getMiRNASampleId();
		Integer sampleId_matched_tumor_normal_diff = miSampleDAO.find(new SmartDBObject("cell", "TCGA-" + cancerType + "-" + matched_tumor_normal_diff)).get(0).getMiRNASampleId();
		
		Integer size = miSampleDAO.find(new SmartDBObject("cell", "TCGA-" + cancerType + "-normal")).size();
		if(size == 0)
		{
			SmartDBObject queryRank = new SmartDBObject();
			queryRank.put("miRNASampleId", sampleId_normal_average);
			miRankDAO.delete(queryRank);
			
			queryRank.put("miRNASampleId", sampleId_normal_std_dev);
			miRankDAO.delete(queryRank);
			
			queryRank.put("miRNASampleId", sampleId_matched_tumor_normal_diff);
			miRankDAO.delete(queryRank);
		}
	}

	private void summaryTumor(String cellType) {
		
		createMiRNASamples(cellType);
		
		SmartDBObject query = new SmartDBObject("cell", "TCGA-"+cellType+"-tumor");
		List<MiRNASample> msList = miSampleDAO.find(query);
		for(int i=0;i<msList.size();i++)
		{
			MiRNASample s = msList.get(i);
			SmartDBObject queryRank = new SmartDBObject();
			queryRank.put("miRNASampleId", s.getMiRNASampleId());
			List<MiRNARank> ranks = miRankDAO.find(queryRank);
			
			for(MiRNARank g : ranks) {
				List<SummaryTrackData> summaryList = new ArrayList<SummaryTrackData>();
				SummaryTrackData summary = new SummaryTrackData();
				
				summary.setGeneId(g.getMiRNAId());
				summary.setCellType(cellType);
				summary.setTumorCount(g.getRead());
				
				if(summaryTumorMap.get(g.getMiRNAId()) != null && summaryTumorMap.get(g.getMiRNAId()).size() > 0)
				{
					summaryList = summaryTumorMap.get(g.getMiRNAId());
					summaryList.add(summary);
				} else {
					summaryList.add(summary);
				}
				summaryTumorMap.put(g.getMiRNAId(), summaryList);
			}
		}
		
		statisticTumorRank(cellType);
	}

	private void statisticTumorRank(String cancerType) {
		String tumor_average = "tumor average";
		String tumor_std_dev = "tumor std dev";
		String total_tumor_normal_diff = "total tumor/normal diff";
		
		Integer sampleId_tumor_average = miSampleDAO.find(new SmartDBObject("cell", "TCGA-" + cancerType + "-" + tumor_average)).get(0).getMiRNASampleId();
		Integer sampleId_tumor_std_dev = miSampleDAO.find(new SmartDBObject("cell", "TCGA-" + cancerType + "-" + tumor_std_dev)).get(0).getMiRNASampleId();
		Integer sampleId_total_tumor_normal_diff = miSampleDAO.find(new SmartDBObject("cell", "TCGA-" + cancerType + "-" + total_tumor_normal_diff)).get(0).getMiRNASampleId();
		
		SmartDBObject query0 = new SmartDBObject("source", 1);
		query0.put("cell", new SmartDBObject("$regex", cancerType + "-normal std dev"));
		List<MiRNASample> sampleListNormalStd = miSampleDAO.find(query0);
		
		if(sampleListNormalStd == null || sampleListNormalStd.size() == 0)
		{
			return;
		}
		
		Integer normalStdSampleId = sampleListNormalStd.get(0).getMiRNASampleId();
		
		List<MiRNARank> normalStdGenRankList = miRankDAO.find(new SmartDBObject("miRNASampleId", normalStdSampleId));
		
		query0.put("cell", new SmartDBObject("$regex", cancerType + "-normal average"));
		List<MiRNASample> sampleListNormalAVG = miSampleDAO.find(query0);
		
		Integer normalAVGSampleId = sampleListNormalAVG.get(0).getMiRNASampleId();
		List<MiRNARank> normalAvgGenRankList = miRankDAO.find(new SmartDBObject("miRNASampleId", normalAVGSampleId));

		Map<Integer, Double> normalAvgMap = new HashMap<Integer, Double>();
		Map<Integer, Double> normalStdMap = new HashMap<Integer, Double>();
		
		for(MiRNARank g : normalAvgGenRankList)
		{
			normalAvgMap.put(g.getMiRNAId(), g.getRead());
		}
		
		for(MiRNARank g : normalStdGenRankList)
		{
			normalStdMap.put(g.getMiRNAId(), g.getRead());
		}
		
		List<SummaryTrackData> summaryList = null;
		List<MiRNASample> list = miSampleDAO.find(new SmartDBObject("cell", "TCGA-"+cancerType+"-normal"));
		if(list == null || list.size() == 0)
		{
			return;
		}
		
		List<SymbolReader> symbolReaderList_tumor_average = new ArrayList<SymbolReader>();
		List<SymbolReader> symbolReaderLis_tumor_std_dev = new ArrayList<SymbolReader>();
		List<SymbolReader> symbolReaderList_total_tumor_normal_diff = new ArrayList<SymbolReader>();
		
		for(int j=0; j<miRNAIds.size(); j++)
		{
			Double tumorAvg = 0.0;
			
			summaryList = summaryTumorMap.get(miRNAIds.get(j).getMiRNAId());
			
			if(summaryList != null)
			{
				List<Double> values = new ArrayList<Double>();
				for(int i=0; i<summaryList.size(); i++)
				{
					 values.add(summaryList.get(i).getTumorCount());
					 tumorAvg += summaryList.get(i).getTumorCount();
				}
				
				tumorAvg = tumorAvg/summaryList.size();
				
				double tumorStd = 0.0;
				 for(Double d : values)
				 {
					 tumorStd += Math.pow(d-tumorAvg, 2);
				 }
				 
				 tumorStd = Math.sqrt(tumorStd/(summaryList.size()-1));
				 
				 SymbolReader sr1 = new SymbolReader();
				 sr1.setGeneId(miRNAIds.get(j).getMiRNAId());
				 sr1.setRead(Math.abs(tumorAvg));
				 
				 SymbolReader sr2 = new SymbolReader();
				 sr2.setGeneId(miRNAIds.get(j).getMiRNAId());
				 sr2.setRead(tumorStd);
				 
				 
				 Double normAvg = normalAvgMap.get(miRNAIds.get(j).getMiRNAId());
				 if(normAvg == null || normAvg == 0.0) {
					 continue;
				 }
				 Double normalStd = normalStdMap.get(miRNAIds.get(j).getMiRNAId());
				 if(normalStd == null || normalStd == 0.0) {
					 continue;
				 }
				 
				 double t = (tumorAvg - normAvg) / Math.sqrt(Math.pow(tumorStd, 2)/summaryList.size() + Math.pow(normalStd, 2)/list.size());
				 
				 
				 SymbolReader sr3 = new SymbolReader();
				 sr3.setGeneId(miRNAIds.get(j).getMiRNAId());
				 sr3.setRead(Math.abs(t));
				 
				 
				 symbolReaderList_tumor_average.add(sr1); 
				 symbolReaderLis_tumor_std_dev.add(sr2); 
				 symbolReaderList_total_tumor_normal_diff.add(sr3);
			}
		}
		
		//排序 
		Collections.sort(symbolReaderList_tumor_average, new Comparator<SymbolReader>() {
			@Override
			public int compare(SymbolReader o1, SymbolReader o2) {
				return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1);
			}
		});
		
		Collections.sort(symbolReaderLis_tumor_std_dev, new Comparator<SymbolReader>() {
			@Override
			public int compare(SymbolReader o1, SymbolReader o2) {
				return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1);
			}
		});
		
		//排序 
		Collections.sort(symbolReaderList_total_tumor_normal_diff, new Comparator<SymbolReader>() {
			@Override
			public int compare(SymbolReader o1, SymbolReader o2) {
				return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1);
			}
		});
		
		java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00000");
		List<MiRNARank> miRanks_normal_average = new ArrayList<MiRNARank>();
		for(SymbolReader sr: symbolReaderList_tumor_average){				
			MiRNARank mr = new MiRNARank();
			mr.setCreateTimeStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
			mr.setMiRNASampleId(sampleId_tumor_average);
			mr.setMiRNAId(sr.getGeneId());
			mr.setRead(sr.getRead());
			mr.setSource(SourceType.TCGA.value());
			mr.setEtype(ExperimentType.MIRNA_SEQ.value());
			mr.setTotalCount(symbolReaderList_tumor_average.size());
			mr.setMixtureperc(Double.parseDouble(df.format((double)(symbolReaderList_tumor_average.indexOf(sr)+1)/symbolReaderList_tumor_average.size())));
			miRanks_normal_average.add(mr);
		}
		
		List<MiRNARank> miRanks_symbolReaderLis_normal_std_dev = new ArrayList<MiRNARank>();
		for(SymbolReader sr: symbolReaderLis_tumor_std_dev){
			MiRNARank mr = new MiRNARank();
			mr.setCreateTimeStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
			mr.setMiRNASampleId(sampleId_tumor_std_dev);
			mr.setMiRNAId(sr.getGeneId());
			mr.setRead(sr.getRead());
			mr.setSource(SourceType.TCGA.value());
			mr.setEtype(ExperimentType.MIRNA_SEQ.value());
			mr.setTotalCount(symbolReaderLis_tumor_std_dev.size());
			mr.setMixtureperc(Double.parseDouble(df.format((double)(symbolReaderLis_tumor_std_dev.indexOf(sr)+1)/symbolReaderLis_tumor_std_dev.size())));
			miRanks_symbolReaderLis_normal_std_dev.add(mr);
		}

		List<MiRNARank> miRanks_matched_tumor_normal_diff = new ArrayList<MiRNARank>();
		for(SymbolReader sr: symbolReaderList_total_tumor_normal_diff){
			MiRNARank mr = new MiRNARank();
			mr.setCreateTimeStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
			mr.setMiRNASampleId(sampleId_total_tumor_normal_diff);
			mr.setMiRNAId(sr.getGeneId());
			mr.setRead(sr.getRead());
			mr.setSource(SourceType.TCGA.value());
			mr.setEtype(ExperimentType.MIRNA_SEQ.value());
			mr.setTotalCount(symbolReaderList_total_tumor_normal_diff.size());
			mr.setMixtureperc(Double.parseDouble(df.format((double)(symbolReaderList_total_tumor_normal_diff.indexOf(sr)+1)/symbolReaderList_total_tumor_normal_diff.size())));
			miRanks_matched_tumor_normal_diff.add(mr);
		}
		
		
		miRankDAO.create(miRanks_normal_average);
		miRankDAO.create(miRanks_symbolReaderLis_normal_std_dev);
		miRankDAO.create(miRanks_matched_tumor_normal_diff);
		
	}

	private void createMiRNASamples(String cellType) {
		String tumor_average = "tumor average";
		String tumor_std_dev = "tumor std dev";
		String total_tumor_normal_diff = "total tumor/normal diff";
		Integer sampleId_tumor_average = miSampleDAO.getSequenceId(SourceType.TCGA);
		Integer sampleId_tumor_std_dev = miSampleDAO.getSequenceId(SourceType.TCGA);
		Integer sampleId_total_tumor_normal_diff = miSampleDAO.getSequenceId(SourceType.TCGA);
		
		MiRNASample ms = new MiRNASample();
		ms.setCreateTimeStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
		ms.setDeleted(0);
		ms.setEtype(ExperimentType.MIRNA_SEQ.value());
		ms.setSource(SourceType.TCGA.value());
		ms.setMiRNASampleId(sampleId_tumor_average);
		ms.setCell("TCGA-" + cellType + "-" + tumor_average);
		miSampleDAO.create(ms);
		
		MiRNASample ms2 = new MiRNASample();
		ms2.setCreateTimeStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
		ms2.setDeleted(0);
		ms2.setEtype(ExperimentType.MIRNA_SEQ.value());
		ms2.setSource(SourceType.TCGA.value());
		ms2.setMiRNASampleId(sampleId_tumor_std_dev);
		ms2.setCell("TCGA-" + cellType + "-" + tumor_std_dev);
		miSampleDAO.create(ms2);
		
		MiRNASample ms3 = new MiRNASample();
		ms3.setCreateTimeStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
		ms3.setDeleted(0);
		ms3.setEtype(ExperimentType.MIRNA_SEQ.value());
		ms3.setSource(SourceType.TCGA.value());
		ms3.setMiRNASampleId(sampleId_total_tumor_normal_diff);
		ms3.setCell("TCGA-" + cellType + "-" + total_tumor_normal_diff);
		miSampleDAO.create(ms3);
	}

	private void summary(String cancerType) {
		SmartDBObject query = new SmartDBObject("cell", new SmartDBObject("$regex", "TCGA-"+cancerType+"-normal"));
		List<MiRNASample> msList = miSampleDAO.find(query);
		
		File file = new File("./logs/"+cancerType+"_diff.txt");
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String data  = "miRNAId\tsampleId\ttumorCount\tnormalCount	cell \n";
		
		try {
			FileWriter writer = new FileWriter("./logs/"+cancerType+"_diff.txt");
			writer.write(data);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(int i=0;i<msList.size();i++)
		{
			MiRNASample s = msList.get(i);
			String sampleCode = s.getBarCode();
			
			if(sampleCode != null && !"".equals(sampleCode))
    		{
    			String[] arr = sampleCode.split("-");
    			SmartDBObject query2 = new SmartDBObject("cell", new SmartDBObject("$regex", "TCGA-"+cancerType+"-tumor"));
    	    	query2.put("barCode", new SmartDBObject("$regex", arr[0]+"-"+arr[1]+"-"+arr[2]));
    	    	
    	    	List<MiRNASample> sampleList2 = miSampleDAO.find(query2);
    	    	if(sampleList2 != null && sampleList2.size() >0)
    	    	{
    	    		MiRNASample sample = sampleList2.get(0);
    	    		
    	    		compare(s.getMiRNASampleId(), sample.getMiRNASampleId(), s.getCell().replace("-normal", "").replace("TCGA-", ""));
    	    	}
    		}
		}
		if(summaryMap.isEmpty())
		{
			return;
		}
		createMiRNARank(cancerType);
	}


	private void compare(Integer miRNASampleId1, Integer miRNASampleId2, String cellType) {

		
		SmartDBObject query = new SmartDBObject();
		query.put("miRNASampleId", miRNASampleId1);
		query.addSort("miRNAId", SortType.ASC);
		List<MiRNARank> ranks_normal = miRankDAO.find(query);
		
		query.put("miRNASampleId", miRNASampleId2);
		List<MiRNARank> ranks_tumor = miRankDAO.find(query);
		
		
		int i =0;
		
		int normalSize = ranks_tumor.size();
		for(MiRNARank g : ranks_normal) {
			List<SummaryTrackData> summaryList = new ArrayList<SummaryTrackData>();
			if(i == normalSize)
			{
				break;
			}
			SummaryTrackData summary = new SummaryTrackData();
			
			summary.setGeneId(g.getMiRNAId());
			summary.setSampleId_tumor_normal(miRNASampleId2+"_"+miRNASampleId1);
			summary.setCellType(cellType);
			
			summary.setNormalCount(g.getRead());
			summary.setTumorCount(ranks_tumor.get(i).getRead());
			summary.setTumorDiffNormalCount(ranks_tumor.get(i).getRead() - g.getRead());
			
			if(summaryMap.get(g.getMiRNAId()) != null && summaryMap.get(g.getMiRNAId()).size() > 0)
			{
				summaryList = summaryMap.get(g.getMiRNAId());
				summaryList.add(summary);
			} else {
				summaryList.add(summary);
			}
			summaryMap.put(g.getMiRNAId(), summaryList);
			i++;
			
			String data = summary.getGeneId() + "\t\t" + summary.getSampleId_tumor_normal() + "\t" +  summary.getTumorCount() + "\t" + summary.getNormalCount() + "\n";
			
			try {
				FileWriter writer = new FileWriter("./logs/"+cellType+"_diff.txt", true);
				writer.write(data);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	private void createMiRNARank(String cellType) {
		String normal_average = "normal average";
		String normal_std_dev = "normal std dev";
		String matched_tumor_normal_diff = "matched tumor/normal diff";
		Integer sampleId_normal_average = miSampleDAO.getSequenceId(SourceType.TCGA);
		Integer sampleId_normal_std_dev = miSampleDAO.getSequenceId(SourceType.TCGA);
		Integer sampleId_matched_tumor_normal_diff = miSampleDAO.getSequenceId(SourceType.TCGA);
		
				
		List<SymbolReader> symbolReaderList_normal_average = new ArrayList<SymbolReader>();
		List<SymbolReader> symbolReaderLis_normal_std_dev = new ArrayList<SymbolReader>();
		List<SymbolReader> symbolReaderList_matched_tumor_normal_diff = new ArrayList<SymbolReader>();
		
		java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00000");
		
		
		MiRNASample ms = new MiRNASample();
		ms.setCreateTimeStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
		ms.setDeleted(0);
		ms.setEtype(ExperimentType.MIRNA_SEQ.value());
		ms.setSource(SourceType.TCGA.value());
		ms.setMiRNASampleId(sampleId_normal_average);
		ms.setCell("TCGA-" + cellType + "-" + normal_average);
		miSampleDAO.create(ms);
		
		MiRNASample ms2 = new MiRNASample();
		ms2.setCreateTimeStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
		ms2.setDeleted(0);
		ms2.setEtype(ExperimentType.MIRNA_SEQ.value());
		ms2.setSource(SourceType.TCGA.value());
		ms2.setMiRNASampleId(sampleId_normal_std_dev);
		ms2.setCell("TCGA-" + cellType + "-" + normal_std_dev);
		miSampleDAO.create(ms2);
		
		MiRNASample ms3 = new MiRNASample();
		ms3.setCreateTimeStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
		ms3.setDeleted(0);
		ms3.setEtype(ExperimentType.MIRNA_SEQ.value());
		ms3.setSource(SourceType.TCGA.value());
		ms3.setMiRNASampleId(sampleId_matched_tumor_normal_diff);
		ms3.setCell("TCGA-" + cellType + "-" + matched_tumor_normal_diff);
		miSampleDAO.create(ms3);
		
//		Integer sampleId_normal_average = miSampleDAO.find(new SmartDBObject("cell", "TCGA-" + cellType + "-" + normal_average)).get(0).getMiRNASampleId();
//		Integer sampleId_normal_std_dev = miSampleDAO.find(new SmartDBObject("cell", "TCGA-" + cellType + "-" + normal_std_dev)).get(0).getMiRNASampleId();
//		Integer sampleId_matched_tumor_normal_diff = miSampleDAO.find(new SmartDBObject("cell", "TCGA-" + cellType + "-" + matched_tumor_normal_diff)).get(0).getMiRNASampleId();
		
		List<SummaryTrackData> summaryList = null;
		for(int j=0; j<miRNAIds.size(); j++)
		{
			summaryList = summaryMap.get(miRNAIds.get(j).getMiRNAId());
			if(summaryList != null)
			{
				Double read2 = 0.0;
				Double read3 = 0.0;
				for(SummaryTrackData s : summaryList)
				{
					read2 += s.getTumorDiffNormalCount();
					read3 += s.getNormalCount();
				}
				
				SymbolReader sr1 = new SymbolReader();
				SymbolReader sr2 = new SymbolReader();
				SymbolReader sr3 = new SymbolReader();
				
				double sd = 0.0;
				double dav 	= read2/summaryList.size();
				double normalAvg  = read3/summaryList.size();
				double normalStd = 0.0;
				
				for(SummaryTrackData s : summaryList) {
					sd += Math.pow(s.getTumorDiffNormalCount()-dav, 2);
					normalStd += Math.pow(s.getNormalCount()-normalAvg, 2);
				}
				
				double sed = Math.sqrt(sd/(summaryList.size()-1))/Math.sqrt(summaryList.size());

				if(sed <= 0)
				{
					continue;
				}
				
				double t = Math.abs(dav/sed);
			
				double normalDev = Math.sqrt(normalStd/(summaryList.size()-1));
				
				sr1.setRead(Math.abs(normalAvg));
				sr2.setRead(normalDev);
				sr3.setRead(t);
				
				sr1.setGeneId(miRNAIds.get(j).getMiRNAId());
				sr2.setGeneId(miRNAIds.get(j).getMiRNAId());
				sr3.setGeneId(miRNAIds.get(j).getMiRNAId());
				
				symbolReaderList_normal_average.add(sr1);
				symbolReaderLis_normal_std_dev.add(sr2);
				symbolReaderList_matched_tumor_normal_diff.add(sr3);
				
			}
		}
		
		//排序 
			Collections.sort(symbolReaderList_normal_average, new Comparator<SymbolReader>() {
				@Override
				public int compare(SymbolReader o1, SymbolReader o2) {
					return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1);
				}
			});
			
			//排序 
			Collections.sort(symbolReaderList_matched_tumor_normal_diff, new Comparator<SymbolReader>() {
				@Override
				public int compare(SymbolReader o1, SymbolReader o2) {
					return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1);
				}
			});
			
			//排序 
			Collections.sort(symbolReaderLis_normal_std_dev, new Comparator<SymbolReader>() {
				@Override
				public int compare(SymbolReader o1, SymbolReader o2) {
					return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1);
				}
			});
			
			
			List<MiRNARank> miRanks_normal_average = new ArrayList<MiRNARank>();
			for(SymbolReader sr: symbolReaderList_normal_average){				
				MiRNARank mr = new MiRNARank();
				mr.setCreateTimeStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
				mr.setMiRNASampleId(sampleId_normal_average);
				mr.setMiRNAId(sr.getGeneId());
				mr.setRead(sr.getRead());
				mr.setSource(SourceType.TCGA.value());
				mr.setEtype(ExperimentType.MIRNA_SEQ.value());
				mr.setTotalCount(symbolReaderList_normal_average.size());
				mr.setMixtureperc(Double.parseDouble(df.format((double)(symbolReaderList_normal_average.indexOf(sr)+1)/symbolReaderList_normal_average.size())));
				miRanks_normal_average.add(mr);
			}
			
			List<MiRNARank> miRanks_symbolReaderLis_normal_std_dev = new ArrayList<MiRNARank>();
			for(SymbolReader sr: symbolReaderLis_normal_std_dev){
				MiRNARank mr = new MiRNARank();
				mr.setCreateTimeStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
				mr.setMiRNASampleId(sampleId_normal_std_dev);
				mr.setMiRNAId(sr.getGeneId());
				mr.setRead(sr.getRead());
				mr.setSource(SourceType.TCGA.value());
				mr.setEtype(ExperimentType.MIRNA_SEQ.value());
				mr.setTotalCount(symbolReaderLis_normal_std_dev.size());
				mr.setMixtureperc(Double.parseDouble(df.format((double)(symbolReaderLis_normal_std_dev.indexOf(sr)+1)/symbolReaderLis_normal_std_dev.size())));
				miRanks_symbolReaderLis_normal_std_dev.add(mr);
			}

			List<MiRNARank> miRanks_matched_tumor_normal_diff = new ArrayList<MiRNARank>();
			for(SymbolReader sr: symbolReaderList_matched_tumor_normal_diff){
				MiRNARank mr = new MiRNARank();
				mr.setCreateTimeStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
				mr.setMiRNASampleId(sampleId_matched_tumor_normal_diff);
				mr.setMiRNAId(sr.getGeneId());
				mr.setRead(sr.getRead());
				mr.setSource(SourceType.TCGA.value());
				mr.setEtype(ExperimentType.MIRNA_SEQ.value());
				mr.setTotalCount(symbolReaderList_matched_tumor_normal_diff.size());
				mr.setMixtureperc(Double.parseDouble(df.format((double)(symbolReaderList_matched_tumor_normal_diff.indexOf(sr)+1)/symbolReaderList_matched_tumor_normal_diff.size())));
				miRanks_matched_tumor_normal_diff.add(mr);
			}
			
			
			miRankDAO.create(miRanks_normal_average);
			miRankDAO.create(miRanks_symbolReaderLis_normal_std_dev);
			miRankDAO.create(miRanks_matched_tumor_normal_diff);
	}
}
