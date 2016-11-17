package com.omicseq.robot.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

public class CCLEParser {
	private static String filePath = "/home/CCLE_Expression_Entrez_2012-09-29.gct";
	private static String sampleDescription = "/home/CCLE_sample_info_file_2012-10-18.txt";
//	private static String sampleDescription = "E:/CCLE/CCLE_sample_info_file_2012-10-18.txt";
//	private static String filePath = "E:/CCLE/CCLE_Expression_Entrez_2012-09-29.gct";
	private static ISampleDAO samplenewDAO = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	private static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	private static ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);
	private static Logger logger = LoggerFactory.getLogger(CCLEParser.class);


	public void parser(String filePath) throws IOException{
		File file = new File(filePath);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		boolean flag = true;
		List<Integer> sampleIds = new ArrayList<Integer>();
		Map<String , Integer > geneIds = new HashMap<String, Integer>();
		//建立sample和所有geneId
		while((line = br.readLine())!=null){
			String [] title = line.split("	");
			if(flag){
				List<String> titles = new ArrayList<String>();
				for(int i=2; i<title.length;i++){
					titles.add(title[i]);
				}
				sampleIds = createSamples(titles);
				flag = false;
				continue;
			}
			String symbol = title[1];
			List<TxrRef> txrRefList = TxrRefCache.getInstance().getTxrRefBySymbol(symbol.toLowerCase());
			if(txrRefList ==null ||txrRefList.size()== 0){
				geneIds.put(symbol, null);
			}else{
				boolean tmp = true; 
				for(TxrRef tr : txrRefList){
					String refseq = tr.getRefseq();
					if(refseq !=null && !"".equals(refseq)){
						Gene gene = GeneCache.getInstance().getGeneByName(refseq);
						if(gene != null){
							geneIds.put(symbol, gene.getGeneId());
							tmp = false;
							break;
						}
					}
				}
				if(tmp){
					geneIds.put(symbol, null);
				}
			}
		}
		
		read(sampleIds,geneIds);
	}
	
	public void read(List<Integer> sampleIds,Map<String,Integer> geneIds) throws IOException{
		for(int i = 1;i<=sampleIds.size();i++){
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line = "";
			boolean flag = true;
			List<SymbolReader> list = new ArrayList<SymbolReader>();
			while((line = br.readLine())!=null){
				if(flag){
					flag = false;
					continue;
				}
				String [] lines = line.split("	");
				SymbolReader sr = new SymbolReader();
				sr.setRead(Double.parseDouble(lines[i+1]));
				//System.out.println(Double.parseDouble(lines[i+1])+"__"+lines[1]);
				sr.setSymbol(lines[1]);
				list.add(sr);
			}
			//paixu
			for(int k=0; k<list.size(); k++){
				if(geneIds.get(list.get(k).getSymbol())==null){
					list.remove(k);
					k--;
				}
			}
			//排序 
			Collections.sort(list, new Comparator<SymbolReader>() {
				@Override
				public int compare(SymbolReader o1, SymbolReader o2) {
					//return o1.getRead().compareTo(o2.getRead()) *(-1);
					return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1);
				}
			});
		
			List<GeneRank> geneRanks = new ArrayList<GeneRank>();
			java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00000"); 
			for(SymbolReader sr: list){
				if(geneIds.get(sr.getSymbol())!=null){
					GeneRank gr = new GeneRank();
					gr.setCreatedTimestamp(System.currentTimeMillis());
					gr.setEtype(ExperimentType.MICROARRAY.value());
					gr.setSource(SourceType.CCLE.value());
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
			logger.debug("Current sample :{}", sampleIds.get(i) + " i:"+i);
		}
		logger.debug("CCLE is finished");
	}
	
	
	public List<Integer> createSamples(List<String> titles) throws IOException{
		List<Sample> sampleList = new ArrayList<Sample>();
		List<Integer> sampleIds = new ArrayList<Integer>();
		for(String sampleCode : titles){
			Sample sample = description(sampleCode);
			sampleIds.add(sample.getSampleId());
			sampleList.add(sample);
		}
		samplenewDAO.create(sampleList);
		return sampleIds;
	}
	
	public Sample description(String sampleCode) throws IOException{
		File file = new File(sampleDescription);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = "";
		boolean flag = true;
		String [] keys = null ;
		Sample sample = new Sample();
		while((line = br.readLine())!=null){
			String [] titles = line.split("	");
			if(flag){
				keys = titles;
				flag = false;
				continue;
			}
			if(line.startsWith(sampleCode)){
				
				String cell = titles[1];
				sample.setCell(cell);
				//System.out.println(sampleCode+" cell:"+cell +keys.length + ":"+titles.length );
				Map<String, String> map = sample.descMap();
				for(int i=3; i<titles.length; i++){
					map.put(keys[i], titles[i]);
				}
				sample.descMap(map);
			}
			
		}
		int sampleId = sampleDAO.getSequenceId(SourceType.CCLE);
		sample.setSampleId(sampleId);
		sample.setSampleCode(sampleCode);
		sample.setSource(SourceType.CCLE.value());
		sample.setEtype(ExperimentType.MICROARRAY.value());
		sample.setCreateTiemStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
		sample.setUrl("http://www.broadinstitute.org/ccle/downloadFile/DefaultSystemRoot/exp_10/ds_21/CCLE_Expression_Entrez_2012-09-29.gct?downloadff=true&fileId=6763");
		//deleted
		sample.setDeleted(0);
		return sample;
	}
	public static void main(String[] args) throws IOException {
		TxrRefCache.getInstance().init();
		GeneCache.getInstance().init();
		new CCLEParser().parser(filePath);
		//new CCLEParser().description(sampleCode)
	}

}
