package com.omicseq.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Min.Wang
 * 
 */
public class Sample extends BaseDomain {

    private static final long serialVersionUID = 4652801485206617446L;
    private Integer sampleId;
    private String url;
    private String description;
    private String segment;

    private String cell;
    private String factor;
    private Integer source;
    private Integer etype;
    private String lab;
    private String timeStamp;
    
    private String inputSampleIds;
    private String settype;
    private String sampleCode;
    private Integer deleted;
    
    private String detail; //显示 tissue/status/factor
    private String sourceUrl; //存放爬取页面地址
    
    private String fromType; //human or mouse
    
    private Integer variationReadTotal;
    public Integer getVariationReadTotal() {
		return variationReadTotal;
	}

	public void setVariationReadTotal(Integer variationReadTotal) {
		this.variationReadTotal = variationReadTotal;
	}

	public Integer getDeleted() {
		return deleted;
	}

	public void setDeleted(Integer deleted) {
		this.deleted = deleted;
	}

	private Integer readCount;
    
    private String createTiemStamp;
    
    private String Instrument;
    
    private String antibody;
    
    private String pubmedUrl;

    public Integer getSampleId() {
        return sampleId;
    }

    public void setSampleId(Integer sampleId) {
        this.sampleId = sampleId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getEtype() {
        return etype;
    }

    public void setEtype(Integer etype) {
        this.etype = etype;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
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

    public String getLab() {
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

    public String getInputSampleIds() {
        return inputSampleIds;
    }

    public void setInputSampleIds(String inputSampleIds) {
        this.inputSampleIds = inputSampleIds;
    }

    public Integer getReadCount() {
        return readCount;
    }

    public void setReadCount(Integer readCount) {
        this.readCount = readCount;
    }

    public String getSettype() {
        return settype;
    }

    public void setSettype(String settype) {
        this.settype = settype;
    }

    public Map<String, String> descMap() {
        if (StringUtils.isBlank(description)) {
            return new HashMap<String, String>();
        }
        Map<String, String> map = new HashMap<String, String>();
        String[] values = description.split(";");
        for (String value : values) {
            value = StringUtils.trimToEmpty(value);
            if (StringUtils.isNoneBlank(value)) {
                String[] valuePair = value.split("=");
                if (ArrayUtils.isNotEmpty(valuePair) && valuePair.length >= 2) {
                    map.put(valuePair[0].toLowerCase(), valuePair[1]);
                }
            }
        }
        return map;
    }

    public void descMap(Map<String, String> map) {
        if (MapUtils.isEmpty(map)) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        // 可添加排序
        Set<String> keys = map.keySet();
        for (String key : keys) {
            if (sb.length() != 0) {
                sb.append(";");
            }
            sb.append(key).append("=").append(map.get(key));
        }
        this.description = sb.toString();
    }

	public String getSampleCode() {
		return sampleCode;
	}

	public void setSampleCode(String sampleCode) {
		this.sampleCode = sampleCode;
	}

	public String getCreateTiemStamp() {
		return createTiemStamp;
	}

	public void setCreateTiemStamp(String createTiemStamp) {
		this.createTiemStamp = createTiemStamp;
	}

	public String getInstrument() {
		return Instrument;
	}

	public void setInstrument(String instrument) {
		Instrument = instrument;
	}

	public String getAntibody() {
		return antibody;
	}

	public void setAntibody(String antibody) {
		this.antibody = antibody;
	}

	public String getPubmedUrl() {
		return pubmedUrl;
	}

	public void setPubmedUrl(String pubmedUrl) {
		this.pubmedUrl = pubmedUrl;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public String getFromType() {
		return fromType;
	}

	public void setFromType(String fromType) {
		this.fromType = fromType;
	}
    
}
