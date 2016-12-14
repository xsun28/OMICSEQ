package com.omicseq.robot.process;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.common.CancerType;
import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.utils.DateUtils;

public class TCGAFirebrowseProcess extends BaseProcess {
	protected Logger logger = LoggerFactory.getLogger(TCGAFirebrowseProcess.class);
	public TCGAFirebrowseProcess(String root) {
		super(root);
	}

	public synchronized String getFile(){
		String fileName = fileList.get(0);
		fileList.remove(fileName);
		return fileName;
	}
	
	
	@Override
	public void parser() {
		try {
			if(fileList.size() == 0) {
				flag = false;
				return;
			}
			String fileName = getFile();
			BufferedReader br = new BufferedReader(new FileReader(root + fileName));
			String line = null;
			Integer line_num = 0;
			List<Integer> cols = new ArrayList<Integer>();
			List<String> resultType = new ArrayList<String>();
 			//读数列的barcode
			List<String> VariableName = new ArrayList<String>();
			//所有barcode
			List<String> VariableName_all = new ArrayList<String>();
			List<Sample> samples = new ArrayList<Sample>();
		
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
						VariableName.add(tmp[i]);
					}
					samples = getSample(fileName , VariableName , fileName.split("-")[0]);
					break;
				}
				
			}
			for(int j=0;j<cols.size();j++){
				logger.debug("Current processing file:{}",fileName);
				line_num = 0;
				List<GeneRank> geneRankList = new ArrayList<GeneRank>();
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
					String symbol = tmp[0].split("\\|")[0];
					Integer geneId = geneIdMap.get(symbol);
					if(geneId == null) continue;
					Double tssTesCount = Double.parseDouble(tmp[cols.get(j)]);
					int start = VariableName_all.indexOf(VariableName_all.get(cols.get(j)));
					int end = VariableName_all.lastIndexOf(VariableName_all.get(cols.get(j)));
					Double tss5kCount = null;
					Double tss5kPerc = null;
					Double tssT5Count = null;
					for(int k=start;k<=end;k++){
						if(k!=cols.get(j)){
							if(tss5kCount == null) {
								tss5kCount = Double.parseDouble(tmp[k]);
								continue;
							}
							if(tss5kPerc == null) {
								tss5kPerc = Double.parseDouble(tmp[k]);
								continue;
							}
							if(tssT5Count == null) {
								tssT5Count = Double.parseDouble(tmp[k]);
								continue;
							}
						}
					}
					GeneRank geneRank = new GeneRank();
					geneRank.setCreatedTimestamp(System.currentTimeMillis());
					geneRank.setEtype(ExperimentType.SUMMARY_TRACK.getValue());
					geneRank.setSource(SourceType.TCGAFirebrowse.getValue());
					geneRank.setGeneId(geneId);
					geneRank.setSampleId(samples.get(j).getSampleId());
					geneRank.setTssTesCount(tssTesCount);;
					geneRank.setTss5kCount(tss5kCount);
					geneRank.setTssT5Count(tssT5Count);
					geneRank.setTss5kPerc(tss5kPerc);
					geneRankList.add(geneRank);
				}
				Collections.sort(geneRankList, new Comparator<GeneRank>() {
					@Override
					public int compare(GeneRank o1, GeneRank o2) {
						return o1.getTssTesCount().compareTo(o2.getTssTesCount());
					}
				});
				
				for(GeneRank geneRank : geneRankList){
					geneRank.setMixturePerc(Double.parseDouble(df.format((double)(geneRankList.indexOf(geneRank)+1)/geneRankList.size())));
					geneRank.setTotalCount(geneRankList.size());
				}
				geneRankDAO.removeBySampleId(samples.get(j).getSampleId());
				geneRankDAO.create(geneRankList);
			}
			
		} catch (Exception e) {
			
		}
	}

	public List<Sample> getSample(String fileName , List<String> VariableName , String cell){
		List<Sample> sampleList = new ArrayList<Sample>();
//		int sampleId = 1000;
		for(String name : VariableName){
			int sampleId = sampleDAO.getSequenceId(SourceType.TCGAFirebrowse);
			Sample sample = new Sample();
			sample.setSampleId(sampleId);
			sample.setSampleCode(name);
			sample.setLab("BROAD GDAC");
			sample.setCell("TCGA-" + cell.toLowerCase());
			sample.setDeleted(0);
			String tORn = "";
			if(Arrays.asList(name.split(".")).contains("T")){
				tORn = "turmor";
			}
			else if(Arrays.asList(name.split(".")).contains("M")){
				tORn = "normal";
			}
			sample.setDetail(CancerType.valueOf(cell.toUpperCase()).getName() +" " + tORn );
			sample.setSource(SourceType.TCGAFirebrowse.getValue());
			sample.setEtype(ExperimentType.SUMMARY_TRACK.getValue());
			sample.setCreateTiemStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
			String [] names = fileName.split("_");
			sample.setSettype(names[2] +" " + name + ":" + names[4]);
			sampleList.add(sample);
		}
		sampleNewDAO.create(sampleList);
		return sampleList;
	}
	
	public static void main(String[] args) {
		String a = "aF|e";
		System.out.println(a.split("\\|")[0]);
	}
}
