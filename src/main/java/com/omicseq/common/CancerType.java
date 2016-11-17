package com.omicseq.common;

import java.util.HashMap;
import java.util.Map;

public enum CancerType {
	ACC("acc", "Adrenocortical carcinoma"),
	BLCA("blca","Bladder Urothelial Carcinoma"),
	BRCA("brca", "Breast invasive carcinoma"),
	LGG("lgg", "Brain Lower Grade Glioma"),
	LAML("laml", "Acute Myeloid Leukemia"),
	CESC("cesc", "Cervical squamous cell carcinoma and endocervical adenocarcinoma"),
	COAD("coad", "Colon adenocarcinoma"),
	ESCA("esca", "Esophageal carcinoma"),
	GBM("gbm","Glioblastoma multiforme"),
	HNSC("hnsc","Head and Neck squamous cell carcinoma"),
	KICH("kich","Kidney Chromophobe"),
	KIRP("kirp","Kidney Renal Papillary Cell Carcinoma"),
	KIRC("kirc", "Kidney Renal Clear Cell Carcinoma"),
	LIHC("lihc", "Liver hepatocellular carcinoma"),
	LUAD("luad", "Lung adenocarcinoma"),
	LUSC("lusc", "Lung squamous cell carcinoma"),
	DLBC("dlbc","Lymphoid Neoplasm Diffuse Large B-cell Lymphoma"),
	OV("ov", "Ovarian serous cystadenocarcinoma"),
	PAAD("paad", "Pancreatic adenocarcinoma"),
	PRAD("prad", "Prostate adenocarcinoma"),
	READ("read", "Rectum adenocarcinoma"),
	SARC("sarc", "Sarcoma"),
	SKCM("skcm", "Skin Cutaneous Melanoma"),
	STAD("stad", "Stomach adenocarcinoma"),
	TGCT("tgct","Testicular Germ Cell Tumors"),
	THYM("thym","Thymoma"),
	THCA("thca", "Thyroid carcinoma"),
	UCS("ucs", "Uterine Carcinosarcoma"),
	UCEC("ucec", "Uterine Corpus Endometrial Carcinoma"),
	UVM("uvm","Uveal Melanoma"),
	MESO("meso", "Mesothelioma"),
	PCPG("pcpg", "Pheochromocytoma and Paraganglioma"),
	PACA("paca", "Pancreatic"),
	CLLE("ccle", "Lymphocyclic"),
	RECA("reca", "Renal"),
	MALY("maly", "Lymphoma"),
	PAEN("paen", "Pancreatic"),
	EOPC("eopc", "Prostate"),
	PBCA("pbca", "Brain"),
	GACA("gaca", "Gastric"),
	ESAD("esad", "Esophageal"),
	BOCA("boca", "Bone"),
	CHOL("chol","Cholangiocarcinoma"),
	FPPP("fppp","FFPE Pilot Phase II") ;
	
	private static Map<String, CancerType> uiMap = new HashMap<String, CancerType>();
	static {
        for (CancerType cancerType : CancerType.values()) {
        	uiMap.put(cancerType.getValue(), cancerType);
        }
	}
	
	private String value;
	
    private String name;
    
	CancerType(String value, String name)
	{
		this.value = value;
        this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
