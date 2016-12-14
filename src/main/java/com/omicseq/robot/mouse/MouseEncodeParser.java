package com.omicseq.robot.mouse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
import com.omicseq.core.MouseGeneCache;
import com.omicseq.core.MouseTxrRefCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.domain.TxrRef;
import com.omicseq.robot.process.MethylationParser;
import com.omicseq.robot.process.SymbolReader;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class MouseEncodeParser {

//	private static String filePath = "E://mouse//geneexp_Jan6th.xls";
	private static String filePath = "/home/tomcat/mouse/geneexp_Jan6th.xls";
	protected static ISampleDAO sampleDao = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	protected static ITxrRefDAO txrRefDAO = DAOFactory.getDAOByTableType(ITxrRefDAO.class, "mouse");
	protected static IGeneDAO geneDAO = DAOFactory.getDAOByTableType(IGeneDAO.class, "mouse");
	private static Logger logger = LoggerFactory.getLogger(MethylationParser.class);
	
	public static void main(String[] args) {
		MouseTxrRefCache.getInstance().doInit();
		MouseGeneCache.getInstance().doInit();
		MouseEncodeParser parser = new MouseEncodeParser();
		parser.createGeneRanks(filePath);
	}

	private void createGeneRanks(String filePath) {
		java.text.DecimalFormat df =new java.text.DecimalFormat("#.00000");
		List<String> symbolList = new ArrayList<String>();
		File file = new File(filePath);
		String [] lineStrings;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String readLine;
			List<String> barCodes = new ArrayList<String>();
			boolean flag = true;
			//第一次读取文件拿到所有barcode添加sample，拿到所有symbol
			while((readLine = reader.readLine())!=null){
				lineStrings = readLine.split("	");
				//排除空行
				if(lineStrings.length>1){
					if(flag){
						for(int i=0; i<lineStrings.length; i++){
							if(lineStrings[i].contains("wgEncode")){
								barCodes.add(lineStrings[i]);
							}
						}
						flag = false;
					}	
					symbolList.add(lineStrings[0]);
				}
			}
			//去掉第一个"GeneSymbol"
			symbolList.remove("genename");
			//取所有geneId
			Map<String,Integer> geneIds = getGeneIde(symbolList);
			//循环methylation文件取读数
			List<Double> column ;
			for(int i=0; i<barCodes.size(); i++){
				String barCode = barCodes.get(i);
				if(barCode.contains("wgEncodePsu"))
				{
					continue;
				}
				SmartDBObject query = new SmartDBObject();
				query.put("source", 2);
				query.put("etype", 2);
				query.put("url", new SmartDBObject("$regex", barCode));
				Sample sample = sampleDao.findOne(query);
				if(sample == null || sample.getDeleted() == 0)
				{
					continue;
				}
				Integer sampleId = sample.getSampleId();
				
				BufferedReader reader1 = new BufferedReader(new FileReader(file));
				column = new ArrayList<Double>();
				while((readLine = reader1.readLine())!=null){
					if(readLine.contains("genename"))
					{
						continue;
					}
					lineStrings = readLine.split("	"); 
					//排除空行
					if(lineStrings.length>1){
						if(lineStrings.length>(i+4)){
							if(!"".equals(lineStrings[i+4]) && !lineStrings[i+4].contains("wgEncode") && !lineStrings[i+4].equals("NA")){
								column.add(Double.parseDouble(lineStrings[i+4]));
							}else if("".equals(lineStrings[i+4]) || "NA".equals(lineStrings[i+4])){
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
					sr.setBarCode(barCodes.get(i));
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
						gr.setSource(SourceType.ENCODE.value());
						gr.setGeneId(geneIds.get(sr.getSymbol()));
						gr.setFromType(2);
						gr.setMixturePerc(Double.parseDouble(df.format((double)(list.indexOf(sr)+1)/list.size())));
						gr.setTotalCount(list.size());
						gr.setSampleId(sampleId);
						gr.setTssTesCount(sr.getRead());
						geneRanks.add(gr);
					}
				}
				geneRankDAO.create(geneRanks);
				sample.setDeleted(0);
				sampleDao.update(sample);
				logger.debug("sampleId:{}", sampleId);
			}
			
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/*
	 * 根据symbol 取所有geneid
	 */
	public Map<String,Integer> getGeneIde(List<String> symbolList){
		Map<String,Integer> geneIds = new HashMap<String, Integer>();
		for(String symbol : symbolList){
			//根据symbol找对应的refseq
			//List<TxrRef> txrRefList = txrRefDAO.findByGeneSymbol(symbol);
			List<TxrRef> txrRefList = MouseTxrRefCache.getInstance().getTxrRefBySymbol(symbol.toLowerCase());
			if(txrRefList == null)
			{
				continue;
			}
			for(TxrRef tr : txrRefList){
				String refseq = tr.getRefseq();
				if(refseq !=null && !"".equals(refseq)){
					Gene gene = MouseGeneCache.getInstance().getGeneByName(refseq);
					if(gene != null){
						geneIds.put(symbol, gene.getGeneId());
						break;
					}
				}
			}
		
		}
		return geneIds;
	}

}
