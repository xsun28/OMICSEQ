package com.omicseq.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.omicseq.common.ExperimentType;
import com.omicseq.common.SortType;
import com.omicseq.core.GeneCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class CheckChipSeqSample {
	private static IGeneDAO geneDao = DAOFactory.getDAO(IGeneDAO.class);
	private static ITxrRefDAO txrRefDao = DAOFactory.getDAO(ITxrRefDAO.class);
	private static IGeneRankDAO geneRankDao = DAOFactory.getDAO(IGeneRankDAO.class);
	public void findSamples(){
		DBCollection col = MongoDBManager.getInstance().getCollection("manage", "manage","txrref_temp");
		DBCursor cursor = col.find();
		List<String> symbolList = new ArrayList<String>();
		while(cursor.hasNext()){
			DBObject obj = cursor.next();
			String geneSymbol = (String) obj.get("geneSymbol");
			symbolList.add(geneSymbol);
		}
		Map<String,Integer> geneIdMap = getGeneIde(symbolList);
		for(int id = 0; id<=32745;id++){
			id=3737;
			List<Gene> geneList = geneDao.find(new SmartDBObject("geneId",id));
			for(Gene gene : geneList){
				String txName = gene.getTxName();
				List<TxrRef> txrRefList = txrRefDao.find(new SmartDBObject("refseq",txName));
				for(TxrRef txrRef : txrRefList){
					String geneSymbol = txrRef.getGeneSymbol();
					Integer id1 = geneIdMap.get(geneSymbol);
					if(id1 == null) continue;
					if(id == id1) continue;
					SmartDBObject query = new SmartDBObject();
					query.put("geneId", id);
					query.put("etype", ExperimentType.CHIP_SEQ_TF.getValue());
					List<GeneRank> geneRankList = geneRankDao.find(query);
					if(CollectionUtils.isEmpty(geneRankList)) continue;
					List<Integer> sampleIdList = new ArrayList<Integer>();
					for(GeneRank gr : geneRankList){
						sampleIdList.add(gr.getSampleId());
					}
					
					SmartDBObject query1 = new SmartDBObject();
					query1.put("geneId", id1);
					query1.put("etype", ExperimentType.CHIP_SEQ_TF.getValue());
					List<GeneRank> geneRankList1 = geneRankDao.find(query1);
					List<Integer> sampleIdList1 = new ArrayList<Integer>();
					for(GeneRank gr : geneRankList1){
						sampleIdList1.add(gr.getSampleId());
					}
					
					sampleIdList.retainAll(sampleIdList1);
					System.out.println();
					for(GeneRank gr : geneRankList){
						if(sampleIdList.contains(gr.getSampleId())){
							geneRankList.remove(gr);
						}
						else{
							gr.setGeneId(id1);
						}
					}
					geneRankDao.create(geneRankList);
				}
			}
		}
		
	}
	
	public Map<String,Integer> getGeneIde(List<String> symbolList){
		Map<String,Integer> geneIds = new HashMap<String, Integer>();
		for(String symbol : symbolList){
			//根据symbol找对应的refseq
			//List<TxrRef> txrRefList = txrRefDAO.findByGeneSymbol(symbol);
			List<TxrRef> txrRefList = TxrRefCache.getInstance().getTxrRefBySymbol(symbol.toLowerCase());
			if(txrRefList ==null ||txrRefList.size()== 0){
				//txrref表找不到对应的refseq 记录下来
				geneIds.put(symbol, null);
			}else{
				boolean flag = true; 
				for(TxrRef tr : txrRefList){
					String refseq = tr.getRefseq();
					if(refseq !=null && !"".equals(refseq)){
						//根据refseq对应gene表txName字段 找geneId
						//Gene gene = geneDAO.getByName(refseq); 
						Gene gene = GeneCache.getInstance().getGeneByName(refseq);
						if(gene != null){
							geneIds.put(symbol, gene.getGeneId());
							flag = false;
							break;
						}
					}
				}
			}
		}
		return geneIds;
	}
	
	public static void main(String[] args) {
	/*	List<Integer> l1 = new ArrayList<Integer>();
		l1.add(1);
		l1.add(2);
		l1.add(3);
		for(Integer i : l1){
			if(i==2) {
				l1.remove(i);
			}
		}
		List<Integer> l2 = new ArrayList<Integer>();
		l2.add(3);
		l2.add(4);
		l2.add(2);
		l1.retainAll(l2);
		System.out.println();
	*/
		TxrRefCache.getInstance().doInit();
		GeneCache.getInstance().doInit();
		new CheckChipSeqSample().findSamples();
	}
}
