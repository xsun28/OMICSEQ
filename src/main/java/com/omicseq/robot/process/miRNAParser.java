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
import com.omicseq.domain.MiRNA;
import com.omicseq.domain.MiRNARank;
import com.omicseq.domain.MiRNASample;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.ImiRNADAO;
import com.omicseq.store.dao.ImiRNARankDAO;
import com.omicseq.store.dao.ImiRNASampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateUtils;


public class miRNAParser {
	private static String pathname = "F:"+File.separator+"mirna"+File.separator;
	private static List<String > parseFiles = null;
	private static Logger logger = LoggerFactory.getLogger(CNVParser.class);
	private static ImiRNADAO miRNADao =  DAOFactory.getDAOByTableType(ImiRNADAO.class, "new");
	private static ISampleDAO sampleDao = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	private static ImiRNASampleDAO miSample = DAOFactory.getDAO(ImiRNASampleDAO.class);
	private static ImiRNARankDAO miRankDAO = DAOFactory.getDAO(ImiRNARankDAO.class);
	private static Map<String,Integer> miMap = new HashMap<String,Integer>();
	public miRNAParser(){
		parseFiles = new ArrayList<String >();
		int i=0;
		//找到文件
		File file1 = new File(pathname);
		String [] cancerType = file1.list(); 
		for(String s : cancerType){
			File file = new File(pathname+File.separator+s+File.separator+"miRNASeq");
			if(file.isDirectory()){
				String [] files = file.list();
				for(String fn : files){
					String g = pathname+s+File.separator+"miRNASeq"+File.separator+fn;
					File gs = new File(g);
					if(gs.isDirectory()){
						File ff = new File(pathname+s+File.separator+"miRNASeq"+File.separator+fn+File.separator+"Level_3");
						String [] fname = ff.list();
						for(String name :fname){
							if(name.contains("mirna") && !name.contains("hg19")){
								parseFiles.add(pathname+s+File.separator+"miRNASeq"+File.separator+fn+File.separator+"Level_3"+File.separator+name);	
							}
						}
						
					}
				}
				System.out.println(s+":"+parseFiles.size());
			}
		}
		logger.debug("total : {} files",parseFiles.size());
	}
	
	
	
	
	public void read(){
		java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00000");  
		for(String fn : parseFiles){
			File file = new File(fn);
			//创建sample
			String barcode = fn.split("\\.")[0];
			barcode = barcode.split("\\\\")[6];
			//数据库查询
			SmartDBObject query = new SmartDBObject("sampleCode",barcode);
			query.put("deleted", 0);
			query.put("source", 1);
			query.put("etype", 2);
			List<Sample> ss = new ArrayList<Sample>();
			ss = sampleDao.find(query);
			if(ss.size()==0){
				String [] cods = barcode.split("-");
				String barcode1 = cods[0]+"-"+cods[1]+"-"+cods[2];
				SmartDBObject qu = new SmartDBObject("$regex", ".*"+barcode1);
				SmartDBObject query1 = new SmartDBObject("sampleCode",qu);
				query1.put("deleted", 0);
				query1.put("source", 1);
				query1.put("etype", 2);
				ss = sampleDao.find(query1);
				if(ss.size()==0){
					//记录找不到的文件
					CNVParser c = new CNVParser();
					c.record(barcode+"\r\n", "MIRNAMissing");
					continue;
				}
			}
			MiRNASample ms = new MiRNASample();
			ms.setCreateTimeStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
			ms.setDeleted(0);
			ms.setEtype(ExperimentType.MIRNA_SEQ.value());
			ms.setSource(SourceType.TCGA.value());
			ms.setMiRNASampleId(miSample.getSequenceId(SourceType.TCGA));
			String [] c = ss.get(0).getCell().split("-");
			String  d = barcode.split("-")[3];
			if(d.startsWith("0")){
				ms.setCell(c[0]+"-"+c[1]+"-tumor");
			}
			if(d.startsWith("1")){
				ms.setCell(c[0]+"-"+c[1]+"-normal");
			}
			if(d.startsWith("2")){
				ms.setCell(c[0]+"-"+c[1]+"-control");
			}
			ms.setFactor(ss.get(0).getFactor());
			ms.setDescription(ss.get(0).getDescription());
			ms.setLab(ss.get(0).getLab());
			ms.setBarCode(barcode);
			String cancerType = ss.get(0).getCell().split("-")[1];
			//https://tcga-data.nci.nih.gov/tcga/dataAccessMatrix.htm?mode=ApplyFilter&showMatrix=true&diseaseType=LAML&tumorNormal=TN&tumorNormal=T&tumorNormal=NT
			String url = "https://tcga-data.nci.nih.gov/tcga/dataAccessMatrix.htm?mode=ApplyFilter&showMatrix=true&diseaseType="+cancerType.toUpperCase()+"&tumorNormal=TN&tumorNormal=T&tumorNormal=NT";
			ms.setUrl(url);
			miSample.create(ms);
			
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String row ;
				Integer miRnaId =0;
				boolean f = true;
				List<SymbolReader> srs = new ArrayList<SymbolReader>();
				while((row=br.readLine())!=null){
					if(f ){
						f=false;
						continue;
					}
					String [] colItem = row.split("	");
					String miRNA_name = colItem[0];
//					MiRNA m = miRNADao.findByName(miRNA_name);
// 					if(m==null){
//						MiRNA obj = new MiRNA();
//						miRnaId = miRNADao.getSquence(SourceType.TCGA);
//						obj.setMiRNAid(miRnaId);
//						obj.setDeleted(0);
//						obj.setCreateTimeStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
//						obj.setName(miRNA_name);
//						miRNADao.create(obj);
//					}else{
//						miRnaId = m.getMiRNAid();
//					}
					miRnaId = miMap.get(miRNA_name);
					if(miRnaId==null){
						System.out.println(miRNA_name.trim());
					}
					Double reads = Double.parseDouble(colItem[2]);
					SymbolReader sr = new SymbolReader();
					sr.setRead(reads);
					sr.setSymbol(miRnaId.toString());
					srs.add(sr);
				}
				Collections.sort(srs, new Comparator<SymbolReader>() {
					@Override
					public int compare(SymbolReader s1, SymbolReader s2) {
						return  new Double(Math.abs(s1.getRead())).compareTo(new Double(Math.abs(s2.getRead()))) *(-1);
					}
				});
				
				List<MiRNARank> miRanks = new ArrayList<MiRNARank>();
				for(SymbolReader sr : srs){
					MiRNARank mr = new MiRNARank();
					mr.setCreateTimeStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
					mr.setMiRNASampleId(ms.getMiRNASampleId());
					mr.setMiRNAId(Integer.parseInt(sr.getSymbol()));
					mr.setRead(sr.getRead());
					mr.setSource(SourceType.TCGA.value());
					mr.setEtype(ExperimentType.MIRNA_SEQ.value());
					mr.setTotalCount(srs.size());
					mr.setMixtureperc(Double.parseDouble(df.format((double)(srs.indexOf(sr)+1)/srs.size())));
					miRanks.add(mr);
				}
				miRankDAO.create(miRanks);
			}  catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args) {
		List<MiRNA> miRNAlist= miRNADao.find(new SmartDBObject("deleted",0));
		for(MiRNA s : miRNAlist){
			if(s.getMiRNAName().equals("hsa-let-7a-1")){
				System.out.println(s.getMiRNAId()+":"+s.getMiRNAName()+";");
			}
			miMap.put(s.getMiRNAName().trim(), s.getMiRNAId());
		}
		new miRNAParser().read();
//		Map<String,Integer> m = new HashMap<String, Integer>();
//		m.put("hsa-let-7a-1", 1);
//		System.out.println(m.get("hsa-let-7a-1"));
	}
}
