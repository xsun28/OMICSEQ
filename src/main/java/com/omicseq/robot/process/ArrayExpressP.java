package com.omicseq.robot.process;

import java.awt.geom.GeneralPath;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.core.GeneCache;
import com.omicseq.core.SampleCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.domain.StatisticInfo;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.IStatisticInfoDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class ArrayExpressP {
	private static IStatisticInfoDAO statisticInfoDAO = DAOFactory.getDAO(IStatisticInfoDAO.class);
	private static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
//	private String root = "D:/files/download/arrayexpress/";
	private String root = "/files/download/arrayexpress/";
	protected List<String> fileList = Collections.synchronizedList(new ArrayList<String>());
	private Map<String,Integer> geneIdMap = Collections.synchronizedMap(new HashMap<String,Integer>());
	private DecimalFormat df = new DecimalFormat("#.00000");
	private Map<String,String> ensg = Collections.synchronizedMap(new HashMap<String, String>());
	
	public ArrayExpressP(){
		File file = new File(root);
		String [] files = file.list();
		//筛选解析文件的格式
		for(String fileName : files){
			if(fileName.endsWith(".txt")){
				fileList.add(fileName);
			}
		}
		DBCollection collection = MongoDBManager.getInstance().getCollection("manage", "manage", "txrref_temp");
		DBCursor cursor = collection.find();
		Set<Integer>  geneIdSet = new HashSet<Integer>();
		while(cursor.hasNext()){
			String symbol = (String)cursor.next().get("geneSymbol");
			List<TxrRef> txrRefList = TxrRefCache.getInstance().getTxrRefBySymbol(symbol.toLowerCase());
			if(CollectionUtils.isNotEmpty(txrRefList)){
				for(TxrRef tr : txrRefList){
					String refseq = tr.getRefseq();
					if(refseq !=null && !"".equals(refseq)){
						Gene gene = GeneCache.getInstance().getGeneByName(refseq);
						if(gene != null && geneIdSet.add(gene.getGeneId())){
							geneIdMap.put(symbol, gene.getGeneId());
							break;
						}
					}
				}
			}
		}
		
		DBCollection collection1 = MongoDBManager.getInstance().getCollection("manage", "manage", "hashdbensemblgene");
		DBCursor cursor1 = collection1.find();
		while(cursor1.hasNext()){
			DBObject obj = cursor1.next();
			String key = (String)obj.get("key");
			String value = (String)obj.get("value");
			ensg.put(key, value) ;			
		}
		
	}
	
	public synchronized String getFile() {
		String fileName = fileList.get(0);
		fileList.remove(fileName);
		return fileName;
	}
	public void  parser(ArrayExpMain aem){
		if(CollectionUtils.isNotEmpty(fileList) ){
			String fileName = getFile();
			SmartDBObject query = new SmartDBObject();
			query.put("source",SourceType.ArrayExpress.getValue());
			query.put("path", "/files/download/arrayexpress/" + fileName);
			List<StatisticInfo> siList = statisticInfoDAO.find(query);
		
			for(StatisticInfo si : siList){
				try{
					if(si.getState() != 0) continue;
					geneRankDAO.removeBySampleId(si.getSampleId());
					Sample sample = SampleCache.getInstance().getSampleById(si.getSampleId());
					BufferedReader br = new BufferedReader(new FileReader(root + fileName));
					String line = null;
					Integer num = 0;
					Integer symbolColNum = null;
					Integer readColNum = null ;
					boolean situation = false;
					List<SymbolReader> srs = new ArrayList<SymbolReader>();
					while((line = br.readLine()) != null){
						num++ ;
						Integer geneId = null;
						Double read = null;
						List<String> tmp = Arrays.asList(line.split("\t"));
						SymbolReader sr = new SymbolReader();
						if(num == 1){
							if(tmp.contains("#Gene symbol") && tmp.contains("RPKM")){
								symbolColNum = tmp.indexOf("#Gene symbol");
								readColNum = tmp.indexOf("RPKM");
								situation = true;
							}
							if(tmp.contains("Gene") && tmp.contains("Rpkm")){
								symbolColNum = tmp.indexOf("Gene");
								readColNum = tmp.indexOf("Rpkm");
								if(readColNum == null) continue;
								situation = true;
								continue;
							}
							if(fileName.endsWith(".RNAseq.txt") && tmp.contains("tracking_id") && tmp.contains("FPKM")){
								symbolColNum = tmp.indexOf("tracking_id");
								readColNum = tmp.indexOf("FPKM");
								situation = true;
								continue;
							}
							if(fileName.endsWith(".fpkm_tracking.txt") && tmp.contains("gene_id") && tmp.contains("FPKM")){
								symbolColNum = tmp.indexOf("gene_id");
								readColNum = tmp.indexOf("FPKM");
								situation = true;
								continue;
							}
							
						}
						if(!situation) break;
						String symbol = tmp.get(symbolColNum);
						if(symbol.startsWith("ENSG")){
							String refseq = ensg.get(symbol);
							Gene gene = GeneCache.getInstance().getGeneByName(refseq);
							if(gene == null ) continue;
							geneId = gene.getGeneId();
						}else{
							geneId = geneIdMap.get(symbol);
						}
						if(geneId == null) continue;
						read = Double.parseDouble(tmp.get(readColNum));
						sr.setGeneId(geneId);
						sr.setRead(read);
						srs.add(sr);
					}
					System.out.println();
					
					if(CollectionUtils.isEmpty(srs)) continue;
					Collections.sort(srs, new Comparator<SymbolReader>() {
						@Override
						public int compare(SymbolReader o1, SymbolReader o2) {
							return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead()))) *(-1);
						}
					});
					List<GeneRank> geneRanks = new ArrayList<GeneRank>();
					for(SymbolReader sr : srs){
						GeneRank gr = new GeneRank();
						gr.setCreatedTimestamp(System.currentTimeMillis());
						gr.setEtype(sample.getEtype());
						gr.setSource(sample.getSource());
						gr.setGeneId(sr.getGeneId());
						int size = srs.size()>10000?srs.size():27000;
						gr.setMixturePerc(Double.parseDouble(df.format((double)(srs.indexOf(sr)+1)/size)));
						//Tsstescount读数
						gr.setTssTesCount(sr.getRead());
						gr.setTotalCount(srs.size());
						//gr.setSampleId(sampleIds.get(i));
						gr.setSampleId(sample.getSampleId());
						geneRanks.add(gr);
					}
					geneRankDAO.create(geneRanks);
					si.setState(99);
					statisticInfoDAO.update(si);
				}catch (Exception e) {
					
				}
			}
			
		}
	}
	
	public static void main(String[] args) {
		List<SymbolReader> srs = new ArrayList<SymbolReader>();
		SymbolReader sr1= new SymbolReader();
		sr1.setRead(1.0);
		SymbolReader sr2= new SymbolReader();
		sr2.setRead(2.0);
		srs.add(sr1);
		srs.add(sr2);
		for(int i=0;i<srs.size();i++){
			SymbolReader sr= srs.get(i);
			sr.setGeneId(1);
		}
		System.out.println(srs.get(0).getGeneId());
	}
	
}
