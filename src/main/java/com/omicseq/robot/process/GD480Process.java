package com.omicseq.robot.process;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

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

public class GD480Process {
	private ImiRNADAO miRNADao = DAOFactory.getDAOByTableType(ImiRNADAO.class,"new");
	private ISampleDAO sampleNewDao = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
	private ImiRNASampleDAO miSampleDao = DAOFactory.getDAO(ImiRNASampleDAO.class);
	private ImiRNARankDAO miRankDao = DAOFactory.getDAO(ImiRNARankDAO.class);
//	private String root = "C:\\Users\\Administrator\\Desktop\\miRna_new\\GD480.MirnaQuantCount.1.2N.txt.gz";
	private String root = "/home/TCGA-Assembler/user/GD480.MirnaQuantCount.1.2N.txt.gz";

	private List<String> barcodeList = new ArrayList<String>();
	private Map<String,Integer> miRNAMap = new HashMap<String, Integer>();
	private Map<String,Integer> sampleMap = new HashMap<String, Integer>();
	private DecimalFormat df = new DecimalFormat("#.00000");

	public GD480Process(){
		try {
			List<MiRNA> miRNaList = miRNADao.find(new SmartDBObject("deleted",0));
			for(MiRNA m : miRNaList){
				miRNAMap.put(m.getMiRNAName(),m.getMiRNAId());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(new File(root)))));
			String line;
			int rowNum = 0 ;
			List<String> un_db = new ArrayList<String>();
			while(( line = br.readLine()) != null){
				rowNum++;
				String [] tmp = line.split("	");
				if(rowNum == 1){
					for(int i =4 ; i<tmp.length; i++){
						barcodeList.add(tmp[i]);
					}
					//创建sample
					sampleMap = createSample(barcodeList);
					continue;
				}
				String name = tmp[0].replace("-5p", "");
				if(!miRNAMap.containsKey(name.toLowerCase())){
					un_db.add(name);
				}
			}
			List<MiRNA> miRNAList = createMiRNA(un_db);
			for(MiRNA miRNA : miRNAList){
				miRNAMap.put(miRNA.getMiRNAName(), miRNA.getMiRNAId());
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}
	
	public List<MiRNA> createMiRNA(List<String > names){
		List<MiRNA> miRNAList = new ArrayList<MiRNA>();
		for(String name : names){
			MiRNA mirna = new MiRNA();
			mirna.setCreateTimeStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
			mirna.setDeleted(0);
			mirna.setMiRNAId(miRNADao.getSquence(SourceType.TCGA));
			mirna.setMiRNAName(name.toLowerCase());
			miRNAList.add(mirna);
		}
		miRNADao.create(miRNAList);
		return miRNAList;
	}
	
	public Map<String, Integer> createSample(List<String> barcodes){
		SmartDBObject query = new SmartDBObject("source", 10);
		List<Sample> samples = sampleNewDao.find(query);
		Map<String , String> desMap = new HashMap<String, String>();
		for(Sample s : samples){
			desMap.put(s.getSampleCode().split("\\.")[0], s.getDescription());
		}
		Map<String , Integer > map = new HashMap<String, Integer>();
		List<MiRNASample> sampleList = new ArrayList<MiRNASample>();
		for(String barcode : barcodes){
			MiRNASample sample = new MiRNASample();
			Integer sampleId= miSampleDao.getSequenceId(SourceType.TCGA);
			sample.setMiRNASampleId(sampleId);
			sample.setDescription(desMap.get(barcode.split("\\.")[0]));
			sample.setBarCode(barcode);
			sample.setCell(barcode.split("\\.")[0]);
			sample.setDeleted(0);
			sample.setCreateTimeStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
			sample.setSource(SourceType.GEUVADIS.value());
			sample.setEtype(ExperimentType.RNA_SEQ.value());
			sample.setUrl("http://www.ebi.ac.uk/arrayexpress/files/E-GEUV-3/GD480.MirnaQuantCount.1.2N.txt.gz");
			sampleList.add(sample);
			map.put(barcode, sampleId);
		}
		miSampleDao.create(sampleList);
		return map;
	}
	
	public void process(){
		try {
			for(String barcode : barcodeList){
				List<SymbolReader> mirna_5p = new ArrayList<SymbolReader>();
				List<SymbolReader> mirna_3p = new ArrayList<SymbolReader>();
				BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(new File(root)))));
				String line;
				int row = 0 ;
				int index = 0;
				while((line = br.readLine()) != null){
					
					row++;
					String [] tmp = line.split("	");
					if(row == 1) {
						index = barcodeList.indexOf(barcode);
						continue;
					}
						
					SymbolReader sr = new SymbolReader();
					sr.setBarCode(barcode);
					sr.setGeneId(miRNAMap.get(tmp[0].replace("-5p", "").toLowerCase()));
					sr.setRead(Double.parseDouble(tmp[index+4]));
					sr.setSymbol(tmp[0].replace("-5p", ""));
					
					if(tmp[0].endsWith("-5p")){
						mirna_5p.add(sr);
					}
					if(tmp[0].endsWith("-3p")){
						mirna_3p.add(sr);
					}
				}
				
				Collections.sort(mirna_5p, new Comparator<SymbolReader>() {
					@Override
					public int compare(SymbolReader o1, SymbolReader o2) {
						// TODO Auto-generated method stub
						return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1);
					}
				});;
				
				Collections.sort(mirna_3p, new Comparator<SymbolReader>() {
					@Override
					public int compare(SymbolReader o1, SymbolReader o2) {
						// TODO Auto-generated method stub
						return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1);
					}
				});;
				
				// 存数据库
				List<MiRNARank> miRanks_5p = new ArrayList<MiRNARank>();
				for(SymbolReader sr : mirna_5p){
					MiRNARank mr = new MiRNARank();
					mr.setCreateTimeStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
					mr.setMiRNASampleId(sampleMap.get(sr.getBarCode()));
					mr.setMiRNAId(sr.getGeneId());
					mr.setRead(sr.getRead());
					mr.setSource(SourceType.GEUVADIS.value());
					mr.setEtype(ExperimentType.RNA_SEQ.value());
					mr.setTotalCount(mirna_5p.size());
					mr.setMixtureperc(Double.parseDouble(df.format((double)(mirna_5p.indexOf(sr)+1)/mirna_5p.size())));
					miRanks_5p.add(mr);
				}
				miRankDao.create(miRanks_5p);
				
				List<MiRNARank> miRanks_3p = new ArrayList<MiRNARank>();
				for(SymbolReader sr : mirna_3p){
					MiRNARank mr = new MiRNARank();
					mr.setCreateTimeStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
					mr.setMiRNASampleId(sampleMap.get(sr.getBarCode()));
					mr.setMiRNAId(sr.getGeneId());
					mr.setRead(sr.getRead());
					mr.setSource(SourceType.GEUVADIS.value());
					mr.setEtype(ExperimentType.RNA_SEQ.value());
					mr.setTotalCount(mirna_3p.size());
					mr.setMixtureperc(Double.parseDouble(df.format((double)(mirna_3p.indexOf(sr)+1)/mirna_5p.size())));
					miRanks_3p.add(mr);
				}
				miRankDao.create(miRanks_3p);
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

	public static void main(String[] args) {
		
		new GD480Process().process();
	}

}
