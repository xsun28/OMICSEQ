package com.omicseq.store.criteria;

import java.util.List;

import com.omicseq.common.SortType;

public class PathWayCriteria {
	private Integer pathId;
    private List<Integer> sourceList;
    private List<Integer> etypeList;
    private SortType sortType;
    private Integer sampleId;
	public Integer getPathId() {
		return pathId;
	}
	public void setPathId(Integer pathId) {
		this.pathId = pathId;
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
}
