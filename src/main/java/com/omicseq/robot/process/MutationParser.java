package com.omicseq.robot.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.omicseq.common.CancerType;
import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.core.GeneCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.utils.DateUtils;

public class MutationParser {
	
//	private static String rootPath = "F:"+File.separator +"mutation";
	private static String rootPath = "/files/download/mutation";
	private static ISampleDAO sampleNewDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
	private static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	private static ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);
	private Map<String,Integer> geneIds = new HashMap<String, Integer>();
	private DecimalFormat df = new DecimalFormat("#.00000");
	
	//读取文件
	public List<String> getParseFile() throws FileNotFoundException, IOException{
		List<String> parseFiles = new ArrayList<String>();
		File file = new File(rootPath);
		if(file.isDirectory()){
			String [] files = {"acc","blca","brca","cesc","coad","gbm","hnsc","kich","kirc","kirp","laml","lgg","lihc","luad","lusc","ov","paad","pcpg","prad","read","skcm","stad","thca","ucec","ucs"};
			for(String f : files){
				String path1 = rootPath + File.separator + "mutation_"+f;
				File mutations = new File(path1);
				String [] mutation = mutations.list();
				for(String mu : mutation){
					if(!mu.equalsIgnoreCase("metadata")){
						String path2 = path1 +File.separator +mu; 
						if(new File(path2).isDirectory() ){
							String [] labs = new File(path2).list();
							for(String lab : labs ){
								String lastPath = path2 +File.separator + lab;
								//创建sample
							
								String [] lastFiles = new File(lastPath+File.separator+"Level_2").list();
								System.out.println(lastPath);
								for(String fn : lastFiles){
									List<Sample> samples = create(fn,lab,f,lastPath);
									sampleNewDAO.create(samples);
									String needPath = lastPath + File.separator+"Level_2" + File.separator +fn;
									//geneRnak写入
									sort(needPath,fn,samples);
								}
							}
						}
					}
				}
			}
		}
		return parseFiles;
	}
	
	public List<Sample> create(String fn ,String lab,String f,String lastPath) throws IOException{
		List<Sample> samples = new  ArrayList<Sample>();
		String needPath = lastPath + File.separator+"Level_2" + File.separator +fn;
		BufferedReader br = new BufferedReader(new FileReader(needPath));
		String lineR = "";
		int i=0;
		String version =null;
		while((lineR = br.readLine())!=null){
			if(i==1){
				String [] sd = lineR.split("	");
				version	 = sd[sd.length-2];
				break;
			}
			i++;
		}
		String [] from = fn.split("__");
		String [] names = lab.split("__");
		Sample sample = new Sample();
		Map<String, String> map = sample.descMap();
		map.put("Center", from[0]);
		map.put("Platform", from[1].split("\\.")[0]);
		sample.descMap(map);
		sample.setCell("TCGA-"+f);
		sample.setDetail(CancerType.valueOf(f.toUpperCase())+" tumor" );
		sample.setCreateTiemStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
		sample.setSource(SourceType.TCGA.value());
		sample.setEtype(ExperimentType.MUTATION.value());
		sample.setSampleId(sampleDAO.getSequenceId(SourceType.TCGA));
		sample.setDeleted(0);
		sample.setLab(from[0]);
		String url = "https://tcga-data.nci.nih.gov/tcgafiles/ftp_auth/distro_ftpusers/anonymous/tumor/"+f.toLowerCase()
				+"/gsc/"+from[0]+"/"+names[1].toLowerCase()+"/mutations/"+version+".tar.gz" ;
		sample.setUrl(url);
		samples.add(sample);
		Sample normalizedSample = new Sample();
		normalizedSample.setDescription(sample.getDescription());
		normalizedSample.setDetail(sample.getDetail());
		normalizedSample.setCell(sample.getCell()+"-normalized mutation frequency");
		normalizedSample.setCreateTiemStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
		normalizedSample.setSource(SourceType.TCGA.value());
		normalizedSample.setEtype(ExperimentType.MUTATION.value());
		normalizedSample.setSampleId(sampleDAO.getSequenceId(SourceType.TCGA));
		normalizedSample.setDeleted(0);
		normalizedSample.setLab(from[0]);
		normalizedSample.setUrl(sample.getUrl());
		samples.add(normalizedSample);
		return samples;
	}
	
	public void sort(String path,String fn,List<Sample> sampleList) throws IOException{
		Set<String > normalTotal = new HashSet<String>();
		List<GeneRank> ranks = new ArrayList<GeneRank>();
		for(Sample sample : sampleList){
			Map<Integer,Integer> read = new HashMap<Integer, Integer>();
			File file = new File(path);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line ="";
			Set<Integer> set = new HashSet<Integer>();
			boolean f = true;
			if(!sample.getCell().contains("normalized")){
				while((line = br.readLine())!= null){
					if(f){
						f=false;
						continue;
					}
					String [] lines = line.split("	");
					if(lines.length<6){
						continue;
					}
					String geneSymbol = lines[0];
					normalTotal.add(lines[16]);
					if(!geneSymbol.equals("Hugo_Symbol")){
						Integer geneId = getGeneIde(geneSymbol);
						if(geneId == null ) continue;
						if(set.contains(geneId)){
							int i = read.get(geneId) + 1;
							read.remove(geneId);
							read.put(geneId, i);
						}else{
							read.put(geneId, 1);
							set.add(geneId);
						}
					}
				}
				for(Integer geneId : read.keySet()){
					GeneRank gr = new GeneRank();
					gr.setSampleId(sample.getSampleId());
					gr.setGeneId(geneId);
					gr.setCreatedTimestamp(System.currentTimeMillis());
					gr.setSource(SourceType.TCGA.value());
					gr.setEtype(ExperimentType.MUTATION.value());;
					gr.setTssTesCount(read.get(geneId)*1.0);
					gr.setTotalCount(32745);
					ranks.add(gr);
				}
				Collections.sort(ranks,new Comparator<GeneRank>() {
	
					@Override
					public int compare(GeneRank o1, GeneRank o2) {
						return new Double(Math.abs(o1.getTssTesCount())).compareTo(new Double(Math.abs(o2.getTssTesCount()))) *(-1);					}
				});
				
				for(GeneRank gr : ranks){
					gr.setMixturePerc(Double.parseDouble(df.format((double)(ranks.indexOf(gr)+1)/32745)));
				}
			}
			if(sample.getCell().contains("normalized")){
				int size = normalTotal.size();
				List<GeneRank> normalRanks = new  ArrayList<GeneRank>();
				for(GeneRank gr : ranks){
					GeneRank nomal = new GeneRank();
					nomal.setSampleId(sample.getSampleId());
					nomal.setGeneId(gr.getGeneId());
					nomal.setSource(gr.getSource());
					nomal.setEtype(gr.getEtype());
					nomal.setCreatedTimestamp(System.currentTimeMillis());
					nomal.setTotalCount(32745);
					nomal.setMixturePerc(gr.getMixturePerc());
					nomal.setTss5kCount(gr.getTssTesCount());
					nomal.setTssTesCount(gr.getTssTesCount()/size);
					normalRanks.add(nomal);
				}
				geneRankDAO.create(normalRanks);
			}
			
		}
	}
	
	public Integer getGeneIde(String symbol){
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
							flag = false;
							return gene.getGeneId();
						}
					}
				}
				if(flag){
					geneIds.put(symbol, null);
				}
			}
		return null;
	}
	
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		GeneCache.getInstance().init();
		TxrRefCache.getInstance().init();
		new MutationParser().getParseFile();
	}

}
