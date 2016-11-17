package com.omicseq.robot.parse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
		/*
		 * Human Gene 往 Mouse gene对应
		 */
		/*List<String > geneSymbolList = new ArrayList<String>();
		DBCollection col = MongoDBManager.getInstance().getCollection("manage", "manage", "txrref_temp");
		DBCursor cursor = col.find(new SmartDBObject());
//		while(cursor.hasNext()){
//			DBObject obj = cursor.next();
//			String symbol =(String)obj.get("geneSymbol");
//			geneSymbolList.add(symbol);
//		}
		geneSymbolList.add("Crisp4");
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
//								geneDAO.update(gene);
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
		}*/
		
		
		
		/*
		 *  没对应上的  从MouseGene 再查找对应的EntrezId
		 */
		List<Gene> geneMouseList = mouseGeneDAO.find(new SmartDBObject("relKey",null));
		for(Gene gene1 : geneMouseList){
			String url = "http://www.ncbi.nlm.nih.gov/gene/?term=" + gene1.getGeneName() + "%5Bsym%5D";
			try {
				Document doc = Jsoup.connect(url).timeout(10000).get();
				Element ele = doc.getElementById("gene-tabular-docsum");
				if(ele != null){
					Elements items = ele.getElementsByClass("rprt");
					for(Element el : items){
						String symb = null;
						Integer id = null;
						if(el.child(1).text().contains("[ (house mouse)]") && el.child(0).child(1).text().trim().equalsIgnoreCase(gene1.getGeneName())){
							symb = el.child(0).child(1).text().trim();
							id = Integer.parseInt(el.getElementsByClass("gene-id").first().text().split(":")[1].trim());
							gene1.setEntrezId(id);
							mouseGeneDAO.update(gene1);
						}
						if(el.child(1).text().contains("[ (human)]") ){
							symb = el.child(0).child(1).text().trim();
							id = Integer.parseInt(el.getElementsByClass("gene-id").first().text().split(":")[1].trim());
							gene1.setRelKey(symb);
							mouseGeneDAO.update(gene1);
							List<TxrRef> refList = TxrRefCache.getInstance().getTxrRefBySymbol(symb);
							if(CollectionUtils.isEmpty(refList)) continue;
							for(TxrRef ref : refList){
								Gene gene = GeneCache.getInstance().getGeneByName(ref.getRefseq());
								if(gene == null) continue;
								if(gene.getRelKey() == null) {
									gene.setRelKey(gene1.getGeneName());
								}
								if(gene.getEntrezId() == null){
									gene.setEntrezId(id);
								}
								boolean flag = false;
								if(gene.getRelKey() != null){
									for(String  t : gene.getRelKey().split(",")){
										if(t.equalsIgnoreCase(gene1.getGeneName())){
											flag = true;
										}
									}
									if(!flag){
										gene.setRelKey(gene.getRelKey()+","+gene1.getGeneName());
									}
								}
								geneDAO.update(gene);
							}
							
						}
					}
				}else{
					Element el = doc.getElementById("summaryDiv");
					if(el == null) continue;
					Elements idText = doc.getElementsByClass("geneid");
					if(idText == null) continue;
					String text = idText.first().text().split(",")[0].split(":")[1].trim();
					Integer id = Integer.parseInt(text);
					gene1.setEntrezId(id);
					mouseGeneDAO.update(gene1);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		/*
		 *  gene表Gene entrezId 为空的补充
		 */
		List<Gene> geneList = geneDAO.find(new SmartDBObject("entrezId",null));
		for(Gene gene : geneList){
			String txName = gene.getTxName();
			if(txName == null) continue;
			String geneSymbol = TxrRefCache.getInstance().getGeneSymbolByRefSeq(txName);
			if(StringUtils.isEmpty(geneSymbol)) continue;
			String url = "http://www.ncbi.nlm.nih.gov/gene/?term=" + geneSymbol + "%5Bsym%5D";
			try {
				Document doc = Jsoup.connect(url).timeout(10000).get();
				Element ele = doc.getElementById("gene-tabular-docsum");
				if(ele == null){
					Element el = doc.getElementById("summaryDiv");
					if(el == null) continue;
					Elements idText = doc.getElementsByClass("geneid");
					if(idText == null) continue;
					String text = idText.first().text().split(",")[0].split(":")[1].trim();
					Integer id = Integer.parseInt(text);
					gene.setEntrezId(id);
					geneDAO.update(gene);
				}else{
					Elements items = ele.getElementsByClass("rprt");
					for(Element el : items){
						String symb = null;
						Integer id = null;
						if(el.child(1).text().contains("[ (human)]") && el.child(0).child(1).text().trim().equalsIgnoreCase(geneSymbol)){
							symb = el.child(0).child(1).text().trim();
							id = Integer.parseInt(el.getElementsByClass("gene-id").first().text().split(":")[1].trim());
							if(gene.getEntrezId()==null || gene.getEntrezId()!=id){
								gene.setEntrezId(id);
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
//		MouseGeneCache.getInstance().doInit();
//		MouseTxrRefCache.getInstance().doInit();
		new EntrezGeneId().parse();;
	}

}
