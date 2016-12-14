package com.omicseq.robot.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.omicseq.core.GeneCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.Sample;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class GDSFileToDB_diff implements Runnable{
	private String root = "F:/GDSDownload";
	BufferedReader reader = null;
	private  String [] fileNames;
	Map<String,Integer> geneIds = new HashMap<String, Integer>();
	
	private static List<String> task1 = new ArrayList<String>();
	
	private static List<String> task3 = new ArrayList<String>();

	private static List<String> task2 = new ArrayList<String>();

	private static List<String> task4 = new ArrayList<String>();

	private static List<String> task5 = new ArrayList<String>();

	private static List<String> task6 = new ArrayList<String>();

	private static List<String> task7 = new ArrayList<String>();

	private static List<String> task8 = new ArrayList<String>();
	
	private static ISampleDAO sampleNewDao = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");

	@Override
	public void run() {
		synchronized(this){
			if(Thread.currentThread().getName().equals("1")){
				process(task1);
			}
			if(Thread.currentThread().getName().equals("2")){
				process(task2);
			}
			if(Thread.currentThread().getName().equals("3")){
				process(task3);
			}
			if(Thread.currentThread().getName().equals("4")){
				process(task4);
			}
			if(Thread.currentThread().getName().equals("5")){
				process(task5);
			}
			if(Thread.currentThread().getName().equals("5")){
				process(task6);
			}
			if(Thread.currentThread().getName().equals("6")){
				process(task7);
			}
			if(Thread.currentThread().getName().equals("8")){
				process(task8);
			}
		}
	}


	//获取文件列表
	public GDSFileToDB_diff(){
		File file = new File(root);
		fileNames = file.list();
		DBCollection DbCol = MongoDBManager.getInstance().getCollection("manage", "manage", "txrref_temp"); 
		DBCursor dbList = DbCol.find(new SmartDBObject());
		List<String > symbolList = new ArrayList<String>();
		while(dbList.hasNext()){
			String sym = (String) dbList.next().get("geneSymbol");
			symbolList.add(sym);
		}
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
				if(flag){
					geneIds.put(symbol, null);
				}
			}
		}
	}
	
	

	public void process(List<String> list){
//		Thread t1 = new Thread(target, name)
		
		for(String fileName : list){
			String path = root + File.separator + fileName;
			try {
				if(fileName.endsWith(".gz")){
					reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(path))));
				
				}else{
					reader = new BufferedReader(new FileReader(fileName));
				}
				
				List<SymbolReader> list1 = new ArrayList<SymbolReader>();
				List<SymbolReader> list2 = new ArrayList<SymbolReader>();

				String line = "";
				int col_1 = 0 , col_2 = 0, _status = 0 ;
				boolean isTitle = true;
				while((line=reader.readLine())!=null){
					String [] datas = line.split("	");
					if(datas.length<5) continue;
					if(isTitle){
						isTitle = false;
						boolean flag = true ;
						for(int x=0; x<datas.length;x++){
							if(datas[x].equalsIgnoreCase("status")){
								_status = x;
							}
							if(datas[x].startsWith("value") && !datas[x].equals("p_value") && !datas[x].equals("q_value")){
								if(flag){
									col_1 = x;
									flag = false;
								}else {
									col_2 = x;
								}
							}
						}
					}else{
						if(datas[_status].equalsIgnoreCase("notest")) continue;
						String symbol = datas[1];
						if(fileName.contains("day") || symbol.contains("ENSG") || symbol.contains("ucsc")){
							symbol = datas[2];
						}
						if(symbol.equals("-")) continue;
						Integer geneId = geneIds.get(symbol);
						if(geneId == null) continue;
						
						SymbolReader sr1 = new SymbolReader();
						sr1.setSymbol(symbol);
						sr1.setGeneId(geneId);
						sr1.setRead(Double.parseDouble(datas[col_1]));
						list1.add(sr1);
						
						SymbolReader sr2 = new SymbolReader();
						sr2.setSymbol(symbol);
						sr2.setGeneId(geneId);
						sr2.setRead(Double.parseDouble(datas[col_2]));
						list2.add(sr2);
					}
					
				}
				//paixu
				Collections.sort(list1, new Comparator<SymbolReader>() {
	
					@Override
					public int compare(SymbolReader o1, SymbolReader o2) {
						return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead())))*(-1);
					}
					
				});
				
				Collections.sort(list2, new Comparator<SymbolReader>() {
	
					@Override
					public int compare(SymbolReader o1, SymbolReader o2) {
						return new Double(Math.abs(o1.getRead())).compareTo(new Double(Math.abs(o2.getRead())))*(-1);
					}
					
				});
				
				//存数据库
				for(int i=0;i<10;i++){
					System.out.println(Thread.currentThread().getName()+"--"+fileName+"--"+list1.get(i).getSymbol()+"---"+list1.get(i).getRead());
				}
				System.out.println("------------------------------------------------");
				for(int i=0;i<10;i++){
					System.out.println(Thread.currentThread().getName()+"--"+fileName+"--"+list2.get(i).getSymbol()+"---"+list2.get(i).getRead());
				}
				System.out.println("------------------------------------------------");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static void main(String[] args) {
		/*SmartDBObject query = new SmartDBObject();
		query.put("source", 7);
		query.put("etype", 1);
		query.put("deleted", 0);
		
		List<Sample> samples = sampleNewDao.find(query);
		List<String> files = new ArrayList<String>();
		for(Sample s : samples ){
			String []  names = s.getUrl().split("=");
			String name = names[names.length-1];
			files.add(name);
		}
		GDSFileToDB_diff file = new GDSFileToDB_diff();
		
		String [] fileNames = file.fileNames;
		List<String> fileN = new ArrayList<String>();
		for(String n : fileNames ){
			fileN.add(n);
		}
		int x = 0 ;
		Set<String> set = new HashSet<String>();
		for(String ss : files){
			set.add(ss);
			if(!fileN.contains(ss)){
				x++;
				System.out.println(x+".--"+ss);
			}
		}
		System.out.println(set.size());*/
		TxrRefCache.getInstance().init();
		GeneCache.getInstance().init();
		GDSFileToDB_diff file = new GDSFileToDB_diff();
		String [] fileNames = file.fileNames;
		int length = fileNames.length;
		int size = length/8>1 ? length/8:1;
		for(int i=0;i<length;i++){
			if(i<size){
				task1.add(fileNames[i]);
			}
			if(i>=length && i< size*2){
				task2.add(fileNames[i]);
			}
			if(i>=size*2 && i< size*3){
				task3.add(fileNames[i]);
			}
			if(i>=size*3 && i< size*4){
				task4.add(fileNames[i]);
			}
			if(i>=size*4 && i<size*5){
				task5.add(fileNames[i]);
			}
			if(i>=size*5 && i<size*6){
				task6.add(fileNames[i]);
			}
			if(i>=size*6 && i<size*7){
				task7.add(fileNames[i]);
			}
			if(i>=size*7 && i<size*8){
				task8.add(fileNames[i]);
			}
		}
		Thread t1 = new Thread(file, "1");
		Thread t2 = new Thread(file, "2");
		Thread t3 = new Thread(file, "3");
		Thread t4 = new Thread(file, "4");
		Thread t5 = new Thread(file, "5");
		Thread t6 = new Thread(file, "6");
		Thread t7 = new Thread(file, "7");
		Thread t8 = new Thread(file, "8");
		t1.start();
		t2.start();
		t3.start();
		t4.start();
		t5.start();
		t6.start();
		t7.start();
		t8.start();
	}
	
}
