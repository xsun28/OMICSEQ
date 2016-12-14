package com.omicseq.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.List;

import com.omicseq.common.Constants;
import com.omicseq.common.ExperimentType;
import com.omicseq.common.SortType;
import com.omicseq.common.SourceType;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

/**
 *  将一个总文件拆分成每个sample对应的小文件，供页面下载
 * @author wangyanjia
 *
 */
public class FileToSampleFiles {
	
	private static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, Constants.STAT_SUFFIX);

	public static void main(String[] args) {
		SmartDBObject query = new SmartDBObject();
		query.put("source", SourceType.JASPAR.getValue());
		query.put("etype", ExperimentType.MOTIFS.getValue());
		query.addSort("sampleId", SortType.ASC);
		List<Sample> sampleList = sampleDAO.find(query);
		
		if(sampleList != null && sampleList.size() >0)
		{
			FileToSampleFiles fileToFiles = new FileToSampleFiles();
			File file = new File("E:\\data\\TF.motif.csv");
			fileToFiles.split(sampleList, file);
		}
		
	}

	private void split(List<Sample> sampleList, File file) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line = null;
			line = br.readLine();
			br.close();
			String [] title = line.split(",");
			String geneTitle = "refseq";
//			List<String> titles = new ArrayList<String>(title.length);
			for(int i=0; i<title.length-1;i++){
				Sample sample = sampleList.get(i);
				String sampleTitle = title[i+1];
				if(sample.getSampleCode().equals(sampleTitle))
				{
					createSampleFile(sample, geneTitle, sampleTitle, file, i);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createSampleFile(Sample sample, String geneTitle, String sampleTitle, File file, int column) {
		boolean flag = true;
		String line = null;
		Integer sampleId = sample.getSampleId();
		try {
			String fileName = sampleTitle.replace(":", "_")  + ".motif.csv";
			String filePath = "E:\\data\\motifs\\" + sampleId + File.separator + fileName;
			File newFile  = new File(filePath);
			if(!newFile.exists())
			{
				newFile.getParentFile().mkdirs(); 
				newFile.createNewFile();
			}
			FileWriter fw = new FileWriter(filePath, true);
			fw.write(geneTitle+","+sampleTitle+"\n");
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			while((line = br.readLine()) != null) {
				if(flag){
					flag = false;
					continue;
				}
				String [] reads = line.split(",");
				String refseq = reads[0];
				String geneRead = reads[column+1];
				
				fw.write(refseq + ","+geneRead+"\n");
			}
			
			br.close();
			fw.close();
			
			sample.setUrl("http://112.25.20.156/download/motifs/"+sampleId+"/"+ fileName);
			
			sampleDAO.update(sample);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
