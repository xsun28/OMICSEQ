package com.omicseq.robot.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SortType;
import com.omicseq.common.SourceType;
import com.omicseq.core.GeneCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.core.WebResourceInitiate;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateUtils;

public class CopyOfMethylationParser {
	
	//protected static String root = "/home/TCGA-Assembler/user/methylation450";
		protected static String root = "E:/";
		protected static ISampleDAO dao = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
		//protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAOByTableType(IGeneRankDAO.class, "_copy");
		protected static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
		protected static ITxrRefDAO txrRefDAO = DAOFactory.getDAO(ITxrRefDAO.class);
		protected static IGeneDAO geneDAO = DAOFactory.getDAO(IGeneDAO.class);
		protected static ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);
		private static Logger logger = LoggerFactory.getLogger(CNVParser.class);

	/*
	 *  璇诲彇methylation闇�瑙ｆ瀽鐨則xt 鏂囦欢
	 */
	public List<String> readMethylationFile(){
		List<String> meFileList = new ArrayList<String>();
		File mefile = new File(root+"geneLevel2");
		if(mefile.isDirectory()){
			String [] meFileNames = mefile.list();
			for(String me : meFileNames){
				if(me.startsWith("PRAD")){
				meFileList.add(me);}
			}

		}
		return meFileList;
	}
	
	/*
	 * 瑙ｆ瀽txt鏂囦欢
	 */
	public void parser (){
		List<String> meFileList = readMethylationFile();
		SmartDBObject query = new SmartDBObject();
		query.put("cell", "TCGA-prad");
		query.put("etype", 12);
		query.addSort("sampleId", SortType.ASC);
		List<Sample> sampleList = dao.find(query);
		List<Double> column ;
		java.text.DecimalFormat df =new java.text.DecimalFormat("#.00000");  
		for(String meFileName : meFileList){
			String cacerType = meFileName.split("_")[0];
			List<String> symbolList = new ArrayList<String>();
			List<Integer> sampleIds = null;
			File file = new File(root+"geneLevel2"+File.separator+meFileName);
			logger.debug("褰撳墠瑙ｆ瀽鏂囦欢锛歿}", meFileName);
			String [] lineStrings;
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String readLine;
				List<String> barCodes = new ArrayList<String>();
				boolean flag = true;
				//绗竴娆¤鍙栨枃浠舵嬁鍒版墍鏈塨arcode娣诲姞sample锛屾嬁鍒版墍鏈塻ymbol
				while((readLine = reader.readLine())!=null){
					lineStrings = readLine.split("	");
					//鎺掗櫎绌鸿
					if(lineStrings.length>1){
						//璇诲彇绗竴琛屾墍鏈夌殑TCGA-XXX-XX鍜宻ample 鏁�
						if(flag){
							for(int i=0; i<lineStrings.length; i++){
								if(lineStrings[i].contains("TCGA")){
									barCodes.add(lineStrings[i]);
								}
							}
							System.out.println("TCGA-G9-6373-01A-11D-1787-05="+barCodes.indexOf("TCGA-G9-6373-01A-11D-1787-05"));
							flag = false;
							//澧炲姞sample
							//sampleIds = CreateSample(barCodes, cacerType);
						}	
						symbolList.add(lineStrings[0]);
					}
				}
				//鍘绘帀绗竴涓�GeneSymbol"
				symbolList.remove("GeneSymbol");
				//鍙栨墍鏈塯eneId
				Map<String,Integer> geneIds = getGeneIde(symbolList);
				//寰幆methylation鏂囦欢鍙栬鏁�
				for(int i=92; i<barCodes.size(); i++){
					BufferedReader reader1 = new BufferedReader(new FileReader(file));
					column = new ArrayList<Double>();
					while((readLine = reader1.readLine())!=null){
						lineStrings = readLine.split("	"); 
						//鎺掗櫎绌鸿
						if(lineStrings.length>1){
							if(lineStrings.length>(i+2)){
								if(!"".equals(lineStrings[i+2]) && !lineStrings[i+2].contains("TCGA") && !lineStrings[i+2].equals("NaN")){
									column.add(Double.parseDouble(lineStrings[i+2]));
								}else if("".equals(lineStrings[i+2]) || "NaN".equals(lineStrings[i+2])){
									column.add(0.0);
								}
							}else{
								column.add(0.0);
							}
						}
					}
					List<SymbolReader> list = new ArrayList<SymbolReader>();
					for(int m=0; m<symbolList.size(); m++){
						SymbolReader sr = new SymbolReader();
						sr.setRead(column.get(m));
						sr.setBarCode(barCodes.get(i));
						sr.setSymbol(symbolList.get(m));
						list.add(sr);
					}
					//鎺掑簭 
					Collections.sort(list, new Comparator<SymbolReader>() {
						@Override
						public int compare(SymbolReader o1, SymbolReader o2) {
							return o1.getRead().compareTo(o2.getRead()) *(-1);
						}
					});
					//鏁版嵁搴撴坊鍔爂eneRank
					List<GeneRank> geneRanks = new ArrayList<GeneRank>();
					for(SymbolReader sr: list){
						if(geneIds.get(sr.getSymbol())!=null){
							GeneRank gr = new GeneRank();
							gr.setCreatedTimestamp(System.currentTimeMillis());
							gr.setEtype(ExperimentType.METHYLATION.value());
							gr.setSource(SourceType.TCGA.value());
							gr.setGeneId(geneIds.get(sr.getSymbol()));
							gr.setMixturePerc(Double.parseDouble(df.format((double)(list.indexOf(sr)+1)/list.size())));
							gr.setTotalCount(list.size());
							gr.setTssTesCount(sr.getRead());
							gr.setSampleId(sampleList.get(i).getSampleId());
							System.out.println(sampleList.get(i).getSampleId());
							geneRanks.add(gr);
							System.out.println(geneRanks.size());
						}
					}
					geneRankDAO.create(geneRanks);
					logger.debug("褰撳墠鏂囦欢锛歿}瑙ｆ瀽瀹屾垚", meFileName);
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * 鏍规嵁symbol 鍙栨墍鏈塯eneid
	 */
	public Map<String,Integer> getGeneIde(List<String> symbolList){
		Map<String,Integer> geneIds = new HashMap<String, Integer>();
		for(String symbol : symbolList){
			//鏍规嵁symbol鎵惧搴旂殑refseq
			//List<TxrRef> txrRefList = txrRefDAO.findByGeneSymbol(symbol);
			List<TxrRef> txrRefList = TxrRefCache.getInstance().getTxrRefBySymbol(symbol.toLowerCase());
			if(txrRefList==null || txrRefList.size()== 0){
				//txrref琛ㄦ壘涓嶅埌瀵瑰簲鐨剅efseq 璁板綍涓嬫潵
				geneIds.put(symbol, null);
				record(symbol,"methylation_txfreftable_cantfind");
			}else{
				boolean flag = true; 
				for(TxrRef tr : txrRefList){
					String refseq = tr.getRefseq();
					if(refseq !=null && !"".equals(refseq)){
						//鏍规嵁refseq瀵瑰簲gene琛╰xName瀛楁 鎵緂eneId
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
					record(symbol, "methylation_genetable_cantfind_or_txfref_nomatch");
				}
			}
		}
		return geneIds;
	}
	
	/*
	 * 璁板綍鎵句笉鍒扮殑refseq鐨剆ymbol
	 */
	public void record(String symbol,String fileName){
		try {
			File file1 = new File(root+"recoder"); 
			if(!file1.exists() && !file1.isDirectory()){
				file1.mkdir();
			}
			File file = new File(root+"recoder"+File.separator+fileName+".txt");    
			FileOutputStream fos;
			fos = new FileOutputStream(file,true);
			OutputStreamWriter osw = new OutputStreamWriter(fos);   
			BufferedWriter bw = new BufferedWriter(osw);
			bw.write(symbol+" ");   
			bw.flush();   
			bw.close();  
			osw.close();  
			fos.close();
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	
	/*
	 *  鏁版嵁搴撳垱寤簊ample
	 */
	public List<Integer> CreateSample(List<String> list,String cacerType){
		List<Integer> samplesIds = new ArrayList<Integer>();
		List<Sample> samples = new ArrayList<Sample>();
		for(String barCode : list){
			int sampleId = sampleDAO.getSequenceId(SourceType.TCGA);
			samplesIds.add(sampleId);
			Sample sample = new Sample();
			sample.setSampleId(sampleId);
			sample.setCreateTiemStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
			sample.setCell("TCGA-"+cacerType.toLowerCase());
			sample.setSource(SourceType.TCGA.value());
			sample.setEtype(ExperimentType.METHYLATION.value());
			//璇诲彇description
			File file = new File(root+File.separator+"nationwidechildrens.org_clinical_patient_"+cacerType.toLowerCase()+".txt");
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String patientLine;
				boolean flag = true;
				String [] keys = null;
				while((patientLine = reader.readLine())!=null){
					//璇诲彇绗竴琛屾椂鎷垮埌鎵�湁map鐨刱ey
					if(flag){
						keys = patientLine.split("	");
						flag = false;
					}
					String [] barCodeStrings = barCode.split("-");
					String code = barCodeStrings[0]+"-"+barCodeStrings[1]+"-"+barCodeStrings[2];
					if(patientLine.startsWith(code)){
						String [] values = patientLine.split("	");
						Map<String, String> map = sample.descMap();
						for(int i=2; i<keys.length; i++){
							map.put(keys[i], values[i]);
						}
						sample.descMap(map);
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			samples.add(sample);
		}
		dao.create(samples);
		return samplesIds;
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WebResourceInitiate.getInstance().init();
		new CopyOfMethylationParser().parser();;
	}

}
