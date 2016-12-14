package com.omicseq.encode;

import java.io.FileWriter;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

import com.omicseq.common.GeneCountType;
import com.omicseq.core.EntrezeSymbolCache;
import com.omicseq.core.GeneCache;
import com.omicseq.core.SampleCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.domain.GeneRank;
import com.omicseq.statistic.IRankStatistic;
import com.omicseq.statistic.TCGARankStatistic;

public class TCGATest {

	public static void main(String[] args) {
		try {
			GeneCache.getInstance().init();
			TxrRefCache.getInstance().init();
			SampleCache.getInstance().init();
			EntrezeSymbolCache.getInstance().init();
			IRankStatistic irankStatistic = new TCGARankStatistic();
			List<GeneRank> geneRankList = irankStatistic.computeRank("D:\\software\\develop\\test.txt",102291).getGeneRankList();
			CSVWriter csvWriter = new CSVWriter(new FileWriter("E:\\projects\\new_omicseq\\tcga.1.csv"));
			csvWriter.writeNext(new String[] { "geneId", GeneCountType.tss_tes.name(), "tsstesrank", "tsstesper", "mixperc"});
			
			for (GeneRank geneRank : geneRankList) {
				csvWriter.writeNext(new String[] { String.valueOf(geneRank.getGeneId()),
						String.valueOf(geneRank.getTssTesCount()),
						String.valueOf(geneRank.getTssTesRank()),
						String.valueOf(geneRank.getTssTesPerc()),
						String.valueOf(geneRank.getMixturePerc())});
				
			}
			csvWriter.flush();
			csvWriter.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
