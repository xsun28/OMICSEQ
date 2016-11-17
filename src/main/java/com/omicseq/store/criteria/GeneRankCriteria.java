package com.omicseq.store.criteria;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.omicseq.common.SortType;

public class GeneRankCriteria {
    public static String ImageFileTempalte = "so%s_e%s/%s.png";
    public static String CSVFileTempalte = "so%s_e%s/%s.csv";
    public static String XLSXFileTempalte = "so%s_e%s/%s.xlsx";
    public static String CacheCountTempalte = "rankcount_so%s_e%s_g%s";
    public static String CacheTotalCountTempalte = "ranktotalcount_so%s_e%s_g%s";
    private Integer geneId;
    private List<Integer> sourceList;
    private List<Integer> etypeList;
    private SortType sortType;
    private Integer sampleId;
    private Double mixturePerc;

    public Integer getGeneId() {
        return geneId;
    }

    public void setGeneId(Integer geneId) {
        this.geneId = geneId;
    }

    public List<Integer> getSourceList() {
        return sourceList;
    }

    public void setSourceList(List<Integer> sourceList) {
        this.sourceList = sourceList;
    }

    public List<Integer> getEtypeList() {
        return etypeList;
    }

    public void setEtypeList(List<Integer> etypeList) {
        this.etypeList = etypeList;
    }

    public SortType getSortType() {
        return sortType;
    }

    public void setSortType(SortType sortType) {
        this.sortType = sortType;
    }

    public Integer getSampleId() {
        return sampleId;
    }

    public void setSampleId(Integer sampleId) {
        this.sampleId = sampleId;
    }

    public String generateKey(String tpl) {
        String sources = StringUtils.trimToEmpty(StringUtils.join(sourceList, "-"));
        String etypes = StringUtils.trimToEmpty(StringUtils.join(etypeList, "-"));
        return String.format(tpl, sources, etypes, geneId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GeneRankCriteria{");
        if (null != sampleId) {
            sb.append("sampleId=").append(sampleId).append(";");
        }
        if (null != geneId) {
            sb.append("geneId=").append(geneId).append(";");
        }
        if (null != sourceList && sourceList.size() != 0) {
            sb.append("sourceList=").append(StringUtils.join(sourceList, ",")).append(";");
        }
        if (null != etypeList && etypeList.size() != 0) {
            sb.append("etypeList=").append(StringUtils.join(etypeList, ",")).append(";");
        }
        if (null != sortType) {
            sb.append("sortType=").append(sortType).append(";");
        }
        if (sb.lastIndexOf(";") != -1) {
            sb.deleteCharAt(sb.lastIndexOf(";"));
        }
        sb.append("}");
        return sb.toString();
    }

	public Double getMixturePerc() {
		return mixturePerc;
	}

	public void setMixturePerc(Double mixturePerc) {
		this.mixturePerc = mixturePerc;
	}

}
