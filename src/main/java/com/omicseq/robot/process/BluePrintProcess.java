package com.omicseq.robot.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.core.GeneCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateUtils;

public class BluePrintProcess {

	String root = "F:\\bluePrint\\";
//	String root = "/home/TCGA-Assembler/user/bluePrint/";
	String [] files ; 
	Map<String,String> map ;
	ISampleDAO sampleDao = DAOFactory.getDAO(ISampleDAO.class);
	ISampleDAO sampleNewDao = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
	IGeneRankDAO geneRankDao = DAOFactory.getDAO(IGeneRankDAO.class);
	
	public BluePrintProcess(){
		
		DBCollection collection = MongoDBManager.getInstance().getCollection("manage", "manage","hashdbensemblgene");
		DBCursor cursor = collection.find(new SmartDBObject("key",new SmartDBObject("$ne", null)));
		map = new HashMap<String, String>();
		while(cursor.hasNext()){
			DBObject obj = cursor.next();
			map.put((String)obj.get("key"),(String) obj.get("value"));
		}
		
		File file = new File(root);
		if(file.isDirectory()){
			files = file.list();
		}
		process(files);
	}

	public void process(String [] names){
		try {
			for(String name : names){
				
				BufferedReader br ;
				if(name.endsWith(".gz")){
					br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(new File( root +name)))));
				}else{
					br = new BufferedReader(new FileReader(new File(root + name)));
				}
				
				List<SymbolReader> srs = new ArrayList<SymbolReader>();
				Integer sampleId = createSample(name);
//				Integer sampleId = 1 ;System.out.println("sample:" +sampleId);
				String line;
				int row = 0;
				if(name.endsWith(".xlsx") ) continue;
				if( name.endsWith(".tsv")) {
					while((line = br.readLine()) != null){
						row++;
						if(row < 2) continue;
						String [] tmp = line.split("	");
						String symbol = tmp[0];
						Gene gene = GeneCache.getInstance().getGeneByName(map.get(symbol));
						if(gene == null) continue;
						Double log_mu =Double.parseDouble(tmp[1]);
						SymbolReader sr = new SymbolReader();
						sr.setGeneId(gene.getGeneId());
						sr.setRead(Math.exp(log_mu));
						sr.setSymbol(symbol);
						srs.add(sr);
					}
				}
				else{
					while((line = br.readLine()) != null){
						String [] tmp = line.split("	");
						String [] next = tmp[8].replaceAll("\"", "").split(";");
						String symbol;
						if(name.endsWith(".gz")){
							symbol = next[1].replace("gene_id", "").trim().split("\\.")[0];
						}else{
							symbol = next[0].replace("gene_id", "").trim().split("\\.")[0];
						}
						Gene gene = GeneCache.getInstance().getGeneByName(map.get(symbol));
						if(gene == null) continue;
						String rbkm;
						if(name.endsWith(".gz")){
							rbkm = next[0].replace("RPKM", "").trim();
						}else{
							rbkm = next[next.length-2].replace("RPKM", "").trim();
						}
						
						SymbolReader sr = new SymbolReader();
						sr.setGeneId(gene.getGeneId());
						sr.setRead(Double.parseDouble(rbkm));
						sr.setSymbol(symbol);
						srs.add(sr);
					}
				}
				Collections.sort(srs, new Comparator<SymbolReader>() {

					@Override
					public int compare(SymbolReader o1, SymbolReader o2) {
						return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1); 
					}
				});
				java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00000"); 
				List<GeneRank> geneRankList = new ArrayList<GeneRank>();
				for(SymbolReader sr: srs){
					GeneRank gr = new GeneRank();
					gr.setCreatedTimestamp(System.currentTimeMillis());
					gr.setEtype(ExperimentType.RNA_SEQ.value());
					gr.setSource(SourceType.BLUEPRINT.value());
					gr.setGeneId(sr.getGeneId());
					gr.setMixturePerc(Double.parseDouble(df.format((double)(srs.indexOf(sr)+1)/srs.size())));
					//Tsstescount读数
					gr.setTssTesCount(sr.getRead());
					gr.setTotalCount(srs.size());
					gr.setSampleId(sampleId);
//					gr.setSampleId(startSampleId);
					geneRankList.add(gr);
				}
//				System.out.println("geneRank :" + geneRankList.size());
				geneRankDao.create(geneRankList);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	public Integer createSample(String fileName){
		Sample sample = new Sample();
		Integer sampleId = sampleDao.getSequenceId(SourceType.BLUEPRINT);
		sample.setSampleId(sampleId);
		sample.setDeleted(0);
		sample.setSource(SourceType.BLUEPRINT.getValue());
		sample.setEtype(ExperimentType.RNA_SEQ.getValue());
		sample.setCreateTiemStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
		try {
			Workbook wb = new XSSFWorkbook(new FileInputStream(new File(root + "test.xlsx")));
			Sheet sheet = wb.getSheetAt(0);
			for(int i=1;i<sheet.getPhysicalNumberOfRows();i++){
				Row row = sheet.getRow(i);
				Cell cell = row.getCell(0);
				if(cell.getStringCellValue().contains(fileName)){
					Map<String, String> map = sample.descMap();
					String url = row.getCell(0).getStringCellValue();
					String ce = row.getCell(2).getStringCellValue();
					String sex =row.getCell(4).getStringCellValue();
					String source = row.getCell(1).getStringCellValue();
					String name = row.getCell(3).getStringCellValue();
					map.put("cell", ce);
					map.put("sex", sex);
					map.put("source", source);
					map.put("name", name);
					sample.setCell(ce);
					sample.setUrl(url);
					sample.descMap(map);
					break;
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		sampleNewDao.create(sample);
		return sampleId;
	}
		
	public static void main(String[] args) {
		GeneCache.getInstance().init();
		new BluePrintProcess();
		
	}
}
