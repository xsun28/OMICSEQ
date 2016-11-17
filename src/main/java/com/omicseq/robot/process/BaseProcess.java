package com.omicseq.robot.process;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.omicseq.core.GeneCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;

public abstract class BaseProcess {
	protected ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);
	protected ISampleDAO sampleNewDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
	protected IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	protected List<String> fileList = Collections.synchronizedList(new ArrayList<String>());
	protected Map<String,Integer> geneIdMap = Collections.synchronizedMap(new HashMap<String,Integer>());
	protected Boolean flag = true;
	protected String root;
	protected DecimalFormat df = new DecimalFormat("#.00000");
	protected Map<String,String> ensg = Collections.synchronizedMap(new HashMap<String, String>());
	public BaseProcess(String root){
		this.root = root;
		File file = new File(root);
		String [] files = file.list();
		for(String name : files){
			if(!name.contains("miRseq")){
				fileList.add(name);
			}
		}
		DBCollection collection = MongoDBManager.getInstance().getCollection("manage", "manage", "txrref_temp");
		DBCursor cursor = collection.find();
		Set<Integer>  geneIdSet = new HashSet<Integer>();
		while(cursor.hasNext()){
			String symbol = (String)cursor.next().get("geneSymbol");
			List<TxrRef> txrRefList = TxrRefCache.getInstance().getTxrRefBySymbol(symbol.toLowerCase());
			if(CollectionUtils.isNotEmpty(txrRefList)){
				for(TxrRef tr : txrRefList){
					String refseq = tr.getRefseq();
					if(refseq !=null && !"".equals(refseq)){
						Gene gene = GeneCache.getInstance().getGeneByName(refseq);
						if(gene != null && geneIdSet.add(gene.getGeneId())){
							geneIdMap.put(symbol, gene.getGeneId());
							break;
						}
					}
				}
			}
		}
		
		DBCollection collection1 = MongoDBManager.getInstance().getCollection("manage", "manage", "hashdbensemblgene");
		DBCursor cursor1 = collection1.find();
		while(cursor1.hasNext()){
			DBObject obj = cursor1.next();
			String key = (String)obj.get("key");
			String value = (String)obj.get("value");
			ensg.put(key, value) ;
		}
		
	}
	
	public abstract void parser();
}
