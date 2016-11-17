package com.omicseq.loader;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections4.comparators.ComparatorChain;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.omicseq.common.SortType;
import com.omicseq.domain.CacheGeneRank;
import com.omicseq.domain.GeneRank;
import com.omicseq.robot.process.SymbolReader;
import com.omicseq.statistic.ComparatorFactory;
import com.omicseq.store.cache.CacheProviderFactory;
import com.omicseq.store.cache.ICacheProvider;
import com.omicseq.store.criteria.GeneRankCriteria;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;

/**
 * @author Min.Wang
 *
 */
public class CacheGeneRankLoader implements IGeneRankLoader {

	IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class); //DAOFactory.getDAOByTableType(IGeneRankDAO.class, "_copy");//

	private static String cacheKeyTempalte = "cacherank_g{0}_so{1}_e{2}_s{3}_st{4}_li{5}";
	private ICacheProvider cacheProvider = CacheProviderFactory.getCacheProvider();
	private Logger logger = LoggerFactory.getLogger(getClass());
	// half hour
	private static Long expirySeconds = 1800L;
	private boolean asySet = true;
	
	
	
	@Override
	public List<CacheGeneRank> load(Integer geneId, List<Integer> sourceList, List<Integer> etypeList, SortType sortType, Double mixturePerc, Integer start, Integer limit) {
		String cacheKey = generateCacheKey(geneId, sourceList, etypeList, sortType, start, limit);
		if (logger.isDebugEnabled()) {
            logger.debug("generateCachekey is : " + cacheKey);
        }
		Object result = cacheProvider.get(cacheKey);
		if (result != null) {
			sortCacheGeneRank((List<CacheGeneRank>) result, sortType);
			return (List<CacheGeneRank>) result;
		}
		
		List<CacheGeneRank>  cacheGeneRankList = this.loadFromDB(geneId, sourceList, etypeList, sortType, mixturePerc, start, limit);
		if (asySet) {
			cacheProvider.asySet(cacheKey, cacheGeneRankList, expirySeconds);
		} else {
			cacheProvider.set(cacheKey, cacheGeneRankList, expirySeconds);
		}
		sortCacheGeneRank(cacheGeneRankList, sortType);
		return cacheGeneRankList;
	}
	
	/**
	 * sort cache gene rank
	 * @param cacheGeneRankList
	 * @param sortType
	 */
	private void sortCacheGeneRank(List<CacheGeneRank> cacheGeneRankList, SortType sortType) {
		List<Comparator<CacheGeneRank>> comparatorList = new ArrayList<Comparator<CacheGeneRank>>();
		comparatorList.add(ComparatorFactory.getMixtureComparator(sortType));
		comparatorList.add(ComparatorFactory.getTssTesCountComparator());
		comparatorList.add(ComparatorFactory.getExperimentTypeComparator());
		comparatorList.add(ComparatorFactory.getCacheSampleIdComparator());
		Collections.sort(cacheGeneRankList, new ComparatorChain(comparatorList));
	}
	
	

	/**
	 * @param geneId
	 * @param sourceList
	 * @param etypeList
	 * @param sortType
	 * @param start
	 * @param limit
	 * @return
	 */
	private String generateCacheKey(Integer geneId, List<Integer> sourceList, List<Integer> etypeList, SortType sortType, Integer start, Integer limit) {
		return MessageFormat.format(cacheKeyTempalte, geneId, StringUtils.join(sourceList, ","), StringUtils.join(etypeList, ","), sortType.value(), start, limit);
	}
	
	/**
	 * @param geneId
	 * @param sourceList
	 * @param etypeList
	 * @param sortType
	 * @param start
	 * @param limit
	 * @return
	 */
	private List<CacheGeneRank> loadFromDB(Integer geneId, List<Integer> sourceList, List<Integer> etypeList, SortType sortType, Double mixturePerc, Integer start, Integer limit) {
		GeneRankCriteria geneRankCriteria = new GeneRankCriteria();
		geneRankCriteria.setGeneId(geneId);
		geneRankCriteria.setEtypeList(etypeList);
		geneRankCriteria.setSourceList(sourceList);
		geneRankCriteria.setSortType(sortType);
		if(mixturePerc != null) {
			geneRankCriteria.setMixturePerc(mixturePerc);
		}
		List<GeneRank> geneRankList = geneRankDAO.findByCriteria(geneRankCriteria, start, limit);
		if (CollectionUtils.isEmpty(geneRankList)) {
			logger.warn("don't find gene rank for : " + geneId);
			return Collections.EMPTY_LIST;
		} else {
			List<CacheGeneRank> cacheGeneRankList = new ArrayList<CacheGeneRank>();
			for (GeneRank geneRank : geneRankList) {
				CacheGeneRank cacheGeneRank = new CacheGeneRank();
				cacheGeneRank.setSampleId(geneRank.getSampleId());
				cacheGeneRank.setTss5kCount(geneRank.getTss5kCount());
				cacheGeneRank.setTss5kRank(geneRank.getTss5kRank());
				cacheGeneRank.setMixturePerc(geneRank.getMixturePerc());
				cacheGeneRank.setTss5kPerc(geneRank.getTss5kPerc());
				cacheGeneRank.setTotal(geneRank.getTotalCount());
				cacheGeneRank.setTssTesCount(geneRank.getTssTesCount());
				cacheGeneRank.setEtype(geneRank.getEtype());
				cacheGeneRankList.add(cacheGeneRank);
			}
			
			return cacheGeneRankList; 
		}
	}

	public boolean isAsySet() {
		return asySet;
	}

	public void setAsySet(boolean asySet) {
		this.asySet = asySet;
	}
	
	
}
