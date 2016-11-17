package com.omicseq.statistic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.Hash;
import com.omicseq.common.GeneCountType;
import com.omicseq.common.SourceType;
import com.omicseq.core.EntrezeSymbolCache;
import com.omicseq.core.GeneCache;
import com.omicseq.core.SampleCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.ObjectPair;
import com.omicseq.domain.Sample;
import com.omicseq.domain.StatisticResult;
import com.omicseq.domain.TxrRef;
import com.omicseq.exception.StatisticException;
import com.omicseq.robot.process.SymbolReader;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;


public class ArrayExpressStatistic implements IRankStatistic{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Map<String, String > ensemblgene = new HashMap<String,String>();
	DBCollection collection = MongoDBManager.getInstance().getCollection("manage", "manage","hashdbensemblgene");
	DecimalFormat df = new DecimalFormat("#.00000");
	private static List<ObjectPair<String, GeneCountType>> genePostionPairList = new ArrayList<ObjectPair<String, GeneCountType>>();
	
	
	static {
		genePostionPairList.add(new ObjectPair<String, GeneCountType>("Gene.TSS.TES.csv", GeneCountType.tss_tes));
	}
	
	public ArrayExpressStatistic() {
		super();
	}
	@Override
	public StatisticResult computeRank(
			List<ObjectPair<Integer, String>> sampleIdPairList) {
		if (CollectionUtils.isEmpty(sampleIdPairList)) {
			StatisticResult statisticResult = new StatisticResult(); 
			statisticResult.setGeneRankList(Collections.EMPTY_LIST);
			return statisticResult;
		}
		
		List<GeneRank> geneRankList = new ArrayList<GeneRank>();
		for (ObjectPair<Integer, String> objectPair : sampleIdPairList) {
			StatisticResult statisticResult = computeRank(objectPair.getObject2(), objectPair.getObject1());
			geneRankList.addAll(statisticResult.getGeneRankList());
		}
		StatisticResult statisticResult = new StatisticResult(); 
		statisticResult.setGeneRankList(geneRankList);
		
		Collections.sort(geneRankList, ComparatorFactory.getGeneIdComparator());
		return statisticResult;
	}
	
	public Integer getGeneIdByGeneSymbol(String geneSymbol){
		Integer geneId = null;
		List<TxrRef>  txrRefList = TxrRefCache.getInstance().getTxrRefBySymbol(geneSymbol);
		if (CollectionUtils.isEmpty(txrRefList) && geneSymbol.startsWith("ENSG")) {
			if(ensemblgene.size()==0){
				DBCursor cursor = collection.find();
				while(cursor.hasNext()){
					DBObject obj = cursor.next();
					ensemblgene.put((String)obj.get("key"),(String)obj.get("value"));
				}
			}
			String refSeq = ensemblgene.get(geneSymbol);
			Gene gene = GeneCache.getInstance().getGeneByName(refSeq);
			if(gene != null){
				geneId = gene.getGeneId();
			}
		}
		if (CollectionUtils.isNotEmpty(txrRefList)) {
			for (TxrRef txrRef : txrRefList) {
				if(txrRef != null){
					Gene gene = GeneCache.getInstance().getGeneByName(txrRef.getRefseq());
					if(gene != null){
						geneId = gene.getGeneId();
					}
				}
			}
		}
		return geneId ;
	}
	
	
	
	@Override
	public StatisticResult computeRank(String samplePath, Integer sampleId) {
		StatisticResult statisticResult = new StatisticResult();
		IGeneRankDAO geneRankDao = DAOFactory.getDAO(IGeneRankDAO.class);
		try {
			if (logger.isDebugEnabled()) {
				logger.debug(" print sample path : " + samplePath);
			}
			@SuppressWarnings("resource")
			BufferedReader bufferReader = new BufferedReader(new FileReader(samplePath));
			List<GeneRank> geneRankList = new ArrayList<GeneRank>();
			
			Sample sample = SampleCache.getInstance().get(sampleId);
			if (sample == null) {
				throw new StatisticException(" can't find sample for " + sampleId);
			}
			
			int line_num = 0;
			String line = null;
			boolean isFirstLine = false;
			Set<Integer> geneIdSet = new HashSet<Integer>();
			while((line = bufferReader.readLine()) != null){
				line_num++;
				Pattern p = Pattern.compile("\"(.*?)\"");
				Matcher m = p.matcher(line);
				ArrayList<String> strs = new ArrayList<String>();
				while(m.find()) {
					strs.add(m.group(1).replaceAll(",", " "));
					line = line.replace(m.group(1), m.group(1).replaceAll(",", " "));
				}
				line = line.replaceAll("\"", "");

				String [] vals = line.split(",");
				if(samplePath.endsWith("_GE_.csv")){
					continue;
				}
				else if(samplePath.endsWith("_genes.csv")){
					if(line_num > 1){
						if(vals[13].equalsIgnoreCase("fail")) continue;
						String symbol = vals[1].trim();
						Integer geneId = getGeneIdByGeneSymbol(symbol);
						if(geneId != null && geneIdSet.add(geneId)){
							GeneRank geneRank = new GeneRank();
							geneRank.setSampleId(sampleId);
							geneRank.setCreatedTimestamp(System.currentTimeMillis());
							geneRank.setEtype(sample.getEtype());
							geneRank.setSource(sample.getSource());
							geneRank.setGeneId(geneId);
							geneRank.setTssTesCount(Double.parseDouble(vals[10]));
							geneRankList.add(geneRank);
						}
					}
				}
				else if(samplePath.endsWith("_GeneStatus.csv")){
					if(line.contains("Percent of  All Unique Matches") || line.contains("Gene  Name")){
						isFirstLine = true;
						continue;
					}
					if(isFirstLine){
						String symbol = vals[0].trim();
						Integer geneId = getGeneIdByGeneSymbol(symbol);
						if(geneId != null && geneIdSet.add(geneId)){
							GeneRank geneRank = new GeneRank();
							geneRank.setSampleId(sampleId);
							geneRank.setCreatedTimestamp(System.currentTimeMillis());
							geneRank.setEtype(sample.getEtype());
							geneRank.setSource(sample.getSource());
							geneRank.setGeneId(geneId);
							geneRank.setTssTesCount(Double.parseDouble(vals[2]));
							geneRankList.add(geneRank);
						}
					}
				}
				else if(samplePath.endsWith("clusters.csv") || samplePath.endsWith("groups.csv") || samplePath.contains("deep")){
					continue;
				}
				else if(samplePath.endsWith("-reads-count-rpkm.csv")){
					if(line_num > 1){
						String symbol = vals[0].trim();
						Integer geneId = getGeneIdByGeneSymbol(symbol);
						if(geneId != null && geneIdSet.add(geneId)){
							GeneRank geneRank = new GeneRank();
							geneRank.setSampleId(sampleId);
							geneRank.setCreatedTimestamp(System.currentTimeMillis());
							geneRank.setEtype(sample.getEtype());
							geneRank.setSource(sample.getSource());
							geneRank.setGeneId(geneId);
							
							geneRank.setTssTesCount(Double.parseDouble(vals[2]));
							geneRankList.add(geneRank);
						}
					}
				}
				else if(samplePath.contains("_HeLa-miR-1")){
					if(line_num > 1){
						String symbol = vals[2].trim();
						Integer geneId = getGeneIdByGeneSymbol(symbol);
						if(geneId != null && geneIdSet.add(geneId)){
							GeneRank geneRank = new GeneRank();
							geneRank.setSampleId(sampleId);
							geneRank.setCreatedTimestamp(System.currentTimeMillis());
							geneRank.setEtype(sample.getEtype());
							geneRank.setSource(sample.getSource());
							geneRank.setGeneId(geneId);
							geneRank.setTssTesCount(Double.parseDouble(vals[7]));
							geneRankList.add(geneRank);
						}
					}
				}
				else if(samplePath.endsWith("fpkm")){
					if(line_num > 1){
						String symbol = vals[0].trim();
						Integer geneId = getGeneIdByGeneSymbol(symbol);
						if(geneId != null && geneIdSet.add(geneId)){
							GeneRank geneRank = new GeneRank();
							geneRank.setSampleId(sampleId);
							geneRank.setCreatedTimestamp(System.currentTimeMillis());
							geneRank.setEtype(sample.getEtype());
							geneRank.setSource(sample.getSource());
							geneRank.setGeneId(geneId);
							geneRank.setTssTesCount(Double.parseDouble(vals[1]));
							geneRankList.add(geneRank);
						}
					}
				}
			}
			
			Collections.sort(geneRankList, new Comparator<GeneRank>() {
				@Override
				public int compare(GeneRank o1, GeneRank o2) {
					//return o1.getRead().compareTo(o2.getRead()) *(-1);
					return new Double(Math.abs(o1.getTssTesCount())).compareTo(new Double(Math.abs(o2.getTssTesCount()))) *(-1);
				}
			});
			
			for(GeneRank geneRank : geneRankList){
				if(geneRankList.size() < 32745){
					geneRank.setTotalCount(32745);
				}
				geneRank.setMixturePerc(Double.parseDouble(df.format((double)(geneRankList.indexOf(geneRank)+1)/32745)));
			}
			statisticResult.setGeneRankList(geneRankList);
			statisticResult.setReadCount(32745);
			geneRankDao.removeBySampleId(sampleId);
			return statisticResult;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
