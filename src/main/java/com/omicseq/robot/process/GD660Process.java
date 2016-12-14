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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.core.GeneCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.dao.ImiRNADAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateUtils;
public class GD660Process {
	
	protected static String root = "/home/TCGA-Assembler/user/";
//	protected static String root = "C:\\Users\\Administrator\\Desktop\\miRna_new\\";
	protected static ISampleDAO sampleNewdao = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	//protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAOByTableType(IGeneRankDAO.class, "_copy");
	protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	protected static ITxrRefDAO txrRefDAO = DAOFactory.getDAO(ITxrRefDAO.class);
	protected static IGeneDAO geneDAO = DAOFactory.getDAO(IGeneDAO.class);
	protected static ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);
	protected static ImiRNADAO miRNADAO = DAOFactory.getDAO(ImiRNADAO.class);
	private static Logger logger = LoggerFactory.getLogger(GD660Process.class);


	/*
	 * 解析txt文件
	 */
	public void parser (){
		File file = new File(root + "GD660.GeneQuantRPKM.txt.gz");
		DBCollection collection = MongoDBManager.getInstance().getCollection("manage", "manage","hashdbensemblgene");
		java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00000");  
		try {
			String line ;
			Integer rowNum = 0;
			List<Integer> geneIds = new ArrayList<Integer>();
			List<String> barcodes = new ArrayList<String>();
			BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
			//第一次读文件 拿到所有gene
			DBCursor cursor = collection.find(new SmartDBObject("key",new SmartDBObject("$ne", null)));
			Map<String,String> map = new HashMap<String, String>();
			while(cursor.hasNext()){
				DBObject obj = cursor.next();
				map.put((String)obj.get("key"),(String) obj.get("value"));
			}
			List<Sample> samples = new ArrayList<Sample>();
			while((line = br.readLine())!= null){
				rowNum++;
				String [] line_split = line.split("	");
				if(rowNum==1) {
					for(int i = 4 ; i<line_split.length;i++){
						barcodes.add(line_split[i]);
					}
				}
				for(int i = 0 ; i<barcodes.size(); i++){
					Sample sample = new Sample();
					System.out.println(barcodes.get(i));
					//meteData
					Map metadata = getmetaData(barcodes.get(i));
					sample.descMap(metadata);
					Integer sampleId= sampleDAO.getSequenceId(SourceType.GEUVADIS);
					sample.setSampleId(sampleId);
					sample.setCell(barcodes.get(i).split("\\.")[0]);
					sample.setDeleted(0);
					sample.setSampleCode(barcodes.get(i));
					sample.setDetail("lymphoblastoid normal");
					sample.setCreateTiemStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
					sample.setSource(SourceType.GEUVADIS.value());
					sample.setEtype(ExperimentType.RNA_SEQ.value());
					sample.setUrl("http://www.ebi.ac.uk/arrayexpress/files/E-GEUV-3/GD660.GeneQuantRPKM.txt.gz");
					samples.add(sample);
				}
				sampleNewdao.create(samples);
				break;
//				String name = line.split("	")[1].replace("-5p", "").replace("-3p", "").toLowerCase();
			}
			rowNum = 0;
			for(int i=0;i<barcodes.size();i++){
				BufferedReader br_ = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
				List<SymbolReader> srs = new ArrayList<SymbolReader>();
				while((line=br_.readLine())!=null){
					rowNum++;
					String [] ls = line.split("	"); 
					if(rowNum == 1) continue;
					String txName = map.get(ls[0].split("\\.")[0]);
					if(txName==null) continue;
					Gene gene = GeneCache.getInstance().getGeneByName(txName);
					if(gene == null) continue;
					Integer geneId = gene.getGeneId();
					Double readDouble = Double.parseDouble(ls[i+4]);
					SymbolReader sr = new SymbolReader();
					sr.setBarCode(barcodes.get(i));
					sr.setGeneId(geneId);
					sr.setRead(readDouble);
					srs.add(sr);
				}
				
				//排序 
				Collections.sort(srs, new Comparator<SymbolReader>() {
					@Override
					public int compare(SymbolReader o1, SymbolReader o2) {
						//return o1.getRead().compareTo(o2.getRead()) *(-1);
						return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1);
					}
				});
				
				//数据库添加geneRank
				List<GeneRank> geneRanks = new ArrayList<GeneRank>();
				for(SymbolReader sr: srs){
					GeneRank gr = new GeneRank();
					gr.setCreatedTimestamp(System.currentTimeMillis());
					gr.setEtype(ExperimentType.RNA_SEQ.value());
					gr.setSource(SourceType.GEUVADIS.value());
					gr.setGeneId(sr.getGeneId());
					gr.setMixturePerc(Double.parseDouble(df.format((double)(srs.indexOf(sr)+1)/srs.size())));
					//Tsstescount读数
					gr.setTssTesCount(sr.getRead());
					gr.setTotalCount(srs.size());
					gr.setSampleId(samples.get(i).getSampleId());
//					gr.setSampleId(startSampleId);
					geneRanks.add(gr);
				}
				
				geneRankDAO.create(geneRanks);

				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	/*
	 *  数据库创建sample
	 */
	public List<Integer> modifySample(List<String> list,String cacerType){
		List<Integer> samplesIds = new ArrayList<Integer>();
		Map<String,String> urls = getURL(cacerType);
		for(String barCode : list){
			Sample sample = sampleNewdao.getByUrl(urls.get(barCode));
			if(sample == null || sample.getSampleCode().split("-").length > 3)
			{
				continue;
			}
			
			//读取description
			File file = new File(root+"PatientData"+File.separator+"nationwidechildrens.org_clinical_patient_"+cacerType.toLowerCase()+".txt");
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String patientLine;
				boolean flag = true;
				String [] keys = null;
				String cell_old = "TCGA-" + cacerType.toLowerCase();
				while((patientLine = reader.readLine())!=null){
					//读取第一行时拿到所有map的key
					if(flag){
						keys = patientLine.split("	");
						flag = false;
					}
					String [] barCodeStrings = barCode.split("-");
//					String code = barCodeStrings[0]+"-"+barCodeStrings[1]+"-"+barCodeStrings[2];
					sample.setSampleCode(barCode);
					String type = barCodeStrings[3];
					int typeI = Integer.parseInt(type.substring(0, 2));
					if(typeI < 10)
					{
						sample.setCell(cell_old + "-tumor");
					}else if (typeI < 20)
					{
						sample.setCell(cell_old + "-normal");
					} else {
						sample.setCell(cell_old + "-control");
					}
//					if(patientLine.startsWith(code)){
//						String [] values = patientLine.split("	");
//						Map<String, String> map = sample.descMap();
//						for(int i=2; i<keys.length; i++){
//							map.put(keys[i], values[i]);
//						}
//						sample.descMap(map);
//					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
//			sample.setCreateTiemStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
			sampleNewdao.update(sample);
		}
		
		return samplesIds;
	}
	
	/*
	 * 读取sample Url
	 */
	public Map<String,String> getURL(String cacerType){
		Map<String,String> map = new HashMap<String, String>();
		try {
			File file = new File(root+"DownloadURL"+File.separator+cacerType.toUpperCase()+".csv");
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			while((line=br.readLine())!=null){
				String [] temp = line.split(",");
				System.out.println(temp[0]);
				if(temp[0].startsWith("\"TCGA")){
					map.put(temp[0].substring(1, temp[0].length()-1), temp[1].substring(1, temp[1].length()-1));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}
	
	
	public Map getmetaData(String barcode){
		Map<String, String> map = new HashMap<String, String>();
		try {
			Workbook wb = new HSSFWorkbook(new FileInputStream(new File(root + "20101214_1000genomes_samples.xls")));
			Sheet sheet = wb.getSheetAt(0);
			for(int i = 39 ; i < sheet.getPhysicalNumberOfRows();i++){
				Row row = sheet.getRow(i);
				if(barcode.contains(row.getCell(1).getStringCellValue())){
					String key1 = "Population";
					Cell val1 = row.getCell(0);
					String key2 = "Coriell sample";
					Cell val2 = row.getCell(1);
					String key3 = "Accession number";
					Cell val3 = row.getCell(2);
					String key4 = "Sex";
					Cell val4 = row.getCell(4);
					String key5 = "Center";
					Cell val5 = row.getCell(7);
					if(val1!=null)map.put(key1, val1.getStringCellValue());
					if(val2!=null)map.put(key2, val2.getStringCellValue());
					if(val3!=null)map.put(key3, val3.getStringCellValue());
					if(val4!=null)map.put(key4, val4.getStringCellValue());
					if(val5!=null)map.put(key5, val5.getStringCellValue());
					break;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		
		return map;
	}
	public static void main(String[] args) {
		System.out.println(Math.exp(-3.63072));
		
		GeneCache.getInstance().init();	
		new GD660Process().parser();
	}

}
