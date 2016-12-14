package com.omicseq.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import com.omicseq.robot.process.CCLEParser;
import com.omicseq.robot.process.SymbolReader;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.utils.DateUtils;

public class GSE59688Paser {

	private static String filePath = "E:/CCLE/GSE59688.Table S1 related to Figs 1D, 5C, 6G, fpkm all NM.xlsx";
	private static String metaDataFile = "E:/CCLE/GSE59688_series_matrix.txt";
	private static ISampleDAO samplenewDAO = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	private static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	private static ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);
	private static Logger logger = LoggerFactory.getLogger(CCLEParser.class); 
	
	
	//获取metadata 
	@SuppressWarnings("resource")
	public Integer createSamples(String  barcode) throws IOException{
		int sampleId = 0;
		File file = new File(metaDataFile);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String lineRead ;
		List<String> keys = new ArrayList<String>();
		List<Map<String,List<String>>> values = new ArrayList<Map<String,List<String>>>();
		
		int x =4 ,y=9; 
		int m =1  , n=6 ;
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
				String v = StringUtils.trimToEmpty(lines[x].split(":")[1]);
				String v1 = StringUtils.trimToEmpty(lines[y].split(":")[1]);
				String v2 = StringUtils.trimToEmpty(lines[m].split(":")[1]);
				String v3 = StringUtils.trimToEmpty(lines[n].split(":")[1]);
				if(v.equals("http")){
					v = v +":" + lines[x].split(":")[2];
					v1 = v1 +":" + lines[y].split(":")[2];
					v2 = v2 +":" + lines[m].split(":")[2];
					v3 = v3 +":" + lines[n].split(":")[2];
				}
				if(!v.equals(v1)){
					v = v+" "+v1;
				}
				if(!v2.equals(v3)){
					v2 = v2+" "+v3;
				}
				if(!v.equals(v2)){
					v = v+" "+v2;
				}
				if(!StringUtils.isNoneEmpty(v)){
					continue;
				}
				value.add(v);
				
			}else{
				key = StringUtils.trimToEmpty(lines[0].substring(8));
					if(!StringUtils.isNoneEmpty(lines[x]) && ! StringUtils.isNoneEmpty(lines[y])){
						continue;
					}
					String v = StringUtils.trimToEmpty(lines[x]);
					String v1 = StringUtils.trimToEmpty(lines[y]);
					String v2 = StringUtils.trimToEmpty(lines[m]);
					String v3 = StringUtils.trimToEmpty(lines[n]);
					if(!v.equals(v1)){
						v = v+" "+v1;
					}
					if(!v2.equals(v3)){
						v2 = v2+" "+v3;
					}
					if(!v.equals(v2)){
						v = v+" "+v2;
					}
					value.add(v);
			}
			Map<String,List<String>> map = new HashMap<String, List<String>>();
			map.put(key, value);
			keys.add(key);
			values.add(map);
		}
			Sample s = new Sample();
			Map<String, String> map = s.descMap();
			sampleId = sampleDAO.getSequenceId(SourceType.GEO);
			for(int j=0;j<keys.size();j++){
				map.put(keys.get(j),values.get(j).get(keys.get(j)).get(0) );
				System.out.println(keys.get(j)+"-----"+values.get(j).get(keys.get(j)).get(0));
			}
			s.descMap(map);
			s.setCreateTiemStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
			s.setSampleId(sampleId);
			//s.setFactor("");
			//s.setCell("");
			s.setSource(SourceType.GEO.value());
			s.setEtype(ExperimentType.RNA_SEQ_DIFF.value());
			//s.setUrl("");
			s.setLab("UCLA");
			s.setDeleted(0);
			s.setSampleCode(barcode);
		samplenewDAO.create(s);
		return sampleId;
		
	}
	
	public void read(String filePath) throws IOException{
		InputStream is  = new FileInputStream(filePath);
		Workbook wb = null;
        if(filePath.toLowerCase().endsWith("xlsx")){  
        	wb = new XSSFWorkbook(is);  
        }else if(filePath.toLowerCase().endsWith("xls")){  
            wb = new HSSFWorkbook(is);  
        }  
        int col =10;
		Map<String,Integer> geneIds = new HashMap<String,Integer>();
		Sheet sheet = wb.getSheetAt(0);
		int rowCount = sheet.getLastRowNum();
		List<String> txnamList = new ArrayList<String>();
		for(int j=1;j<rowCount;j++){
			Row row = sheet.getRow(j);
			Cell cell0 = row.getCell((short)0);
			String txname = cell0.getStringCellValue();	
			txnamList.add(txname);
			Gene gene = GeneCache.getInstance().getGeneByName(txname);
			if(gene==null){
				geneIds.put(txname, null);
			}else{
				geneIds.put(txname, gene.getGeneId());
			}
		}
			Map<String,Double> columnR = new HashMap<String,Double>();
			for(int j=1;j<rowCount;j++){
				Row row = sheet.getRow(j);
				//gai
				Cell cell0 = row.getCell((short)col);
				Double geneRead = cell0.getNumericCellValue();
				Cell txCell = row.getCell((short)0);
				String txname = txCell.getStringCellValue();
				System.out.println(txname+"--"+geneRead);
				columnR.put(txname, geneRead);
			}
			Row row0 =sheet.getRow(0);
			//gai
			Cell cellbarc = row0.getCell(col);
			String barcode = cellbarc.getStringCellValue();
			
			//根据code找sample的metadata
			int sampleid = createSamples(barcode);
			
			List<SymbolReader> list = new ArrayList<SymbolReader>();
			//
			for(int m=0; m<txnamList.size(); m++){
				SymbolReader sr = new SymbolReader();
				sr.setRead(columnR.get(txnamList.get(m)));
				sr.setBarCode(barcode);
				sr.setSymbol(txnamList.get(m));
				list.add(sr);
			}
			for(int k=0; k<list.size(); k++){
				if(geneIds.get(list.get(k).getSymbol())==null){
					list.remove(k);
					k--;
				}
			}
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
					gr.setSampleId(sampleid);
					geneRanks.add(gr);
				}
			}
			geneRankDAO.create(geneRanks);
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
		new GSE59688Paser().read(filePath);
		//new GSE59688Paser().createSamples("dl312_fpkm");
	}

}
