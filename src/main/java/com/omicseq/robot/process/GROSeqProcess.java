package com.omicseq.robot.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.core.GeneCache;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.utils.DateUtils;

public class GROSeqProcess {
//	private ITxrRefDAO txrRefDAO = DAOFactory.getDAO(ITxrRefDAO.class);
//	private IGeneDAO geneDAO = DAOFactory.getDAO(IGeneDAO.class);
	private ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);
	private ISampleDAO sampleNewDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
	private IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	
	DecimalFormat   df   =new   DecimalFormat("#.00000");  
	
	public void parse(){
		Sample sample = new Sample();
		sample.setDeleted(0);
		sample.setCell("breast cancer cells");
		sample.setSampleCode("GSE41324");
		sample.setCreateTiemStamp(DateUtils.getNowDate());
		sample.setDetail("heart normal MCF7");
		sample.setLab("Kraus Lab");
		sample.setSource(SourceType.GEO.value());
		sample.setEtype(ExperimentType.GRO_SEQ.value());
		sample.setSampleId(sampleDAO.getSequenceId(SourceType.GEO));
		sampleNewDAO.create(sample);
		
		File file = new File("E:\\GeneLevel\\MCF7PI.quantNorm.Rflat.txt");
		try {
			List<GeneRank> geneRankList = new ArrayList<GeneRank>();
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line = null;
//			Integer row = 1;
			while((line = br.readLine()) != null){
//				if(1 == row++) continue;
				if(line.contains("GeneID")) continue;
				String [] tmp = line.split("\t");
				String geneInfo = tmp[1];
				
				Integer geneId = 0;
				if(geneInfo.split("_").length > 2)
				{
					geneId = GeneCache.getInstance().getGeneIdStartAndEnd(Integer.parseInt(geneInfo.split("_")[1])-1, Integer.parseInt(geneInfo.split("_")[2]), geneInfo.split("_")[0]);
				} else {
					geneId = GeneCache.getInstance().getGeneByName(geneInfo) == null?null:GeneCache.getInstance().getGeneByName(geneInfo).getGeneId();
				}
				
				if(geneId == null || geneId == 0) continue;
				
				String rpkmCount = tmp[3];
				if("NA".equals(rpkmCount))
				{
					rpkmCount = "0";
				}
				Double promoterRPKM = Double.parseDouble(rpkmCount);
				
				GeneRank geneRank = new GeneRank();
				geneRank.setCreatedTimestamp(System.currentTimeMillis());
				geneRank.setSampleId(sample.getSampleId());
				geneRank.setGeneId(geneId);
				geneRank.setEtype(ExperimentType.GRO_SEQ.getValue());
				geneRank.setSource(SourceType.GEO.getValue());
				geneRank.setTssTesCount(Double.parseDouble(df.format(promoterRPKM)));
				geneRankList.add(geneRank);
			}
			
			br.close();
			
			Collections.sort(geneRankList, new Comparator<GeneRank>() {

				@Override
				public int compare(GeneRank g1, GeneRank g2) {
					return (g1.getTssTesCount().compareTo(g2.getTssTesCount())) * (-1);
				}
			});;
			
			for(GeneRank gr : geneRankList){
				gr.setTotalCount(geneRankList.size());
				gr.setMixturePerc(Double.parseDouble(df.format((double)(geneRankList.indexOf(gr)+1)/geneRankList.size())));
			}
			
			geneRankDAO.create(geneRankList);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
//		TxrRefCache.getInstance().doInit();
		GeneCache.getInstance().doInit();
		new GROSeqProcess().parse();
	}

}
