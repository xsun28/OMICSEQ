package com.omicseq.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.domain.MiRNASample;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.ImiRNASampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;

public class MiRNASampleCache extends AbstractCache<Integer, MiRNASample> implements IInitializeable {
    private ImiRNASampleDAO miRNASampleDAO = DAOFactory.getDAO(ImiRNASampleDAO.class);
    private static MiRNASampleCache miRNASampleCache = new MiRNASampleCache(false);
    private Map<String, List<MiRNASample>> miRNASampleCodeMap = new HashMap<String, List<MiRNASample>>();
    private Map<String, Integer> miRNASampleSumMap = new HashMap<String, Integer>();
    private Map<String, List<MiRNASample>> cellMap = new HashMap<String,List<MiRNASample>>();

    private MiRNASampleCache(boolean lazy) {
        super(lazy);
    }

    @Override
    public void doInit() {
        synchronized (MiRNASampleCache.class) {
            Integer start = 0;
            Integer limit = 3000;
            List<MiRNASample> miRNASampleList = null;
            while (CollectionUtils.isNotEmpty(miRNASampleList = miRNASampleDAO.loadMiRNASampleList(start, limit))) {
                for (MiRNASample miRNASample : miRNASampleList) {
                    put(miRNASample.getMiRNASampleId(), miRNASample);
                    String donorId = miRNASample.getBarCode();
                    if (StringUtils.isNotBlank(donorId)) {
                    	List<MiRNASample> donorMiRNASampleList = miRNASampleCodeMap.get(donorId);
                    	if (donorMiRNASampleList == null) {
                    		donorMiRNASampleList = new ArrayList<MiRNASample>();
                    		miRNASampleCodeMap.put(donorId, donorMiRNASampleList);
                    	}
                    	donorMiRNASampleList.add(miRNASample);
                    }
                    
                    String cell = miRNASample.getCell();
                    if(StringUtils.isNoneBlank(cell)){
                    	List<MiRNASample> cellSampleList = cellMap.get(cell.toLowerCase());
                    	if(cellSampleList == null ) {
                    		cellSampleList = new ArrayList<MiRNASample>();
                    		cellMap.put(cell.toLowerCase(), cellSampleList);
                    	}
                    	cellSampleList.add(miRNASample);
                    }
                    //统计每个source和etype的sample数
                    SourceType type = miRNASample.getSource() != null ? SourceType.parse(miRNASample.getSource()) : null;
                    ExperimentType eType = miRNASample.getEtype() != null ? ExperimentType.parse(miRNASample.getEtype()) : null;
                    if (type != null) {
                        if (!miRNASampleSumMap.containsKey(type.desc())) {
                        	miRNASampleSumMap.put(type.desc(), 0);
                        }
                        miRNASampleSumMap.put(type.desc(), miRNASampleSumMap.get(type.desc())+1);
                    }
                    if (eType != null) {
                        if (!miRNASampleSumMap.containsKey(eType.getDesc())) {
                        	miRNASampleSumMap.put(eType.getDesc(), 0);
                        }
                        miRNASampleSumMap.put(eType.getDesc(), miRNASampleSumMap.get(eType.getDesc())+1);
                    }
                }
                start = start + limit;
            }
        }
    }

    public MiRNASample getMiRNASampleById(Integer id) {
        return super.get(id);
    }

    @Override
    MiRNASample lazyLoad(Integer key) {
        return miRNASampleDAO.getByMiRNASampleId(key);
    }

    public static MiRNASampleCache getInstance() {
        return miRNASampleCache;
    }
    
    public MiRNASample getSampleBySampleCode(String sampleCode, Integer source) {
    	List<MiRNASample> donorSampleList = miRNASampleCodeMap.get(sampleCode);
    	if (CollectionUtils.isNotEmpty(donorSampleList)) {
    		for (MiRNASample miRNASample : donorSampleList) {
    			if (source.compareTo(miRNASample.getSource()) == 0) {
    				return miRNASample;
    			}
    		}
    	}
    	return null;
    }
    
    public List<MiRNASample> getSampleByCell(String cell){
    	List<MiRNASample> cList = new ArrayList<MiRNASample>();
    	List<MiRNASample> finalList = new ArrayList<MiRNASample>();
    	if(StringUtils.isNotBlank(cell)){
    		for(String key :cellMap.keySet()){
	    		if(key.contains(cell.toLowerCase())){
	    			List<MiRNASample> samples = cellMap.get(key);
	    			for(MiRNASample sa : samples){
	    				cList.add(sa);
	    			}
	    		}
	    	}
    	}
    	
    	if(StringUtils.isNotBlank(cell)) finalList = cList;
    	
    	
    	if(StringUtils.isNotBlank(cell)){
    		 finalList = cList;
    	}
    	return finalList;
    }
    
    public Map<String, Integer> getSampleSumMap() {
        return miRNASampleSumMap;
    }
    
    public static void main(String[] args) {
		MiRNASampleCache.getInstance().init();
		MiRNASample sample = MiRNASampleCache.getInstance().getMiRNASampleById(1);
		System.out.println();
		
	}
}
