package com.omicseq.statistic.variation;

import com.omicseq.common.SourceType;

/**
 * 
 * 
 * @author zejun.du
 */
public class VariationStatisticFactory {

    /**
     * @param source
     * @return
     */
    public static IVariationRankStatistic get(SourceType source) {
    	if (SourceType.ENCODE.equals(source)) {
            return new EncodeVariationRankStatistic();
        } 
        return null;
    }
}
