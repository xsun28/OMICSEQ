package com.omicseq.domain;

public class CellTypeDesc extends BaseDomain {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7380239262638665175L;
	
	private String cell;
	
	private String cell_desc;

	public String getCell() {
		return cell;
	}

	public void setCell(String cell) {
		this.cell = cell;
	}

	public String getCell_desc() {
		return cell_desc;
	}

	public void setCell_desc(String cell_desc) {
		this.cell_desc = cell_desc;
	}

}
