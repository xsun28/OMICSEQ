package com.omicseq.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.util.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.mongodb.DBCollection;
import com.omicseq.common.SourceType;
import com.omicseq.core.GeneCache;
import com.omicseq.core.MouseGeneCache;
import com.omicseq.core.MouseTxrRefCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.MiRNA;
import com.omicseq.domain.MiRNARank;
import com.omicseq.domain.MiRNASample;
import com.omicseq.domain.Sample;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.ImiRNADAO;
import com.omicseq.store.dao.ImiRNARankDAO;
import com.omicseq.store.dao.ImiRNASampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MiRNADAOImpl;
import com.omicseq.store.daoimpl.mongodb.MiRNASampleDAO;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateUtils;

public class ProcessFile {
	private IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	private ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
	private ImiRNASampleDAO miRNASampleDAO = DAOFactory.getDAO(ImiRNASampleDAO.class);
	private ImiRNADAO miRNADAO = DAOFactory.getDAOByTableType(ImiRNADAO.class,"new");
	private ImiRNARankDAO miRNARankDAO = DAOFactory.getDAO(ImiRNARankDAO.class);
	private ISampleDAO sampleIdDAO = DAOFactory.getDAO(ISampleDAO.class);
	private DecimalFormat df = new DecimalFormat("#.00000");
	public void parse(){
		Sample sample = sampleDAO.getBySampleId(1300063);
		Map<String,Integer> geneIdMap = getMouseGeneIdByEnsembl();
		String path = "C:/Users/Administrator/Desktop/新建文件夹/Supplemental_Tables_1300063.xlsx";
		Workbook workbook;
		try {
			workbook = new XSSFWorkbook(new FileInputStream(new File(path)));
			for(int sheetNum = 0;sheetNum < workbook.getNumberOfSheets();sheetNum++){
				Sheet sheet = workbook.getSheetAt(sheetNum);
				for(int rowNum = 0; rowNum < sheet.getLastRowNum(); rowNum++){
					if(rowNum == 0) continue;
					Row row = sheet.getRow(rowNum);
					int geneId = geneIdMap.get(row.getCell(0));
					Double value = row.getCell(5).getNumericCellValue();
					GeneRank rank = new GeneRank();
					rank.setSampleId(1300063);
					rank.setCreatedTimestamp(System.currentTimeMillis());
					rank.setEtype(sample.getEtype());
					rank.setSource(sample.getSource());
					rank.setTssTesCount(value);
					rank.setGeneId(geneId);
					
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public Map<String,Integer> getMouseGeneIdByEnsembl(){
		Map<String,Integer> geneIdMap = new HashMap<String, Integer>();
		File file = new File("C:/Users/Administrator/Desktop/Mouse_ensembl.txt");
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			int i = 1;
			int j = 0;
			while((line = br.readLine()) != null){
				if(1 == i++) continue;
				String [] temp = line.split("\t");
				List<TxrRef> txrRefList = MouseTxrRefCache.getInstance().getTxrRefBySymbol(temp[1]);
				if(txrRefList == null) continue;
				for(TxrRef txr : txrRefList){
					Gene gene = MouseGeneCache.getInstance().getGeneByName(txr.getRefseq());
					if(gene != null){
						System.out.println(temp[0]+"  "+temp[1]+"  "+gene.getGeneId());System.err.println(++j);
						geneIdMap.put(temp[0], gene.getGeneId());
						break;
					}
				}
				/*if(gene == null) continue;
				System.out.println(temp[0]+"  "+temp[1]+"  "+gene.getGeneId());
				System.err.println(++j);
				geneIdMap.put(temp[0], gene.getGeneId());*/
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return geneIdMap;
	}
	
	public static void main(String[] args) {
//		MouseGeneCache.getInstance().doInit();
//		MouseTxrRefCache.getInstance().doInit();
		GeneCache.getInstance().doInit();
		TxrRefCache.getInstance().doInit();
		new ProcessFile().parse1();	
	}
	
	public void parse1(){
		Sample sample = sampleDAO.getBySampleId(1300063);
		String path = "C:/Users/Administrator/Desktop/新建文件夹/Supplemental_Tables_1300063.xlsx";
		List<MiRNA> miRnaList = miRNADAO.find(new SmartDBObject());
		Map<String,MiRNA> map = new HashMap<String,MiRNA>();
		for(MiRNA mirna : miRnaList){
			map.put(mirna.getMiRNAName(), mirna);
		}
		Workbook workbook;
		try {
			workbook = new XSSFWorkbook(new FileInputStream(new File(path)));
			for(int sheetNum = 0;sheetNum < workbook.getNumberOfSheets();sheetNum++){
				Sheet sheet = workbook.getSheetAt(sheetNum);
			/*	if(sheetNum == 0){
					List<MiRNARank> rankList = new ArrayList<MiRNARank>();
					MiRNASample sample1 = convertSampleToMiRNASample(sample);
					for(int rowNum = 0; rowNum < sheet.getLastRowNum(); rowNum++){
						Row row = sheet.getRow(rowNum);
						if(rowNum < 4) continue;
						System.out.println(rowNum+1);
						String name = row.getCell(0).getStringCellValue().toLowerCase().replace("*", "").replace("-5p", "");
						Double val = null;
						try {
							String value = row.getCell(3).getStringCellValue();
							val = Double.valueOf(value);
						} catch (Exception e) {
							val = row.getCell(3).getNumericCellValue();
						}
						if(map.get(name)==null) continue;
						MiRNARank rank = new MiRNARank();
						int id = map.get(name).getMiRNAId();
						rank.setCreateTimeStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
						rank.setEtype(sample1.getEtype());
						rank.setSource(sample1.getSource());
						rank.setMiRNAId(id);
						rank.setRead(val);
						rank.setMiRNASampleId(sample1.getMiRNASampleId());
						rankList.add(rank);
					}
					Collections.sort(rankList, new Comparator<MiRNARank>() {
						@Override
						public int compare(MiRNARank o1, MiRNARank o2) {
							return (o1.getRead().compareTo(o2.getRead())) * (-1);
						}
					});
					for(MiRNARank rank : rankList){
						rank.setTotalCount(rankList.size());
						rank.setMixtureperc(Double.valueOf(df.format((rankList.indexOf(rank)+1)/rankList.size())));
					}
					miRNASampleDAO.create(sample1);
					miRNARankDAO.create(rankList);
				}*/
				if(sheetNum == 6){
					List<GeneRank> rankList1 = new ArrayList<GeneRank>();
					List<GeneRank> rankList2 = new ArrayList<GeneRank>();
					List<GeneRank> rankList3 = new ArrayList<GeneRank>();
					List<GeneRank> rankList4 = new ArrayList<GeneRank>();
					List<GeneRank> rankList5 = new ArrayList<GeneRank>();
					List<GeneRank> rankList6 = new ArrayList<GeneRank>();
					List<Sample> sampleList = new ArrayList<Sample>();
					for(int i = 0;i < 5; i++){
						Sample sa = new Sample();
						sa.setSampleId(1300215+i);
						sa.setAntibody(sample.getAntibody());
						sa.setCell(sample.getCell());
						sa.setCreateTiemStamp(sample.getCreateTiemStamp());
						sa.setDeleted(0);
						sa.setDescription(sample.getDescription());
						sa.setDetail(sample.getDetail());
						sa.setEtype(sample.getEtype());
						sa.setFactor(sample.getFactor());
						sa.setFromType(sample.getFromType());
						sa.setInputSampleIds(sample.getInputSampleIds());;
						sa.setInstrument(sample.getInstrument());
						sa.setLab(sample.getLab());
						sa.setPubmedUrl(sample.getPubmedUrl());
						sa.setReadCount(sample.getReadCount());
						sa.setSampleCode(sample.getSampleCode());
						sa.setSource(sample.getSource());
						sa.setSourceUrl(sample.getSourceUrl());
						sa.setTimeStamp(sample.getTimeStamp());
						sa.setUrl("http://genome.cshlp.org/content/suppl/2014/02/28/gr.161471.113.DC1/Supplemental_Tables.xlsx");
//						sa.setSampleId(sampleIdDAO.getSequenceId(SourceType.SUPPLEMENTTARY));
						sampleList.add(sa);
					}
					sample.setUrl("http://genome.cshlp.org/content/suppl/2014/02/28/gr.161471.113.DC1/Supplemental_Tables.xlsx");
//					sampleDAO.update(sample);
					for(int rowNum = 0; rowNum < sheet.getLastRowNum(); rowNum++){
						Row row = sheet.getRow(rowNum);
						if(rowNum < 4) continue;
						String symbol = row.getCell(0).getStringCellValue();
						Gene gene = getGeneId(symbol);
						if(gene == null) continue;
						String read1 = row.getCell(3).getStringCellValue();
						String read2 = row.getCell(6).getStringCellValue();
						String read3 = row.getCell(9).getStringCellValue();
						String read4 = row.getCell(12).getStringCellValue();
						String read5 = row.getCell(15).getStringCellValue();
						String read6 = row.getCell(18).getStringCellValue();
						
						GeneRank gr1 = new GeneRank();
						GeneRank gr2 = new GeneRank();
						GeneRank gr3 = new GeneRank();
						GeneRank gr4 = new GeneRank();
						GeneRank gr5 = new GeneRank();
						GeneRank gr6 = new GeneRank();
						
						gr1.setCreatedTimestamp(System.currentTimeMillis());
						gr2.setCreatedTimestamp(System.currentTimeMillis());
						gr3.setCreatedTimestamp(System.currentTimeMillis());
						gr4.setCreatedTimestamp(System.currentTimeMillis());
						gr5.setCreatedTimestamp(System.currentTimeMillis());
						gr6.setCreatedTimestamp(System.currentTimeMillis());
						
						gr1.setGeneId(gene.getGeneId());
						gr2.setGeneId(gene.getGeneId());
						gr3.setGeneId(gene.getGeneId());
						gr4.setGeneId(gene.getGeneId());
						gr5.setGeneId(gene.getGeneId());
						gr6.setGeneId(gene.getGeneId());
						
						gr1.setSampleId(sample.getSampleId());
						gr2.setSampleId(sampleList.get(0).getSampleId());
						gr3.setSampleId(sampleList.get(1).getSampleId());
						gr4.setSampleId(sampleList.get(2).getSampleId());
						gr5.setSampleId(sampleList.get(3).getSampleId());
						gr6.setSampleId(sampleList.get(4).getSampleId());
						
						gr1.setSource(sample.getSource());
						gr2.setSource(sample.getSource());
						gr3.setSource(sample.getSource());
						gr4.setSource(sample.getSource());
						gr5.setSource(sample.getSource());
						gr6.setSource(sample.getSource());
						
						gr1.setEtype(sample.getEtype());
						gr2.setEtype(sample.getEtype());
						gr3.setEtype(sample.getEtype());
						gr4.setEtype(sample.getEtype());
						gr5.setEtype(sample.getEtype());
						gr6.setEtype(sample.getEtype());
						
						gr1.setTssTesCount(Double.valueOf(read1));
						gr2.setTssTesCount(Double.valueOf(read2));
						gr3.setTssTesCount(Double.valueOf(read3));
						gr4.setTssTesCount(Double.valueOf(read4));
						gr5.setTssTesCount(Double.valueOf(read5));
						gr6.setTssTesCount(Double.valueOf(read6));
						
						rankList1.add(gr1);
						rankList2.add(gr2);
						rankList3.add(gr3);
						rankList4.add(gr4);
						rankList5.add(gr5);
						rankList6.add(gr6);
					}
					
					Collections.sort(rankList1, new Comparator<GeneRank>(){
						@Override
						public int compare(GeneRank o1, GeneRank o2) {
							return ( o1.getTssTesCount().compareTo(o2.getTssTesCount()) ) * (-1);
						}
					});
					Collections.sort(rankList2, new Comparator<GeneRank>(){
						@Override
						public int compare(GeneRank o1, GeneRank o2) {
							return ( o1.getTssTesCount().compareTo(o2.getTssTesCount()) ) * (-1);
						}
					});
					Collections.sort(rankList3, new Comparator<GeneRank>(){
						@Override
						public int compare(GeneRank o1, GeneRank o2) {
							return ( o1.getTssTesCount().compareTo(o2.getTssTesCount()) ) * (-1);
						}
					});
					Collections.sort(rankList4, new Comparator<GeneRank>(){
						@Override
						public int compare(GeneRank o1, GeneRank o2) {
							return ( o1.getTssTesCount().compareTo(o2.getTssTesCount()) ) * (-1);
						}
					});
					Collections.sort(rankList5, new Comparator<GeneRank>(){
						@Override
						public int compare(GeneRank o1, GeneRank o2) {
							return ( o1.getTssTesCount().compareTo(o2.getTssTesCount()) ) * (-1);
						}
					});
					Collections.sort(rankList6, new Comparator<GeneRank>(){
						@Override
						public int compare(GeneRank o1, GeneRank o2) {
							return ( o1.getTssTesCount().compareTo(o2.getTssTesCount()) ) * (-1);
						}
					});
					
					for(GeneRank gr : rankList1){
						gr.setTotalCount(32745);
						gr.setMixturePerc(Double.valueOf(df.format((double)(rankList1.indexOf(gr)+1)/rankList1.size())));
					}
					for(GeneRank gr : rankList2){
						gr.setTotalCount(32745);
						gr.setMixturePerc(Double.valueOf(df.format((double)(rankList2.indexOf(gr)+1)/rankList2.size())));
					}
					for(GeneRank gr : rankList3){
						gr.setTotalCount(32745);
						gr.setMixturePerc(Double.valueOf(df.format((double)(rankList3.indexOf(gr)+1)/rankList3.size())));
					}
					for(GeneRank gr : rankList4){
						gr.setTotalCount(32745);
						gr.setMixturePerc(Double.valueOf(df.format((double)(rankList4.indexOf(gr)+1)/rankList4.size())));
					}
					for(GeneRank gr : rankList5){
						gr.setTotalCount(32745);
						gr.setMixturePerc(Double.valueOf(df.format((double)(rankList5.indexOf(gr)+1)/rankList5.size())));
					}
					for(GeneRank gr : rankList6){
						gr.setTotalCount(32745);
						gr.setMixturePerc(Double.valueOf(df.format((double)(rankList6.indexOf(gr)+1)/rankList6.size())));
					}
					
					geneRankDAO.create(rankList1);
					geneRankDAO.create(rankList2);
//					geneRankDAO.create(rankList3);
//					geneRankDAO.create(rankList4);
//					geneRankDAO.create(rankList5);
//					geneRankDAO.create(rankList6);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Gene getGeneId(String symbol){
		//根据symbol找对应的refseq
		//List<TxrRef> txrRefList = txrRefDAO.findByGeneSymbol(symbol);
	List<TxrRef> txrRefList = TxrRefCache.getInstance().getTxrRefBySymbol(symbol.toLowerCase());
	if(txrRefList ==null ||txrRefList.size()== 0){
	}else{
		boolean flag = true; 
		for(TxrRef tr : txrRefList){
			String refseq = tr.getRefseq();
			if(refseq !=null && !"".equals(refseq)){
					//根据refseq对应gene表txName字段 找geneId
					//Gene gene = geneDAO.getByName(refseq); 
				Gene gene = GeneCache.getInstance().getGeneByName(refseq);
				if(gene != null){
					return gene;
				}
			}
		}
	}
		return null;
	}
	
	public MiRNASample convertSampleToMiRNASample(Sample sample){
		MiRNASample miSample = new MiRNASample();
		miSample.setMiRNASampleId(miRNASampleDAO.getSequenceId(SourceType.TCGA));
		miSample.setBarCode(sample.getSampleCode());
		miSample.setSetType(sample.getSettype());
		miSample.setCell(sample.getCell());
		miSample.setSource(sample.getSource());
		miSample.setEtype(sample.getEtype());
		miSample.setCreateTimeStamp(sample.getCreateTiemStamp());
		miSample.setDeleted(0);
		miSample.setFactor(sample.getFactor());
		miSample.setLab(sample.getLab());
		miSample.setUrl(sample.getUrl());;
		return miSample;
	}

}
