package com.omicseq.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;

import com.omicseq.core.VariationGeneCache;
import com.omicseq.domain.VariationGene;
import com.omicseq.store.dao.IVariationGeneDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class TestbianyiGene {
	static Map<Integer,List<VariationGene>> map = new HashMap<Integer,List<VariationGene>>();

	public static void main(String[] args) {
		IVariationGeneDAO dao = DAOFactory.getDAO(IVariationGeneDAO.class);
		List<VariationGene> list = null;
		Integer start = 0 ;
		while(CollectionUtils.isNotEmpty(list = dao.find(new SmartDBObject(),start,10000))){
			for(VariationGene gene : list){
				for(int first = 2000000;first < 60000000; first+=2000000 ){
					if(gene.getChromStart()<first && gene.getChromStart() >= (first-2000000)){
						if(map.get(first) == null){
							List<VariationGene> geneList = new ArrayList<VariationGene>();
							geneList.add(gene);
							map.put(first, geneList);
						}
						else{
							List<VariationGene> geneList = map.get(first);
							geneList.add(gene);
						}
						break;
					}
				}
			}
			start += 10000;
		}
		
		getGeneIdStartAndEnd(2272146,2273143,"chr19");
	}

	   public static String getGeneIdStartAndEnd(Integer start, Integer end, String reqName) {
		   	long begin = System.nanoTime();
	    	List<VariationGene> list = null;
		   	for(int first = 2000000;first < 60000000; first+=2000000 ){
		   		if(start.compareTo(first-2000000) == 1 && end.compareTo(first) == -1){
		   			list = map.get(first);
		   			break;
		   		}
		   	}
		   	if(list == null) return null;
		   	System.out.println("list.size =" + list.size());
	    	int i=0;
	    	for(VariationGene v : list)
	    	{
	    		i++;
	    		if (start.compareTo(v.getChromStart()) == 1 && end.compareTo(v.getChromEnd()) == -1 && v.getChrom().equalsIgnoreCase(reqName)) {
	    			System.out.println(v.getVariationId() + "==============  " + i);
	    			long en = System.nanoTime();
	    			begin=begin/1000;
	    		    en=end/1000;
	    		    System.out.println(((double)end-begin)/(double)(1000*1000));
	                return v.getVariationId();
	            }
	    	}
	    	
	        return null;
	    }
}
