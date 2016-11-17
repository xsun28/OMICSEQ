package com.omicseq.encode;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import net.sf.javaml.core.kdtree.KDTree;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import au.com.bytecode.opencsv.bean.ColumnPositionMappingStrategy;
import au.com.bytecode.opencsv.bean.CsvToBean;

import com.omicseq.common.GeneCountType;
import com.omicseq.concurrent.IThreadTaskPoolsExecutor;
import com.omicseq.concurrent.ThreadTaskPoolsExecutor;
import com.omicseq.concurrent.ThreadTaskPoolsFactory;
import com.omicseq.concurrent.WaitFutureTask;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;

public class BAMTest {

	public static void main(String[] args) {
		  try {
			  MongoDBManager.getInstance();
			  final IGeneDAO geneDAO = DAOFactory.getDAO(IGeneDAO.class);
			  ThreadTaskPoolsExecutor.getInstance().init();
			   IThreadTaskPoolsExecutor threadPoolsExecutor = ThreadTaskPoolsFactory.getThreadTaskPoolsExecutor();
		        List<WaitFutureTask<String>> taskList = new ArrayList<WaitFutureTask<String>>();
		        int threadCount = 32;
		        Semaphore semaphore = new Semaphore(threadCount);
		        for (int i = 0; i < threadCount; i++) {
		        	WaitFutureTask<String> consumMessageTask = new WaitFutureTask<String>(new Callable<String>(){
						@Override
						public String call() throws Exception {
							try {
								List<Gene> geneList = geneDAO.loadGeneList(0, 10);
								System.out.println();
								return "";
							} catch (Exception e) {
								e.printStackTrace();
								return "";
							}
						}
		        		
		        	}, null);
		        	taskList.add(consumMessageTask);
		        }
			    
		        threadPoolsExecutor.blockRun(taskList, 10L, TimeUnit.MINUTES);
			 // comare();
		  } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void comare()  throws Exception{
		String fileName = "E:\\projects\\omicseq-master\\txu\\counttest.csv";
		List<GeneRank> rankList = readGeneRank(fileName);
		String oldfileName = "E:\\tes5kcount.csv";
		List<GeneRank> compareRankList = readGeneRank(oldfileName);
		for (int i = 0; i < rankList.size(); i++) {
			GeneRank geneRank = rankList.get(i);
			GeneRank comparedGeneRank = compareRankList.get(i);
			if (Math.abs(comparedGeneRank.getTssTesCount() - geneRank.getTssTesCount()) > 0.00000002) {
				System.out.println(" id : " + i +"; " + comparedGeneRank.getTssTesCount() + "; " + geneRank.getTssTesCount());
			}
		}
	}
	
	private static List<GeneRank> readGeneRank(String fileName) throws Exception{
		CSVReader reader = new CSVReader(new FileReader(fileName), ',');
		// skip the one
		reader.readNext();
		ColumnPositionMappingStrategy<GeneRank> strat = new ColumnPositionMappingStrategy<GeneRank>();
		strat.setType(GeneRank.class);
		String[] columns = new String[] { "geneId", "tssTesCount"}; // the fields to bind do in your JavaBean
		strat.setColumnMapping(columns);
		
		CsvToBean<GeneRank> csv = new CsvToBean<GeneRank>();
		List<GeneRank> rankList = csv.parse(strat, reader);
		List<GeneRank> oldRankList = new ArrayList<GeneRank>();
		for (GeneRank geneRank : rankList) {
			geneRank.setSampleId(1);
			oldRankList.add(geneRank);
		}
		return oldRankList;
	}
	
	
	private static void wrtieGeneRank() throws Exception {
		CSVReader refReader = new CSVReader(new FileReader("E:\\projects\\omicseq-master\\txu\\ENCODE\\refseq.csv"), ',');
		// skip the one
		//refReader.readNext();
		ColumnPositionMappingStrategy<Gene> geneStrat = new ColumnPositionMappingStrategy<Gene>();
		geneStrat.setType(Gene.class);
		String[] geneColumns = new String[] {"geneId"}; // the fields to bind do in your JavaBean
		geneStrat.setColumnMapping(geneColumns);

		CsvToBean<Gene> geneCsv = new CsvToBean<Gene>();
		List<Gene> geneList = geneCsv.parse(geneStrat, refReader);
		
		CSVReader reader = new CSVReader(new FileReader("E:\\projects\\omicseq-master\\txu\\ENCODE\\count4.csv"), ',');
		// skip the one
		reader.readNext();
		ColumnPositionMappingStrategy<GeneRank> strat = new ColumnPositionMappingStrategy<GeneRank>();
		strat.setType(GeneRank.class);
		String[] columns = new String[] { "geneId", "tssTesCount", "tss5kCount", "tssT5Count" }; // the fields to bind do in your JavaBean
		strat.setColumnMapping(columns);
		
		CsvToBean<GeneRank> csv = new CsvToBean<GeneRank>();
		List<GeneRank> rankList = csv.parse(strat, reader);
		List<GeneRank> oldRankList = new ArrayList<GeneRank>();
		for (GeneRank geneRank : rankList) {
			geneRank.setSampleId(1);
			oldRankList.add(geneRank);
		}
		updateGeneRank(rankList, GeneCountType.tss_tes.name());
		updateGeneRank(rankList, GeneCountType.tss_5k.name());
		updateGeneRank(rankList, GeneCountType.tes_5k.name());
		
		CSVWriter csvGeneRankWriter = new CSVWriter(new FileWriter("E:/projects/omicseq-master/txu/ENCODE/GeneRank.csv"));
		csvGeneRankWriter.writeNext(new String[]{"s.id", "gene.id", "gene.tss.tes.rank", "gene.tss.tes.counts", "gene.tss.5k.rank", "gene.tss.5k.counts", "gene.tss.tes.5k.rank", "gene.tss.tes.5k.counts"});
		for (GeneRank geneRank : oldRankList) {
			Integer geneId = geneList.get(geneRank.getGeneId() - 1).getGeneId();
			String[] values = new String[] { String.valueOf(geneRank.getSampleId()),
					String.valueOf(geneId), 
					String.valueOf(geneRank.getTssTesRank()),
					String.valueOf(geneRank.getTssTesCount()),
					String.valueOf(geneRank.getTss5kRank()),
					String.valueOf(geneRank.getTss5kCount()),
					String.valueOf(geneRank.getTssT5Rank()),
					String.valueOf(geneRank.getTssT5Count()) };
			csvGeneRankWriter.writeNext(values);
		}
		csvGeneRankWriter.flush();
		csvGeneRankWriter.close();
	}

	private static void updateGeneRank(List<GeneRank> geneRankList, String countType) {
		Comparator<GeneRank> geneComparator = null;
		if (GeneCountType.tss_tes.name().equalsIgnoreCase(countType)) {
			geneComparator = new TssTesComparator();
		} else if (GeneCountType.tss_5k.name().equalsIgnoreCase(countType)) {
			geneComparator = new Tss5kComparator();
		} else {
			geneComparator = new TssTes5kComparator();
		}
		
		Collections.sort(geneRankList, geneComparator);
		// inverse update
		Map<Double, Integer> countRankMap = new HashMap<Double, Integer>();
		for (int i = 0; i < geneRankList.size(); i++) {
			GeneRank geneRank = geneRankList.get(i);
			Double count = null;
			if (GeneCountType.tss_tes.name().equalsIgnoreCase(countType)) {
				count = geneRank.getTssTesCount();
			} else if (GeneCountType.tss_5k.name().equalsIgnoreCase(countType)) {
				count = geneRank.getTss5kCount();
			} else {
				count = geneRank.getTssT5Count();
			}
			
			Integer postion = countRankMap.get(count);
			if (postion == null) {
				postion = i + 1;
				if (count == 0.0) {
					postion = geneRankList.size();
				}
				countRankMap.put(count, postion);
			}
			
			if (GeneCountType.tss_tes.name().equalsIgnoreCase(countType)) {
				geneRank.setTssTesRank(postion);
			} else if (GeneCountType.tss_5k.name().equalsIgnoreCase(countType)) {
				geneRank.setTss5kRank(postion);
			} else {
				geneRank.setTssT5Rank(postion);
			}
		}
	}
	
	static class TssTesComparator implements Comparator<GeneRank>{
		@Override
		public int compare(GeneRank o1, GeneRank o2) {
			Double tssTesCount1 = o1.getTssTesCount();
			Double tssTesCount2 = o2.getTssTesCount();
			return tssTesCount2.compareTo(tssTesCount1);
		}
	}
	
	static class Tss5kComparator implements Comparator<GeneRank>{
		@Override
		public int compare(GeneRank o1, GeneRank o2) {
			Double tss5kCount1 = o1.getTss5kCount();
			Double tss5kCount2 = o2.getTss5kCount();
			return tss5kCount2.compareTo(tss5kCount1);
		}
	}
	
	static class TssTes5kComparator implements Comparator<GeneRank>{
		@Override
		public int compare(GeneRank o1, GeneRank o2) {
			Double tssT5Count1 = o1.getTssT5Count();
			Double tssT5Count2 = o2.getTssT5Count();
			return tssT5Count2.compareTo(tssT5Count1);
		}
	}
	
	private static void updateCountMap(SAMRecord samRecord, KDTree  kdTree,  Map<Integer, Integer> countMap, Integer maxStart) {
		Integer start = samRecord.getAlignmentStart();
		Integer end = samRecord.getAlignmentEnd();
		//start <= gene.getStart() < maxstart
		//0< gene.getEnd() <= end
		//gene.getStart >= start
		Object[] resultArray = kdTree.range(new double[] { start.doubleValue(), 0.0 }, new double[] {
				maxStart.doubleValue(), end.doubleValue() });
		for (Object object : resultArray) {
			Gene matchGene = (Gene) object;
			Integer count = countMap.get(matchGene.getGeneId());
			if (count == null) {
				count = 0;
			}
			countMap.put(matchGene.getGeneId(), count + 1);
		}
	}
	
	public static Double getNumber(Double number) {
		BigDecimal b = new BigDecimal(number);
		Double f1 = b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
		return f1;
	}
	
	private static List<Gene> readGeneList(String fileName, String countType) {
		try {
			CSVReader reader = new CSVReader(new FileReader(fileName), ',');
			// skip the one
			reader.readNext();
			ColumnPositionMappingStrategy<Gene> strat = new ColumnPositionMappingStrategy<Gene>();
			strat.setType(Gene.class);
			String[] columns = new String[] {"seqName", "start", "end", "width", "strand"}; // the fields to bind do in your JavaBean
			strat.setColumnMapping(columns);

			CsvToBean<Gene> csv = new CsvToBean<Gene>();
			List<Gene> tssList = csv.parse(strat, reader);
			if (CollectionUtils.isNotEmpty(tssList)) {
				for (Gene gene : tssList) {
					gene.setCountType(countType);
				}
			}
			return tssList;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static void computeCount() throws Exception {
		String tesTssFileName = "E:/projects/omicseq/Gene.TSS.TES.csv";
		List<Gene> tesTssList = readGeneList(tesTssFileName, GeneCountType.tss_tes.name());
		System.out.println(tesTssList.get(0).toString());

		String tss5kFileName = "E:/projects/omicseq/Gene.TSS.5k.csv";
		List<Gene> tss5kList = readGeneList(tss5kFileName, GeneCountType.tss_5k.name());
		
		String tes5kFileName = "E:/projects/omicseq/Gene.TSS.TES.5k.csv";
		List<Gene> tes5kList = readGeneList(tes5kFileName, GeneCountType.tes_5k.name());
		
		System.out.println(" size is : " + tesTssList.size());
		
        // Open the input file.  Automatically detects whether input is SAM or BAM
        // and delegates to a reader implementation for the appropriate format.
		File file = new File("E:/projects/omicseq-master/txu/ENCODE/1.bam");
        final SAMFileReader inputSam = new SAMFileReader(file);
        // makeSAMorBAMWriter() writes a file in SAM text or BAM binary format depending
        // on the file extension, which must be either .sam or .bam.

        // Since the SAMRecords will be written in the same order as they appear in the input file,
        // and the output file is specified as having the same sort order (as specified in
        // SAMFileHeader.getSortOrder(), presorted == true.  This is much more efficient than
        // presorted == false, if coordinate or queryname sorting is specified, because the SAMRecords
        // can be written to the output file directly rather than being written to a temporary file
        // and sorted after all records have been sent to outputSam.
		
		/*
        final SAMFileWriter outputSam = new SAMFileWriterFactory().makeSAMOrBAMWriter(inputSam.getFileHeader(),
                true, outputSamOrBamFile);
                */
        List<String> valueList = new ArrayList<String>();
        
        //SAMRecordIterator sAMRecordIterator = inputSam.query("chrM", 0, 0, true);
        //SAMRecord sb = sAMRecordIterator.next();

        KDTree tssTesKdTree = new KDTree(2);
        Integer maxEnd = 0;
        Integer geneId = 0;
        Map<String, KDTree> kdTreeMap = new HashMap<String, KDTree>();
        Map<String, Integer> startEndKeyMap = new HashMap<String,Integer>();
        Set<Integer> usedGeneIdSet = new HashSet<Integer>();
        for (Gene gene : tesTssList) {
        	String key = StringUtils.trimToEmpty(gene.getSeqName()).toLowerCase()+ StringUtils.trimToEmpty(gene.getStrand()).toLowerCase();
        	KDTree kdTree = kdTreeMap.get(key);
        	if (kdTree == null) {
        		kdTree = new KDTree(2);
        		kdTreeMap.put(key, kdTree);
        	}
        	geneId = geneId + 1;
        	gene.setGeneId(geneId);
        	maxEnd = Math.max(maxEnd, gene.getEnd());
        	
        	String startEndKey = generateKey(gene, GeneCountType.tss_tes.name());
        	Integer updatedGeneId = startEndKeyMap.get(startEndKey);
        	if (updatedGeneId != null) {
        		continue;
        	} else {
        		startEndKeyMap.put(startEndKey, geneId);
        		usedGeneIdSet.add(geneId);
        	}
        	kdTree.insert(new double[]{gene.getStart().doubleValue(), gene.getEnd().doubleValue()}, gene);
        }
        /*
       for (Object ob : tssTesKdTree.range(new double[]{11873.0, 11873.0}, new double[]{14410.0,14410.0})) {
    	   Gene gene = (Gene)ob;
    	   System.out.println(gene.toString());
       }
       */
        
        
        //KDTree tss5kKdTree = new KDTree(2);
        Integer updatedGeneId = 0;
        for (Gene gene : tss5kList) {
        	updatedGeneId = updatedGeneId  + 1;
        	gene.setGeneId(updatedGeneId);
    		String key = StringUtils.trimToEmpty(gene.getSeqName()).toLowerCase()+ StringUtils.trimToEmpty(gene.getStrand()).toLowerCase();
        	KDTree kdTree = kdTreeMap.get(key);
        	String startEndKey = generateKey(gene, GeneCountType.tss_5k.name());
        	Integer oldId = startEndKeyMap.get(startEndKey);
        	if (oldId == null) {
        		startEndKeyMap.put(startEndKey, gene.getGeneId());
        		kdTree.insert(new double[]{gene.getStart().doubleValue(), gene.getEnd().doubleValue()}, gene);
        	} 
        }
        
        //KDTree tes5kKdTree = new KDTree(2);
        Integer updatedTes5kGeneId = 0;
        for (Gene gene : tes5kList) {
        	updatedTes5kGeneId = updatedTes5kGeneId  + 1;
        	gene.setGeneId(updatedTes5kGeneId);
    		String key = StringUtils.trimToEmpty(gene.getSeqName()).toLowerCase()+ StringUtils.trimToEmpty(gene.getStrand()).toLowerCase();
        	KDTree kdTree = kdTreeMap.get(key);
        	String startEndKey = generateKey(gene, GeneCountType.tes_5k.name());
        	Integer oldId = startEndKeyMap.get(startEndKey);
        	if (oldId == null) {
        		startEndKeyMap.put(startEndKey, gene.getGeneId());
        		kdTree.insert(new double[]{gene.getStart().doubleValue(), gene.getEnd().doubleValue()}, gene);
        	} 
        	
        }
        
        SAMRecordIterator  saMRecordIterator = inputSam.iterator();
        Map<String, Integer> countMaps = new HashMap<String, Integer>();
        Integer size = 0;
        Integer realCount = 0;
        Long startTime = System.currentTimeMillis();
        while(true) {
        	try {
        		SAMRecord samRecord = saMRecordIterator.next();
        		size = size + 1;
	        	if (samRecord == null) {
	        		break;
	        	}
	        	String ref = samRecord.getReferenceName();
	        	if (ref.equalsIgnoreCase("random")) {
	        		continue;
	        	}
	        	if (ref.equalsIgnoreCase("hap")) {
	        		continue;
	        	}
	        	if (ref.equalsIgnoreCase("chrM")) {
	        		continue;
	        	}
	        	if (samRecord.getReadUnmappedFlag()) {
	        		continue;
	        	}
	        	Integer start = samRecord.getAlignmentStart();
	        	Integer end = samRecord.getAlignmentStart() + samRecord.getReadLength() - 1;
	        	//0<gene.start<start;
	        	//end<gene.end<maxend
	        	String strand = "+";
	        	if (samRecord.getFlags() == 16) {
	        		strand = "-";
	        	}
	        	KDTree kdTree = kdTreeMap.get(StringUtils.trimToEmpty(ref).toLowerCase()+strand);
	        	if (kdTree == null) {
	        		continue;
	        	}
	        	// contain:new double[]{0, end.doubleValue()}, new double[]{start.doubleValue(), maxEnd}
	        	// overlap:
	        	//Object[] geneList = kdTree.range(new double[]{0, end.doubleValue()}, new double[]{start.doubleValue(), maxEnd});
	        	Object[] geneList = kdTree.range(new double[]{0, start.doubleValue()}, new double[]{end.doubleValue(), Integer.MAX_VALUE});
	        	for (Object ob : geneList) {
	        		Gene gene = (Gene)ob;
	        		String countType = gene.getCountType();
	        		String genekey = generateKey(gene, countType);
	        		Integer count = countMaps.get(genekey);
	        		if (count == null) {
	        			count = 0;
	        		}
	        		count = count + 1;
	        		countMaps.put(genekey, count);
	        	}
	        	
	        	realCount = realCount + 1;
	        	if (size % 10000 == 0) {
	        		System.out.println(" prcocess zie : " + size + "; using time : " + String.valueOf(System.currentTimeMillis() - startTime));
	        	}
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
        
       //NavigableMap<Integer, Gene> subGeneMap = startTreeMap.tailMap(362659, true);
       //System.out.println("start" + subGeneMap.keySet());;
       
       //NavigableMap<Integer, Gene> startNaviMap = startTreeMap.descendingMap();
       //NavigableMap<Integer, Gene> endNaviMap = endTreeMap.descendingMap();
       
       // Integer size = 0;
        //Long startTime = System.currentTimeMillis();
        CSVWriter csvWriter = new CSVWriter(new FileWriter("E:/projects/omicseq-master/txu/ENCODE/count4.csv"));
        csvWriter.writeNext(new String[]{"geneId", GeneCountType.tss_tes.name(), GeneCountType.tss_5k.name(), GeneCountType.tes_5k.name()});
        List<String> usedCountTypeList = new ArrayList<String>();
        usedCountTypeList.add(GeneCountType.tss_tes.name());
        usedCountTypeList.add(GeneCountType.tss_5k.name());
        usedCountTypeList.add(GeneCountType.tes_5k.name());
        Double factor = Math.pow(10, 9) / realCount.doubleValue();
        List<GeneRank> geneRankList = new ArrayList<GeneRank>();
        for (int i = 0; i < tesTssList.size(); i++) {
        	//String key = StringUtils.trimToEmpty(gene.getSeqName()).toLowerCase()+ StringUtils.trimToEmpty(gene.getStrand()).toLowerCase();
        	Integer insertGeneId = i + 1;
        	String[] values = new String[1 + usedCountTypeList.size()];
        	values[0] = insertGeneId.toString();
        	int index = 1;
        	GeneRank geneRank = new GeneRank();
        	geneRank.setSampleId(1);
        	geneRank.setGeneId(insertGeneId);
        	for (String countType : usedCountTypeList) {
        		Gene gene = null;
        		if (GeneCountType.tes_5k.name().equalsIgnoreCase(countType)) {
        			gene = tes5kList.get(i);
        		} else if (GeneCountType.tss_5k.name().equalsIgnoreCase(countType)) {
        			gene = tss5kList.get(i);
        		} else {
        			gene =  tesTssList.get(i);
        		}
        		
        		String startEndKey = generateKey(gene, countType);
        		Integer count = countMaps.get(startEndKey);
	        	if (count == null) {
	        		count = 0;
	        	}
	        	Integer width = gene.getWidth();
	        	Double factorCount = (count.doubleValue() / width.doubleValue()) * factor;
	        	if (GeneCountType.tss_tes.name().equalsIgnoreCase(countType)) {
	        		geneRank.setTssTesCount(count.doubleValue());
	        	} else if (GeneCountType.tss_5k.name().equalsIgnoreCase(countType)) {
	        		geneRank.setTssT5Count(count.doubleValue());
	        	} else {
	        		geneRank.setTssT5Count(count.doubleValue());
	        	}
	        	values[index] = String.valueOf(factorCount);
	        	index = index + 1;
        	}
        	geneRankList.add(geneRank);
        	csvWriter.writeNext(values);
        	
        }
       
        csvWriter.close();
       CSVWriter csvGeneRankWriter = new CSVWriter(new FileWriter("E:/projects/omicseq-master/txu/ENCODE/GeneRank.csv"));
       for (GeneRank geneRank : geneRankList) {
    	   String[] values = new String[]{String.valueOf(geneRank.getSampleId()), String.valueOf(geneRank.getGeneId()),
    			   String.valueOf(geneRank.getTssTesRank()), String.valueOf(geneRank.getTssTesCount()), 
    			   String.valueOf(geneRank.getTss5kRank()), String.valueOf(geneRank.getTss5kCount()), String.valueOf(geneRank.getTssT5Rank()), 
    			   String.valueOf(geneRank.getTssT5Count())};
    	   csvGeneRankWriter.writeNext(values);
       }
       csvGeneRankWriter.flush();
       csvGeneRankWriter.close();
	}
	
	private static String generateKey(Gene gene, String type) {
		String key = StringUtils.trimToEmpty(gene.getSeqName()).toLowerCase() + StringUtils.trimToEmpty(gene.getStrand()).toLowerCase();
		return key + "_" +gene.getStart()+ "_" + gene.getEnd();
	}
	
}
