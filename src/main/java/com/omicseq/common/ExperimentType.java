package com.omicseq.common;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public enum ExperimentType {
	SUMMARY_TRACK(0,"Summary Track"),
	CHIP_SEQ_TF(1, "ChIP-seq TF"), 
    CHIP_SEQ_HISTONE(17,"ChIP-seq His"),
	CHIP_SEQ_CHR(18,"ChIP-seq Chr"),
    RNA_SEQ(2, "RNA-seq"), 
    BS_SEQ(3, "BS-seq"),
    DNASE_SEQ(4, "Dnase-seq"), 
    MNASE_SEQ(5, "Mnase-seq"), 
    GRO_SEQ(6, "GRO-seq"), 
    HI_C(7, "Hi-C"), 
    MICROARRAY(8, "Microarray"), 
    GWAS(9, "GWAS"), 
    MOTIFS(10, "Motifs"),
    CVN(11,"CNV"),
    METHYLATION(12,"MethyLation"),
    RNA_SEQ_DIFF(13,"RNA-seq-diff"),
    MIRNA_SEQ(14,"MIRNA-seq"),
    MUTATION(15,"Somatic Mutations"),
    RIP_SEQ(16,"RIP-seq"),
    SUPPLEMENTTARY(19,"Supplementary Track");
    
    private static Map<Integer,ExperimentType> valueMap = new HashMap<Integer,ExperimentType>();
    private static LinkedHashMap<Integer,ExperimentType> uiMap = new LinkedHashMap<Integer,ExperimentType>();
    static {
        for (ExperimentType experimentType : ExperimentType.values()) {
            valueMap.put(experimentType.getValue(), experimentType);
            if (experimentType.getValue() == 1 || experimentType.getValue() == 2 || experimentType.getValue() == 4 ||experimentType.getValue() == 11 || experimentType.getValue() == 12 
            		|| experimentType.getValue() == 8 || experimentType.getValue() == 10 || experimentType.getValue() == 13 | experimentType.getValue() == 0 || experimentType.getValue() == 15 || experimentType.getValue() == 6
            		|| experimentType.getValue() == 16 || experimentType.getValue() == 17 || experimentType.getValue() == 18|| experimentType.getValue() == 19) {
                uiMap.put(experimentType.getValue(), experimentType );
            }
        }
    }
    private static Map<String,ExperimentType> descMap = new HashMap<String,ExperimentType>();
    static {
        for (ExperimentType experimentType : ExperimentType.values()) {
            descMap.put(experimentType.getDesc(), experimentType);
        }
    }
    
    private Integer value;
    private String desc;
    
    public static ExperimentType getType(String desc){
        ExperimentType experimentType = descMap.get(desc);
        return experimentType;
    }
    public static Map<Integer, ExperimentType> getUiMap() {
        return uiMap;
    }
    private ExperimentType(String desc){
        this.desc = desc;
    }
    ExperimentType(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static ExperimentType parse(Integer value) {
        return valueMap.get(value);
    }
    public Integer value() {
        return value;
    }
    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
    
}
