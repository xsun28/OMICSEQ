package com.omicseq.web.serviceimpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.omicseq.bean.GeneItem;
import com.omicseq.bean.SampleItem;
import com.omicseq.bean.SampleResult;
import com.omicseq.common.CancerType;
import com.omicseq.common.Constants;
import com.omicseq.common.ExperimentType;
import com.omicseq.common.SortType;
import com.omicseq.common.SourceType;
import com.omicseq.core.AntibodyCache;
import com.omicseq.core.GeneCache;
import com.omicseq.core.SampleCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.domain.CacheGeneRank;
import com.omicseq.domain.CellTypeDesc;
import com.omicseq.domain.Comment;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.domain.StatisticInfo;
import com.omicseq.domain.TxrRef;
import com.omicseq.pathway.PathWay;
import com.omicseq.pathway.PathWaySample;
import com.omicseq.store.cache.CacheProviderFactory;
import com.omicseq.store.cache.ICacheProvider;
import com.omicseq.store.criteria.GeneRankCriteria;
import com.omicseq.store.criteria.PathWayCriteria;
import com.omicseq.store.dao.ICellDescDao;
import com.omicseq.store.dao.ICommentDAO;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.IPathWayDAO;
import com.omicseq.store.dao.IPathWaySampleDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.IStatisticInfoDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.store.helper.MongodbHelper;
import com.omicseq.utils.DateUtils;
import com.omicseq.utils.MathUtils;
import com.omicseq.web.service.ISampleSearchService;

/**
 * @author Min.Wang
 * 
 */
@Service
public class SampleSearchServiceImpl extends Thread  implements ISampleSearchService {
    private Logger logger = LoggerFactory.getLogger(SampleSearchServiceImpl.class);
    //private IGeneRankDAO geneRankDAO = DAOFactory.getDAOByTableType(IGeneRankDAO.class,"_copy" );
    private IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
    private ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, Constants.STAT_SUFFIX);
    private ICacheProvider localCacheProvider = CacheProviderFactory.getLocalCacheProvider();
    private IPathWaySampleDAO pathWaySampleDAO = DAOFactory.getDAO(IPathWaySampleDAO.class);
    private IPathWayDAO pathWayDAO = DAOFactory.getDAO(IPathWayDAO.class);
    private IStatisticInfoDAO statisticInfoDAO = DAOFactory.getDAO(IStatisticInfoDAO.class);
    @Autowired
    private SampleSearchServiceHelper sampleSearchServiceHelper;
    
    private static List<Sample> samples;
    private List<SampleItem> sampleItemList;
    private String _genomicRegion="";

    // compute the cache start, and cache end.
    // gene key.SampleResult_g${geneId}_s${start}_e${end}(default key), store
    // pagesize=100, page = 3

    @Override
    public SampleResult searchSample(String query, List<String> sourceList, List<String> etypeList, SortType sortType,
            Integer start, Integer limit) {
        query = query.toLowerCase();
        boolean refSeq = query.startsWith("nm_") || query.startsWith("nr_");
        if (refSeq) {
            Gene gene = GeneCache.getInstance().getGeneByName(query);
            if (gene == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug(" can't find gene for name : " + query);
                }
                return new SampleResult();
            } else {
                GeneItem geneItem = convertGeneToGeneItem(gene);
                geneItem.setUsedForQuery(true);
                SampleResult sampleResult = searchSampleByGeneId(geneItem, sourceList, etypeList, sortType, start,
                        limit);
                List<GeneItem> geneItemList = new ArrayList<GeneItem>();
                String geneSymbol = TxrRefCache.getInstance().getGeneSymbolByRefSeq(gene.getTxName());
                geneItem.setGeneSymbol(geneSymbol);
                geneItemList.add(geneItem);
                sampleResult.setGeneItemList(geneItemList);
                return sampleResult;
            }
        } else {
            // gene symbol process flow
            List<TxrRef> txrRefList = TxrRefCache.getInstance().getTxrRefBySymbol(query);
            if (CollectionUtils.isEmpty(txrRefList)) {
                return new SampleResult();
            }
            List<GeneItem> geneItemList = new ArrayList<GeneItem>();
            Set<String> geneSet = new HashSet<String>();
            for (TxrRef txrRef : txrRefList) {
                if (StringUtils.isBlank(txrRef.getRefseq())) {
                    continue;
                }
                Gene gene = GeneCache.getInstance().getGeneByName(txrRef.getRefseq());
                
                if (gene == null) {
                    logger.warn(" can't find gene for name : " + txrRef.getRefseq() + "; genesymbol : " + query);
                    continue;
                }
                if (geneSet.add(gene.getTxName())) {
                    GeneItem geneItem = convertGeneToGeneItem(gene);
                    geneItem.setGeneSymbol(txrRef.getGeneSymbol());
                    geneItemList.add(geneItem);
                }
            }
            if (CollectionUtils.isEmpty(geneItemList)) {
                return new SampleResult();
            }

            GeneItem geneItem = geneItemList.get(0);
            geneItem.setUsedForQuery(true);
            SampleResult sampleResult = searchSampleByGeneId(geneItem, sourceList, etypeList, sortType, start, limit);
            sampleResult.setGeneItemList(geneItemList);
            return sampleResult;
        }
    }

    private GeneItem convertGeneToGeneItem(Gene gene) {
        GeneItem geneItem = new GeneItem();
        geneItem.setEnd(gene.getEnd());
        geneItem.setGeneId(gene.getGeneId());
        geneItem.setSeqName(gene.getSeqName());
        geneItem.setSeqNameShort(gene.getSeqName().replace("chr", ""));
        geneItem.setStart(gene.getStart());
        geneItem.setTxName(gene.getTxName());
        geneItem.setStrand(gene.getStrand());
        geneItem.setEntrezId(String.valueOf(gene.getEntrezId()));
        geneItem.setRelKey(gene.getRelKey());
        return geneItem;
    }

    @Override
    public SampleResult searchSampleByGeneId(GeneItem geneItem, List<String> sourceList, List<String> etypeList,
            SortType sortType, Integer start, Integer limit) {
        List<Integer> intSourceList = sampleSearchServiceHelper.toSourceTypies(sourceList);
        List<Integer> intEtypeList = sampleSearchServiceHelper.toEtypies(etypeList);
        Integer geneId = geneItem.getGeneId();
        List<CacheGeneRank> cacheRankList = sampleSearchServiceHelper.searchSampleByGeneId(geneId, intSourceList,
                intEtypeList, sortType, (double)0.01, start, limit);
        SampleResult sampleResult = new SampleResult();
        List<SampleItem> sampleItemList = new ArrayList<SampleItem>();
        
        for (CacheGeneRank cacheGeneRank : cacheRankList) {
            Sample sample = SampleCache.getInstance().getSampleById(cacheGeneRank.getSampleId());
            if (sample == null) {
                logger.warn(" can't find sample for id : " + cacheGeneRank.getSampleId());
                continue;
            }
            Double mixturePerc = cacheGeneRank.getMixturePerc();
            Double tss5kPerc = cacheGeneRank.getTss5kPerc();
            Double tss5kCount = cacheGeneRank.getTss5kCount();
            Double tssTesCount = cacheGeneRank.getTssTesCount();
            //total number.
           
            SampleItem sampleItem = new SampleItem(sample, null, cacheGeneRank.getTotal(), mixturePerc, tss5kPerc, null, false, null, tss5kCount, tssTesCount);
         
            StatisticInfo s = statisticInfoDAO.getBySampleId(sample.getSampleId());
            if(s!=null){
            	sampleItem.setAddress(s.getServerIp());
            }
            //去除cell中#号 ，避免导出异常
            if(null !=sampleItem.getCell() && sampleItem.getCell().contains("#")){
            	String cell = sampleItem.getCell().split("#")[0];
            	sampleItem.setCell(cell);
            }
            sampleItemList.add(sampleItem);
        }
        
        sampleResult.setSampleItemList(sampleItemList);
        
        GeneRankCriteria geneRankCriteria = new GeneRankCriteria();
        geneRankCriteria.setGeneId(geneId);
        geneRankCriteria.setEtypeList(intEtypeList);
        geneRankCriteria.setSourceList(intSourceList);
        geneRankCriteria.setSortType(sortType);

        String countKey = geneRankCriteria.generateKey(GeneRankCriteria.CacheCountTempalte);
        Integer count = (Integer) localCacheProvider.get(countKey);
        String countKey_total = geneRankCriteria.generateKey(GeneRankCriteria.CacheTotalCountTempalte);
        Integer count_total = (Integer) localCacheProvider.get(countKey_total);
        if(count_total == null)
        {
        	count_total =  geneRankDAO.count(geneRankCriteria);
        	localCacheProvider.set(countKey_total, count, 3600L);
        }
        if (count == null) {
            // if the real time count is too slowly, we will use batch count.
        	geneRankCriteria.setMixturePerc(Double.valueOf(0.01));
            count = geneRankDAO.count(geneRankCriteria);
            localCacheProvider.set(countKey, count, 3600L);
        }
        sampleResult.setTotal(count);
        sampleResult.setTotal_all(count_total);
        return sampleResult;
    }

    @Override
    public List<String> findFactorMapingNotExist() {
        List<String> factors = new ArrayList<String>();
        List<Sample> samples = sampleDAO.find(new SmartDBObject("deleted", 0));
        if (logger.isDebugEnabled()) {
            logger.debug("find [" + samples.size() + "] samples in total.");
        }
        for (Sample sample : samples) {
            if (StringUtils.isNotBlank(sample.getFactor())) {
                String fac = sample.getFactor().replaceAll("\\(", "").replaceAll("\\)", "").replaceAll(",", "").trim();
                String target = AntibodyCache.getInstance().getTargetByAntibody(fac);
                if (StringUtils.isBlank(target) && !factors.contains(sample.getFactor())) {
                    factors.add(sample.getFactor());
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("find [" + factors.size() + "] factors not matched.");
        }
        return factors;
    }

	@Override
	public SampleResult searchSampleByPathway(String pathwayName, Integer pathwayId, List<String> sourceList,
			List<String> experimentsList, SortType sortType, Integer start, int pageSize) {
		PathWay pathWay = pathWayDAO.findOne(pathwayName);
		if(pathWay == null)
		{
			return new SampleResult();
		}
		List<Integer> intSourceList = sampleSearchServiceHelper.toSourceTypies(sourceList);
        List<Integer> intEtypeList = sampleSearchServiceHelper.toEtypies(experimentsList);
        
        
		SmartDBObject query = new SmartDBObject("pathId", pathWay.getPathId());
		if (CollectionUtils.isNotEmpty(sourceList)) {
            query = MongodbHelper.and(query, MongodbHelper.in("source", intSourceList.toArray()));
        }
        if (CollectionUtils.isNotEmpty(experimentsList)) {
            query = MongodbHelper.and(query, MongodbHelper.in("etype", intEtypeList.toArray()));
        }
        if(sortType == null)
        {
        	sortType = SortType.ASC;
        }
        query.addSort("rank", sortType);
		List<PathWaySample> pathWaySampleList = pathWaySampleDAO.find(query, start, pageSize);
		
		Integer pathId = pathWay.getPathId();
		String pathGenes = pathWay.getGeneNames();
		String url = pathWay.getUrl();
		SampleResult sampleResult = new SampleResult();
		List<SampleItem> sampleItemList = new ArrayList<SampleItem>();
        sampleResult.setSampleItemList(sampleItemList);
        List<GeneItem> geneItemList = new ArrayList<GeneItem>();
        sampleResult.setGeneItemList(geneItemList);
        
        sampleResult.setUrl(url);
        
        String[] geneSymbole = pathGenes.split(",");
        for(int i=0; i<geneSymbole.length; i++)
        {
        	GeneItem geneItem = new GeneItem();
        	geneItem.setTxName(geneSymbole[i]);
        	geneItemList.add(geneItem);
        }
        for(PathWaySample ps : pathWaySampleList)
        {
        	Sample sample = SampleCache.getInstance().getSampleById(ps.getSampleId());
            if (sample == null) {
                logger.warn(" can't find sample for id : " + ps.getSampleId());
                continue;
            }
            
            SampleItem sampleItem = new SampleItem(sample, null, null, 0.0, 0.0, null, false, null, 0.0, 0.0);
            //去除cell中#号 ，避免导出异常
            if(sampleItem.getCell() != null) {
            	if(sampleItem.getCell().contains("#")){
                	String cell = sampleItem.getCell().split("#")[0];
                	sampleItem.setCell(cell);
                }
            }
            
         // 保留三位小数
            DecimalFormat df = new DecimalFormat("0.000");
            
            sampleItem.setMixturePerc(MathUtils.floor(ps.getAvgA()*100)); //实验数据对应该基因组的平均percentile
            sampleItem.setPercentileFormat(df.format(ps.getRank()*100));
            sampleItem.setTssTesCount(Math.abs(ps.getB()));
//            sampleItem.setPathwayOfRank(ps.getRank());
            
            sampleItemList.add(sampleItem);
        }
        Integer count = 6000;
		try {
			PathWayCriteria criteria = new PathWayCriteria();
			criteria.setPathId(pathId);
			criteria.setEtypeList(intEtypeList);
			criteria.setSourceList(intSourceList);
//			count = (Integer) pathWaySampleDAO.countTotal(criteria);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
        sampleResult.setTotal(count);
        return sampleResult;
	
	}

	public SampleSearchServiceImpl() {
		
	}
	
	public SampleSearchServiceImpl(SmartDBObject query, List<GeneRank> geneRanks, CountDownLatch threadSignal) {
		this.geneRanks = geneRanks;
		this.query = query;
		this.threadsSignal = threadSignal;
	}
	
	private List<GeneRank> geneRanks = null;
	private SmartDBObject query = new SmartDBObject();
	private CountDownLatch threadsSignal;  
	
	@Override  
	public void run() {
		System.out.println("run: " + Thread.currentThread().getName());
        List<GeneRank> geneRank = geneRankDAO.find(query,0,5000);
        geneRanks.addAll(geneRank);
        //Do somethings  
        threadsSignal.countDown();//线程结束时计数器减1 
        System.out.println(Thread.currentThread().getName() + "结束. 还有" + threadsSignal.getCount() + " 个线程");  

	}
	
	@SuppressWarnings("static-access")
	@Override
	public SampleResult searchSampleByMultigene(String multigene,List<String> sourceList, List<String> experimentsList,
			SortType sortType, Integer start, int pageSize) {
		geneRanks = new ArrayList<GeneRank>();
		List<Integer> intSourceList = sampleSearchServiceHelper.toSourceTypies(sourceList);
        List<Integer> intEtypeList = sampleSearchServiceHelper.toEtypies(experimentsList);
		if(multigene.endsWith(",")){
			multigene = multigene.substring(0, multigene.length()-1);
		}
		String [] symbols = multigene.split(",");
		int n = symbols.length;
		//BasicDBList values = new BasicDBList();
		List<Integer> values = new ArrayList<Integer>();
		for(String s :symbols){
			List<TxrRef> txrRefList = TxrRefCache.getInstance().getTxrRefBySymbol(s.toLowerCase());
			if(txrRefList !=null && txrRefList.size() != 0){
				for(TxrRef t : txrRefList){
					String refseq = t.getRefseq();
					if(refseq !=null && !"".equals(refseq)){
						//根据refseq对应gene表txName字段 找geneId
						//Gene gene = geneDAO.getByName(refseq); 
						Gene gene = GeneCache.getInstance().getGeneByName(refseq);
						if(gene != null){
							values.add(gene.getGeneId());
							break;
						}
					}
				}
			}
		}
		/*
		SmartDBObject query = new SmartDBObject("geneId", new SmartDBObject("$in", values));
		if (CollectionUtils.isNotEmpty(sourceList)) {
            query = MongodbHelper.and(query, MongodbHelper.in("source", intSourceList.toArray()));
        }
        if (CollectionUtils.isNotEmpty(experimentsList)) {
            query = MongodbHelper.and(query, MongodbHelper.in("etype", intEtypeList.toArray()));
        }
        if(sortType == null)
        {
        	sortType = SortType.ASC;
        }
        query.addSort("mixturePerc", sortType);*/

		
		CountDownLatch threadSignal = new CountDownLatch(values.size());//初始化countDown  
		for(Integer geneId : values){
			query = new SmartDBObject();
			query.put("geneId", geneId);
			if (CollectionUtils.isNotEmpty(sourceList)) {
	            query = MongodbHelper.and(query, MongodbHelper.in("source", intSourceList.toArray()));
	        }
	        if (CollectionUtils.isNotEmpty(experimentsList)) {
	            query = MongodbHelper.and(query, MongodbHelper.in("etype", intEtypeList.toArray()));
	        }
	        if(sortType == null)
	        {
	        	sortType = SortType.ASC;
	        }
	        query.addSort("mixturePerc", sortType);
	        SampleSearchServiceImpl mythread = new SampleSearchServiceImpl(query, geneRanks, threadSignal);
//	        mythread.setQuery(query);
//	        mythread.setGeneRanks(geneRanks);
			new Thread(mythread, String.valueOf(geneId)).start();
		}
		

		try {
			threadSignal.await(); //等待所有子线程执行完
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		//List<GeneRank> geneRanks = geneRankDAO.find(query,0,10000*n);
		double d = Math.sqrt(12*n);
        
		HashMap<Integer, Double> mapA = new HashMap<Integer, Double>();
		HashMap<Integer, Integer> mapB = new HashMap<Integer, Integer>();
		HashMap<Integer, Double> mapC = new HashMap<Integer, Double>();
		HashMap<Integer, Integer> mapSource = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> mapEtype = new HashMap<Integer, Integer>();
        
		for(GeneRank g : geneRanks){
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
			
			java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.0000000"); 
			try {
				PathWaySample ps = new PathWaySample();
				ps.setSampleId(sampleId);
				ps.setAvgA(avgR);
				ps.setB(b);
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
		SampleResult sampleResult = new SampleResult();
		if(psList!=null){
			List<SampleItem> sampleItemList = new ArrayList<SampleItem>();
	        List<GeneItem> geneItemList = new ArrayList<GeneItem>();
	        for(String s : symbols){
	        	GeneItem geneItem = new GeneItem();
	        	geneItem.setTxName(s);
	        	geneItemList.add(geneItem);
	        }
	        int count = 0; 
	        for(PathWaySample ps : psList){
	        	Sample sample = SampleCache.getInstance().getSampleById(ps.getSampleId());
	            if (sample == null) {
	                logger.warn(" can't find sample for id : " + ps.getSampleId());
	                continue;
	            }
	            
	            SampleItem sampleItem = new SampleItem(sample, null, null, 0.0, 0.0, null, false, null, 0.0, 0.0);
	            //去除cell中#号 ，避免导出异常
	            if(sampleItem.getCell()!=null && sampleItem.getCell().contains("#")){
	            	String cell = sampleItem.getCell().split("#")[0];
	            	sampleItem.setCell(cell);
	            }
	            sampleItem.setMixturePerc(MathUtils.floor(ps.getAvgA()*100)); //实验数据对应该基因组的平均percentile
	            sampleItem.setPathwayOfRank(ps.getRank());
	            sampleItemList.add(sampleItem);
	        }
	        count = sampleItemList.size();
	        List<SampleItem> sampleItemList1= new ArrayList<SampleItem>();
	        int num = 0;
	        if(count<=start+pageSize){
	        	num = count;
	        }else{
	        	num = start+pageSize;
	        }
	        for(int i=start;i<num;i++){
	        	sampleItemList1.add(sampleItemList.get(i));
	        }
	        sampleResult.setGeneItemList(geneItemList);
	        sampleResult.setSampleItemList(sampleItemList1);
	        sampleResult.setTotal(count);
		}
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
	
	public void removeSampleByCretrie()
	{
//		List<Sample>  sampleList = sampleDAO.find(new SmartDBObject("deleted", 1));
		SmartDBObject query = new SmartDBObject("state", 3);
		query.put("source", 5);
		List<StatisticInfo>  infoList = statisticInfoDAO.find(query);
		for(StatisticInfo stInfo : infoList)
		{
			geneRankDAO.removeBySampleId(stInfo.getSampleId());
		}
	}
	
	public static void main(String[] args) {
		SampleSearchServiceImpl ss = new SampleSearchServiceImpl();
		
//		ss.removeSampleByCretrie();
		
//		SmartDBObject query = new SmartDBObject();
//		query.put("source", 1);
//		query.put("etype", 2);
//		
//		ss.removeDuplicateSamples(query);
		
//		ss.createSample();
		
//		ss.modifyFactorInfo();
		
		ss.modifySampleDetails();
		
	}

	private void modifySampleDetails() {
//		query = new SmartDBObject("cell", new SmartDBObject("$regex", "CAL-1"));
		query.put("source", SourceType.JASPAR.getValue());
		query.put("etype", ExperimentType.MOTIFS.getValue());
//		query.put("detail", new SmartDBObject("$regex", "Tumor"));
//		query = new SmartDBObject("cell","CAL-1");
		query.put("deleted", 0);
		List<Sample> sampleList = sampleDAO.find(query);
		for(Sample sample : sampleList)
		{
			if(sample.getSampleId() == 1400436) {
				continue;
			}
			String description = sample.getDescription();
			
			if(description != null)
			{
				description = "JASPAR link=" + description.replace("=", "#@#");
				sample.setDescription(description);
				sampleDAO.update(sample);
				
			}
//			if(sample.getDetail() == null || "".equals(sample.getDetail()))
//			{
//				sample.setDetail("Bone marrow Tumor");
//			} else {
//				sample.setDetail(sample.getDetail().replace("Leukemia", "Bone marrow"));
//			}
//			if(sample.getDetail().contains("Central nervous system"))
//			{
//				sample.setDetail(sample.getDetail().replace("Central nervous system", "Brain"));
//			}else if(sample.getDetail().contains("Central"))
//			{
//				sample.setDetail(sample.getDetail().replace("Central", "Brain"));
//			}
//			if(sample.getFactor() == null)
//			{
//				sample.setDetail("Epithelium Tumor");
//			} else {
//				sample.setDetail("Epithelium Tumor " + sample.getFactor());
//			}
//			String detail = sample.getDetail();
//			if(detail != null)
//			{
//				detail = detail.replaceAll("_[0-9].*", "");
//				sample.setDetail(detail);
//				sampleDAO.update(sample);
//			}
//			String cell = sample.getCell();
//			
//			cell = cell.replace("normalized ", "").replace("frequency", "proportion");
//			
//			sample.setCell(cell);
//			if(detail != null)
//			{
//				detail = detail.replace("T Tumor ", "Blood Tumor ");
//				detail = detail.replace("Tumor", "Normal");
//				String status = detail.split(" ")[1];
//				String factor = "";
//				if(detail.split(" ").length > 2)
//				{
//					factor = detail.split(" ")[2];
//				}
//				detail = "plasmacytoid dendritic cell (pDC) " + status + " " + factor;
//				detail = detail.replace("normal ", "");
//				sample.setDetail(detail);
//				sample.setPubmedUrl("http://www.ncbi.nlm.nih.gov/pubmed/14681366");
//				sampleDAO.update(sample);
//			}
//			sampleDAO.update(sample);
		}
	}

	protected static ICellDescDao cellTypeDescDAO = DAOFactory.getDAO(ICellDescDao.class);
	
	@SuppressWarnings("unused")
	private void modifyFactorInfo() {
		Map<String, String> cdMap = new HashMap<String, String>();
		List<CellTypeDesc> fds = cellTypeDescDAO.find(new SmartDBObject());
		for(CellTypeDesc fd : fds)
		{
			cdMap.put(fd.getCell(), fd.getCell_desc());
		}
		String unconfirmedCells = "cell\n";
        Integer start = 0;
        Integer limit = 3000;
        List<Sample> sampleList = null;
        List<Integer> intSourceList = new ArrayList<Integer>();
//        intSourceList.add(2);
//        intSourceList.add(3);
//        intSourceList.add(5);
        intSourceList.add(7);
        query = new SmartDBObject("source", new SmartDBObject("$in", intSourceList.toArray()));
//        query.put("detail", new SmartDBObject("$ne", null));
        query.put("detail", null);
        query.put("cell", new SmartDBObject("$ne", null));
        query.put("deleted", 0);
        while (CollectionUtils.isNotEmpty(sampleList = sampleDAO.find(query, start, limit))) {
            for (Sample sample : sampleList) {

            	if(sample.getCell() == null || sample.getCell().isEmpty() || (sample.getDetail() != null && sample.getDetail().contains("(C)")))
            	{
            		continue;
            	}
				String factor = sample.getFactor();
				
				if (factor == null || "".equals(factor)) {
					if(sample.getSource() == SourceType.TCGA.value() || sample.getSource() == SourceType.ICGC.value())
					{
						String desc = sample.getDescription();
						if(desc != null && !"".equals(desc))
						{
							Map<String, String> mapTemp = sample.descMap();
							String tumor_tissue_site = mapTemp.get("tumor_tissue_site");
							if(tumor_tissue_site != null)
							{
								factor = tumor_tissue_site;
								String cell = sample.getCell();
								if (cell.contains("tumor")) {
									factor += " tumor";
								}
								if (cell.contains("normal")) {
									factor += " normal";
								}
								if (cell.contains("control")) {
									factor += " control";
								}
							} else {
								String cell = sample.getCell();
								if(!cell.isEmpty())
								{
									String[] cells = cell.split("-");
									String cancerType = "";
									if(cells != null && cells.length >1)
									{
										if(sample.getSource() == SourceType.TCGA.value())
										{
											cancerType = cells[1].toUpperCase();
										} else if(sample.getSource() == SourceType.ICGC.value())
										{
											cancerType = cells[0].toUpperCase();
										}
										try {
											factor = CancerType.valueOf(cancerType).getName();
											if (cell.contains("tumor")) {
												factor += " tumor";
											}
											else if (cell.contains("normal")) {
												if(sample.getEtype() == ExperimentType.MUTATION.getValue())
												{
													factor += " tumor";
												}else {
													factor += " normal";
												}
											}
											else if (cell.contains("control")) {
												factor += " control";
											} else {
												factor += " tumor";
											}
											
											factor = factor.replaceFirst(factor.substring(0, 1), factor.substring(0, 1).toUpperCase());
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}
							}
						} else {
							String cell = sample.getCell();
							if(cell != null && !cell.isEmpty())
							{
								String[] cells = cell.split("-");
								String cancerType = "";
								if(cells != null && cells.length >1)
								{
									if(sample.getSource() == SourceType.TCGA.value())
									{
										cancerType = cells[1].toUpperCase();
									} else if(sample.getSource() == SourceType.ICGC.value())
									{
										cancerType = cells[0].toUpperCase();
									}
									try {
										factor = CancerType.valueOf(cancerType).getName();
										if (cell.contains("tumor")) {
											factor += " tumor";
										}
										else if (cell.contains("normal")) {
											if(sample.getEtype() == ExperimentType.MUTATION.getValue())
											{
												factor += " tumor";
											}else {
												factor += " normal";
											}
										}
										else if (cell.contains("control")) {
											factor += " control";
										} else {
											factor += " tumor";
										}
										
										factor = factor.replaceFirst(factor.substring(0, 1), factor.substring(0, 1).toUpperCase());
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
						}
					}
				}
				
				if(sample.getSource() == SourceType.ENCODE.getValue() || sample.getSource() == SourceType.SRA.getValue() || sample.getSource() == SourceType.CCLE.getValue()|| sample.getSource() == SourceType.GEO.getValue())
				{
					String cell = sample.getCell();
					String cellDesc = cdMap.get(cell);
					if(cellDesc != null)
					{
						String s = cellDesc.replace("human ", "").replace("Human ", "").replace("normal ", "").replace("Normal ", "").replace("child ", "").replace("Adult ", "").replace("Adult ", "Neonatal ").trim();
						factor = s.substring(0, s.indexOf(" ") == -1?s.length()-1:s.indexOf(" ")).replace(",", "");
						if(cellDesc.contains("lung"))
						{
							factor = "Lung";
						}
						if(cellDesc.contains("prostate"))
						{
							factor = "Prostate";
						}
						if(cellDesc.contains("breast"))
						{
							factor = "Breast";
						}
						if(cellDesc.contains("central nervous"))
						{
							factor = "Brain";
						}
					} else {
						cellDesc = cell;
					}
					
					if(cell.contains("ependymomas"))
					{
						factor = "Brain";
					}
					else if(cell.contains("embryonic"))
					{
						factor = "embryonic";
					}
					
					if(cell.contains("T cells"))
					{
						factor = "T Lymphocyte";
					}
					if(cell.equals("T-47D"))
					{
						factor = "Epithelial";
					}
					if(cell.contains("MCF"))
					{
						factor = "breast";
					}
					
					if(cell.contains("H1-embryonic"))
					{
						factor = "H1 cell";
					}
					
					if(cell.contains("T47D") || cell.contains("T-47D"))
					{
						factor = "breast";
					}
					
					if(cell.contains("hESC"))
					{
						factor = "hESC";
					}
					
					if(cell.contains("HUEVC"))
					{
						factor = "umbilical vein";
					}
					
					if(cell.equals("Th17"))
					{
						factor = "Th17";
					}
					
					if(cell.contains("HCC-"))
					{
						factor = "Lung";
					}
					
					if(cell.contains("A549"))
					{
						factor = "Epithelial";
					}
					
					if(cell.contains("GM"))
					{
						factor = "Blood";
					}
					if(cell.contains("MDA"))
					{
						factor = "Breast";
					}
					if(cell.contains("BEAS-2B"))
					{
						factor = "Bronchus";
					} 
					if(cell.contains("A375"))
					{
						factor = "skin";
					}
					if(cell.contains("HeLa") || cell.contains("Hela"))
					{
						factor = "Cervix";
					} 
					if(cell.toLowerCase().contains("mcf7") || cell.toLowerCase().contains("mcf-7"))
					{
						factor = "Breast";
					} 
					if(cell.contains("LNCaP"))
					{
						factor = "Prostate";
					} 
					if(cell.contains("HCT116"))
					{
						factor = "Colon";
					}
					if(cell.contains("B-cells") || cell.contains("B cells"))
					{
						factor = "Blood";
					}
					if(cell.contains("H1 cell"))
					{
						factor = "H1 cell";
					}
					if(cell.contains("Breast"))
					{
						factor = "Breast";
					}
					if(cell.contains("skin") || cell.contains("Skin"))
					{
						factor = "Skin";
					}
					if(cell.contains("Brain") || cell.contains("Memory") || cell.contains("brain"))
					{
						factor = "Brain";
					}
					if(cell.contains("Muscle") || cell.contains("muscle"))
					{
						factor = "Muscle";
					}
					if(cell.contains("Stomach"))
					{
						factor = "Stomach";
					}
					if(cell.contains("Thymus"))
					{
						factor = "Thymus";
					}
					if(cell.contains("Naive"))
					{
						factor = "Naive";
					}
					if(cell.contains("liver") || cell.contains("Liver"))
					{
						factor = "Liver";
					}
					if(cell.contains("Kidney"))
					{
						factor = "Kidney";
					}
					if(cell.contains("Ovary"))
					{
						factor = "Ovary";
					}
					if(cell.contains("Lung") || cell.contains("lung"))
					{
						factor = "Lung";
					}
					if(cell.contains("Spinal"))
					{
						factor = "Spinal";
					}
					if(cell.contains("CD56"))
					{
						factor = "T cel";
					}
					
					if(cell.contains("NA") || cell.contains("lymphoblastoid"))
					{
						factor = "Lymphoblastoid";
					}
					if(cell.contains("T cell") || cell.contains("T-cell") || cell.contains("T Cell"))
					{
						factor = "T cell";
					}
					if(cell.contains("Fetal primary tissue"))
					{
						factor = "Fetal Primary";
					}
					if(cell.contains("Lymphocyte"))
					{
						factor = "Lymphoblastoid ";
					} 
					if(cell.contains("Embryonic"))
					{
						factor = "Embryonic ";
					}
					if(cell.contains("IMR90"))
					{
						factor = "Lung";
					}
					if(cell.contains("LoVo"))
					{
						factor = "LoVo";
					}
					if(cell.contains("H1"))
					{
						factor = "H1 Cells";
					}
					if(cell.contains("MG"))
					{
						factor = "Brain";
					}
					if(cell.contains("K562"))
					{
						factor = "Bone marrow";
					}
					if(cell.contains("CD4"))
					{
						factor = "T cell";
					}
					if(cell.contains("lymph"))
					{
						factor = "Lymphoma";
					}
					if(cell.toLowerCase().contains("kidney"))
					{
						factor = "Kidney";
					}
					
					if(factor == null)
					{
						factor = cell;
						unconfirmedCells += cell + "\n";
					}
					
					if(sample.getSource() == SourceType.CCLE.getValue())
					{
						factor += "(C)";
					}
					
					if(factor != null) 
					{
						factor = factor.replace("_tissue", "").replace("tissue", "");
						if(cell.contains("GM") || cell.contains("IM") || cell.contains("HM") || cell.contains("HP") || cell.contains("H1") ||cell.contains("H9") || cell.equals("Th17") || cell.contains("HS") || cell.contains("NH") || cell.contains("Osteobl")
								 || cell.contains("Astrocy")|| cell.contains("Fib") || cell.contains("HUEVC") || cellDesc.contains("Normal") || cellDesc.contains("normal") || cell.equals("BEAS"))
						{
							factor += " Normal ";
						} else {
							factor += " Tumor ";
						}
						factor += sample.getFactor() == null?"":sample.getFactor();
						factor = factor.replaceFirst(factor.substring(0, 1), factor.substring(0, 1).toUpperCase());
					}
					
				}
				
				if(sample.getSource() == SourceType.Roadmap.getValue())
				{
					String cell = sample.getCell();
					String cell_desc = cdMap.get(cell);
					if(cell_desc == null)
					{
						cell_desc = cell;
					} else {
						String s = cell_desc.replace("human ", "").replace("Human ", "").replace("normal ", "").replace("Normal ", "").replace("child ", "").replace("Adult ", "").replace("Adult ", "Neonatal ");
						factor = s.substring(0, s.indexOf(" ") == -1?s.length()-1:s.indexOf(" ")).replace(",", "");
						if(cell_desc.contains("T cells"))
						{
							factor = "T Lymphocyte";
						}
						if(cell_desc.equals("T-47D"))
						{
							factor = "Epithelial";
						}
						if(cell_desc.contains("lung"))
						{
							factor = "Lung";
						}
					}
					String cell_temp = cell.replaceAll(".*derived ", "");
					factor = cell_temp.substring(0, cell_temp.indexOf(" ") != -1?cell_temp.indexOf(" "):cell_temp.length());
					if(cell.contains("CD"))
					{
						if(cell.contains("memory"))
						{
							factor = "memory T cell";
						}
						else if(cell.contains("naive"))
						{
							factor = "memory T cell";
						} else {
							factor = "T cell";
						}
					}
					if(cell.contains("cultured") && cell.contains("derived"))
					{
						factor = cell.substring(cell.indexOf("derived") + 8, cell.indexOf(" cultured")).replaceAll("CD[0-9]+", "").replace("+ ", "");
					}
					if(cell.contains("primary") || cell.contains("derived") || cell_desc.contains("Normal") || cell_desc.contains("normal")) {
						factor = factor.concat(" Normal ").replace(",", "");
					} else {
						factor = factor.concat(" Tumor ").replace(",", "");
					}
					
					factor += sample.getFactor() != null?sample.getFactor():"";
				}
				
				if(sample.getEtype() == ExperimentType.SUMMARY_TRACK.getValue())
				{

					String cell = sample.getCell();
					if(!cell.isEmpty())
					{
						String[] cells = cell.split("-");
						if(cells != null && cells.length >1)
						{
							String cancerType = cells[1].toUpperCase();
							try {
								factor = CancerType.valueOf(cancerType).getName();
								if (cell.contains("tumor")) {
									factor += " tumor";
								}
								else if (cell.contains("normal")) {
									factor += " normal";
								}
								else if (cell.contains("control")) {
									factor += " control";
								} else {
									factor += " tumor";
								}
								factor = factor.replaceFirst(factor.substring(0, 1), factor.substring(0, 1).toUpperCase());
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				
				sample.setDetail(factor.trim());
				
				sampleDAO.update(sample);
            }
            start = start + limit;
        }
        File file = new File("E:\\临时文件\\unconfirmedCells.txt");
        if(!file.exists())
        {
        	try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        try {
			FileWriter fw = new FileWriter(file);
			fw.write(unconfirmedCells);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
	}

	@SuppressWarnings("unused")
	private void createSample() {
		Sample sample = new Sample();
		Integer sampleId = sampleDAO.getSequenceId(SourceType.ILLUMINA);
		sample.setSampleId(sampleId);
		sample.setEtype(ExperimentType.RNA_SEQ.getValue());
		sample.setSource(SourceType.ILLUMINA.getValue());
		sample.setCreateTiemStamp(DateUtils.getNowDate());
		sample.setDeleted(0);
		sample.setFactor("Kidney Tumor");
		sample.setCell("Kidney");
		sample.setDescription("Organism:Homo sapiens;Age:60 years;OrganismPart:kidney;Sex:female;Phenotype:caucasian;BioSourceProvider:Human total RNA;Instrument model:Illumina HiSeq 2000;Run accession:ERR030893;Sample accession:SAMEA962348");
		sample.setLab("ILLUMINA");
		sample.setReadCount(79772393);
		sample.setUrl("http://www.ebi.ac.uk/ena/data/view/ERR030893");
		sampleDAO.create(sample);
	}

	@SuppressWarnings("unused")
	private void removeDuplicateSamples(SmartDBObject query) {
		List<Sample> sampleList = sampleDAO.find(query);
		
		for(Sample sample : sampleList)
		{
			Integer sampleId = sample.getSampleId();
			
			List<Sample> samples = sampleDAO.find(new SmartDBObject("sampleId", sampleId));
			
			if(samples.size() > 1)
			{
				Sample todelete = samples.get(1);
				todelete.setDeleted(1);
				sampleDAO.update(todelete);
			}
		}
	}
	@Override
	public Map<String,Gene> searchTop5Genes(int sampleId , int size){
		SmartDBObject query  = new SmartDBObject("sampleId",sampleId);
		query.addSort("mixturePerc", SortType.ASC);
		List<GeneRank> geneRanks = geneRankDAO.find(query,0,size*2);
		Map<String,Gene> symbols = new LinkedHashMap<String,Gene>();
		for(GeneRank gr : geneRanks){
			int geneId = gr.getGeneId();
			List<Gene> genes = GeneCache.getInstance().getGeneById(geneId);
			for(Gene g : genes){
				String geneSymbol = TxrRefCache.getInstance().getGeneSymbolByRefSeq(g.getTxName());
				if(StringUtils.isNotBlank(geneSymbol) && !symbols.containsKey(geneSymbol)){
					List<TxrRef> txrRefList = TxrRefCache.getInstance().getTxrRefBySymbol(geneSymbol);
					if(StringUtils.isNoneEmpty(txrRefList.get(0).getAlias())){
						geneSymbol = txrRefList.get(0).getAlias();
					}
					symbols.put(geneSymbol,g);
					if(symbols.size()==size){
						return symbols;
					}
					break;
				}
			}
		}
		return symbols;
	}

	@Override
	public SampleResult searchSample(String cell, String factor,List<String> sourceList, List<String> experimentsList,
			SortType sortType, Integer start, int pageSize) {
		List<Integer> intSourceList = sampleSearchServiceHelper.toSourceTypies(sourceList);
        List<Integer> intEtypeList = sampleSearchServiceHelper.toEtypies(experimentsList);
        
        //List<Sample> sampleList = SampleCache.getInstance().getSampleByCellAndDetail(cell, factor, intSourceList, intEtypeList);
        SmartDBObject query = new  SmartDBObject();
        if(StringUtils.isNotEmpty(cell)) {
        	SmartDBObject qu = new SmartDBObject();
        	qu.put("$regex",cell);
        	qu.put("$options", "i");
        	query.put("cell",qu);
        }
        if(StringUtils.isNoneEmpty(factor)) 
        {
        	SmartDBObject qu = new SmartDBObject();
        	qu.put("$regex",factor);
        	qu.put("$options", "i");
        	query.put("detail",qu);
        }
//        	query.put("detail","/"+factor+"/i");
        query.put("source", new SmartDBObject("$in",intSourceList.toArray()));
        query.put("etype", new SmartDBObject("$in",intEtypeList.toArray()));
        List<Sample> sampleList = sampleDAO.find(query);
        SampleResult result = new SampleResult();
        result.setTotal_all(sampleList.size());
        result.setTotal(sampleList.size());
        List<SampleItem> sampleItemList = new ArrayList<SampleItem>();
        result.setSampleItemList(sampleItemList);
        for(int i = start; i<start+pageSize;i++){
        	if(i == sampleList.size())
        	{
        		break;
        	}
        	SampleItem sampleItem = new SampleItem(sampleList.get(i), null, null, null, null, null, false, null, null, null);
        	sampleItemList.add(sampleItem);
        }
        GeneItem geneItem = new GeneItem();
        geneItem.setUsedForQuery(true);
        List<GeneItem> geneItemList = new ArrayList<GeneItem>();
        geneItemList.add(geneItem);
        result.setGeneItemList(geneItemList);
		return result;
	}

	public void setGeneRanks(List<GeneRank> geneRanks) {
		this.geneRanks = geneRanks;
	}

	public void setQuery(SmartDBObject query) {
		this.query = query;
	}

	@Override
	public List<Sample> findSamplesByCell(String cell, String factor, List<String> sourceList, List<String> experimentsList,
			Integer start, Integer limit) {
		List<Integer> intSourceList = sampleSearchServiceHelper.toSourceTypies(sourceList);
        List<Integer> intEtypeList = sampleSearchServiceHelper.toEtypies(experimentsList);
        SmartDBObject query = new SmartDBObject("cell", cell);
		if (CollectionUtils.isNotEmpty(sourceList)) {
            query = MongodbHelper.and(query, MongodbHelper.in("source", intSourceList.toArray()));
        }
        if (CollectionUtils.isNotEmpty(experimentsList)) {
            query = MongodbHelper.and(query, MongodbHelper.in("etype", intEtypeList.toArray()));
        }
        
        SortType sortType = SortType.ASC;
        query.addSort("sampleId", sortType);
        
		return sampleDAO.find(query, start, limit);
	}

	@Override
	public SampleResult findSampleByGenomicRegion(String genomicRegion, Integer start, Integer pageSize) {
		if(StringUtils.isEmpty(genomicRegion)) return new SampleResult();
		if(CollectionUtils.isEmpty(samples)){
			SmartDBObject query = new SmartDBObject();
			Integer [] sources = {2,3};
			query.put("source",new SmartDBObject("$in",sources));
			query.put("deleted",0);
			samples = sampleDAO.find(query);
		}
		if( !genomicRegion.equalsIgnoreCase(_genomicRegion)){
			_genomicRegion = genomicRegion;
			String path = null;
			if(genomicRegion.equals("promoters")){
				path = "promoter_combined.xls";
			}else if(genomicRegion.equals("H1")){
				path = "H1_combined.xls";
			}else if(genomicRegion.equals("IMR90")){
				path = "IMR90_combined .xls";
			}else if(genomicRegion.equals("vista.neg")){
				path = "vista.neg_combined.xls";
			}else if(genomicRegion.equals("vista.pos")){
				path = "vista.pos_combined.xls";
			}else if(genomicRegion.equals("cpghg19")){
				path = "cpghg19_combined.xls";
			}else if(genomicRegion.equals("U87")){
				path = "U87_Enhancers_combined.xls";
			}else if(genomicRegion.equals("Super_U87")){
				path = "Super_U87_Enhancers_combined.xls";
			}else if(genomicRegion.equals("Super_MM1S")){
				path = "Super_MM1S_Enhancers_combined.xls";
			}else if(genomicRegion.equals("Super_H2171")){
				path = "Super_H2171_Enhancers_combined.xls";
			}else if(genomicRegion.equals("MM1S")){
				path = "MM1S_Enhancers_combined.xls";
			}else if(genomicRegion.equals("H2171")){
				path = "H2171_Enhancers_combined.xls";
			}
			File file = new File("/opt/tomcat7/regionfiles/"+ path);
//			File file = new File("F:\\miRna_new\\"+path);
			try { 
				@SuppressWarnings("resource")
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = "";
				int lineNum = 0;
				sampleItemList = new ArrayList<SampleItem>();
				while((line=br.readLine())!=null){
					lineNum++;
					if(lineNum == 1){
						continue;
					}
					String [] cols = line.split("	");
					String fName = cols[0].trim().replace(".reads.rda", "").replace(".rda", "");
					
					int totalCounts = Integer.parseInt(cols[3].trim());
					int regioncounts = Integer.parseInt(cols[4].trim()); 
					Double enhancer = Double.parseDouble(cols[6].trim())/100;
					
					
//					boolean  no =true;
					for(Sample sample : samples){
						String url = sample.getUrl();
						if(url != null && url.contains(fName)){
							SampleItem sampleItem = new SampleItem(sample, null, totalCounts, enhancer, null, null, false, null, null, null);
							sampleItem.setRank(regioncounts);
							sampleItemList.add(sampleItem);
//							no =false;
							break;
						}
					}
//					if(no) System.out.println(cols[0]);
				}
			} catch (Exception e) {
			}
		}
		
		List<SampleItem> showItemList = sampleItemList.subList(start, start+pageSize);
		
		SampleResult sampleResult = new SampleResult();
		sampleResult.setSampleItemList(showItemList);
		sampleResult.setTotal(sampleItemList.size());
		
		return sampleResult;
	}
	
	@Override
	public List<String> regionList(String bedPath) {
		List<String> list = new ArrayList<String>();
		File file = new File("/opt/tomcat7/regionfiles/"+ bedPath);
		try {
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			list.add("Chrom	Start	End");
			while((line = br.readLine()) != null){
				list.add(line);
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return list;
	}

	@Override
	public void modifyCell(String cellOld , String cellNew) {
		if(cellOld.contains("(")){
			cellOld = cellOld.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
		}
		if(cellOld.contains("[")){
			cellOld = cellOld.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]");		
		}
		if(cellOld.contains("{")){
			cellOld = cellOld.replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}");
		}
		if(cellOld.contains("*")){
			cellOld = cellOld.replaceAll("\\*", "\\\\*");
		}
		SmartDBObject query = new SmartDBObject("cell",cellOld);
		List<Sample> sampleList = sampleDAO.find(query);
		for(Sample s : sampleList){
			s.setCell(cellNew.trim());
			sampleDAO.update(s);
		}
	}

	@Override
	public void modifyDetail(String cell , String factorOld, String factorNew,Integer sampleId) {
		Sample sample = SampleCache.getInstance().getSampleById(sampleId);
		SmartDBObject query = new SmartDBObject();
		if(cell.contains("(")){
			cell = cell.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
		}
		if(cell.contains("[")){
			cell = cell.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]");		
		}
		if(cell.contains("{")){
			cell = cell.replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}");
		}
		if(cell.contains("*")){
			cell = cell.replaceAll("\\*", "\\\\*");
		}

		if(factorOld.contains("(")){
			factorOld = factorOld.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
		}
		if(factorOld.contains("[")){
			factorOld = factorOld.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]");		
		}
		if(factorOld.contains("{")){
			factorOld = factorOld.replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}");
		}
		if(factorOld.contains("*")){
			factorOld = factorOld.replaceAll("\\*", "\\\\*");
		}
		
		query.put("detail",factorOld);
		query.put("cell",new SmartDBObject("$regex",cell));
		query.put("source", sample.getSource());
		query.put("etype", sample.getEtype());
		List<Sample> sampleList = sampleDAO.find(query);
		for(Sample s : sampleList){
			s.setDetail(factorNew.trim());
			sampleDAO.update(s);
		}
	}

	@Override
	public void modifyLab(String labOld, String labNew) {
		SmartDBObject query = new SmartDBObject("lab",labOld);
		List<Sample> sampleList = sampleDAO.find(query);
		for(Sample s : sampleList){
			s.setLab(labNew.trim());
			sampleDAO.update(s);
		}
	}

	@Override
	public SampleResult advancedSearch(String geneSymbol, List<String> sourceList, List<String> etypeList, String cell,
			String detail, SortType sortType, Integer start, Integer limit) {
		Gene gene = GeneCache.getInstance().getGeneByName(geneSymbol.toLowerCase());
		GeneItem geneItem = null;
		boolean refSeq = geneSymbol.startsWith("nm_") || geneSymbol.startsWith("nr_");
		List<GeneItem> geneItemList = null ;
		if (refSeq) {
			if (gene == null) {
	            if (logger.isDebugEnabled()) {
	                logger.debug(" can't find gene for name : " + geneSymbol);
	            }
	            return new SampleResult();
	        } else {
	            geneItem = convertGeneToGeneItem(gene);
	            geneItem.setUsedForQuery(true);
	        }
		} else {
			List<TxrRef> txrRefList = TxrRefCache.getInstance().getTxrRefBySymbol(geneSymbol);
            if (CollectionUtils.isEmpty(txrRefList)) {
                return new SampleResult();
            }
            geneItemList = new ArrayList<GeneItem>();
            Set<String> geneSet = new HashSet<String>();
            for (TxrRef txrRef : txrRefList) {
                if (StringUtils.isBlank(txrRef.getRefseq())) {
                    continue;
                }
                gene = GeneCache.getInstance().getGeneByName(txrRef.getRefseq());
                
                if (gene == null) {
                    logger.warn(" can't find gene for name : " + txrRef.getRefseq() + "; genesymbol : " + geneSymbol);
                    continue;
                }
                if (geneSet.add(gene.getTxName())) {
                    geneItem = convertGeneToGeneItem(gene);
                    geneItem.setGeneSymbol(txrRef.getGeneSymbol());
                    geneItemList.add(geneItem);
                }
            }
            if (CollectionUtils.isEmpty(geneItemList)) {
                return new SampleResult();
            }

            geneItem = geneItemList.get(0);
            geneItem.setUsedForQuery(true);
		}
		
		List<Integer> intSourceList = sampleSearchServiceHelper.toSourceTypies(sourceList);
        List<Integer> intEtypeList = sampleSearchServiceHelper.toEtypies(etypeList);
        Integer geneId = geneItem.getGeneId();
        List<CacheGeneRank> cacheRankList = sampleSearchServiceHelper.searchSampleByGeneId(geneId, intSourceList,
                intEtypeList, sortType, (double)0.01, 0, null);
        SampleResult sampleResult = new SampleResult();
        List<SampleItem> sampleItemList = new ArrayList<SampleItem>();
        for (CacheGeneRank cacheGeneRank : cacheRankList) {
            Sample sample = SampleCache.getInstance().getSampleById(cacheGeneRank.getSampleId());
            
            if (sample == null) {
                logger.warn(" can't find sample for id : " + cacheGeneRank.getSampleId());
                continue;
            }
            
            if(cell != null && !cell.isEmpty()) {
            	if(sample.getCell() == null || !sample.getCell().toLowerCase().contains(cell.toLowerCase()))
                {
                	continue;
                }
            }
            
            if(detail != null && !detail.isEmpty()) {
            	if(sample.getDetail() == null || !sample.getDetail().toLowerCase().contains(detail.toLowerCase()))
                {
                	continue;
                }
            }
            
            Double mixturePerc = cacheGeneRank.getMixturePerc();
            Double tss5kPerc = cacheGeneRank.getTss5kPerc();
            Double tss5kCount = cacheGeneRank.getTss5kCount();
            Double tssTesCount = cacheGeneRank.getTssTesCount();
            //total number.
           
            SampleItem sampleItem = new SampleItem(sample, null, cacheGeneRank.getTotal(), mixturePerc, tss5kPerc, null, false, null, tss5kCount, tssTesCount);
            //去除cell中#号 ，避免导出异常
            if(null !=sampleItem.getCell() && sampleItem.getCell().contains("#")){
            	String cellStr = sampleItem.getCell().split("#")[0];
            	sampleItem.setCell(cellStr);
            }
            sampleItemList.add(sampleItem);
        }
        List<SampleItem> list = sampleItemList.subList(start, start+limit>sampleItemList.size()?sampleItemList.size():start+limit);
        for(SampleItem sampleItem : list){
          StatisticInfo s = statisticInfoDAO.getBySampleId(sampleItem.getSampleId());
          if(s!=null){
          	sampleItem.setAddress(s.getServerIp());
          }
        }
        sampleResult.setSampleItemList(list);
        sampleResult.setGeneItemList(geneItemList);
        GeneRankCriteria geneRankCriteria = new GeneRankCriteria();
        geneRankCriteria.setGeneId(geneId);
        geneRankCriteria.setEtypeList(intEtypeList);
        geneRankCriteria.setSourceList(intSourceList);
        geneRankCriteria.setSortType(sortType);

        String countKey = geneRankCriteria.generateKey(GeneRankCriteria.CacheCountTempalte);
        Integer count = (Integer) localCacheProvider.get(countKey);
        if (count == null) {
            // if the real time count is too slowly, we will use batch count.
//        	geneRankCriteria.setMixturePerc(Double.valueOf(0.01));
//          count = geneRankDAO.count(geneRankCriteria);
        	count = sampleItemList.size();
        	
        }
        sampleResult.setTotal(count);
        return sampleResult;
	}
	
	public Integer[] findProcessedAndInProcessSample(String showTab){
		Integer [] counts = sampleDAO.countProcessAndInProcess(showTab);
		return counts;
	}
	
	public List<Comment> showCommentsBySampleId(Integer sampleId){
		ICommentDAO commentDAO = DAOFactory.getDAO(ICommentDAO.class);
		List<Comment> comments = commentDAO.findBySampleId(sampleId);
		return comments;
	}
}
