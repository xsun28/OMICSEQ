package com.omicseq.data.transfer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import au.com.bytecode.opencsv.CSVWriter;
import com.omicseq.utils.ConvertUtil;

/**
 * @author Min.Wang
 *
 */
public class GeneTransfer {

	public static void main(String[] args) {
		//transferGene();
		transferXref();
	}
	
	private static void transferGene() {
		// need compute three file.
		// Gene
		try {
			CSVWriter cSVWriter = new CSVWriter(new FileWriter("E:\\projects\\new_omicseq\\gene_meta_hg19_new.csv"));
			cSVWriter.writeNext(new String[]{"seqname", "start", "end", "width", "strand", "tx_id", "tx_name"});
			
			CSVWriter tssTescSVWriter = new CSVWriter(new FileWriter("E:\\projects\\new_omicseq\\Gene.TSS.TES_new.csv"));
			tssTescSVWriter.writeNext(new String[]{"seqnames", "start", "end", "width", "strand"});
			
			CSVWriter tss5kcSVWriter = new CSVWriter(new FileWriter("E:\\projects\\new_omicseq\\Gene.TSS.5k_new.csv"));
			tss5kcSVWriter.writeNext(new String[]{"seqnames", "start", "end", "width", "strand"});
			CSVWriter tssTes5kcSVWriter = new CSVWriter(new FileWriter("E:\\projects\\new_omicseq\\Gene.TSS.TES.5k_new.csv"));
			tssTes5kcSVWriter.writeNext(new String[]{"seqnames", "start", "end", "width", "strand"});
			
			BufferedReader geneBufferedReader = new BufferedReader(new FileReader("E:\\projects\\new_omicseq\\gene"));
			String line = "";
			Integer txId = 0;
			while(StringUtils.isNoneBlank((line = geneBufferedReader.readLine()))) {
				if (txId != 0) {
					String[] values = line.split("\t");
					String seqname = values[2];
					Integer startInteger = ConvertUtil.toInteger(values[4], 0) + 1;
					if (startInteger == 1) {
						break;
					}
					String start = String.valueOf(startInteger);
					Integer endInteger = ConvertUtil.toInteger(values[5], 0);
					String end =  String.valueOf(endInteger);
					Integer widthInteger = endInteger - startInteger + 1;
					String width = String.valueOf(widthInteger);
					String strand = values[3];
					String txName = values[1];
					String tss5kStart = "";
					String tss5kEnd = "";
					String tss5kWidth = "10001";
					String tssTes5kWidth = String.valueOf(widthInteger + 10000);
					if (strand.equalsIgnoreCase("+")) {
						tss5kStart = String.valueOf(startInteger - 5000);
						tss5kEnd = String.valueOf(startInteger + 5000);
					} else {
						tss5kStart = String.valueOf(endInteger - 5000);
						tss5kEnd = String.valueOf(endInteger + 5000);
					}
					
					String tssTes5kStart =  String.valueOf(startInteger - 5000);
					String tssTes5kEnd = String.valueOf(endInteger + 5000);
					//"seqname", "start", "end", "width", "strand", "tx_id", "tx_name"
					cSVWriter.writeNext(new String[]{seqname, start, end, width, strand, String.valueOf(txId), txName});
					//"seqnames", "start", "end", "width", "strand"
					tssTescSVWriter.writeNext(new String[] {seqname, start, end, width, strand});
					
					tss5kcSVWriter.writeNext(new String[] {seqname, tss5kStart, tss5kEnd, tss5kWidth, strand});
					tssTes5kcSVWriter.writeNext(new String[]{seqname, tssTes5kStart, tssTes5kEnd, tssTes5kWidth, strand});
				}
				txId = txId + 1;
			}
			
			cSVWriter.flush();
			cSVWriter.close();
			
			tssTescSVWriter.flush();
			tssTescSVWriter.close();
			
			tss5kcSVWriter.flush();
			tss5kcSVWriter.close();
			tssTes5kcSVWriter.flush();
			tssTes5kcSVWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void transferXref() {
		try {
			BufferedReader geneBufferedReader = new BufferedReader(new FileReader("E:\\projects\\new_omicseq\\txref"));
			String line = "";
			CSVWriter cSVWriter = new CSVWriter(new FileWriter("E:\\projects\\new_omicseq\\txXref_hg19_new.csv"));
			cSVWriter.writeNext(new String[]{"tx_id", "mRNA", "spID", "spDisplayID", "geneSymbol", "refseq", "protAcc", "description"});
			Integer count = 0;
			while(StringUtils.isNoneBlank((line = geneBufferedReader.readLine()))) {
				if (count != 0) {
					String[] valueArray =  line.split("\t");
					cSVWriter.writeNext(new String[]{valueArray[0],valueArray[1], valueArray[2], valueArray[3], valueArray[4], valueArray[5], valueArray[6], valueArray[7]});
				}
				count = count + 1;
			}
			cSVWriter.flush();
			cSVWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
