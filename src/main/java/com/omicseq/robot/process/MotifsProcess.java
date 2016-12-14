package com.omicseq.robot.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.core.GeneCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.utils.DateUtils;

public class MotifsProcess {
	private ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);
	private ISampleDAO sampleNewDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
	private IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	DecimalFormat   df   = new   DecimalFormat("#.00000");
	private static Logger logger = LoggerFactory.getLogger(MotifsProcess.class);
	
	private static File file = new File("/home/tomcat/TF.motif.csv");

	public static void main(String[] args) {
		GeneCache.getInstance().doInit();
		new MotifsProcess().parse(file);
	}

	private void parse(File file) {	
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line = null;
			boolean flag = true;
			List<Integer> sampleIds = new ArrayList<Integer>();
			Map<String , Integer > geneIds = new HashMap<String, Integer>();
			while((line = br.readLine()) != null)
			{
				String [] title = line.split(",");
				if(flag){
					List<String> titles = new ArrayList<String>(title.length);
					for(int i=1; i<title.length;i++){
						titles.add(title[i]);
					}
					sampleIds = createSamples(titles);
					flag = false;
					continue;
				}
				if(!flag) {
					String refseq = title[0];
					if(refseq !=null && !"".equals(refseq)){
						Gene gene = GeneCache.getInstance().getGeneByName(refseq);
						if(gene != null){
							geneIds.put(gene.getTxName(), gene.getGeneId());
						}
					}
				}
			}
			br.close();
			createGeneRanks(sampleIds,geneIds);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createGeneRanks(List<Integer> sampleIds,
			Map<String, Integer> geneIds)  throws Exception {
		for(int i = 0;i<sampleIds.size();i++){
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			boolean flag = true;
			List<SymbolReader> list = new ArrayList<SymbolReader>();
			while((line = br.readLine())!=null){
				if(flag){
					flag = false;
					continue;
				}
				String [] lines = line.split(",");
				SymbolReader sr = new SymbolReader();
				sr.setRead(Double.parseDouble(lines[i+1]));
				sr.setSymbol(lines[0]);
				list.add(sr);
			}
			
//			for(int k=0; k<list.size(); k++){
//				if(geneIds.get(list.get(k).getSymbol())==null){
//					list.remove(k);
//					k--;
//				}
//			}
			//排序 
			Collections.sort(list, new Comparator<SymbolReader>() {
				@Override
				public int compare(SymbolReader o1, SymbolReader o2) {
					return o1.getRead().compareTo(o2.getRead()) *(-1);
				}
			});
		
			List<GeneRank> geneRanks = new ArrayList<GeneRank>();
			for(SymbolReader sr: list){
				if(geneIds.get(sr.getSymbol())!=null){
					GeneRank gr = new GeneRank();
					gr.setCreatedTimestamp(System.currentTimeMillis());
					gr.setEtype(ExperimentType.MOTIFS.value());
					gr.setSource(SourceType.JASPAR.value());
					gr.setGeneId(geneIds.get(sr.getSymbol()));
					gr.setMixturePerc(Double.parseDouble(df.format((double)(list.indexOf(sr)+1)/list.size())));
					//Tsstescount读数
					gr.setTssTesCount(sr.getRead());
					gr.setTotalCount(list.size());
					gr.setSampleId(sampleIds.get(i));
					geneRanks.add(gr);
				}
			}
			geneRankDAO.create(geneRanks);
			logger.debug("Current sample :{}", sampleIds.get(i) + " i:"+ i);
			br.close();
		}
		logger.debug("motif is finished");
	}

	private List<Integer> createSamples(List<String> titles) {
		List<Sample> sampleList = new ArrayList<Sample>();
		List<Integer> sampleIds = new ArrayList<Integer>();
		for(String sampleCode : titles){
			Sample sample = new Sample();
			sample.setDeleted(0);
			sample.setCell("");
			sample.setSampleCode(sampleCode);
			sample.setCreateTiemStamp(DateUtils.getNowDate());
			sample.setDetail("normal " + sampleCode.split("__")[1]);
			sample.setFactor(sampleCode.split("__")[1]);
			sample.setLab("serendi");
			sample.setSource(SourceType.JASPAR.value());
			sample.setEtype(ExperimentType.MOTIFS.value());
			sample.setSampleId(sampleDAO.getSequenceId(SourceType.JASPAR));
			sample.setUrl("http://112.25.20.156/download/TF.motif.csv");
			sample.setPubmedUrl("");
			
			sampleList.add(sample);
			sampleIds.add(sample.getSampleId());
		}
		sampleNewDAO.create(sampleList);
		return sampleIds;
	}

}
