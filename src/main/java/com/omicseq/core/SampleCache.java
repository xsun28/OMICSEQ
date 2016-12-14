package com.omicseq.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.omicseq.common.Constants;
import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.domain.Gene;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;

/**
 * @author Min.Wang
 * 
 */
public class SampleCache extends AbstractCache<Integer, Sample> implements IInitializeable {
    private ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, Constants.STAT_SUFFIX);
    private static SampleCache sampleCache = new SampleCache(false);
    private Map<String, List<Sample>> sampleCodeMap = new HashMap<String, List<Sample>>();
    private Map<String, List<Sample>> cellMap = new HashMap<String,List<Sample>>();
    private Map<String, List<Sample>> detailMap = new HashMap<String,List<Sample>>();
    private Map<String, Integer> sampleSumMap = new HashMap<String, Integer>();
    private Map<String, Integer> mouseSampleSumMap = new HashMap<String, Integer>();

    private SampleCache(boolean lazy) {
        super(lazy);
    }

    @Override
    public void doInit() {
        synchronized (SampleCache.class) {
            Integer start = 0;
            Integer limit = 3000;
            List<Sample> sampleList = null;
            while (CollectionUtils.isNotEmpty(sampleList = sampleDAO.loadSampleList(start, limit))) {
                for (Sample sample : sampleList) {
                    put(sample.getSampleId(), sample);
                    String donorId = sample.getSampleCode();
                    if (StringUtils.isNotBlank(donorId)) {
                    	List<Sample> donorSampleList = sampleCodeMap.get(donorId);
                    	if (donorSampleList == null) {
                    		donorSampleList = new ArrayList<Sample>();
                    		sampleCodeMap.put(donorId, donorSampleList);
                    	}
                    	donorSampleList.add(sample);
                    }
                    String cell = sample.getCell();
                    if(StringUtils.isNoneBlank(cell)){
                    	List<Sample> cellSampleList = cellMap.get(cell.toLowerCase());
                    	if(cellSampleList == null ) {
                    		cellSampleList = new ArrayList<Sample>();
                    		cellMap.put(cell.toLowerCase(), cellSampleList);
                    	}
                    	cellSampleList.add(sample);
                    }
                    String detail = sample.getDetail();
                    if(StringUtils.isNotBlank(detail)){
                    	List<Sample> factorSampleList = detailMap.get(detail.toLowerCase());
                    	if(factorSampleList == null) {
                    		factorSampleList = new ArrayList<Sample>();
                    		detailMap.put(detail.toLowerCase(), factorSampleList);
                    	}
                    	factorSampleList.add(sample);
                    }
                    //统计每个source和etype的sample数
                    SourceType type = sample.getSource() != null ? SourceType.parse(sample.getSource()) : null;
                    ExperimentType eType = sample.getEtype() != null ? ExperimentType.parse(sample.getEtype()) : null;
                    if (type != null && StringUtils.isEmpty(sample.getFromType())) {
                        if (!sampleSumMap.containsKey(type.desc())) {
                            sampleSumMap.put(type.desc(), 0);
                        }
                        sampleSumMap.put(type.desc(), sampleSumMap.get(type.desc())+1);
                    }
                    else if(type != null && "mouse".equals(sample.getFromType())){
                    	if (!mouseSampleSumMap.containsKey(type.desc())) {
                    		mouseSampleSumMap.put(type.desc(), 0);
                        }
                    	mouseSampleSumMap.put(type.desc(), mouseSampleSumMap.get(type.desc())+1);
                    }
                    if (eType != null && StringUtils.isEmpty(sample.getFromType())) {
                        if (!sampleSumMap.containsKey(eType.getDesc())) {
                            sampleSumMap.put(eType.getDesc(), 0);
                        }
                        sampleSumMap.put(eType.getDesc(), sampleSumMap.get(eType.getDesc())+1);
                    }
                    else if(eType != null && "mouse".equals(sample.getFromType())){
                    	  if (!mouseSampleSumMap.containsKey(eType.getDesc())) {
                    		  mouseSampleSumMap.put(eType.getDesc(), 0);
                          }
                    	  mouseSampleSumMap.put(eType.getDesc(), mouseSampleSumMap.get(eType.getDesc())+1);
                    }
                }
                start = start + limit;
            }
        }
    }

    public Sample getSampleById(Integer id) {
        return super.get(id);
    }

    @Override
    Sample lazyLoad(Integer key) {
        return sampleDAO.getBySampleId(key);
    }

    public static SampleCache getInstance() {
        return sampleCache;
    }
    
    public List<Sample> getSampleByCellAndDetail(String cell,String detail, List<Integer> sourceList, List<Integer> etypeList){
    	List<Sample> cList = new ArrayList<Sample>();
    	List<Sample> dList = new ArrayList<Sample>();
    	List<Sample> finalList = new ArrayList<Sample>();
    	if(StringUtils.isNotBlank(cell)){
    		for(String key :cellMap.keySet()){
	    		if(key.contains(cell.toLowerCase())){
	    			List<Sample> samples = cellMap.get(key);
	    			for(Sample sa : samples){
	    				if(sourceList.contains(sa.getSource()) && etypeList.contains(sa.getEtype())){
	    					cList.add(sa);
	    				}
	    			}
	    		}
	    	}
    	}
    	if(StringUtils.isNotBlank(detail)){
    		for(String key : detailMap.keySet()){
    			if(key.contains(detail.toLowerCase())){
    				List<Sample> samples = detailMap.get(key);
    				for(Sample sa : samples){
    					if(sourceList.contains(sa.getSource()) && etypeList.contains(sa.getEtype())){
	    					dList.add(sa);
	    				}
    				}
    			}
    		}
    	}
    	
    	if(StringUtils.isNotBlank(cell) && StringUtils.isBlank(detail)) finalList = cList;
    	
    	if(StringUtils.isNotBlank(detail) && StringUtils.isBlank(cell)) finalList = dList;
    	
    	
    	if(StringUtils.isNotBlank(cell) && StringUtils.isNotBlank(detail)){
    		 cList.retainAll(dList);
    		 finalList = cList;
    	}
    	return finalList;
    }
    
    public Sample getSampleBySampleCode(String sampleCode, Integer source) {
    	List<Sample> donorSampleList = sampleCodeMap.get(sampleCode);
    	if (CollectionUtils.isNotEmpty(donorSampleList)) {
    		for (Sample sample : donorSampleList) {
    			if (source.compareTo(sample.getSource()) == 0) {
    				return sample;
    			}
    		}
    	}
    	return null;
    }
    
    public Map<String, Integer> getSampleSumMap() {
        return sampleSumMap;
    }
    
    public Map<String, Integer> getMouseSampleSumMap() {
        return mouseSampleSumMap;
    }
    
    /**
     * 获取所有不重复的gene_id
     * 
     * @return
     */
    public List<Integer> getSampleIds() {
        Collection<Sample> coll = values();
        if (CollectionUtils.isEmpty(coll)) {
            return Collections.emptyList();
        }
        List<Integer> rs = new ArrayList<Integer>(5);
        for (Sample sample : coll) {
            Integer key = sample.getSampleId();
            if (!rs.contains(key)) {
                rs.add(key);
            }
        }
        Collections.sort(rs);
        return rs;
    }
    public static void main(String[] args) {
		sampleCache.doInit();
	}
}
