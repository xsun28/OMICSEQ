package com.omicseq.robot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.omicseq.common.CancerType;
import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.domain.CellTypeDesc;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.ICellDescDao;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class SampleDetail {
	static ICellDescDao cellTypeDescDAO = DAOFactory.getDAO(ICellDescDao.class);
	public static Sample getDetailBySourceAndCell(Sample sample){
		Map<String, String> cdMap = new HashMap<String, String>();
		List<CellTypeDesc> fds = cellTypeDescDAO.find(new SmartDBObject());
		for(CellTypeDesc fd : fds)
		{
			cdMap.put(fd.getCell(), fd.getCell_desc());
		}
		if(sample.getCell() == null || sample.getCell().isEmpty() || (sample.getDetail() != null && sample.getDetail().contains("(C)")))
    	{
    		return sample;
    	}
		String factor = sample.getFactor();
		
		if (factor == null || "".equals(factor)) {
			if(sample.getSource() == SourceType.TCGA.value() || sample.getSource() == SourceType.ICGC.value())
			{
				String desc = sample.getDescription();
				if(desc != null && !"".equals(desc))
				{
					Map<String, String> mapTemp = sample.descMap();
					String tumor_tissue_site = mapTemp.get("tumor_tissue_site");
					if(tumor_tissue_site != null)
					{
						factor = tumor_tissue_site;
						String cell = sample.getCell();
						if (cell.contains("tumor")) {
							factor += " tumor";
						}
						if (cell.contains("normal")) {
							factor += " normal";
						}
						if (cell.contains("control")) {
							factor += " control";
						}
					} else {
						String cell = sample.getCell();
						if(!cell.isEmpty())
						{
							String[] cells = cell.split("-");
							String cancerType = "";
							if(cells != null && cells.length >1)
							{
								if(sample.getSource() == SourceType.TCGA.value())
								{
									cancerType = cells[1].toUpperCase();
								} else if(sample.getSource() == SourceType.ICGC.value())
								{
									cancerType = cells[0].toUpperCase();
								}
								try {
									factor = CancerType.valueOf(cancerType).getName();
									if (cell.contains("tumor")) {
										factor += " tumor";
									}
									else if (cell.contains("normal")) {
										if(sample.getEtype() == ExperimentType.MUTATION.getValue())
										{
											factor += " tumor";
										}else {
											factor += " normal";
										}
									}
									else if (cell.contains("control")) {
										factor += " control";
									} else {
										factor += " tumor";
									}
									
									factor = factor.replaceFirst(factor.substring(0, 1), factor.substring(0, 1).toUpperCase());
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
				} else {
					String cell = sample.getCell();
					if(cell != null && !cell.isEmpty())
					{
						String[] cells = cell.split("-");
						String cancerType = "";
						if(cells != null && cells.length >1)
						{
							if(sample.getSource() == SourceType.TCGA.value())
							{
								cancerType = cells[1].toUpperCase();
							} else if(sample.getSource() == SourceType.ICGC.value())
							{
								cancerType = cells[0].toUpperCase();
							}
							try {
								factor = CancerType.valueOf(cancerType).getName();
								if (cell.contains("tumor")) {
									factor += " tumor";
								}
								else if (cell.contains("normal")) {
									if(sample.getEtype() == ExperimentType.MUTATION.getValue())
									{
										factor += " tumor";
									}else {
										factor += " normal";
									}
								}
								else if (cell.contains("control")) {
									factor += " control";
								} else {
									factor += " tumor";
								}
								
								factor = factor.replaceFirst(factor.substring(0, 1), factor.substring(0, 1).toUpperCase());
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		
		if(sample.getSource() == SourceType.ENCODE.getValue() || sample.getSource() == SourceType.SRA.getValue() || sample.getSource() == SourceType.CCLE.getValue()|| sample.getSource() == SourceType.GEO.getValue()
				|| sample.getSource() == SourceType.ArrayExpress.getValue())
		{
			String cell = sample.getCell();
			String cellDesc = cdMap.get(cell);
			if(cellDesc != null)
			{
				String s = cellDesc.replace("human ", "").replace("Human ", "").replace("normal ", "").replace("Normal ", "").replace("child ", "").replace("Adult ", "").replace("Adult ", "Neonatal ").trim();
				factor = s.substring(0, s.indexOf(" ") == -1?s.length()-1:s.indexOf(" ")).replace(",", "");
				if(cellDesc.contains("lung"))
				{
					factor = "Lung";
				}
				if(cellDesc.contains("prostate"))
				{
					factor = "Prostate";
				}
				if(cellDesc.contains("breast"))
				{
					factor = "Breast";
				}
				if(cellDesc.contains("central nervous"))
				{
					factor = "Brain";
				}
			} else {
				cellDesc = cell;
			}
			
			if(cell.toLowerCase().contains("ependymomas"))
			{
				factor = "Brain";
			}
			else if(cell.toLowerCase().contains("embryonic"))
			{
				factor = "embryonic";
			}
			
			if(cell.toLowerCase().contains("T cells"))
			{
				factor = "T Lymphocyte";
			}
			if(cell.toLowerCase().equals("t-47d"))
			{
				factor = "Epithelial";
			}
			if(cell.contains("MCF"))
			{
				factor = "breast";
			}
			
			if(cell.contains("H1-embryonic"))
			{
				factor = "H1 cell";
			}
			
			if(cell.contains("T47D") || cell.contains("T-47D"))
			{
				factor = "breast";
			}
			
			if(cell.toLowerCase().contains("hesC"))
			{
				factor = "hESC";
			}
			
			if(cell.toLowerCase().contains("huevc"))
			{
				factor = "umbilical vein";
			}
			
			if(cell.equals("Th17"))
			{
				factor = "Th17";
			}
			
			if(cell.contains("HCC-"))
			{
				factor = "Lung";
			}
			
			if(cell.toLowerCase().contains("a549"))
			{
				factor = "Epithelial";
			}
			
			if(cell.contains("GM"))
			{
				factor = "Blood";
			}
			if(cell.contains("MDA"))
			{
				factor = "Breast";
			}
			if(cell.toLowerCase().contains("beas-2b"))
			{
				factor = "Bronchus";
			} 
			if(cell.toLowerCase().contains("a375"))
			{
				factor = "skin";
			}
			if(cell.toLowerCase().contains("hela"))
			{
				factor = "Cervix";
			} 
			if(cell.toLowerCase().contains("mcf7") || cell.toLowerCase().contains("mcf-7"))
			{
				factor = "Breast";
			} 
			if(cell.toLowerCase().contains("lncap"))
			{
				factor = "Prostate";
			} 
			if(cell.toLowerCase().contains("hct116"))
			{
				factor = "Colon";
			}
			if(cell.toLowerCase().contains("b-cells") || cell.toLowerCase().contains("b cells"))
			{
				factor = "Blood";
			}
			if(cell.contains("H1 cell"))
			{
				factor = "H1 cell";
			}
			if(cell.toLowerCase().contains("breast"))
			{
				factor = "Breast";
			}
			if(cell.toLowerCase().contains("skin"))
			{
				factor = "Skin";
			}
			if(cell.toLowerCase().contains("brain") || cell.toLowerCase().contains("memory") || cell.toLowerCase().contains("brain"))
			{
				factor = "Brain";
			}
			if(cell.toLowerCase().contains("muscle"))
			{
				factor = "Muscle";
			}
			if(cell.toLowerCase().contains("stomach"))
			{
				factor = "Stomach";
			}
			if(cell.toLowerCase().contains("thymus"))
			{
				factor = "Thymus";
			}
			if(cell.toLowerCase().contains("aaive"))
			{
				factor = "Naive";
			}
			if(cell.toLowerCase().contains("liver"))
			{
				factor = "Liver";
			}
			if(cell.toLowerCase().contains("kidney"))
			{
				factor = "Kidney";
			}
			if(cell.toLowerCase().contains("ovary"))
			{
				factor = "Ovary";
			}
			if(cell.toLowerCase().contains("lung"))
			{
				factor = "Lung";
			}
			if(cell.toLowerCase().contains("spinal"))
			{
				factor = "Spinal";
			}
			if(cell.contains("CD56"))
			{
				factor = "T cel";
			}
			
			if(cell.contains("NA") || cell.toLowerCase().contains("lymphoblastoid"))
			{
				factor = "Lymphoblastoid";
			}
			if(cell.contains("T cell") || cell.contains("T-cell") || cell.contains("T Cell"))
			{
				factor = "T cell";
			}
			if(cell.toLowerCase().contains("fetal primary tissue"))
			{
				factor = "Fetal Primary";
			}
			if(cell.toLowerCase().contains("lymphocyte"))
			{
				factor = "Lymphoblastoid ";
			} 
			if(cell.toLowerCase().contains("embryonic"))
			{
				factor = "Embryonic ";
			}
			if(cell.contains("IMR90"))
			{
				factor = "Lung";
			}
			if(cell.toLowerCase().contains("Lovo"))
			{
				factor = "LoVo";
			}
			if(cell.contains("H1"))
			{
				factor = "H1 Cells";
			}
			if(cell.contains("MG"))
			{
				factor = "Brain";
			}
			if(cell.contains("K562"))
			{
				factor = "Bone marrow";
			}
			if(cell.contains("CD4"))
			{
				factor = "T cell";
			}
			if(cell.toLowerCase().contains("lymph"))
			{
				factor = "Lymphoma";
			}
			if(cell.toLowerCase().contains("kidney"))
			{
				factor = "Kidney";
			}
			
			if(factor == null)
			{
				factor = cell;
			}
			
			if(sample.getSource() == SourceType.CCLE.getValue())
			{
				factor += "(C)";
			}
			
			if(factor != null) 
			{
				factor = factor.replace("_tissue", "").replace("tissue", "");
				if(cell.contains("GM") || cell.contains("IM") || cell.contains("HM") || cell.contains("HP") || cell.contains("H1") ||cell.contains("H9") || cell.equals("Th17") || cell.contains("HS") || cell.contains("NH") || cell.contains("Osteobl")
						 || cell.contains("Astrocy")|| cell.contains("Fib") || cell.contains("HUEVC") || cellDesc.contains("Normal") || cellDesc.contains("normal") || cell.equals("BEAS"))
				{
					factor += " Normal ";
				} else {
					factor += " Tumor ";
				}
				factor += sample.getFactor() == null?"":sample.getFactor();
				factor = factor.replaceFirst(factor.substring(0, 1), factor.substring(0, 1).toUpperCase());
			}
			
		}
		
		if(sample.getSource() == SourceType.Roadmap.getValue())
		{
			String cell = sample.getCell();
			String cell_desc = cdMap.get(cell);
			if(cell_desc == null)
			{
				cell_desc = cell;
			} else {
				String s = cell_desc.replace("human ", "").replace("Human ", "").replace("normal ", "").replace("Normal ", "").replace("child ", "").replace("Adult ", "").replace("Adult ", "Neonatal ");
				factor = s.substring(0, s.indexOf(" ") == -1?s.length()-1:s.indexOf(" ")).replace(",", "");
				if(cell_desc.contains("T cells"))
				{
					factor = "T Lymphocyte";
				}
				if(cell_desc.equals("T-47D"))
				{
					factor = "Epithelial";
				}
				if(cell_desc.contains("lung"))
				{
					factor = "Lung";
				}
			}
			String cell_temp = cell.replaceAll(".*derived ", "");
			factor = cell_temp.substring(0, cell_temp.indexOf(" ") != -1?cell_temp.indexOf(" "):cell_temp.length());
			if(cell.contains("CD"))
			{
				if(cell.contains("memory"))
				{
					factor = "memory T cell";
				}
				else if(cell.contains("naive"))
				{
					factor = "memory T cell";
				} else {
					factor = "T cell";
				}
			}
			if(cell.toLowerCase().contains("cultured") && cell.toLowerCase().contains("derived"))
			{
				factor = cell.substring(cell.indexOf("derived") + 8, cell.indexOf(" cultured")).replaceAll("CD[0-9]+", "").replace("+ ", "");
			}
			if(cell.toLowerCase().contains("primary") || cell.contains("derived") || cell_desc.contains("Normal") || cell_desc.contains("normal")) {
				factor = factor.concat(" Normal ").replace(",", "");
			} else {
				factor = factor.concat(" Tumor ").replace(",", "");
			}
			
			factor += sample.getFactor() != null?sample.getFactor():"";
		}
		
		if(sample.getEtype() == ExperimentType.SUMMARY_TRACK.getValue())
		{

			String cell = sample.getCell();
			if(!cell.isEmpty())
			{
				String[] cells = cell.split("-");
				if(cells != null && cells.length >1)
				{
					String cancerType = cells[1].toUpperCase();
					try {
						factor = CancerType.valueOf(cancerType).getName();
						if (cell.contains("tumor")) {
							factor += " tumor";
						}
						else if (cell.contains("normal")) {
							factor += " normal";
						}
						else if (cell.contains("control")) {
							factor += " control";
						} else {
							factor += " tumor";
						}
						factor = factor.replaceFirst(factor.substring(0, 1), factor.substring(0, 1).toUpperCase());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		if(factor !=null) sample.setDetail(factor.trim());
		return sample;		
	}
	
	public static void main(String[] args) {
		ISampleDAO sampleDao = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
		SmartDBObject query = new SmartDBObject();
		query.put("deleted", 0);
		query.put("cell", new SmartDBObject("$ne",null));
		query.put("detail", null);
		List<Sample> sampleList = sampleDao.find(query);
		for(Sample sample : sampleList){
			System.out.println(SampleDetail.getDetailBySourceAndCell(sample).getDetail());
		}
	}

}
