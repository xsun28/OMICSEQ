package com.omicseq.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.omicseq.core.GeneCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.TxrRef;

public class GetGeneByGeneSymbol {
	
	public GetGeneByGeneSymbol(){
		
	}
	
	public static Gene getByGeneSymbol(String symbol) {
		symbol = symbol.toLowerCase();
		boolean refSeq = symbol.startsWith("nm_") || symbol.startsWith("nr_");
		if (refSeq) {
			Gene gene = GeneCache.getInstance().getGeneByName(symbol);
			return gene;
		} else {
			// gene symbol process flow
			List<TxrRef> txrRefList = TxrRefCache.getInstance()
					.getTxrRefBySymbol(symbol);
			if (CollectionUtils.isEmpty(txrRefList)) {
				return new Gene();
			}
			List<Gene> geneList = new ArrayList<Gene>();
			Set<String> geneSet = new HashSet<String>();
			for (TxrRef txrRef : txrRefList) {
				if (StringUtils.isBlank(txrRef.getRefseq())) {
					continue;
				}
				Gene gene = GeneCache.getInstance().getGeneByName(
						txrRef.getRefseq());
				if (gene == null) {
					continue;
				}
				if (geneSet.add(gene.getTxName())) {
					geneList.add(gene);
				}
			}
			if (CollectionUtils.isEmpty(geneList)) {
				return new Gene();
			}
			Gene gene = geneList.get(0);
			return gene;
		}
	}
}
