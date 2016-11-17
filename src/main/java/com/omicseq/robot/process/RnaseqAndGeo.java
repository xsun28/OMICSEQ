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

import org.apache.commons.lang3.StringUtils;
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

public class RnaseqAndGeo {
	
	private static String filePath = "E:/CCLE/GSE51783_normalized_gene_counts.txt";
	private static String metaDataFile = "E:/CCLE/GSE51783-GPL11154_series_matrix.txt";
	private static ISampleDAO samplenewDAO = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	private static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	private static ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);
	private static Logger logger = LoggerFactory.getLogger(CCLEParser.class); 
	
	
	//获取metadata 
	@SuppressWarnings("resource")
	public List<Integer> createSamples(List<String> barcode) throws IOException{
		List<Integer> samplesId = new ArrayList<Integer>(48);
		List<Sample> samples = new ArrayList<Sample>(48);
		File file = new File(metaDataFile);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String lineRead ;
		List<String> keys = new ArrayList<String>();
		List<Map<String,List<String>>> values = new ArrayList<Map<String,List<String>>>();
		
		while((lineRead = br.readLine()) != null){
			lineRead = lineRead.replaceAll("\"", "");
			String [] lines = lineRead.split("	");
			if(lines.length < 2 || !lines[0].startsWith("!Sample")){
				continue;
			}
			String key = "" ;
			List<String> value = new ArrayList<String>();
			if(lines[1].contains(":")){
				key = StringUtils.trimToEmpty(lines[1].split(":")[0]);
				for(int i=1; i<lines.length; i++){
					String v = StringUtils.trimToEmpty(lines[i].split(":")[1]);
					if(v.equals("http")){
						v = v +":" + lines[i].split(":")[2];
					}
					if(!StringUtils.isNoneEmpty(v)){
						continue;
					}
					value.add(v);
				}
				Map<String,List<String>> map = new HashMap<String, List<String>>();
				map.put(key, value);
				keys.add(key);
				values.add(map);
			}else{
				key = StringUtils.trimToEmpty(lines[0].substring(8));
				for(int i=1; i<lines.length; i++){
					if(!StringUtils.isNoneEmpty(lines[i])){
						continue;
					}
					value.add(StringUtils.trimToEmpty(lines[i]));
				}
			}
			Map<String,List<String>> map = new HashMap<String, List<String>>();
			map.put(key, value);
			keys.add(key);
			values.add(map);
		}
		for(int i=0;i<48;i++){
			Sample s = new Sample();
			Map<String, String> map = s.descMap();
			for(int j=0;j<keys.size();j++){
				map.put(keys.get(j),values.get(j).get(keys.get(j)).get(i) );
			}
			s.descMap(map);
			s.setCreateTiemStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
			s.setSampleId(sampleDAO.getSequenceId(SourceType.GEO));
			//s.setFactor("");
			//s.setCell("");
			s.setCreateTiemStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
			s.setSource(SourceType.GEO.value());
			s.setEtype(ExperimentType.RNA_SEQ.value());
			//s.setUrl("");
			s.setLab("University of North Carolina at Chapel Hill");
			s.setDeleted(0);
			s.setSampleCode(barcode.get(i));
			samplesId.add(s.getSampleId());
			samples.add(s);
		}
		samplenewDAO.create(samples);
		return samplesId;
		
	}
	
	public void read(String filePath) throws IOException{
		File file = new File(filePath);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String lineRead ;
		List<String > barCodes = new ArrayList<String>();
		List<String > symbols = new ArrayList<String>();
		List<Integer> sid = new ArrayList<Integer>();
		boolean flag = true;
		while((lineRead=br.readLine())!=null){
			String [] titles = lineRead.split("	");
			if(titles.length < 2 ){
				continue;
			}
			if(flag){
				for(int i=3; i<titles.length; i++){
					barCodes.add(titles[i]);
				}
				flag = false;
				sid = createSamples(barCodes);
			}
			symbols.add(titles[2]);
		}
		symbols.remove("Symbol");
		Map<String,Integer> geneIds = getGeneIde(symbols);
		
		List<Double> column;
		for(int i=0; i<barCodes.size(); i++){
			boolean f = true;
			@SuppressWarnings("resource")
			BufferedReader reader1 = new BufferedReader(new FileReader(file));
			column = new ArrayList<Double>();
			while((lineRead = reader1.readLine())!=null){
				if(f){
					f = false;
					continue;
				}
				String []lineStrings = lineRead.split("	"); 
				//排除空行
				if(lineStrings.length>1){
					if(lineStrings.length>(i+3)){
						if(!"".equals(lineStrings[i+3]) && !lineStrings[i+3].contains("TCGA") && !lineStrings[i+3].equals("NaN")){
							column.add(Double.parseDouble(lineStrings[i+3]));
						}else if("".equals(lineStrings[i+3]) || "NaN".equals(lineStrings[i+3])){
							column.add(0.0);
						}
					}else{
						column.add(0.0);
					}
				}
			}
			List<SymbolReader> list = new ArrayList<SymbolReader>();
			//
			for(int m=0; m<symbols.size(); m++){
				SymbolReader sr = new SymbolReader();
				sr.setRead(column.get(m));
				sr.setBarCode(barCodes.get(i));
				sr.setSymbol(symbols.get(m));
				list.add(sr);
			}
			//去掉无效数据（symbol对应不到gene的数据）
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
					gr.setEtype(ExperimentType.RNA_SEQ.value());
					gr.setSource(SourceType.GEO.value());
					gr.setGeneId(geneIds.get(sr.getSymbol()));
					gr.setMixturePerc(Double.parseDouble(df.format((double)(list.indexOf(sr)+1)/list.size())));
					//Tsstescount读数
					gr.setTssTesCount(sr.getRead());
					gr.setTotalCount(list.size());
					gr.setSampleId(sid.get(i));
					geneRanks.add(gr);
				}
			}
			geneRankDAO.create(geneRanks);
		}	
		
	}
	
	public Map<String,Integer> getGeneIde(List<String> symbolList){
		Map<String,Integer> geneIds = new HashMap<String, Integer>();
		for(String symbol : symbolList){
			//根据symbol找对应的refseq
			//List<TxrRef> txrRefList = txrRefDAO.findByGeneSymbol(symbol);
			List<TxrRef> txrRefList = TxrRefCache.getInstance().getTxrRefBySymbol(symbol.toLowerCase());
			if(txrRefList ==null ||txrRefList.size()== 0){
				//txrref表找不到对应的refseq 记录下来
				geneIds.put(symbol, null);
			}else{
				boolean flag = true; 
				for(TxrRef tr : txrRefList){
					String refseq = tr.getRefseq();
					if(refseq !=null && !"".equals(refseq)){
						//根据refseq对应gene表txName字段 找geneId
						//Gene gene = geneDAO.getByName(refseq); 
						Gene gene = GeneCache.getInstance().getGeneByName(refseq);
						if(gene != null){
							geneIds.put(symbol, gene.getGeneId());
							flag = false;
							break;
						}
					}
				}
				if(flag){
					geneIds.put(symbol, null);
				}
			}
		}
		return geneIds;
	}
	
	public static void main(String[] args) throws IOException {
		TxrRefCache.getInstance().init();
		GeneCache.getInstance().init();
		new RnaseqAndGeo().read(filePath);
	}

}
