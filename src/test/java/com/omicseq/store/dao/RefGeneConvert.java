package com.omicseq.store.dao;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Min.Wang
 *
 */
public class RefGeneConvert {

	public static void main(String[] args) {
		try {
			BufferedReader bufferReader = new BufferedReader(new FileReader("E:\\projects\\new_omicseq\\gene2refseq\\gene2refseq.txt"));
			String line = "";
			Integer count = 0;
			Long start = System.currentTimeMillis();
			Set<String> geneIdSet = new HashSet<String>();
			//"project=wgEncode; grant=Myers; lab=HudsonAlpha; composite=wgEncodeHaibTfbs; dataType=ChipSeq; view=Alignments; cell=U87; treatment=None; antibody=RevXlinkChromatin; protocol=PCR2x; replicate=1; submittedDataVersion=V2 - remapped U87 to correct reference
			String value = "objStatus=revoked - Original experiment was mapped to wrong sex; project=wgEncode; grant=Myers; lab=HudsonAlpha; composite=wgEncodeHaibTfbs; dataType=ChipSeq; view=Alignments; cell=U87; treatment=None; antibody=RevXlinkChromatin; protocol=PCR2x; replicate=1; dataVersion=ENCODE Jan 2011 Freeze; dccAccession=wgEncodeEH001559; dateSubmitted=2010-08-06; dateUnrestricted=2011-05-06; subId=1848; geoSampleAccession=GSM803375; setType=input; controlId=SL103; labExpId=SL103; type=bam; md5sum=1f298401c2b86540cdbd7baa4146c8e5; size=642M";
			while(StringUtils.isNoneBlank(line = bufferReader.readLine())) {
				if (count != 0) {
					String geneId = line.split("\t")[1];
					geneIdSet.add(geneId);
				}
				count = count + 1;
				if ((count % 1000000) == 0) {
					System.out.println(" using time : " + String.valueOf(System.currentTimeMillis() - start) + "; count is : " + count);
				}
			}
			System.out.println("size : " + geneIdSet.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
