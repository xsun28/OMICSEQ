package com.omicseq.bean;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.core.AntibodyCache;
import com.omicseq.core.GeneCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.utils.DateUtils;

/**
 * @author Min.Wang
 * 
 */
public class SampleItem implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Integer sampleId;
    private String cell;
    private String cell_desc;
    private String factor;
    private String factor_desc;
    private Integer rank;
    private Integer total;
    private String percentileFormat;
    private String description;
    private String url;
    private String dccAccession;
    private String geoSampleAccession;
    private String dataType; // experimentType
    private String study; // sourceType
    private String lab;
    private String timeStamp;
    private Double mixturePerc;
    private Double tss5kPerc;
    private Double tssCount; //页面上显示为tss5kCount
    private Double tssTesCount;
    private String ucscUrl;
    private String address;
    private Double pathwayOfRank;
    private Map<String,Gene> top5Genes;
    private Double variationOrderNoRank;
    private String pubMedUrl;
    
    private String settype;
    
    private String detail;

    public Double getTssTesCount() {
		return tssTesCount;
	}

	public void setTssTesCount(Double tssTesCount) {
		this.tssTesCount = tssTesCount;
	}

	public Double getTssCount() {
		return tssCount;
	}

	public void setTssCount(Double tssCount) {
		this.tssCount = tssCount;
	}

	
	public Double getVariationOrderNoRank() {
		return variationOrderNoRank;
	}

	public void setVariationOrderNoRank(Double variationOrderNoRank) {
		this.variationOrderNoRank = variationOrderNoRank;
	}



	private static Map<String, String> tcgaPubMeMap = new HashMap<String, String>();
    static {
    	tcgaPubMeMap.put("TCGA-GBM", "18772890");
    	tcgaPubMeMap.put("TCGA-OV", "21720365");
    	tcgaPubMeMap.put("TCGA-LUSC", "22960745");
    	tcgaPubMeMap.put("TCGA-LAML", "23634996");
    	tcgaPubMeMap.put("TCGA-UCEC", "23636398");
    	tcgaPubMeMap.put("TCGA-KIRC", "23792563");
    	tcgaPubMeMap.put("TCGA-BLCA", "24476821");
    }
    
    private String[] metaData;

    private static Map<String, String> labMap = new HashMap<String, String>();
    static {
        labMap.put("unc.edu", "UNC");
        labMap.put("bcgsc.ca", "BCGSC");
        labMap.put("University of Washington", "UW");
    }

    public SampleItem(Sample sample, GeneRank geneRank) {
        this(sample, geneRank.getTotalCount(), geneRank.getMixturePerc(), geneRank.getTss5kPerc(), geneRank.getTss5kCount(), geneRank.getTssTesCount());
    }

    public SampleItem(Sample sample, Integer total, Double mixturePerc, Double tss5kPerc, Double tss5kCount, Double tssTesCount) {
        this(sample, null, total, mixturePerc, tss5kPerc, null, false, null, tss5kCount, tssTesCount);
    }

    public SampleItem(Sample sample, Integer rank, Integer total, Double mixturePerc, Double tss5kPerc,
            String percentileFormat, boolean isHistory, String dateType, Double tss5kCount, Double tssTesCount) {
        if (null != sample) {
        	this.pubMedUrl = sample.getPubmedUrl();
            this.sampleId = sample.getSampleId();
            this.url = sample.getUrl();
            this.description = sample.getDescription();
            
            this.cell = sample.getCell();
            
            SourceType source = SourceType.parse(sample.getSource());
            this.study = null == source ? "" : source.desc();
            this.factor = sample.getFactor();
            ExperimentType experimentType = ExperimentType.parse(sample.getEtype());
            this.dataType = null == experimentType ? "" : experimentType.getDesc();
            
            this.lab = sample.getLab();
            this.detail = sample.getDetail();
            if(sample.getUrl() !=null ){
	            String [] url = sample.getUrl().split("/");
			   String dataSource;
	            if(sample.getSource() > 15)
	            dataSource = "unkown";
	            else
	             dataSource = SourceType.getUiMap().get(sample.getSource()).toString().toLowerCase();
//			String dataSource = SourceType.getUiMap().get(sample.getSource()).toString().toLowerCase();
		        if(dataSource.equals("Epigenome Roadmap".toLowerCase())){
		        	dataSource = "roadmap";
		        }
	            this.ucscUrl ="$url=/files/download/"
		            + dataSource
		            + "/"
		            + url[url.length-1]
		            + "$sampleid="+sample.getSampleId()
		            + "$sourceType="+sample.getSource()
		            + "$version=19$format=html";
            }
            // parse data.
            Date date = null == sample.getTimeStamp() ? null : DateUtils.parseToDate(sample.getTimeStamp());
            this.timeStamp = null == date ? null : DateUtils.format(date, "yyyy/MM/dd");
            // 兼容性处理
            Map<String, String> map = sample.descMap();
            this.cell = null == this.cell ? map.get("cell") : this.cell;
            this.factor = null == this.factor ? map.get("antibody") : this.factor;
            this.lab = null == this.lab ? map.get("lab") : this.lab;
            this.timeStamp = null == this.timeStamp ? map.get("datesubmitted") : this.timeStamp;
            this.dccAccession = map.get("dccaccession");
            this.geoSampleAccession = map.get("geosampleaccession");
            if(StringUtils.isBlank(geoSampleAccession))
            {
            	this.geoSampleAccession = map.get("geo sample accession");
            }
            if(sample.getEtype() == 0)
            {
            	this.settype = sample.getSettype() == null?"":sample.getSettype()+" ";
            }
            if(sample.getEtype() != 11 && sample.getEtype() != 12){
            	 formatSample(sample);
            }
        }
        if (isHistory) {
            this.rank = rank;
            this.total = total;
            this.percentileFormat = percentileFormat;
            this.dataType = dateType;
        } else {
            this.mixturePerc = null == mixturePerc ? 0D : mixturePerc;
            // 保留三位小数
            DecimalFormat df = new DecimalFormat("0.000");
            logger.debug("current mixturePerc : " + mixturePerc + ", tss5kPerc : " + tss5kPerc);
            this.mixturePerc = null == mixturePerc ? 0D : mixturePerc;
            this.tss5kPerc = null == tss5kPerc ? 0D : tss5kPerc;
            this.tssCount = null==tss5kCount ?0D:tss5kCount;
            this.tssTesCount = null==tssTesCount?0D:tssTesCount;
            this.percentileFormat = df.format(this.mixturePerc * 100);
            if (total != null) {
            	this.total = total;
            } else {
            	 if (Double.compare(this.mixturePerc, this.tss5kPerc) == 0) {
                     this.total = GeneCache.getInstance().getTss5kTotal();
                     if(this.dataType.contains("ChIP-seq")){
                    	 this.dataType =  dataType + "(P)";
                     }
                 } else {
                     this.total = GeneCache.getInstance().getTssTesTotal();
                     if(this.dataType.contains("ChIP-seq")){
                    	 this.dataType =  dataType + "(B)";
                     } 
                 }
            }
            BigDecimal bigDecimal = new BigDecimal(this.mixturePerc * this.total).setScale(0, BigDecimal.ROUND_HALF_UP);
            this.rank = bigDecimal.intValue();
        }

        if(sample.getEtype() != ExperimentType.CHIP_SEQ_HISTONE.getValue() && sample.getEtype() != ExperimentType.CHIP_SEQ_TF.getValue() && sample.getEtype() != ExperimentType.CHIP_SEQ_CHR.getValue())
        {
        	if(this.tssTesCount < 0)
            {
            	this.rank = (-1) * this.rank;
            }
        }
    }

    public SampleItem(Sample sample, Integer rank, Integer total, Double mixturePerc, Double tss5kPerc,
            String percentileFormat, boolean isHistory, String dateType, Double tss5kCount, Double tssTesCount, Double variationOrderNoRank) {
        if (null != sample) {
        	this.pubMedUrl = sample.getPubmedUrl();
            this.sampleId = sample.getSampleId();
            this.url = sample.getUrl();
            this.description = sample.getDescription();
            this.cell = sample.getCell();
            this.variationOrderNoRank = variationOrderNoRank;
            SourceType source = SourceType.parse(sample.getSource());
            this.study = null == source ? "" : source.desc();
            this.factor = sample.getFactor();
            ExperimentType experimentType = ExperimentType.parse(sample.getEtype());
            this.dataType = null == experimentType ? "" : experimentType.getDesc();
            
            this.lab = sample.getLab();
            this.detail = sample.getDetail();
            if(sample.getUrl() !=null ){
	            String [] url = sample.getUrl().split("/");
	            String dataSource = SourceType.getUiMap().get(sample.getSource()).toString().toLowerCase();
		        if(dataSource.equals("Epigenome Roadmap".toLowerCase())){
		        	dataSource = "roadmap";
		        }
	            this.ucscUrl ="$url=/files/download/"
		            + dataSource
		            + "/"
		            + url[url.length-1]
		            + "$sampleid="+sample.getSampleId()
		            + "$sourceType="+sample.getSource()
		            + "$version=19$format=html";
            }
            // parse data.
            Date date = null == sample.getTimeStamp() ? null : DateUtils.parseToDate(sample.getTimeStamp());
            this.timeStamp = null == date ? null : DateUtils.format(date, "yyyy/MM/dd");
            // 兼容性处理
            Map<String, String> map = sample.descMap();
            this.cell = null == this.cell ? map.get("cell") : this.cell;
            this.factor = null == this.factor ? map.get("antibody") : this.factor;
            this.lab = null == this.lab ? map.get("lab") : this.lab;
            this.timeStamp = null == this.timeStamp ? map.get("datesubmitted") : this.timeStamp;
            this.dccAccession = map.get("dccaccession");
            this.geoSampleAccession = map.get("geosampleaccession");
            if(StringUtils.isBlank(geoSampleAccession))
            {
            	this.geoSampleAccession = map.get("geo sample accession");
            }
            if(sample.getEtype() == 0)
            {
            	this.settype = sample.getSettype() == null?"":sample.getSettype()+" ";
            }
            if(sample.getEtype() != 11 && sample.getEtype() != 12){
            	 formatSample(sample);
            }
        }
        if (isHistory) {
            this.rank = rank;
            this.total = total;
            this.percentileFormat = percentileFormat;
            this.dataType = dateType;
        } else {
            this.mixturePerc = null == mixturePerc ? 0D : mixturePerc;
            // 保留三位小数
            DecimalFormat df = new DecimalFormat("0.000");
            logger.debug("current mixturePerc : " + mixturePerc + ", tss5kPerc : " + tss5kPerc);
            this.mixturePerc = null == mixturePerc ? 0D : mixturePerc;
            this.tss5kPerc = null == tss5kPerc ? 0D : tss5kPerc;
            this.tssCount = null==tss5kCount ?0D:tss5kCount;
            this.tssTesCount = null==tssTesCount?0D:tssTesCount;
            this.percentileFormat = df.format(this.mixturePerc * 100);
            if (total != null) {
            	this.total = total;
            } else {
            	 if (Double.compare(this.mixturePerc, this.tss5kPerc) == 0) {
                     this.total = GeneCache.getInstance().getTss5kTotal();
                     if(this.dataType.contains("ChIP-seq")){
                    	 this.dataType =  dataType + "(P)";
                     }
                 } else {
                     this.total = GeneCache.getInstance().getTssTesTotal();
                     if(this.dataType.contains("ChIP-seq")){
                    	 this.dataType =  dataType + "(B)";
                     } 
                 }
            }
            BigDecimal bigDecimal = new BigDecimal(this.mixturePerc * this.total).setScale(0, BigDecimal.ROUND_HALF_UP);
            this.rank = bigDecimal.intValue();
        }

        if(sample.getEtype() != ExperimentType.CHIP_SEQ_HISTONE.getValue() && sample.getEtype() != ExperimentType.CHIP_SEQ_TF.getValue() && sample.getEtype() != ExperimentType.CHIP_SEQ_CHR.getValue())
        {
        	if(this.tssTesCount < 0)
            {
            	this.rank = (-1) * this.rank;
            }
        }
    }
    private void formatSample(Sample sample) {
        // factor
        SourceType source = SourceType.parse(sample.getSource());
        if (SourceType.TCGA.equals(source) && StringUtils.isBlank(this.factor) && StringUtils.isNotBlank(this.url)) {
//            String[] arr = StringUtils.split(this.url, "/");
//            this.factor = arr.length > 8 ? arr[7] : null;
        }
        if (StringUtils.isNotBlank(this.factor)) {
            String fac = this.factor.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll(",", "").trim();
            String target = AntibodyCache.getInstance().getTargetByAntibody(fac);
            this.factor = StringUtils.isNotBlank(target) ? target : this.factor;
        }
        if (StringUtils.equalsIgnoreCase(this.factor, "ChIP-seq input")) {
            this.factor = "input";
        }
        // lab
        for (String key : labMap.keySet()) {
            if (key.equalsIgnoreCase(this.lab)) {
                this.lab = labMap.get(key);
            }
        }
        // TCGA
        if (SourceType.TCGA.equals(source)) {
//            this.cell = "TCGA-" + this.factor;
//            this.factor = "";
        }
        // ICGC
        if (SourceType.ICGC.equals(source) && StringUtils.isNotBlank(this.cell)) {
            this.cell = "ICGC-" + this.cell;
        }
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }

    public String getFactor() {
        return factor;
    }

    public void setFactor(String factor) {
        this.factor = factor;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getTotal() {
        return total;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDccAccession() {
        return dccAccession;
    }

    public void setDccAccession(String dccAccession) {
        this.dccAccession = dccAccession;
    }

    public String getGeoSampleAccession() {
        return geoSampleAccession;
    }

    public void setGeoSampleAccession(String geoSampleAccession) {
        this.geoSampleAccession = geoSampleAccession;
    }

    public Integer getSampleId() {
        return sampleId;
    }

    public void setSampleId(Integer sampleId) {
        this.sampleId = sampleId;
    }

    @Override
    public String toString() {
        return "SampleItem [sampleId=" + sampleId + ", cell=" + cell + ", factor=" + factor + ", rank=" + rank
                + ", total=" + total + ", mixturePerc=" + mixturePerc + ", description=" + description + ", url=" + url
                + ", dccAccession=" + dccAccession + ", geoSampleAccession=" + geoSampleAccession + "]";
    }

    public String getGeoUrl() {
    	if (this.study.equalsIgnoreCase(SourceType.TCGA.getDesc())){
    		String pubmeId = tcgaPubMeMap.get(this.cell);
    		String geourl = "";
        	if (pubmeId != null && pubmeId.equals("18772890")) {
        		geourl = "http://www.ncbi.nlm.nih.gov/pmc/articles/PMC2671642/?tool="+pubmeId;
        	}else if(pubmeId != null && pubmeId.equals("21720365")){
        		geourl = "http://www.ncbi.nlm.nih.gov/pmc/articles/PMC3163504/?tool="+pubmeId;
        	}else if (pubmeId != null && pubmeId != null && pubmeId.equals("24071849")){
        		geourl = "http://www.nature.com/nature/journal/vaop/ncurrent/full/nature11404.html";
        	}else if (pubmeId != null && pubmeId.equals("23634996")){
        		geourl = "http://www.nejm.org/doi/full/10.1056/NEJMoa1301689";
        	}else if (pubmeId != null && pubmeId.equals("23636398")){
        		geourl = "http://www.nature.com/nature/journal/v497/n7447/full/nature12113.html";
        	}else if (pubmeId != null && pubmeId.equals("23792563")){
        		geourl = "http://www.nature.com/nature/journal/vaop/ncurrent/full/nature12222.html";
        	}else if (pubmeId != null && pubmeId.equals("24476821")){
        		geourl = "http://www.nature.com/nature/journal/vaop/ncurrent/full/nature12965.html";
        	}else{
        		pubmeId = "24071849";
        		geourl = "http://www.nature.com/ng/journal/v45/n10/full/ng.2764.html";
        	}
        	return geourl;
    	}else if(this.study.equalsIgnoreCase(SourceType.CCLE.getDesc())){
    		return "http://www.broadinstitute.org/ccle/data/browseData?conversationPropagation=begin";
    	}else if(this.study.equalsIgnoreCase(SourceType.GEO.getDesc())){
    		return this.url;
    	}else if(this.study.equalsIgnoreCase(SourceType.SUMMARY.getDesc())){
        	return "http://macarthurlab.org/lof/";
        }else if(this.study.equalsIgnoreCase(SourceType.TCGAFirebrowse.getDesc())) {
        	return "http://www.firebrowse.org/";
        }else if(this.study.equalsIgnoreCase(SourceType.JASPAR.getDesc())){
        	return "#";
        }else{
	    	this.description = this.description==null?"" : this.description.replaceAll("; ", ";").replaceAll("\"", "");
	    	String[] arr = StringUtils.split(this.description, ";");
	    	boolean flag = false;
	    	String geoAccession = "";
	    	for(int i = 0;i<arr.length;i++){
	    		String [] arr1 = arr[i].split("=");
	    		if("Experiment attributes".equals(arr1[0]) || "experiment attributes".equals(arr1[0])){
	    			flag = true;
	    			geoAccession = arr1[arr1.length-1].split(":")[1];
	    			break;
	    		}
	    	}
	    	if (flag) {
	            String ncbiUrl = "http://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc={0}";
	            return MessageFormat.format(ncbiUrl, geoAccession);
	        }
	       if (StringUtils.isNoneBlank(geoSampleAccession)) {
	            String ncbiUrl = "http://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc={0}";
	            return MessageFormat.format(ncbiUrl, geoSampleAccession);
	        }else {
	            String genomeUrl = "http://genome.ucsc.edu/cgi-bin/hgFileSearch?db=hg19&hgt_mdbVar1=dccAccession&hgt_mdbVal1={0}&hgfs_Search=search";
	            return MessageFormat.format(genomeUrl, this.dccAccession);
	        }
    	}
    }

    public String getDownLoadUrl() {
        return this.url;
    }

    public String getPubMedUrl() {
        if (this.study.equalsIgnoreCase(SourceType.Roadmap.getDesc())) {
            return "http://www.ncbi.nlm.nih.gov/pubmed/20944595";
        } else if (this.study.equalsIgnoreCase(SourceType.SRA.getDesc())) {
        	if(this.pubMedUrl != null)
        	{
        		return pubMedUrl;
        	}
        	if(this.sampleId > 400005)
        	{
        		return "#";
        	}
        	return "http://www.ncbi.nlm.nih.gov/pubmed/20478527";
        } else if (this.study.equalsIgnoreCase(SourceType.TCGA.getDesc())) {
        	String pubmeId = tcgaPubMeMap.get(this.cell);
        	if (StringUtils.isNotBlank(pubmeId)) {
        		return "http://www.ncbi.nlm.nih.gov/pubmed/" + pubmeId;
        	} else {
        		return "http://www.ncbi.nlm.nih.gov/pubmed/24071849";
        	}
        } else if(this.study.equalsIgnoreCase(SourceType.GEO.getDesc()) || this.study.equalsIgnoreCase(SourceType.ArrayExpress.getDesc()) 
        		|| this.study.equalsIgnoreCase(SourceType.SUMMARY.getDesc()) || this.study.equalsIgnoreCase(SourceType.JASPAR.getDesc())) {
        	if (this.pubMedUrl != null) {
        		return pubMedUrl;
        	} else {
        		return "#";
        	}
        } else if (this.study.equalsIgnoreCase(SourceType.ICGC.getDesc())) {
        	return "http://www.ncbi.nlm.nih.gov/pubmed/21930502";
        } else if(this.study.equalsIgnoreCase(SourceType.TCGAFirebrowse.getDesc())) {
        	return "http://www.firebrowse.org/";
        }else {
            return "http://www.ncbi.nlm.nih.gov/pubmed/22955616";
        } 
    }
    
    

    public String[] getMetaData() {
        if (null == metaData && StringUtils.isNotBlank(this.description)) {
            this.metaData = new String[2];
            this.description = this.description.replaceAll("; ", ";").replaceAll("\"", "");
            String vendorID = AntibodyCache.getInstance().getVendorIdByAntibody(this.factor);
            if (StringUtils.isNotBlank(vendorID)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("find vendorId : " + vendorID + " for sampleId:" + this.sampleId);
                }
               // this.description = this.description + ";vendorID=" + vendorID;
                if (logger.isDebugEnabled()) {
                    logger.debug("this description is : " + description);
                }
            }
            String[] arr = StringUtils.split(this.description, ";");
            //过滤显示
            List<String> list = new ArrayList<String>(5);
            for (String str : arr) {
                if (StringUtils.trimToEmpty(str).toLowerCase().startsWith("digital_image_of_stained_section")) {
                    continue;
                }
                if(this.study.equals(SourceType.JASPAR.getDesc())) {
			if(str.contains("#@#")) {
                		//JASPAR link=http://jaspar.genereg.net/cgi-bin/jaspar_db.pl?ID#@#MA0484.1&rm#@#present&collection#@#CORE
                		
                		String url = "";
				if(str.split("=").length>1)
                		url = str.split("=")[1];
				url = url.replaceAll("JASPAR link#@#","");
                		str = "JASPAR link=<a style#@#'cursor: pointer' href#@#'" + url + "' target#@#'blank'>" + url + "</a>";
                		list.add(str);
				break;
				}
                }
                list.add(str);
            }
            arr = list.toArray(new String[] {});
            int pos = arr.length % 2 == 0 ? arr.length / 2 : (arr.length / 2) + 1;
            String[] _arr1 = new String[pos];
            System.arraycopy(arr, 0, _arr1, 0, pos);
            
	 if(!this.study.equals(SourceType.JASPAR.getDesc())) {
		this.metaData[0] = StringUtils.join(_arr1, ";").replaceAll("=", ": ");
         }else this.metaData[0] = StringUtils.join(_arr1, ";");   
            this.metaData[0] = this.metaData[0].replace("#@#", "=");

            String[] _arr2 = new String[arr.length - pos];
            System.arraycopy(arr, pos, _arr2, 0, arr.length - pos);
            this.metaData[1] = StringUtils.join(_arr2, ";").replaceAll("=", ": ");
        }
        return this.metaData;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getStudy() {
        return study;
    }

    public void setStudy(String study) {
        this.study = study;
    }

    public String getPercentileFormat() {
        return percentileFormat;
    }

    public void setPercentileFormat(String percentileFormat) {
        this.percentileFormat = percentileFormat;
    }

    public String getLab() {
    	if(lab != null && !"".equals(lab)){
    		lab = lab.replaceFirst(lab.substring(0, 1), lab.substring(0, 1).toString().toUpperCase());
    	}
        return lab;
    }

    public void setLab(String lab) {
        this.lab = lab;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Double getMixturePerc() {
        return mixturePerc;
    }

    public void setMixturePerc(Double mixturePerc) {
        this.mixturePerc = mixturePerc;
    }

    public Double getTss5kPerc() {
        return tss5kPerc;
    }

    public void setTss5kPerc(Double tss5kPerc) {
        this.tss5kPerc = tss5kPerc;
    }

	public Double getPathwayOfRank() {
		return pathwayOfRank;
	}

	public void setPathwayOfRank(Double pathwayOfRank) {
		this.pathwayOfRank = pathwayOfRank;
	}

	public String getUcscUrl() {
		return ucscUrl;
	}

	public void setUcscUrl(String ucscUrl) {
		this.ucscUrl = ucscUrl;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getFactor_desc() {
		return factor_desc;
	}

	public void setFactor_desc(String factor_desc) {
		this.factor_desc = factor_desc;
	}

	public String getCell_desc() {
		return cell_desc;
	}

	public void setCell_desc(String cell_desc) {
		this.cell_desc = cell_desc;
	}

	public Map<String,Gene> getTop5Genes() {
		return top5Genes;
	}

	public void setTop5Genes(Map<String,Gene> top5Genes) {
		this.top5Genes = top5Genes;
	}


	public String getSettype() {
		return settype;
	}

	public void setSettype(String settype) {
		this.settype = settype;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}
	
}
