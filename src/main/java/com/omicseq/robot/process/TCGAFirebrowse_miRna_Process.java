package com.omicseq.robot.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.MiRNA;
import com.omicseq.domain.MiRNARank;
import com.omicseq.domain.MiRNASample;
import com.omicseq.store.dao.ImiRNADAO;
import com.omicseq.store.dao.ImiRNARankDAO;
import com.omicseq.store.dao.ImiRNASampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateUtils;

public class TCGAFirebrowse_miRna_Process {
	
//	private String root = "/files/download/tcgafirebrowse/";
	private String root = "D:/ArrayExpress/firebrowse-data/";
	
	private ImiRNADAO miRnaDAO = DAOFactory.getDAOByTableType(ImiRNADAO.class, "new");
	private ImiRNASampleDAO miSampleDAO = DAOFactory.getDAO(ImiRNASampleDAO.class);
	private ImiRNARankDAO miRankDAO = DAOFactory.getDAO(ImiRNARankDAO.class);
	private DecimalFormat df = new DecimalFormat("#.00000");
	private String [] files = {"ACC","BLCA","BRCA","CESC","COAD","COADREAD","DLBC","ESCA","GBM","HNSC","KICH","KIRC","KIRP","LGG","LIHC","LUAD","LUSC","MESO","OV","PAAD","PCPG","PRAD","READ","SARC","STAD","THCA","UCEC","UCS"}; 
	private Map<String ,Integer> miRnaIdMap = new HashMap<String , Integer>();
	
	public TCGAFirebrowse_miRna_Process(){
		
		List<MiRNA> miRnaList = miRnaDAO.find(new SmartDBObject());
		
		for(MiRNA mi : miRnaList){
			miRnaIdMap.put(mi.getMiRNAName().toLowerCase(), mi.getMiRNAId());
		}
		
	}
	
	public void parse(){
		for(String fileName : files){
			System.out.println("解析============"+fileName);
			fileName = fileName + "-TP_Correlate_Clinical_vs_miRseq_supp_table1.txt";
			try {
				BufferedReader br = new BufferedReader(new FileReader(root + fileName));
				String line = null;
				Integer line_num = 0;
				List<Integer> cols = new ArrayList<Integer>();
				List<String> resultType = new ArrayList<String>();
				//读数列的barcode
				List<String> variableName = new ArrayList<String>();
				//所有barcode
				List<String> VariableName_all = new ArrayList<String>();
				List<MiRNASample> sampleList = new ArrayList<MiRNASample>();
				
				while((line  = br.readLine()) != null){
					line_num++;
					String [] tmp = line.split("\t");
					if(line_num == 1){
						for(int i=0; i<tmp.length; i++ ){
							resultType.add(tmp[i]);
							List<String> a = Arrays.asList(tmp[i].split("_"));
							if(tmp[i].endsWith("P") || a.contains("P")){
								cols.add(i);
							}
						}
					}
					if(CollectionUtils.isEmpty(cols)) break;
					if(line_num == 2){
						VariableName_all = Arrays.asList(tmp);
						for(int i : cols){
							variableName.add(tmp[i]);
						}
						sampleList = getSample(fileName , variableName , fileName.split("-")[0]);
						break;
					}
				}
				for(int j=0;j<cols.size();j++){
					line_num = 0;
					List<MiRNARank> geneRankList = new ArrayList<MiRNARank>();
					BufferedReader br1 = new BufferedReader(new FileReader(root + fileName));
					while((line=br1.readLine()) != null){
						line_num++;
						if(line_num < 3) continue;
						String [] tmp = line.split("\t");
						for(int l=0; l<tmp.length; l++){
							if(tmp[l].equals("NA")){
								tmp[l] = "0";
							}
						}
						String name = tmp[0].toLowerCase();
						Integer miRnaId =  miRnaIdMap.get(name);
						if(miRnaId == null) continue;
						MiRNARank geneRank = new MiRNARank();
						geneRank.setCreateTimeStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
						geneRank.setEtype(ExperimentType.SUMMARY_TRACK.getValue());
						geneRank.setSource(SourceType.TCGAFirebrowse.getValue());
						geneRank.setMiRNAId(miRnaId);
						geneRank.setMiRNASampleId(sampleList.get(j).getMiRNASampleId());
						geneRank.setRead(Double.parseDouble(tmp[cols.get(j)]));
						geneRankList.add(geneRank);
					}
					Collections.sort(geneRankList, new Comparator<MiRNARank>() {
						@Override
						public int compare(MiRNARank o1, MiRNARank o2) {
							return o1.getRead().compareTo(o2.getRead());
						}
					});
					for(MiRNARank geneRank : geneRankList){
//						geneRank.setMixtureperc(Double.parseDouble(df.format(((double)geneRankList.indexOf(geneRank)+1)/geneRankList.size())));
//						geneRank.setTotalCount(geneRankList.size());
						geneRank.setMixtureperc(Double.parseDouble(df.format(((double)geneRankList.indexOf(geneRank)+1)/1046)));
						geneRank.setTotalCount(1046);
					}
					miRankDAO.create(geneRankList);
				}
			} catch (Exception e) {
			}
			
		}
	}

	private List<MiRNASample> getSample(String fileName, List<String> variableName,
			String cell) {
		List<MiRNASample> sampleList = new ArrayList<MiRNASample>();
//		int sampleId = 1000;
		for(String name : variableName){
//			sampleId++;
			int sampleId = miSampleDAO.getSequenceId(SourceType.TCGAFirebrowse);
			MiRNASample sample = new MiRNASample();
			sample.setMiRNASampleId(sampleId);
			sample.setBarCode(name);
			sample.setLab("BROAD GDAC");
			sample.setCell("TCGA-" + cell.toLowerCase());
			sample.setDeleted(0);
			String tORn = "";
//			if(Arrays.asList(name.split(".")).contains("T")){
//				tORn = "turmor";
//			}
//			else if(Arrays.asList(name.split(".")).contains("M")){
//				tORn = "normal";
//			}
			sample.setSource(SourceType.TCGAFirebrowse.getValue());
			sample.setEtype(ExperimentType.SUMMARY_TRACK.getValue());
			sample.setCreateTimeStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
//			String [] names = fileName.split("_");
//			sample.setSetType(names[2] +" " + name + ":" + names[4]);
			sampleList.add(sample);
		}
		miSampleDAO.create(sampleList);
		return sampleList;
	}
	public static void main(String[] args) {
		new TCGAFirebrowse_miRna_Process().parse();
	}
}
