package com.omicseq.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;



import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.omicseq.core.GeneCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.store.daoimpl.mongodb.TxrRefDAO;

public class TxrRefTransposition {
	private ITxrRefDAO txrRefDAO = DAOFactory.getDAO(ITxrRefDAO.class);
	public void find(){
		//拿到所有geneSymbol
		DBCollection col = MongoDBManager.getInstance().getCollection("manage", "manage","txrref_temp");
		DBCursor cursor = col.find();
		List<String> symbolList = new ArrayList<String>();
		while(cursor.hasNext()){
			DBObject obj = cursor.next();
			String geneSymbol = (String) obj.get("geneSymbol");
			symbolList.add(geneSymbol);
		}
		
		for(String geneSymbol : symbolList){
			Set<Gene> geneSet = new HashSet<Gene>();
			
			List<TxrRef>  txrrefList = TxrRefCache.getInstance().getTxrRefBySymbol(geneSymbol);
			if(txrrefList == null) continue;
			for(TxrRef txrref : txrrefList){
				Gene gene = GeneCache.getInstance().getGeneByName(txrref.getRefseq());
				if(gene == null) continue;
				geneSet.add(gene);
			}
			
			if(geneSet.size()<2) continue;
			
			Integer length = 0;
			for(Gene gene : geneSet){
				Integer width = gene.getWidth();
				if(width > length){
					length = width;
				}
			}
			
			Gene gene_longest = new Gene();
			for(Gene gene : geneSet){
				if(gene.getWidth() == length){
					gene_longest = gene;
				}
			}
			
			 List<TxrRef> txrlist = TxrRefCache.getInstance().getTxrRefBySymbol(geneSymbol);
			 if(txrlist == null) continue;
			 TxrRef txrRef_first = txrlist.get(0);
			 
			 SmartDBObject query = new SmartDBObject("refseq",gene_longest.getTxName());
			 List<TxrRef> tlist = txrRefDAO.find(query);
			 
			 for(TxrRef t : tlist){
				 if(t.getGeneSymbol().equals(geneSymbol) && StringUtils.isEmpty(t.getAlias()) && StringUtils.isEmpty(t.getRefseq())){
					 if(!t.equals(txrRef_first)){
						 String ucscName1 = t.getUcscName();
						 String spId1 = t.getSpID();
						 String spDisplayId1 = t.getSpDisplayID();
						 String nRna1 = t.getmRNA();
						 String refseq1 = t.getRefseq();
						 String protAcc1 = t.getProtAcc();
						 String description1 = t.getDescription();
						 String alias1 = t.getAlias();
						 
						 String ucscName2 = txrRef_first.getUcscName();
						 String spId2 = txrRef_first.getSpID();
						 String spDisplayId2 = txrRef_first.getSpDisplayID();
						 String nRna2 = txrRef_first.getmRNA();
						 String refseq2 = txrRef_first.getRefseq();
						 String protAcc2 = txrRef_first.getProtAcc();
						 String description2 = txrRef_first.getDescription();
						 String alias2 = txrRef_first.getAlias();
						 
						 t.setAlias(alias2);
						 t.setDescription(description2);
						 t.setmRNA(nRna2);
						 t.setProtAcc(protAcc2);
						 t.setRefseq(refseq2);
						 t.setSpDisplayID(spDisplayId2);
						 t.setSpID(spId2);
						 t.setUcscName(ucscName2);
						 
						 txrRef_first.setAlias(alias1);
						 txrRef_first.setDescription(description1);
						 txrRef_first.setmRNA(nRna1);
						 txrRef_first.setProtAcc(protAcc1);
						 txrRef_first.setRefseq(refseq1);
						 txrRef_first.setSpDisplayID(spDisplayId1);
						 txrRef_first.setSpID(spId1);
						 txrRef_first.setUcscName(ucscName1);
						 
						 txrRefDAO.update(t);
						 txrRefDAO.update(txrRef_first);
						 
					 }
					 break;
				 }
			 }
		}
		
	}
	
	public static void main(String[] args) {
		GeneCache.getInstance().doInit();
		TxrRefCache.getInstance().doInit();
		new TxrRefTransposition().find();
	}

}
