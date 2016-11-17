package com.omicseq.web.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SortType;
import com.omicseq.common.SourceType;
import com.omicseq.domain.CacheGeneRank;
import com.omicseq.loader.IGeneRankLoader;
import com.omicseq.loader.PreCachGeneRankLoader;

@Component
public class SampleSearchServiceHelper {
    private IGeneRankLoader cacheRankLoader = new PreCachGeneRankLoader();

    public List<Integer> toSourceTypies(String sourcies) {
        if (StringUtils.isBlank(sourcies)) {
            return new ArrayList<Integer>(0);
        }
        List<Integer> intSourceList = new ArrayList<Integer>();
        String[] arr = StringUtils.split(sourcies, ",");
        for (String val : arr) {
            intSourceList.add(Integer.valueOf(val));
        }
        return intSourceList;
    }

    public List<Integer> toSourceTypies(List<String> sourceList) {
        List<Integer> intSourceList = new ArrayList<Integer>();
        if (CollectionUtils.isNotEmpty(sourceList)) {
            for (String source : sourceList) {
                intSourceList.add(SourceType.getType(source).value());
            }
        }
        return intSourceList;
    }

    public List<Integer> toEtypies(String experiments) {
        if (StringUtils.isBlank(experiments)) {
            return new ArrayList<Integer>(0);
        }
        List<Integer> intEtypeList = new ArrayList<Integer>();
        String[] arr = StringUtils.split(experiments, ",");
        for (String val : arr) {
            intEtypeList.add(Integer.valueOf(val));
        }
        return intEtypeList;
    }

    public List<Integer> toEtypies(List<String> etypeList) {
        List<Integer> intEtypeList = new ArrayList<Integer>();
        if (CollectionUtils.isNotEmpty(etypeList)) {
            for (String eType : etypeList) {
                intEtypeList.add(ExperimentType.getType(eType).getValue());
            }
        }
        return intEtypeList;
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
    public List<CacheGeneRank> searchSampleByGeneId(Integer geneId, List<Integer> sourceList, List<Integer> etypeList,
            SortType sortType, Double mixturePerc, Integer start, Integer limit) {
        return cacheRankLoader.load(geneId, sourceList, etypeList, sortType, mixturePerc, start, limit);
    }

}
