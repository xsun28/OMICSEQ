package com.omicseq.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.omicseq.domain.SampleCount;
import com.omicseq.store.dao.ISampleCountDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;

/**
 * @author Min.Wang
 * 
 */
public class SampleCountCache {

    private ISampleCountDAO sampleCountDAO = DAOFactory.getDAO(ISampleCountDAO.class);
    private static SampleCountCache instance = new SampleCountCache();
    private List<SampleCount> sampleCountList = null;

    public static SampleCountCache getInstance() {
        return instance;
    }

    public List<SampleCount> getSampleCountById(Integer sampleId) {
        if (sampleCountList != null && sampleCountList.get(0).getSampleId().compareTo(sampleId) == 0) {
            return sampleCountList;
        } else {
            List<SampleCount> tempSampleCountList = sampleCountDAO.findSampleCountById(sampleId);
            Set<Integer> geneIdSet = new HashSet<Integer>();
            List<SampleCount> finalSampleCountList = new ArrayList<SampleCount>();
            for (SampleCount sampleCount : tempSampleCountList) {
                if (geneIdSet.add(sampleCount.getGeneId())) {
                    finalSampleCountList.add(sampleCount);
                }
            }
            synchronized (SampleCountCache.class) {
                sampleCountList = finalSampleCountList;
                return sampleCountList;
            }
        }
    }

    public static void main(String[] args) {
        List<SampleCount> sampleCountList = SampleCountCache.getInstance().getSampleCountById(100037);
        List<SampleCount> preSampleCountList = SampleCountCache.getInstance().getSampleCountById(100039);
        System.out.println(" pre sample count size : " + preSampleCountList.size());
        for (SampleCount sampleCount : preSampleCountList) {
            System.out.println(" sampleCount size : " + sampleCount.getGeneId() + "; sample id : "
                    + sampleCount.getSampleId());
        }

    }

}
