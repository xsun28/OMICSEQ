package com.omicseq.web.serviceimpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.omicseq.bean.GeneItem;
import com.omicseq.bean.SampleItem;
import com.omicseq.bean.SampleResult;
import com.omicseq.common.SortType;
import com.omicseq.core.GeneCache;
import com.omicseq.core.MouseGeneCache;
import com.omicseq.core.MouseTxrRefCache;
import com.omicseq.core.SampleCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.domain.CacheGeneRank;
import com.omicseq.domain.Gene;
import com.omicseq.domain.Sample;
import com.omicseq.domain.StatisticInfo;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.cache.CacheProviderFactory;
import com.omicseq.store.cache.ICacheProvider;
import com.omicseq.store.criteria.GeneRankCriteria;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.IStatisticInfoDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.web.service.IMouseSampleSearchService;
@Service
public class MouseSampleSearchService implements IMouseSampleSearchService {
    private IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
    private Logger logger = LoggerFactory.getLogger(MouseSampleSearchService.class);
    @Autowired
    private SampleSearchServiceHelper sampleSearchServiceHelper;
    private ICacheProvider localCacheProvider = CacheProviderFactory.getLocalCacheProvider();
    private IStatisticInfoDAO statisticInfoDAO = DAOFactory.getDAO(IStatisticInfoDAO.class);

	@Override
	public SampleResult searchSample(String query, List<String> sourceList,
			List<String> etypeList, SortType sortType, Integer start,
			Integer limit) {
		query = query.toLowerCase();
        boolean refSeq = query.startsWith("nm_") || query.startsWith("nr_");
        if (refSeq) {
            Gene gene = MouseGeneCache.getInstance().getGeneByName(query);
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
                String geneSymbol = MouseTxrRefCache.getInstance().getGeneSymbolByRefSeq(gene.getTxName());
                geneItem.setGeneSymbol(geneSymbol);
                geneItemList.add(geneItem);
                sampleResult.setGeneItemList(geneItemList);
                return sampleResult;
            }
        } else {
            // gene symbol process flow
            List<TxrRef> txrRefList = MouseTxrRefCache.getInstance().getTxrRefBySymbol(query);
            if (CollectionUtils.isEmpty(txrRefList)) {
                return new SampleResult();
            }
            List<GeneItem> geneItemList = new ArrayList<GeneItem>();
            Set<String> geneSet = new HashSet<String>();
            for (TxrRef txrRef : txrRefList) {
                if (StringUtils.isBlank(txrRef.getRefseq())) {
                    continue;
                }
                Gene gene = MouseGeneCache.getInstance().getGeneByName(txrRef.getRefseq());
                
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

	@Override
	public SampleResult searchSampleByGeneId(GeneItem geneItem,List<String> sourceList, 
			List<String> etypeList, SortType sortType,Integer start, Integer limit) {
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
		public SampleResult advancedSearch(String geneSymbol, List<String> sourceList, List<String> etypeList, String cell,
				String detail, SortType sortType, Integer start, Integer limit) {
			Gene gene = MouseGeneCache.getInstance().getGeneByName(geneSymbol.toLowerCase());
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
				List<TxrRef> txrRefList = MouseTxrRefCache.getInstance().getTxrRefBySymbol(geneSymbol);
	            if (CollectionUtils.isEmpty(txrRefList)) {
	                return new SampleResult();
	            }
	            geneItemList = new ArrayList<GeneItem>();
	            Set<String> geneSet = new HashSet<String>();
	            for (TxrRef txrRef : txrRefList) {
	                if (StringUtils.isBlank(txrRef.getRefseq())) {
	                    continue;
	                }
	                gene = MouseGeneCache.getInstance().getGeneByName(txrRef.getRefseq());
	                
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
//	        	geneRankCriteria.setMixturePerc(Double.valueOf(0.01));
//	          count = geneRankDAO.count(geneRankCriteria);
	        	count = sampleItemList.size();
	        	
	        }
	        sampleResult.setTotal(count);
	        return sampleResult;
		}
}
