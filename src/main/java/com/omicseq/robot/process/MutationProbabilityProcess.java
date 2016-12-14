package com.omicseq.robot.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.utils.DateUtils;
import com.omicseq.utils.ExcelReader;

public class MutationProbabilityProcess {
	
	private static String rootPath = "E:"+File.separator +"临时文件" + File.separator;
	private static ISampleDAO sampleNewDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
	private static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	private static ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);
	private static IGeneDAO	geneDAO = DAOFactory.getDAO(IGeneDAO.class);

	public static void main(String[] args) {
		String filePath = rootPath + "probMutation.xls";
		readFile(filePath);
	}

	private static void readFile(String filePath) {
//		File file = new File(filePath);
		ExcelReader excelReader = new ExcelReader();  
		InputStream is2;
		try {
			is2 = new FileInputStream(filePath);
			Map<Integer,String> map = excelReader.readExcelContent(is2);
//			String title  = map.get(0);
//			createSamples(title);
			List<GeneRank> allList = new ArrayList<GeneRank>();
			List<GeneRank> synList = new ArrayList<GeneRank>();
			List<GeneRank> misList = new ArrayList<GeneRank>();
			List<GeneRank> nonList = new ArrayList<GeneRank>();
			List<GeneRank> splice_siteList = new ArrayList<GeneRank>();
			List<GeneRank> frame_shiftList = new ArrayList<GeneRank>();
			for(int i=1; i<map.size(); i++)
			{
				String row = map.get(i);
                if(row != null && !"@".equals(row) && row.indexOf("null") == -1 && row.split("@").length > 1)
                {
                	GeneRank grAll = new GeneRank();
                	GeneRank grsyn = new GeneRank();
                	GeneRank grmis = new GeneRank();
                	GeneRank grnon = new GeneRank();
                	GeneRank grsplice_site = new GeneRank();
                	GeneRank grframe_shift = new GeneRank();
                	String[] values = row.split("@");
                	String txName = values[0];
                	Gene gene = geneDAO.getByName(txName);
                	if(gene == null)
                	{
                		continue;
                	}
                	Integer geneId = gene.getGeneId();
                	Double all = values[3].equals("NA")?0:Double.valueOf(values[3]);
                	Double syn = values[4].equals("NA")?0:Double.valueOf(values[4]);
                	Double mis = values[5].equals("NA")?0:Double.valueOf(values[5]);
                	Double non = values[6].equals("NA")?0:Double.valueOf(values[6]);
                	Double splice_site = values[7].equals("NA")?0:Double.valueOf(values[7]);
                	Double frame_shift = values[8].equals("NA")?0:Double.valueOf(values[8]);
                	
                	grAll.setGeneId(geneId);
                	grAll.setSampleId(10000514);
                	grAll.setCreatedTimestamp(System.currentTimeMillis());
                	grAll.setSource(SourceType.SUMMARY.getValue());
                	grAll.setTotalCount(23376);
                	grAll.setTssTesCount(Math.abs(all));
                	grAll.setEtype(ExperimentType.SUMMARY_TRACK.getValue());
                	allList.add(grAll);
                	
                	grsyn.setGeneId(geneId);
                	grsyn.setSampleId(10000515);
                	grsyn.setCreatedTimestamp(System.currentTimeMillis());
                	grsyn.setSource(SourceType.SUMMARY.getValue());
                	grsyn.setTotalCount(23376);
                	grsyn.setTssTesCount(Math.abs(syn));
                	grsyn.setEtype(ExperimentType.SUMMARY_TRACK.getValue());
                	synList.add(grsyn);
                	
                	grmis.setGeneId(geneId);
                	grmis.setSampleId(10000516);
                	grmis.setCreatedTimestamp(System.currentTimeMillis());
                	grmis.setSource(SourceType.SUMMARY.getValue());
                	grmis.setTotalCount(23376);
                	grmis.setTssTesCount(Math.abs(mis));
                	grmis.setEtype(ExperimentType.SUMMARY_TRACK.getValue());
                	misList.add(grmis);
                	
                	grnon.setGeneId(geneId);
                	grnon.setSampleId(10000517);
                	grnon.setCreatedTimestamp(System.currentTimeMillis());
                	grnon.setSource(SourceType.SUMMARY.getValue());
                	grnon.setTotalCount(23376);
                	grnon.setTssTesCount(Math.abs(non));
                	grnon.setEtype(ExperimentType.SUMMARY_TRACK.getValue());
                	nonList.add(grnon);
                	
                	grsplice_site.setGeneId(geneId);
                	grsplice_site.setSampleId(10000518);
                	grsplice_site.setCreatedTimestamp(System.currentTimeMillis());
                	grsplice_site.setSource(SourceType.SUMMARY.getValue());
                	grsplice_site.setTotalCount(23376);
                	grsplice_site.setTssTesCount(Math.abs(splice_site));
                	grsplice_site.setEtype(ExperimentType.SUMMARY_TRACK.getValue());
                	splice_siteList.add(grsplice_site);
                	
                	grframe_shift.setGeneId(geneId);
                	grframe_shift.setSampleId(10000519);
                	grframe_shift.setCreatedTimestamp(System.currentTimeMillis());
                	grframe_shift.setSource(SourceType.SUMMARY.getValue());
                	grframe_shift.setTotalCount(23376);
                	grframe_shift.setTssTesCount(Math.abs(frame_shift));
                	grframe_shift.setEtype(ExperimentType.SUMMARY_TRACK.getValue());
                	frame_shiftList.add(grframe_shift);
                	
                	System.out.println(txName + " is over");
                }
			}
			
			Collections.sort(allList, new Comparator<GeneRank>() {
				@Override
				public int compare(GeneRank o1, GeneRank o2) {
					//return o1.getRead().compareTo(o2.getRead()) *(-1);
					return new Double(Math.abs(o1.getTssTesCount())).compareTo(new Double(Math.abs(o2.getTssTesCount()))) *(-1);
				}
			});
			
			Collections.sort(synList, new Comparator<GeneRank>() {
				@Override
				public int compare(GeneRank o1, GeneRank o2) {
					//return o1.getRead().compareTo(o2.getRead()) *(-1);
					return new Double(Math.abs(o1.getTssTesCount())).compareTo(new Double(Math.abs(o2.getTssTesCount()))) *(-1);
				}
			});
			
			Collections.sort(misList, new Comparator<GeneRank>() {
				@Override
				public int compare(GeneRank o1, GeneRank o2) {
					//return o1.getRead().compareTo(o2.getRead()) *(-1);
					return new Double(Math.abs(o1.getTssTesCount())).compareTo(new Double(Math.abs(o2.getTssTesCount()))) *(-1);
				}
			});
			
			Collections.sort(nonList, new Comparator<GeneRank>() {
				@Override
				public int compare(GeneRank o1, GeneRank o2) {
					//return o1.getRead().compareTo(o2.getRead()) *(-1);
					return new Double(Math.abs(o1.getTssTesCount())).compareTo(new Double(Math.abs(o2.getTssTesCount()))) *(-1);
				}
			});
			
			Collections.sort(splice_siteList, new Comparator<GeneRank>() {
				@Override
				public int compare(GeneRank o1, GeneRank o2) {
					//return o1.getRead().compareTo(o2.getRead()) *(-1);
					return new Double(Math.abs(o1.getTssTesCount())).compareTo(new Double(Math.abs(o2.getTssTesCount()))) *(-1);
				}
			});
			
			Collections.sort(frame_shiftList, new Comparator<GeneRank>() {
				@Override
				public int compare(GeneRank o1, GeneRank o2) {
					//return o1.getRead().compareTo(o2.getRead()) *(-1);
					return new Double(Math.abs(o1.getTssTesCount())).compareTo(new Double(Math.abs(o2.getTssTesCount()))) *(-1);
				}
			});
			
			createGeneRank(allList);
			createGeneRank(synList);
			createGeneRank(misList);
			createGeneRank(nonList);
			createGeneRank(splice_siteList);
			createGeneRank(frame_shiftList);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void createGeneRank(List<GeneRank> list) {
		java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00000");  
		for(int i=0; i<list.size(); i++)
		{
			GeneRank gr = list.get(i);
			gr.setMixturePerc(Double.parseDouble(df.format((double)(i+1)/23376)));
		}
		
		geneRankDAO.create(list);
	}

	private static void createSamples(String title) {
		String[] titleArray = title.split("@");
		for(int i=4; i<titleArray.length; i++)
		{
			String type = "Mutation probability(" + titleArray[i] + ")";
			Sample sample = new Sample();
			sample.setSource(SourceType.SUMMARY.getValue());
			sample.setEtype(ExperimentType.SUMMARY_TRACK.getValue());
			sample.setDeleted(0);
			sample.setSettype("Mutation probability");
			sample.setCell(type);
			sample.setDetail("");
			sample.setLab("Broad");
			sample.setPubmedUrl("http://www.ncbi.nlm.nih.gov/pubmed/25086666");
			sample.setUrl("http://www.nature.com/ng/journal/v46/n9/full/ng.3050.html#supplementary-information");
			sample.setReadCount(23376);
			sample.setSampleId(sampleDAO.getSequenceId(SourceType.TCGA));
			sample.setCreateTiemStamp(DateUtils.getNowDate());
			
			sampleNewDAO.create(sample);
		}
	}

}
