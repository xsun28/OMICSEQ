package com.omicseq.core.summary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.core.GeneCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.ISummaryTrackDataDao;
import com.omicseq.store.daoimpl.factory.DAOFactory;

public class CalculateGeneRanksOfGeneSummary {
	
	protected static GeneCache geneCache = GeneCache.getInstance();
	protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	protected static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	protected static ISummaryTrackDataDao summaryDao = DAOFactory.getDAO(ISummaryTrackDataDao.class);
	protected static ISampleDAO dao = DAOFactory.getDAO(ISampleDAO.class);
	
	public static void main(String[] args) {
		geneCache.init();
		
//		CalculateGeneRanksOfGeneSummary g = new CalculateGeneRanksOfGeneSummary();
//		g.createGeneRank(10000132);
//		g.createGeneRank(10000133);
//		g.createGeneRank(10000134);
	}
	
	public void createGeneRank(Integer sampleId)
	{
		java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00000");
		
		Sample sample = sampleDAO.getBySampleId(sampleId);
		
		Collection<Gene> geneList = geneCache.genes();
		
		List<Gene> geneList1 = new ArrayList<Gene>();
		List<Integer> geneIds = new ArrayList<Integer>();
		for(Gene g : geneList)
		{
			if(geneIds.contains(g.getGeneId()) || g.getExonNum() == null)
			{
				continue;
			}
			geneList1.add(g);
			geneIds.add(g.getGeneId());
		}
		
		//数据库添加geneRank
		List<GeneRank> geneRanks = new ArrayList<GeneRank>();
		
		
		if(sample.getCell().contains("gene length"))
		{
			//排序 
			Collections.sort(geneList1, new Comparator<Gene>() {
				@Override
				public int compare(Gene o1, Gene o2) {
					return new Double(Math.abs(o1.getWidth())).compareTo(new Double(Math.abs(o2.getWidth()))) *(-1);
				}
			});
			
			for(Gene sr: geneList1){
				GeneRank gr = new GeneRank();
				gr.setCreatedTimestamp(System.currentTimeMillis());
				gr.setEtype(ExperimentType.SUMMARY_TRACK.value());
				gr.setSource(SourceType.SUMMARY.value());
				gr.setGeneId(sr.getGeneId());
				gr.setMixturePerc(Double.parseDouble(df.format((double)(geneList1.indexOf(sr)+1)/geneList1.size())));
				//Tsstescount读数
				gr.setTssTesCount(1.0*sr.getWidth());
				gr.setTotalCount(geneList1.size());
				gr.setSampleId(sampleId);
				geneRanks.add(gr);
			}
		}
		
		if(sample.getCell().contains("exon number"))
		{
			//排序 
			Collections.sort(geneList1, new Comparator<Gene>() {
				@Override
				public int compare(Gene o1, Gene o2) {
					return new Double(Math.abs(o1.getExonNum())).compareTo(new Double(Math.abs(o2.getExonNum()))) *(-1);
				}
			});
			
			for(Gene sr: geneList1){
				GeneRank gr = new GeneRank();
				gr.setCreatedTimestamp(System.currentTimeMillis());
				gr.setEtype(ExperimentType.SUMMARY_TRACK.value());
				gr.setSource(SourceType.SUMMARY.value());
				gr.setGeneId(sr.getGeneId());
				gr.setMixturePerc(Double.parseDouble(df.format((double)(geneList1.indexOf(sr)+1)/geneList1.size())));
				//Tsstescount读数
				gr.setTssTesCount(1.0*sr.getExonNum());
				gr.setTotalCount(geneList1.size());
				gr.setSampleId(sampleId);
				geneRanks.add(gr);
			}
		}
		
		if(sample.getCell().contains("exon length"))
		{
			//排序 
			Collections.sort(geneList1, new Comparator<Gene>() {
				@Override
				public int compare(Gene o1, Gene o2) {
					return new Double(Math.abs(o1.getExonLength())).compareTo(new Double(Math.abs(o2.getExonLength()))) *(-1);
				}
			});
			
			for(Gene sr: geneList1){
				GeneRank gr = new GeneRank();
				gr.setCreatedTimestamp(System.currentTimeMillis());
				gr.setEtype(ExperimentType.SUMMARY_TRACK.value());
				gr.setSource(SourceType.SUMMARY.value());
				gr.setGeneId(sr.getGeneId());
				gr.setMixturePerc(Double.parseDouble(df.format((double)(geneList1.indexOf(sr)+1)/geneList1.size())));
				//Tsstescount读数
				gr.setTssTesCount(1.0*sr.getExonLength());
				gr.setTotalCount(geneList1.size());
				gr.setSampleId(sampleId);
				geneRanks.add(gr);
			}
		}
		
		
		
		geneRankDAO.create(geneRanks);
	}

}
