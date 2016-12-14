package com.omicseq.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

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
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;

public class pausing_xlsx {
	private File file = new File("C:/Users/Administrator/Desktop/11111.xls");
	private ITxrRefDAO txrRefDAO = DAOFactory.getDAO(ITxrRefDAO.class);
	private IGeneDAO geneDAO = DAOFactory.getDAO(IGeneDAO.class);
	private ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);
	private ISampleDAO sampleNewDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
	private IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	
	DecimalFormat   df   =new   DecimalFormat("#.00000");  

	/**
	 * 
	 */
	public void parse(){
		List<Sample> sampleList = new ArrayList<Sample>();
		Map<String, String> map = new HashMap<String, String>();
		map.put("Organism", "Homo sapiens");
		map.put("lastUpdate", "Feb 12, 2015");
		map.put("Organization nam", "University of Eastern Finland");
		map.put("", "minna.kaikkonen@uef.fi");
//		for(int i = 0; i<6;i++){
//			Sample sample = new Sample();
//			sample.descMap(map);
//			sample.setSampleId(sampleDAO.getSequenceId(SourceType.GEO));
//			sample.setCell("HUVECs and HAECs");
//			sample.setSource(SourceType.GEO.value());
//			sample.setEtype(ExperimentType.GRO_SEQ.value());
//			sample.setDeleted(0);
//			sample.setDetail("HUVECs and HAECs tumor");
//			sample.setPubmedUrl("http://www.ncbi.nlm.nih.gov/pubmed/25352550");
//			System.out.println(sample.getSampleId());
//			sampleList.add(sample);
//		}
//		sampleNewDAO.create(sampleList);
		try {
			List<GeneRank> geneRank1 = new ArrayList<GeneRank>();
			List<GeneRank> geneRank2 = new ArrayList<GeneRank>();
			List<GeneRank> geneRank3 = new ArrayList<GeneRank>();
			List<GeneRank> geneRank4 = new ArrayList<GeneRank>();
			List<GeneRank> geneRank5 = new ArrayList<GeneRank>();
			List<GeneRank> geneRank6 = new ArrayList<GeneRank>();
			InputStream inputStream = new FileInputStream(file);
			Workbook workBook = new HSSFWorkbook(inputStream);
			Sheet sheet = workBook.getSheetAt(0);
			Set<Integer > set = new HashSet<Integer>();
			for(int i = 1; i <= sheet.getLastRowNum(); i++){
				
				System.out.println(i);
				Row row = sheet.getRow(i);
				String txName = row.getCell(0).getStringCellValue();
				Double read1 = row.getCell(15).getNumericCellValue();
				Double read2 = row.getCell(18).getNumericCellValue();
				Double read3 = row.getCell(21).getNumericCellValue();
				Double read4 = row.getCell(24).getNumericCellValue();
				Double read5 = row.getCell(27).getNumericCellValue();
				Double read6 = row.getCell(30).getNumericCellValue();
				Gene gene = GeneCache.getInstance().getGeneByName(txName);
				if(gene == null) continue;
				Integer geneId = gene.getGeneId();
				if(!set.add(geneId)) continue;
				GeneRank gr1 = new GeneRank();
				gr1.setGeneId(geneId);
				gr1.setSampleId(614047);
//				gr1.setSampleId(sampleList.get(0).getSampleId());
				gr1.setEtype(ExperimentType.GRO_SEQ.getValue());
				gr1.setSource(SourceType.GEO.getValue());
				gr1.setTssTesCount(read1);
				geneRank1.add(gr1);
				
				GeneRank gr2 = new GeneRank();
				gr2.setGeneId(geneId);
//				gr2.setSampleId(sampleList.get(1).getSampleId());
				gr2.setSampleId(614048);
				gr2.setEtype(ExperimentType.GRO_SEQ.getValue());
				gr2.setSource(SourceType.GEO.getValue());
				gr2.setTssTesCount(read2);
				geneRank2.add(gr2);
				
				GeneRank gr3 = new GeneRank();
				gr3.setGeneId(geneId);
//				gr3.setSampleId(sampleList.get(2).getSampleId());
				gr3.setSampleId(614049);
				gr3.setEtype(ExperimentType.GRO_SEQ.getValue());
				gr3.setSource(SourceType.GEO.getValue());
				gr3.setTssTesCount(read3);
				geneRank3.add(gr3);
				
				GeneRank gr4 = new GeneRank();
				gr4.setGeneId(geneId);
				gr4.setSampleId(614050);
//				gr4.setSampleId(sampleList.get(3).getSampleId());
				gr4.setEtype(ExperimentType.GRO_SEQ.getValue());
				gr4.setSource(SourceType.GEO.getValue());
				gr4.setTssTesCount(read4);
				geneRank4.add(gr4);
				
				GeneRank gr5 = new GeneRank();
				gr5.setGeneId(geneId);
				gr5.setSampleId(614051);
//				gr5.setSampleId(sampleList.get(4).getSampleId());
				gr5.setEtype(ExperimentType.GRO_SEQ.getValue());
				gr5.setSource(SourceType.GEO.getValue());
				gr5.setTssTesCount(read5);
				geneRank5.add(gr5);
				
				GeneRank gr6 = new GeneRank();
				gr6.setGeneId(geneId);
				gr6.setSampleId(614052);
				gr6.setEtype(ExperimentType.GRO_SEQ.getValue());
				gr6.setSource(SourceType.GEO.getValue());
				gr6.setTssTesCount(read6);
				geneRank6.add(gr6);
				
			/*	List<TxrRef> list = txrRefDAO.find(new SmartDBObject("geneSymbol",symbol));
				boolean flag = true;
				for(TxrRef ref : list){
					if(ref.getRefseq() == null) continue;
					if(ref.getRefseq().equals(txName)){
						flag = false;
						break;
					}
				}
				List<Gene> geneList = geneDAO.find(new SmartDBObject("txName",txName));
				if(flag){
					if(CollectionUtils.isEmpty(list) && CollectionUtils.isEmpty(geneList)) continue;
					if(!CollectionUtils.isEmpty(geneList)){
						TxrRef txrRef = new TxrRef();
						txrRef.setGeneSymbol(symbol);
						txrRef.setRefseq(txName);
						txrRefDAO.create(txrRef);
					}
				}else{
					if(CollectionUtils.isEmpty(geneList) && !CollectionUtils.isEmpty(list)){
						Gene gene = getGeneId(symbol);
						if(gene == null) continue;
						Gene gene1 = new Gene();
						gene1.setGeneId(gene.getGeneId());
						gene1.setTxName(gene.getTxName());
						gene1.setSeqName(gene.getSeqName());
						gene1.setStart(gene.getStart());
						gene1.setStrand(gene.getStrand());
						gene1.setEnd(gene.getEnd());
						gene1.setEntrezId(gene.getEntrezId());
						gene1.setExonLength(gene.getExonLength());
						gene1.setExonNum(gene.getExonNum());
						gene1.setGeneLength(gene.getGeneLength());
						gene1.setGeneName(gene.getGeneName());
						gene1.setRelKey(gene.getRelKey());
						gene1.setWidth(gene.getWidth());
					}
				}*/
			}
			
			Collections.sort(geneRank1, new Comparator<GeneRank>() {

				@Override
				public int compare(GeneRank o1, GeneRank o2) {
					return (o1.getTssTesCount().compareTo(o2.getTssTesCount())) * (-1);
				}
			});
			Collections.sort(geneRank2, new Comparator<GeneRank>() {

				@Override
				public int compare(GeneRank o1, GeneRank o2) {
					return (o1.getTssTesCount().compareTo(o2.getTssTesCount())) * (-1);
				}
			});
			Collections.sort(geneRank3, new Comparator<GeneRank>() {

				@Override
				public int compare(GeneRank o1, GeneRank o2) {
					return (o1.getTssTesCount().compareTo(o2.getTssTesCount())) * (-1);
				}
			});
			Collections.sort(geneRank4, new Comparator<GeneRank>() {

				@Override
				public int compare(GeneRank o1, GeneRank o2) {
					return (o1.getTssTesCount().compareTo(o2.getTssTesCount())) * (-1);
				}
			});
			
			Collections.sort(geneRank5, new Comparator<GeneRank>() {

				@Override
				public int compare(GeneRank o1, GeneRank o2) {
					return (o1.getTssTesCount().compareTo(o2.getTssTesCount())) * (-1);
				}
			});
			
			Collections.sort(geneRank6, new Comparator<GeneRank>() {

				@Override
				public int compare(GeneRank o1, GeneRank o2) {
					return (o1.getTssTesCount().compareTo(o2.getTssTesCount())) * (-1);
				}
			});
			for(GeneRank gr : geneRank1){
				gr.setCreatedTimestamp(System.currentTimeMillis());
				gr.setMixturePerc(Double.parseDouble(df.format((double)(geneRank1.indexOf(gr)+1)/32745)));
				gr.setTotalCount(32745);
			}
			for(GeneRank gr : geneRank2){
				gr.setCreatedTimestamp(System.currentTimeMillis());
				gr.setMixturePerc(Double.parseDouble(df.format((double)(geneRank2.indexOf(gr)+1)/32745)));
				gr.setTotalCount(32745);
			}
			for(GeneRank gr : geneRank3){
				gr.setCreatedTimestamp(System.currentTimeMillis());
				gr.setMixturePerc(Double.parseDouble(df.format((double)(geneRank3.indexOf(gr)+1)/32745)));
				gr.setTotalCount(32745);
			}
			for(GeneRank gr : geneRank4){
				gr.setCreatedTimestamp(System.currentTimeMillis());
				gr.setMixturePerc(Double.parseDouble(df.format((double)(geneRank4.indexOf(gr)+1)/32745)));
				gr.setTotalCount(32745);
			}
			for(GeneRank gr : geneRank5){
				gr.setCreatedTimestamp(System.currentTimeMillis());
				gr.setMixturePerc(Double.parseDouble(df.format((double)(geneRank5.indexOf(gr)+1)/32745)));
				gr.setTotalCount(32745);
			}
			for(GeneRank gr : geneRank6){
				gr.setCreatedTimestamp(System.currentTimeMillis());
				gr.setMixturePerc(Double.parseDouble(df.format((double)(geneRank6.indexOf(gr)+1)/32745)));
				gr.setTotalCount(32745);
			}
			geneRankDAO.create(geneRank1);
			geneRankDAO.create(geneRank2);
			geneRankDAO.create(geneRank3);
			geneRankDAO.create(geneRank4);
			geneRankDAO.create(geneRank5);
			geneRankDAO.create(geneRank6);
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

	public static void main(String[] args) {
		GeneCache.getInstance().doInit();
//		TxrRefCache.getInstance().doInit();
		new pausing_xlsx().parse();
	}
}
