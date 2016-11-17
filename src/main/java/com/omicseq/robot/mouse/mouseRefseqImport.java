package com.omicseq.robot.mouse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;

/**
 * @author Min.Wang
 *
 */
public class mouseRefseqImport {
	private static ITxrRefDAO txrRefDAO = DAOFactory.getDAOByTableType(ITxrRefDAO.class, "mouse");
	 
	public static void main(String[] args) {
		try {
			BufferedReader bufferReader = new BufferedReader(new FileReader("E:\\mouse\\refFlat.txt"));
			String line = "";
			List<TxrRef> txrRefList = new ArrayList<TxrRef>();
//			Set<String> geneIdSet = new HashSet<String>();
			//"project=wgEncode; grant=Myers; lab=HudsonAlpha; composite=wgEncodeHaibTfbs; dataType=ChipSeq; view=Alignments; cell=U87; treatment=None; antibody=RevXlinkChromatin; protocol=PCR2x; replicate=1; submittedDataVersion=V2 - remapped U87 to correct reference
//			String value = "objStatus=revoked - Original experiment was mapped to wrong sex; project=wgEncode; grant=Myers; lab=HudsonAlpha; composite=wgEncodeHaibTfbs; dataType=ChipSeq; view=Alignments; cell=U87; treatment=None; antibody=RevXlinkChromatin; protocol=PCR2x; replicate=1; dataVersion=ENCODE Jan 2011 Freeze; dccAccession=wgEncodeEH001559; dateSubmitted=2010-08-06; dateUnrestricted=2011-05-06; subId=1848; geoSampleAccession=GSM803375; setType=input; controlId=SL103; labExpId=SL103; type=bam; md5sum=1f298401c2b86540cdbd7baa4146c8e5; size=642M";
			while(StringUtils.isNoneBlank(line = bufferReader.readLine())) {
				String[] values = line.split("\t");
				String geneSymbol = values[0];
				String refseq =  values[1];
				
				TxrRef mouseTxrRef = new TxrRef();
				mouseTxrRef.setDescription("");
				mouseTxrRef.setGeneSymbol(geneSymbol);
				mouseTxrRef.setmRNA("");
				mouseTxrRef.setProtAcc("");
				mouseTxrRef.setRefseq(refseq);
				mouseTxrRef.setSpDisplayID("");
				mouseTxrRef.setSpID("");
				mouseTxrRef.setUcscName("");
				txrRefList.add(mouseTxrRef);
			}
			
			txrRefDAO.create(txrRefList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
