package com.omicseq.robot.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.utils.DateUtils;

public class IlluminaBodayMap {
	private static String rootPath = "E:"+File.separator +"临时文件" + File.separator;
	private static ISampleDAO sampleNewDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
	private static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	private static ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);
	private static IGeneDAO	geneDAO = DAOFactory.getDAO(IGeneDAO.class);
	
	public static void main(String[] args) throws Exception {
		String filePath = rootPath + "gene.matrix.csv";
		GeneCache.getInstance().doInit();
		TxrRefCache.getInstance().doInit();
		readFile(filePath);
	}

	private static void readFile(String filePath) throws Exception {
		File file = new File(filePath);
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		
		List<String> tissues = new ArrayList<String>();
		List<String> symbolList = new ArrayList<String>();
		String readLine;
		String [] lineStrings;
		boolean flag = true;
		while((readLine = reader.readLine())!=null){
			lineStrings = readLine.split(",");
			//排除空行
			if(lineStrings.length>1){
				//读取第一行所有tissue
				if(flag){
					for(int i=1; i<lineStrings.length; i++){
						tissues.add(lineStrings[i]);
					}
					flag = false;
				}	
				symbolList.add(lineStrings[0]);
			}
		}
		
		//去掉第一个"GeneSymbol"
		symbolList.remove("GeneSymbol");
		
//		createSamples(tissues);
		
		createGeneRank(tissues, symbolList, filePath);
		
		reader.close();
	}

	private static void createGeneRank(List<String> tissues, List<String> symbolList, String filePath) throws Exception {
		File file = new File(filePath);
		
		//取所有geneId
		Map<String,Integer> geneIds = getGeneIde(symbolList);
		List<Double> column ;
		java.text.DecimalFormat df =new java.text.DecimalFormat("#.00000");
		//循环文件取读数
		String readLine;
		String [] lineStrings;
		for(int i=0; i<tissues.size(); i++){
			BufferedReader reader1 = new BufferedReader(new FileReader(file));
			column = new ArrayList<Double>();
			while((readLine = reader1.readLine())!=null){
				lineStrings = readLine.split(","); 
				//排除空行
				if(lineStrings.length>1 && !lineStrings[0].contains("GeneSymbol")){
					if(lineStrings.length>(i+1)){
						if(!"".equals(lineStrings[i+1]) && !lineStrings[i+1].equals("NaN")){
							column.add(Double.parseDouble(lineStrings[i+1]));
						}else if("".equals(lineStrings[i+1]) || "NaN".equals(lineStrings[i+1])){
							column.add(0.0);
						}
					}else{
						column.add(0.0);
					}
				}
			}
			List<SymbolReader> list = new ArrayList<SymbolReader>();
			for(int m=0; m<symbolList.size(); m++){
				SymbolReader sr = new SymbolReader();
				sr.setRead(column.get(m));
				sr.setSymbol(symbolList.get(m));
				list.add(sr);
			}
			//排序 
			Collections.sort(list, new Comparator<SymbolReader>() {
				@Override
				public int compare(SymbolReader o1, SymbolReader o2) {
					return o1.getRead().compareTo(o2.getRead()) *(-1);
				}
			});
			//数据库添加geneRank
			List<GeneRank> geneRanks = new ArrayList<GeneRank>();
			for(SymbolReader sr: list){
				if(geneIds.get(sr.getSymbol())!=null){
					GeneRank gr = new GeneRank();
					gr.setCreatedTimestamp(System.currentTimeMillis());
					gr.setEtype(ExperimentType.RNA_SEQ.value());
					gr.setSource(SourceType.ILLUMINA.value());
					gr.setGeneId(geneIds.get(sr.getSymbol()));
					gr.setMixturePerc(Double.parseDouble(df.format((double)(list.indexOf(sr)+1)/list.size())));
					gr.setTotalCount(list.size());
					gr.setSampleId(800001 + i);
					gr.setTssTesCount(sr.getRead());
					geneRanks.add(gr);
				}
			}
			geneRankDAO.create(geneRanks);
			
			reader1.close();
		}
	}
	
	/*
	 * 根据symbol 取所有geneid
	 */
	public static Map<String,Integer> getGeneIde(List<String> symbolList){
		Map<String,Integer> geneIds = new HashMap<String, Integer>();
		for(String symbol : symbolList){
			//根据symbol找对应的refseq
			List<TxrRef> txrRefList = TxrRefCache.getInstance().getTxrRefBySymbol(symbol.toLowerCase());
			if(txrRefList==null || txrRefList.size()== 0){
				continue;
			}else{
				for(TxrRef tr : txrRefList){
					String refseq = tr.getRefseq();
					if(refseq !=null && !"".equals(refseq)){
						//根据refseq对应gene表txName字段 找geneId
						Gene gene = GeneCache.getInstance().getGeneByName(refseq);
						if(gene != null){
							geneIds.put(symbol, gene.getGeneId());
							break;
						}
					}
				}
			}
		}
		return geneIds;
	}
	

	private static void createSamples(List<String> tissues) {
		for(String tissue : tissues)
		{
			Sample sample = new Sample();
			Integer sampleId = sampleDAO.getSequenceId(SourceType.ILLUMINA);
			sample.setSampleId(sampleId);
			sample.setEtype(ExperimentType.RNA_SEQ.getValue());
			sample.setSource(SourceType.ILLUMINA.getValue());
			sample.setCreateTiemStamp(DateUtils.getNowDate());
			sample.setDeleted(0);
			sample.setFactor("");
			sample.setCell(tissue);
			sample.setDescription("Tissue:"+tissue+";Tool:Cufflinks;Organism:Human BodyMap 2.0;BioSourceProvider:Human total RNA;Instrument model:Illumina HiSeq 2000;Contector:Follow @cureffi");
			sample.setLab("Eric Vallabh Minikel");
			sample.setReadCount(52687);
			sample.setUrl("http://www.cureffi.org/2013/07/11/tissue-specific-gene-expression-data-based-on-human-bodymap-2-0/");
			sample.setDetail(tissue + " Tumor");
			sampleNewDAO.create(sample);
		}
	}

}
