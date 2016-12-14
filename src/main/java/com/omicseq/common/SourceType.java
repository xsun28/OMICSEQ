package com.omicseq.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import com.omicseq.core.PropertiesHolder;

public enum SourceType {
    TCGA(1, "TCGA"), //
    ENCODE(2, "ENCODE"), //
    Roadmap(3, "Epigenome Roadmap"), //
    ICGC(4, "ICGC"), //
    SRA(5, "SRA"), //
    CCLE(6,"CCLE"),
    GEO(7,"GEO"),
    SUMMARY(8,"SUMMARY"),
    ILLUMINA(9, "Illumina BodyMap"),
    GEUVADIS(10, "gEUVADIS"),
    BLUEPRINT(11, "BluePrint Epigenome"),
    ArrayExpress(12, "ArrayExpress"),
    TCGAFirebrowse(13, "TCGA Firebrowse"),
    SUPPLEMENTTARY(14,"Supplementary"),
    JASPAR(15,"JASPAR")
    ;

    private static Map<Integer, SourceType> valueMap = new HashMap<Integer, SourceType>(3);
    private static Map<String, SourceType> descMap = new HashMap<String, SourceType>();
    private static Map<SourceType, String> urlMap = new HashMap<SourceType, String>(3);
    private static Map<SourceType, Account> accMap = new HashMap<SourceType, SourceType.Account>(3);
    private static Map<Integer,SourceType> uiMap = new HashMap<Integer,SourceType>();
    static {
        for (SourceType sourceType : SourceType.values()) {
            descMap.put(sourceType.desc(), sourceType);
            valueMap.put(sourceType.value(), sourceType);
            //if (sourceType.getValue() != 5) {
            uiMap.put(sourceType.getValue(), sourceType);
            //}
        }
        urlMap.put(SourceType.TCGA, "https://tcga-data.nci.nih.gov/tcga/findArchives.htm");
        urlMap.put(SourceType.ENCODE, "");
        urlMap.put(SourceType.Roadmap, "ftp://ftp.genboree.org/EpigenomeAtlas/Current-Release/experiment-sample");
        urlMap.put(SourceType.ICGC, "https://dcc.icgc.org/repository/current/Projects");
        urlMap.put(SourceType.SRA, "http://www.ncbi.nlm.nih.gov/sra");
        urlMap.put(SourceType.GEO, "http://www.ncbi.nlm.nih.gov/gds");
        urlMap.put(SourceType.ArrayExpress, "http://www.ebi.ac.uk/arrayexpress/experiments/search.html");
        accMap.put(SourceType.Roadmap, Account.anonymous);
    }
    private Integer value;
    private String desc;

    SourceType(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static Map<Integer, SourceType> getUiMap() {
        return uiMap;
    }

    public static SourceType parse(Integer value) {
        return valueMap.get(value);
    }

    public static SourceType getType(String desc) {
        SourceType sourceType = descMap.get(desc);
        return sourceType;
    }

    public Integer value() {
        return value;
    }

    public Integer getValue() {
        return value();
    }

    public String desc() {
        return desc;
    }

    public String getDesc() {
        return desc();
    }

    public String url() {
        return urlMap.get(this);
    }

    public Account account() {
        return accMap.containsKey(this) ? accMap.get(this) : Account.anonymous;
    }

    /**
     * 网络地址转成本地文件路径
     * 
     * @param url
     * @return
     */
    public String convertToDisk(String url) {
        String fname = FilenameUtils.getName(url);
        if(fname.contains("file="))
        {
        	fname = fname.substring(fname.indexOf("file=")).replace("file=", "");
        }
        return String.format("%s/%s", path(), fname);
    }

    public String path() {
        StringBuilder sb = new StringBuilder();
        String root = PropertiesHolder.get(PropertiesHolder.FILES, "download", "/files/download");
        sb.append(root).append("/").append(this.name().toLowerCase());
        return sb.toString();
    }

    public static class Account {
        static Account anonymous = new Account("anonymous", "anonymous");
        public String username;
        public String password;

        public Account(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
