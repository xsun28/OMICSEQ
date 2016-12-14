package com.omicseq.web.dto;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SettingDTO {
    private LinkedHashMap<String, Boolean> experimentsMap = new LinkedHashMap<String, Boolean>();
    private Map<String, Boolean> sourcesMap = new HashMap<String, Boolean>();
    private LinkedHashMap<String, Boolean> experimentsMap_mouse = new LinkedHashMap<String, Boolean>();
    private Map<String, Boolean> sourcesMap_mouse = new HashMap<String, Boolean>();
    private String cellType;
    private String geneDefinition;
    private Integer regionSolution;
    
    public Map<String, Boolean> getExperimentsMap() {
        return experimentsMap;
    }
    public void setExperimentsMap(LinkedHashMap<String, Boolean> experimentsMap) {
        this.experimentsMap = experimentsMap;
    }
    public Map<String, Boolean> getSourcesMap() {
        return sourcesMap;
    }
    public void setSourcesMap(Map<String, Boolean> sourcesMap) {
        this.sourcesMap = sourcesMap;
    }
    public LinkedHashMap<String, Boolean> getExperimentsMap_mouse() {
		return experimentsMap_mouse;
	}
	public void setExperimentsMap_mouse(
			LinkedHashMap<String, Boolean> experimentsMap_mouse) {
		this.experimentsMap_mouse = experimentsMap_mouse;
	}
	public Map<String, Boolean> getSourcesMap_mouse() {
		return sourcesMap_mouse;
	}
	public void setSourcesMap_mouse(Map<String, Boolean> sourcesMap_mouse) {
		this.sourcesMap_mouse = sourcesMap_mouse;
	}
	public String getCellType() {
        return cellType;
    }
    public void setCellType(String cellType) {
        this.cellType = cellType;
    }
    public String getGeneDefinition() {
        return geneDefinition;
    }
    public void setGeneDefinition(String geneDefinition) {
        this.geneDefinition = geneDefinition;
    }
    public Integer getRegionSolution() {
        return regionSolution;
    }
    public void setRegionSolution(Integer regionSolution) {
        this.regionSolution = regionSolution;
    }
    
}
