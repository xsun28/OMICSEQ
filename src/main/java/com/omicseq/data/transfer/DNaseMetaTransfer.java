package com.omicseq.data.transfer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.omicseq.common.ExperimentType;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * @author Min.Wang
 *
 */
public class DNaseMetaTransfer {

	static Map<String, String> baseUrlMap = new HashMap<String, String>();
	static {
		baseUrlMap.put("wgEncodeOpenChromDnase", "http://hgdownload.cse.ucsc.edu/goldenPath/hg19/encodeDCC/wgEncodeOpenChromDnase/");
		baseUrlMap.put("wgEncodeOpenChromSynth", "http://hgdownload.cse.ucsc.edu/goldenPath/hg19/encodeDCC/wgEncodeOpenChromSynth/");
		baseUrlMap.put("wgEncodeUwDgf", "http://hgdownload.cse.ucsc.edu/goldenPath/hg19/encodeDCC/wgEncodeUwDgf/");
		baseUrlMap.put("wgEncodeUwDnase", "http://hgdownload.cse.ucsc.edu/goldenPath/hg19/encodeDCC/wgEncodeUwDnase/");
	}
	
	public static void main(String[] args) {
		try {
			CSVWriter cSVWriter = new CSVWriter(new FileWriter("E:\\projects\\new_omicseq\\encodeDNASE.csv"));
			Integer totalCount = 0;
			for (String key : baseUrlMap.keySet()) {
				BufferedReader geneBufferedReader = new BufferedReader(new FileReader("E:\\projects\\new_omicseq\\" + key + ".txt"));
				String line = "";
				Integer count = 0;
				while(StringUtils.isNoneBlank(line = geneBufferedReader.readLine())) {
					String[] valueArray = line.split("\t");
					String fileName = valueArray[0];
					if (!fileName.endsWith(".bam")) {
						continue;
					}
					count = count + 1;
					totalCount = totalCount + 1;
					String url = baseUrlMap.get(key) + fileName;
					cSVWriter.writeNext(new String[]{String.valueOf(totalCount), key +"_"+ count, url, valueArray[1], ExperimentType.DNASE_SEQ.name()});
				}
				
			}
			cSVWriter.flush();
			cSVWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
}
