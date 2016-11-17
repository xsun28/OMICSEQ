package com.omicseq.web.serviceimpl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.DBCollection;
import com.omicseq.bean.GeneItem;
import com.omicseq.bean.SampleItem;
import com.omicseq.bean.SampleResult;
import com.omicseq.common.SortType;
import com.omicseq.core.SampleCache;
import com.omicseq.domain.Sample;
import com.omicseq.domain.StatisticInfo;
import com.omicseq.domain.VariationGene;
import com.omicseq.domain.VariationRank;
import com.omicseq.pathway.VariationSample;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.IStatisticInfoDAO;
import com.omicseq.store.dao.IVariationGeneDAO;
import com.omicseq.store.dao.IVariationRankDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.store.helper.MongodbHelper;
import com.omicseq.utils.MathUtils;
import com.omicseq.web.service.IVariationGeneService;
@Service
public class VariationGeneServiceImpl extends Thread implements IVariationGeneService {
	private static IVariationGeneDAO variationGeneDAO = DAOFactory.getDAO(IVariationGeneDAO.class);
	@Autowired
	private SampleSearchServiceHelper sampleSearchServiceHelper;
	private static IVariationRankDAO variationRankDAO = DAOFactory.getDAO(IVariationRankDAO.class);
	DecimalFormat   df   =new  DecimalFormat("#.00000");
	/*public static void main(String[] args) {
		try {
			BufferedReader bufferReader = new BufferedReader(new FileReader("E:/hgTables.txt"));
			String line = "";
			List<VariationGene> genefList = new ArrayList<VariationGene>();
			int i = 0;
			while(StringUtils.isNoneBlank(line = bufferReader.readLine())) {
				if(i == 0)
				{
					i++;
					continue;
				}
				i++;
				String[] values = line.split("\t");
				
				VariationGene vg = new VariationGene();
				Integer binId = Integer.parseInt(values[0]);
				String chrom =  values[1];
				Integer chromStart = Integer.parseInt(values[2]) -1000;
				Integer chromEnd = chromStart + 999;
				String variationId = values[3];
				String strand = values[4];
				
				vg.setBinId(binId);
				vg.setChrom(chrom);
				vg.setChromEnd(chromEnd);
				vg.setChromStart(chromStart);
				vg.setStrand(strand);
				vg.setVariationId(variationId);
				
				genefList.add(vg);
			}
			
			variationGeneDAO.create(genefList);
			bufferReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
*/
	public VariationGeneServiceImpl(){
		
	}
	@Override
	public SampleResult searchSample(String query, List<String> sourceList,
			List<String> etypeList, SortType sortType, Integer start,
			Integer limit) {
		 query = query.toLowerCase();
		 SmartDBObject queryDb = new SmartDBObject();
		 queryDb.put("variationId", query);
		 VariationGene gene  = variationGeneDAO.findOne(queryDb);
		 if(gene == null) return new SampleResult();
		 GeneItem geneItem = convertVariationGeneToGeneItem(gene);
         geneItem.setUsedForQuery(true);
         List<Integer> intSourceList = sampleSearchServiceHelper.toSourceTypies(sourceList);
         List<Integer> intEtypeList = sampleSearchServiceHelper.toEtypies(etypeList);
         SmartDBObject rankQuery = new SmartDBObject();
         rankQuery.put("variationId", gene.getVariationId());
         rankQuery = MongodbHelper.and(rankQuery, MongodbHelper.in("etype", intEtypeList.toArray()));
         rankQuery = MongodbHelper.and(rankQuery, MongodbHelper.in("source", intSourceList.toArray()));
//         rankQuery.addSort("mixturePerc", sortType);
         rankQuery.addSort("orderNo", sortType);
         DBCollection col = MongoDBManager.getInstance().getCollection("generank", "generank", "variationrank");
         Long num = col.count(rankQuery);
         int total_all = num.intValue();
         rankQuery.put("orderNo", new SmartDBObject("$lte",3000));
//         rankQuery.put("mixturePerc", new SmartDBObject("$lte",0.05));
         List<VariationRank> variationRankList = variationRankDAO.find(rankQuery,start,limit);
         Long num1 = col.count(rankQuery);
         int count = num1.intValue();
        
         List<SampleItem> sampleItemList = new ArrayList<SampleItem>();
         for(VariationRank rank : variationRankList){
        	 Sample sample = SampleCache.getInstance().getSampleById( rank.getSampleId());
        	 Double varRank = Double.parseDouble(df.format((Double)(rank.getOrderNo()*1.0)/3000));
        	 SampleItem sampleItem =  new SampleItem(sample, null, rank.getTotalCount(), rank.getMixturePerc(), null, null, false, null, null, null, varRank);
        	 sampleItemList.add(sampleItem);
         }
         List<GeneItem> geneItemList = new ArrayList<GeneItem>();
         geneItemList.add(geneItem);
         SampleResult sampleResult = new SampleResult();
         sampleResult.setGeneItemList(geneItemList);
         sampleResult.setSampleItemList(sampleItemList);
         sampleResult.setTotal(count);
         sampleResult.setTotal_all(total_all);
		return sampleResult;
	}
	
	private GeneItem convertVariationGeneToGeneItem(VariationGene gene) {
		String rsId = gene.getVariationId().replace("rs", "");
        GeneItem geneItem = new GeneItem();
        geneItem.setEnd(gene.getChromEnd());
        geneItem.setGeneId(Integer.valueOf(rsId));
        geneItem.setSeqNameShort(gene.getChrom().replace("chr", ""));
        geneItem.setStart(gene.getChromStart());
        geneItem.setStrand(gene.getStrand());
        return geneItem;
    }
	
	private List<VariationRank> variationRankList = null;
	private SmartDBObject query = null;
	private CountDownLatch threadsSignal; 
	private Set<String> currentVariationIds = null;
	private Map<Integer,Double> sampleRankMap = null;
	private Map<Integer,Integer> sampleVariationIdSumMap = null;
	private Map<Integer,Integer> sampleVariationIdTotalMap = null;
	
	public VariationGeneServiceImpl(SmartDBObject query, List<VariationRank> variationRankList, CountDownLatch threadSignal,Set<String > currentVariationIds,Map<Integer,Double> sampleRankMap,Map<Integer,Integer> sampleVariationIdSumMap,Map<Integer,Integer> sampleVariationIdTotalMap) {
		this.variationRankList = variationRankList;
		this.query = query;
		this.threadsSignal = threadSignal;
		this.currentVariationIds = currentVariationIds;
		this.sampleRankMap	= sampleRankMap;
		this.sampleVariationIdSumMap = sampleVariationIdSumMap;
		this.sampleVariationIdTotalMap = sampleVariationIdTotalMap;
	}
	@Override  
	public void run() {
		System.out.println("run: " + Thread.currentThread().getName());
        List<VariationRank> geneRank = variationRankDAO.find(query);
        for(VariationRank rank : geneRank){
        	if(currentVariationIds.contains(rank.getVariationId())){
        		synchronized (VariationGeneServiceImpl.class) {
        			if(sampleRankMap.containsKey(rank.getSampleId())){
            			sampleRankMap.put(rank.getSampleId(), sampleRankMap.get(rank.getSampleId()) + rank.getReadCount());
            			sampleVariationIdSumMap.put(rank.getSampleId(), sampleVariationIdSumMap.get(rank.getSampleId()) + 1);
            		}
            		else{
            			sampleRankMap.put(rank.getSampleId(), rank.getReadCount());
            			sampleVariationIdSumMap.put(rank.getSampleId(), 1);
            			sampleVariationIdTotalMap.put(rank.getSampleId(), rank.getTotalCount());
            		}
				}
        	}
        }
//        variationRankList.addAll(geneRank);
        //Do somethings  
        threadsSignal.countDown();//线程结束时计数器减1 
        System.out.println(Thread.currentThread().getName() + "结束. 还有" + threadsSignal.getCount() + " 个线程");  

	}
	
	
	@Override
	public SampleResult searchSampleByVariationGenes(String variationGenes, List<String> sourceList,
			List<String> etypeList, SortType sortType, Integer start,Integer limit){
		List<Integer> intSourceList = sampleSearchServiceHelper.toSourceTypies(sourceList);
        List<Integer> intEtypeList = sampleSearchServiceHelper.toEtypies(etypeList);
        sampleRankMap = new HashMap<Integer, Double>(); 
        sampleVariationIdSumMap = new HashMap<Integer, Integer>(); 
    	sampleVariationIdTotalMap = new HashMap<Integer, Integer>(); 
//		variationGenes = "rs74188896,rs62136068,rs74185208,rs74206884,rs74186745,rs74190045,rs74185883,rs4787561,rs74190034,rs74185903";
		String [] variationGeneIds = variationGenes.split(",");
		currentVariationIds = new LinkedHashSet<String>();
		for(String id : variationGeneIds){
			if(StringUtils.isNotEmpty(id)){
				currentVariationIds.add(id);
			}
		}
		threadsSignal = new CountDownLatch(variationGeneIds.length);//初始化countDown 
		variationRankList = new ArrayList<VariationRank>();
		for(String variationId : currentVariationIds){
			query = new SmartDBObject();
			query.put("variationId", variationId);
			if (CollectionUtils.isNotEmpty(sourceList)) {
	            query = MongodbHelper.and(query, MongodbHelper.in("source", intSourceList.toArray()));
	        }
	        if (CollectionUtils.isNotEmpty(etypeList)) {
	            query = MongodbHelper.and(query, MongodbHelper.in("etype", intEtypeList.toArray()));
	        }
	        if(sortType == null)
	        {
	        	sortType = SortType.ASC;
	        }
	        query.addSort("orderNo", sortType);
	        VariationGeneServiceImpl mythread = new VariationGeneServiceImpl(query, variationRankList, threadsSignal,currentVariationIds,sampleRankMap,sampleVariationIdSumMap, sampleVariationIdTotalMap);
//	        mythread.setQuery(query);
//	        mythread.setGeneRanks(geneRanks);
			new Thread(mythread, variationId).start();
		}
		
		try {
			threadsSignal.await(); //等待所有子线程执行完
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	
		List<VariationSample> vsList = new ArrayList<VariationSample>();
		for(int sampleId : sampleRankMap.keySet()){
			Sample sample = SampleCache.getInstance().getSampleById(sampleId);
			double totalX = sample.getVariationReadTotal() == null ? 100000 :sample.getVariationReadTotal();
			double totalY = sampleRankMap.get(sampleId);
			Integer pathNumIn = sampleVariationIdSumMap.get(sampleId);
			Integer totalNum = sampleVariationIdTotalMap.get(sampleId);
			if(pathNumIn == null) continue;
			double result = (totalY/totalX)*(totalNum/pathNumIn);
			if("非数字".equals(result))
			{
				continue;
			}
			VariationSample vs = new VariationSample();
			vs.setAvgA(totalY/totalX);
			vs.setB(result);
			vs.setSampleId(sampleId);
			vs.setSource(sample.getSource());
			vs.setEtype(sample.getEtype());
			vsList.add(vs);
		}
		
		Collections.sort(vsList, new Comparator<VariationSample>() {
			@Override
			public int compare(VariationSample o1, VariationSample o2) {
				if(null != o1.getB() && null != o2.getB())
				{
					return o1.getB().compareTo(o2.getB()) * (-1);
				}
				return 0;
			}
		});
		

		
		for(VariationSample vs : vsList){
			vs.setRank(Double.parseDouble(df.format((double)(vsList.indexOf(vs)+1)/vsList.size())));
		}
		
		/*try {
			Workbook wb = new XSSFWorkbook();
			Sheet sheet = wb.createSheet("sheet");
			Row row1 = sheet.createRow(0);
			CellUtil.createCell(row1, 0, "SampleId");
			CellUtil.createCell(row1, 1, "AvgA");
			CellUtil.createCell(row1, 2, "b");
			CellUtil.createCell(row1, 3, "rank");
			CellUtil.createCell(row1, 4, "cell");
			CellUtil.createCell(row1, 5, "detail");
			for(VariationSample ps : vsList){
				Row row = sheet.createRow(vsList.indexOf(ps)+1);
				CellUtil.createCell(row, 0, ps.getSampleId().toString());
				CellUtil.createCell(row, 1, ps.getAvgA().toString());
				CellUtil.createCell(row, 2, ps.getB().toString());
				CellUtil.createCell(row, 3, ps.getRank().toString());
				CellUtil.createCell(row, 4, SampleCache.getInstance().getSampleById(ps.getSampleId()).getCell());
				CellUtil.createCell(row, 5, SampleCache.getInstance().getSampleById(ps.getSampleId()).getDetail());
			}
			OutputStream out = new FileOutputStream(new File("D:/variations1.xlsx"));
			wb.write(out);
			IOUtils.closeQuietly(out);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		SampleResult sampleResult = new SampleResult();
		sampleResult.setTotal(vsList.size());
		List<GeneItem> geneItemList = new ArrayList<GeneItem>();
        sampleResult.setGeneItemList(geneItemList);
		for(String id : currentVariationIds){
			GeneItem geneItem = new GeneItem();
			geneItem.setGeneSymbol(id);
			geneItemList.add(geneItem);
		}
		
		List<SampleItem> sampleItemList = new ArrayList<SampleItem>();
        sampleResult.setSampleItemList(sampleItemList);
        int end = (start + limit) >= vsList.size() ? vsList.size() : (start + limit);  
        vsList = vsList.subList(start, end);
        for(VariationSample vs : vsList){
        	Sample sample = SampleCache.getInstance().getSampleById(vs.getSampleId());
            if (sample == null)	continue;
            
            SampleItem sampleItem = new SampleItem(sample, null, null, 0.0, 0.0, null, false, null, 0.0, 0.0);
            //去除cell中#号 ，避免导出异常
            if(sampleItem.getCell() != null) {
            	if(sampleItem.getCell().contains("#")){
                	String cell = sampleItem.getCell().split("#")[0];
                	sampleItem.setCell(cell);
                }
            }
            java.text.DecimalFormat   df1   =new   java.text.DecimalFormat("0.000");
            sampleItem.setMixturePerc(MathUtils.floor(vs.getAvgA()*100)); //实验数据对应该基因组的平均percentile
            sampleItem.setPercentileFormat(df1.format(vs.getRank()*100));
            sampleItem.setTssTesCount(Math.abs(vs.getB()));
            sampleItemList.add(sampleItem);
        }
		
		
		
		/*
		int n = 10;
		double d = Math.sqrt(12*n);
        
		HashMap<Integer, Double> mapA = new HashMap<Integer, Double>();
		HashMap<Integer, Integer> mapB = new HashMap<Integer, Integer>();
		HashMap<Integer, Double> mapC = new HashMap<Integer, Double>();
		HashMap<Integer, Integer> mapSource = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> mapEtype = new HashMap<Integer, Integer>();
        
		for(VariationRank g : variationRankList){
			if (mapA.containsKey(g.getSampleId())) {
				if(g.getMixturePerc() != null)
				{
					mapA.put(g.getSampleId(), mapA.get(g.getSampleId())+g.getMixturePerc());
					mapB.put(g.getSampleId(), mapB.get(g.getSampleId()) +1);
					mapC.put(g.getSampleId(), mapC.get(g.getSampleId())+g.getMixturePerc());
				}
				
			}else {
				if(g.getMixturePerc() != null) {
					mapA.put(g.getSampleId(), g.getMixturePerc());
					mapB.put(g.getSampleId(), 1);
					mapC.put(g.getSampleId(), g.getMixturePerc());
				}
			}
			if(!mapSource.containsKey(g.getSampleId()))
			{
				mapSource.put(g.getSampleId(), g.getSource());
			}
			if(!mapEtype.containsKey(g.getSampleId()))
			{
				mapEtype.put(g.getSampleId(), g.getEtype());
			}
		}
		
		Iterator<Integer> itC = mapC.keySet().iterator();
		while(itC.hasNext()) {
			int sampleId = itC.next();
			if(mapB.get(sampleId) < n/2)
			{
				if(mapA.get(sampleId) != null)
				{
					mapA.remove(sampleId);
				}
			}
		}
		
		Iterator<Integer> it = mapA.keySet().iterator();
		List<PathWaySample> psList = new ArrayList<PathWaySample>();
		while(it.hasNext()) {
			Integer sampleId = (Integer)it.next();
			double avgR = mapA.get(sampleId)/mapB.get(sampleId);
			double b = d*(avgR - 0.5);
			
			double test = NORMSDIST(b);
			
			df   =new   java.text.DecimalFormat("#.000000000"); 
			for (int i = 0; i < variationGeneIds.length; i++) {
	            String temp = variationGeneIds[i];
	            for (int j = i; j < variationGeneIds.length - i; j++) {
	                if (temp.compareTo(variationGeneIds[j]) > 0) {
	                	variationGeneIds[i] = variationGeneIds[j];
	                	variationGeneIds[j] = temp;
	                }
	            }
	        }
			StringBuffer sb = new StringBuffer();
			for(String str : variationGeneIds){
				sb.append(str);
			}
			try {
				PathWaySample ps = new PathWaySample();
				ps.setSampleId(sampleId);
				ps.setAvgA(Double.valueOf(df.format(avgR)));
				ps.setB(Double.valueOf(df.format(b)));
				ps.setVariationName(sb.toString());
				ps.setRank(Double.valueOf(df.format(test)));
				ps.setSource(mapSource.get(sampleId));
				ps.setEtype(mapEtype.get(sampleId));
				psList.add(ps);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		//根据rank值排序
		if(sortType == sortType.ASC){
			Collections.sort(psList, new Comparator<PathWaySample>() {
				@Override
				public int compare(PathWaySample o1, PathWaySample o2) {
					if(null != o1.getRank() && null != o2.getRank())
					{
						return o1.getRank().compareTo(o2.getRank());
					}
					return 0;
				}
			});
		}else{
			Collections.sort(psList, new Comparator<PathWaySample>() {
				@Override
				public int compare(PathWaySample o1, PathWaySample o2) {
					if(null != o1.getRank() && null != o2.getRank())
					{
						return o1.getRank().compareTo(o2.getRank()) *(-1);
					}
					return 0;
				}
			});
		}
		
		
		try {
			Workbook wb = new XSSFWorkbook();
			Sheet sheet = wb.createSheet("sheet");
			Row row = sheet.createRow(0);
			CellUtil.createCell(row, 0, "SampleId");
			CellUtil.createCell(row, 1, "AvgA");
			CellUtil.createCell(row, 2, "b");
			CellUtil.createCell(row, 3, "rank");
			CellUtil.createCell(row, 4, "cell");
			CellUtil.createCell(row, 5, "detail");
			for(PathWaySample ps : psList){
				Row row1 = sheet.createRow(psList.indexOf(ps)+1);
				CellUtil.createCell(row1, 0, ps.getSampleId().toString());
				CellUtil.createCell(row1, 1, ps.getAvgA().toString());
				CellUtil.createCell(row1, 2, ps.getB().toString());
				CellUtil.createCell(row1, 3, ps.getRank().toString());
				CellUtil.createCell(row1, 4, SampleCache.getInstance().getSampleById(ps.getSampleId()).getCell());
				CellUtil.createCell(row1, 5, SampleCache.getInstance().getSampleById(ps.getSampleId()).getDetail());
			}
			OutputStream out = new FileOutputStream(new File("D:/variations.xlsx"));
			wb.write(out);
			IOUtils.closeQuietly(out);
		} catch (Exception e) {
			e.printStackTrace();
		}
*/
		return sampleResult;
	}
	public static double NORMSDIST(double b)
    {
        double p = 0.2316419;
        double b1 = 0.31938153;
        double b2 = -0.356563782;
        double b3 = 1.781477937;
        double b4 = -1.821255978;
        double b5 = 1.330274429;
         
        double x = Math.abs(b);
        double t = 1/(1+p*x);
         
        double val = 1 - (1/(Math.sqrt(2*Math.PI))  * Math.exp(-1*Math.pow(b, 2)/2)) 
						* (b1*t + b2 * Math.pow(t,2) + b3*Math.pow(t,3) + b4 * Math.pow(t,4) + b5 * Math.pow(t,5));
        if(b < 0)
        {
        	val = 1 - val;
        }
        return val;
    }

	public static void main(String[] args) {
		ISampleDAO sampleNewDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
		IStatisticInfoDAO statisticInfoDAO = DAOFactory.getDAO(IStatisticInfoDAO.class);
		Integer [] serverIp = {157};
		Set<Integer> sampleIdList = new HashSet<Integer>();
		for(Integer ip : serverIp){
			SmartDBObject query = new SmartDBObject();
			query.put("serverIp", new SmartDBObject("$regex","112.25.20."+ip));
			query.put("source", 2);
			List<StatisticInfo> list = statisticInfoDAO.find(query);
			for(StatisticInfo s : list){
				String []  u = s.getPath().split("/");
				String  fileName = u[u.length-1];
				SmartDBObject sampleQuery = new SmartDBObject();
				sampleQuery.put("url", new SmartDBObject("$regex",fileName));
				Sample sample = sampleNewDAO.findOne(sampleQuery );
				sampleIdList.add(sample.getSampleId());
			}
		}
		System.out.println(sampleIdList.size());
		for(int sampleId : sampleIdList){
			Sample sample = sampleNewDAO.getBySampleId(sampleId);
			if(sample.getVariationReadTotal() != null) continue;
			List<VariationRank> rankList = variationRankDAO.find(new SmartDBObject("sampleId", sampleId));
			
			int variationReadTotal = 0;
			for(VariationRank rank : rankList){
				variationReadTotal += rank.getReadCount();
			}
			sample.setVariationReadTotal(variationReadTotal);
			sampleNewDAO.update(sample);
		}
	}
}
