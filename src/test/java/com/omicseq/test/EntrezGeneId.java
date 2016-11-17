package com.omicseq.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.omicseq.core.GeneCache;
import com.omicseq.core.MouseGeneCache;
import com.omicseq.core.MouseTxrRefCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class EntrezGeneId {
	static IGeneDAO geneDAO = DAOFactory.getDAO(IGeneDAO.class);
	static IGeneDAO mouseGeneDAO =  DAOFactory.getDAOByTableType(IGeneDAO.class, "mouse");
	
	public void parse(){
		List<String > geneSymbolList = new ArrayList<String>();
		DBCollection col = MongoDBManager.getInstance().getCollection("manage", "manage", "txrref_temp");
		DBCursor cursor = col.find(new SmartDBObject());
		while(cursor.hasNext()){
			DBObject obj = cursor.next();
			String symbol =(String)obj.get("geneSymbol");
			geneSymbolList.add(symbol);
		}
		for(String symbol : geneSymbolList){
			String url = "http://www.ncbi.nlm.nih.gov/gene/?term=" + symbol + "%5Bsym%5D";
			List<Gene> geneList = new ArrayList<Gene>();
			try {
				List<TxrRef> txrRefList = TxrRefCache.getInstance().getTxrRefBySymbol(symbol);
				if(CollectionUtils.isEmpty(txrRefList)) continue;
				for(TxrRef txrRef : txrRefList){
					String refseq = txrRef.getRefseq();
					Gene gene = GeneCache.getInstance().getGeneByName(refseq);
					if(gene == null) continue;
					geneList.add(gene);
				}
				Document doc = Jsoup.connect(url).timeout(10000).get();
				Element ele = doc.getElementById("gene-tabular-docsum");
				if(ele == null) continue;
				Elements items = ele.getElementsByClass("rprt");
				for(Element el : items){
					String symb = null;
					Integer id = null;
					if(el.child(1).text().contains("[ (human)]") && el.child(0).child(1).text().trim().equalsIgnoreCase(symbol)){
						symb = el.child(0).child(1).text().trim();
						id = Integer.parseInt(el.getElementsByClass("gene-id").first().text().split(":")[1].trim());
						for(Gene gene : geneList){
							if(gene.getEntrezId()==null || gene.getEntrezId()!=id){
								gene.setEntrezId(id);
								geneDAO.update(gene);
							}
						}
						
					}
					if(el.child(1).text().contains("[ (house mouse)]")){
						symb = el.child(0).child(1).text().trim();
						id = Integer.parseInt(el.getElementsByClass("gene-id").first().text().split(":")[1].trim());
						if(symb != null) System.out.println(symb);
						if(id !=null) System.out.println(id);
						List<TxrRef> txrRefList_mouse = MouseTxrRefCache.getInstance().getTxrRefBySymbol(symb);
						if(CollectionUtils.isEmpty(txrRefList_mouse)) continue;
						for(TxrRef txr : txrRefList_mouse) {
							String refseq = txr.getRefseq();
							Gene gene = MouseGeneCache.getInstance().getGeneByName(refseq);
							if(gene == null) continue;
							if(gene.getEntrezId()==null || gene.getEntrezId()!=id){
								gene.setEntrezId(id);
								gene.setRelKey(symbol);
								mouseGeneDAO.update(gene);
							}
						}
						for(Gene gene : geneList){
							if(gene.getRelKey() != null){
								String [] keys = gene.getRelKey().split(",");
								boolean flag = false;
								for(String key : keys){
									if(key.equalsIgnoreCase(symb)){
										flag = true;
										break;
									}
								}
								if(!flag){
									gene.setRelKey(gene.getRelKey() + "," + symb);
									geneDAO.update(gene);
								}
							}else{
								gene.setRelKey(symb);
								geneDAO.update(gene);
							}
						}
					}
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		TxrRefCache.getInstance().doInit();
		GeneCache.getInstance().doInit();
		MouseGeneCache.getInstance().doInit();
		MouseTxrRefCache.getInstance().doInit();
		new EntrezGeneId().parse();;
	}

}
