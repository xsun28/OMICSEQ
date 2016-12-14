package com.omicseq.statistic;

import com.omicseq.common.SourceType;

/**
 * 
 * 
 * @author zejun.du
 */
public class StatisticFactory {

    /**
     * @param source
     * @return
     */
    public static IRankStatistic get(SourceType source) {
        if (SourceType.TCGA.equals(source)) {
            return new TCGARankStatistic();
        } else if (SourceType.ENCODE.equals(source)) {
            return new EncodeRankStatistic();
        } else if (SourceType.Roadmap.equals(source)) {
            return new RoadmapRankStatistic();
        } else if (SourceType.SRA.equals(source) || SourceType.ILLUMINA.equals(source)) {
            return new SRARankStatistic();
        } else if (SourceType.ICGC.equals(source)) {
            return new ICGCRankStatistic();
        } else if (SourceType.ArrayExpress.equals(source)){
        	return new ArrayExpressStatistic();
        }
        return null;
    }
}
