package com.omicseq.robot.parse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.omicseq.common.SourceType;
import com.omicseq.core.TxrRefCache;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;

public class SynonymsFromHGNC extends BaseParser {
	private static DBCollection collection = MongoDBManager.getInstance().getCollection("manage", "manage", "txrref_temp");
	private static ITxrRefDAO txrRefDAO = DAOFactory.getDAO(ITxrRefDAO.class);
	
	@Override
	public void parser(String url) {
		DBCursor col = collection.find();
		File file = new File("./logs/alias.txt");
		try {
			if(!file.exists())
			{
				file.createNewFile();
			}
			while(col.hasNext()){
				List<TxrRef> trList = new ArrayList<TxrRef>();
				DBObject obj = col.next();
				String symbol = (String)obj.get("geneSymbol");
				String real_url = "http://www.genenames.org/cgi-bin/search?search_type=symbols&search="+symbol+"&submit=Submit";
				try {
					Document doc = Jsoup.connect(real_url).timeout(timeout).get();
					Elements els = doc.getElementsByAttributeValue("class", "search_result");
					for(Element el : els){
						String  title = el.getElementsByAttributeValue("class", "title").get(0).child(0).text();
						if(title.equalsIgnoreCase(symbol)){
							String next_url = el.getElementsByTag("a").get(0).attr("href");
							Document doc_next = Jsoup.connect("http://www.genenames.org"+next_url).timeout(timeout).get();
							Elements els_next = doc_next.getElementsByClass("symbol_data");
							for(Element e : els_next){
								String tagName = e.tagName();
								boolean flag = false;
								if(tagName.equals("dl")){
									for(Element EName : e.children()){
										if(EName.text().equalsIgnoreCase("Synonyms")){
											flag = true;
											continue;
										}
										if(flag){
											String value = EName.text().trim().equals("-")?null:EName.text().trim();
											if(value!=null){
												Pattern p = Pattern.compile("\"(.*?)\"");
												Matcher m = p.matcher(value);
												ArrayList<String> strs = new ArrayList<String>();
												while(m.find()) {
													strs.add(m.group(1).replaceAll(",", " "));
													value = value.replace(m.group(1), m.group(1).replaceAll(",", " "));
												}
												value = value.replaceAll("\"", "");
											}
											System.out.println(title+"  -->  "+value);
											
	//										File file1 = new File("F:/"); 
	//										if(!file1.exists() && !file1.isDirectory()){
	//											file1.mkdir();
	//										}
//											FileOutputStream fos = new FileOutputStream(file,true);
//											OutputStreamWriter osw = new OutputStreamWriter(fos);   
//											BufferedWriter bw = new BufferedWriter(osw);
//											bw.write(title+","+value+"\r\n");  
//											bw.flush();   
//											bw.close();  
//											osw.close();
//											fos.close();
											
											FileWriter writer = new FileWriter("./logs/alias.txt", true);
											writer.write(title+","+value+"\r\n");
											writer.close();
											
											if(value != null){
												String [] new_gsy = value.split(",");
												for(String newSy : new_gsy){
													List<TxrRef> isExist = TxrRefCache.getInstance().getTxrRefBySymbol(newSy);
													if(CollectionUtils.isNotEmpty(isExist))continue;
													List<TxrRef> trs= TxrRefCache.getInstance().getTxrRefBySymbol(title);
													if(CollectionUtils.isNotEmpty(trs)) {
														TxrRef tr = trs.get(0);
														TxrRef tr_new = new TxrRef();
														tr_new.setUcscName(tr.getUcscName());
														tr_new.setmRNA(tr.getmRNA());
														tr_new.setSpID(tr.getSpID());
														tr_new.setSpDisplayID(tr.getSpDisplayID());
														tr_new.setGeneSymbol(newSy);
														tr_new.setRefseq(tr.getRefseq());
														tr_new.setProtAcc(tr.getProtAcc());
														tr_new.setDescription(tr.getDescription());
														tr_new.setAlias(title);
														trList.add(tr_new);
													}
												}
											}
											break;
										}
									}
								}
							}
						}
					}
					
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println(symbol);
				}
				if(CollectionUtils.isNotEmpty(trList)){
					txrRefDAO.create(trList);
				}
			}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
	}

	@Override
	SourceType getSourceType() {
		return SourceType.ENCODE;
	}
	
	public static void main(String[] args) {
		TxrRefCache.getInstance().init();
		new SynonymsFromHGNC().start();
	}
}
