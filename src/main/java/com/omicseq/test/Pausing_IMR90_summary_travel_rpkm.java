package com.omicseq.test;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.core.GeneCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;

public class Pausing_IMR90_summary_travel_rpkm {
	private ITxrRefDAO txrRefDAO = DAOFactory.getDAO(ITxrRefDAO.class);
	private IGeneDAO geneDAO = DAOFactory.getDAO(IGeneDAO.class);
	private ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);
	private ISampleDAO sampleNewDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
	private IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	
	DecimalFormat   df   =new   DecimalFormat("#.00000");  
	public Map<String,Integer> getGeneIds(){
		Map<String, Integer> map = new HashMap<String, Integer>();
		File file = new File("C:/Users/Administrator/Desktop/active.genes.bed.IMR90.gz");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
			String line = null;
			int row = 1;
			while((line = br.readLine()) != null){
				String [] tmp = line.split("\t");
				String g = tmp[3];
				String symbol = tmp[4];
				Gene gene = getGeneId(symbol);
				if(gene == null) continue;
				map.put(g, gene.getGeneId());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}
	
	public Gene getGeneId(String symbol){
		//根据symbol找对应的refseq
		//List<TxrRef> txrRefList = txrRefDAO.findByGeneSymbol(symbol);
	List<TxrRef> txrRefList = TxrRefCache.getInstance().getTxrRefBySymbol(symbol.toLowerCase());
	if(txrRefList ==null ||txrRefList.size()== 0){
	}else{
		boolean flag = true; 
		for(TxrRef tr : txrRefList){
			String refseq = tr.getRefseq();
			if(refseq !=null && !"".equals(refseq)){
					//根据refseq对应gene表txName字段 找geneId
					//Gene gene = geneDAO.getByName(refseq); 
				Gene gene = GeneCache.getInstance().getGeneByName(refseq);
				if(gene != null){
					return gene;
				}
			}
		}
	}
		return null;
	}
	
	public void parse(){
		Sample sample = new Sample();
		sample.setDeleted(0);
		sample.setCell("IMR: IMR 90 cells (before transient TNF-α stimulation.)");
		sample.setSampleCode("GSE43070");
		sample.setCreateTiemStamp("2015-01-30");
		sample.setDetail("lung normal");
		sample.setSource(SourceType.GEO.value());
		sample.setEtype(ExperimentType.GRO_SEQ.value());
		sample.setSampleId(sampleDAO.getSequenceId(SourceType.GEO));
		sampleNewDAO.create(sample);
		Map<String, Integer> geneIdMap = getGeneIds();
		File file = new File("C:/Users/Administrator/Desktop/IMR90.summary.travel.rpkm.gz");
		try {
			List<GeneRank> geneRankList = new ArrayList<GeneRank>();
			BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
			String line = null;
			Integer row = 1;
			while((line = br.readLine()) != null){
				if(1 == row++) continue;
				String [] tmp = line.split("\t");
				String key = tmp[0];
				Double promoterRPKM = Double.parseDouble(tmp[1]);
				Double genebodyRPKM = Double.parseDouble(tmp[2]);
				Integer geneId = geneIdMap.get(key);
				if(geneId == null) continue;
				GeneRank geneRank = new GeneRank();
				geneRank.setCreatedTimestamp(System.currentTimeMillis());
				//geneRank.setSampleId(sample.getSampleId());
				geneRank.setSampleId(614044);
				geneRank.setGeneId(geneId);
				geneRank.setEtype(ExperimentType.GRO_SEQ.getValue());
				geneRank.setSource(SourceType.GEO.getValue());
				Double read = (promoterRPKM + 0.5)/(genebodyRPKM + 0.5);
				geneRank.setTssTesCount(Double.parseDouble(df.format(read)));
				geneRankList.add(geneRank);
			}
			
			Collections.sort(geneRankList, new Comparator<GeneRank>() {

				@Override
				public int compare(GeneRank g1, GeneRank g2) {
					return (g1.getTssTesCount().compareTo(g2.getTssTesCount())) * (-1);
				}
			});;
			
			for(GeneRank gr : geneRankList){
				gr.setTotalCount(32745);
				gr.setMixturePerc(Double.parseDouble(df.format((double)(geneRankList.indexOf(gr)+1)/32745)));
			}
			//sampleNewDAO.create(sample);
			geneRankDAO.create(geneRankList);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
//		TxrRefCache.getInstance().doInit();
//		GeneCache.getInstance().doInit();
		new Pausing_IMR90_summary_travel_rpkm().parse();
	}

}
