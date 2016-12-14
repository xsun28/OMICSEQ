package com.omicseq.robot.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
import com.omicseq.utils.ExcelReader;

public class ProbabilityRecessivedisease {
	
	private static String rootPath = "E:"+File.separator +"临时文件" + File.separator;
	private static ISampleDAO sampleNewDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
	private static IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	private static ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);

	public static void main(String[] args) {
		GeneCache.getInstance().init();
		TxrRefCache.getInstance().init();
		String filePath = rootPath + "RecessiveDisease.xls";
		readFile(filePath);
	}

	private static void readFile(String filePath) {
//		File file = new File(filePath);
		ExcelReader excelReader = new ExcelReader();  
		InputStream is2;
		try {
			is2 = new FileInputStream(filePath);
			Map<Integer,String> map = excelReader.readExcelContent(is2);
//			String title  = map.get(0);
//			Integer sampleId = createSamples();
			Integer sampleId = 700000;
			List<GeneRank> allList = new ArrayList<GeneRank>();
			for(int i=1; i<map.size(); i++)
			{
				String row = map.get(i);
                if(row != null && !"@".equals(row) && row.split("@").length > 1)
                {
                	GeneRank grAll = new GeneRank();
                	String[] values = row.split("@");
                	String symbol = values[0];
                	List<TxrRef> txrRefList = TxrRefCache.getInstance().getTxrRefBySymbol(symbol.toLowerCase());
                	if(txrRefList ==null ||txrRefList.size()== 0) {
                		continue;
                	}
                	Integer geneId = 0;
                	for(TxrRef tr : txrRefList){
    					String refseq = tr.getRefseq();
    					if(refseq !=null && !"".equals(refseq)){
    						//根据refseq对应gene表txName字段 找geneId
    						//Gene gene = geneDAO.getByName(refseq); 
    						Gene gene = GeneCache.getInstance().getGeneByName(refseq);
    						if(gene != null){
    							geneId = gene.getGeneId();
    							break;
    						}
    					}
    				}
                	
                	if(geneId == 0)
                	{
                		continue;
                	}
                	
                	Double all = values[1].equals("NA")?0:Double.valueOf(values[1]);
                	
                	grAll.setGeneId(geneId);
                	grAll.setSampleId(sampleId);
                	grAll.setCreatedTimestamp(System.currentTimeMillis());
                	grAll.setSource(SourceType.SUMMARY.getValue());
                	grAll.setTotalCount(23376);
                	grAll.setTssTesCount(all);
                	grAll.setEtype(ExperimentType.SUMMARY_TRACK.getValue());
                	allList.add(grAll);
                	
                	System.out.println(symbol + " is over");
                }
			}
			
			Collections.sort(allList, new Comparator<GeneRank>() {
				@Override
				public int compare(GeneRank o1, GeneRank o2) {
					//return o1.getRead().compareTo(o2.getRead()) *(-1);
					return new Double(Math.abs(o1.getTssTesCount())).compareTo(new Double(Math.abs(o2.getTssTesCount()))) *(-1);
				}
			});
			
			createGeneRank(allList);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void createGeneRank(List<GeneRank> list) {
		java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00000");  
		for(int i=0; i<list.size(); i++)
		{
			GeneRank gr = list.get(i);
			gr.setMixturePerc(Double.parseDouble(df.format((double)(i+1)/23376)));
		}
		
		geneRankDAO.create(list);
	}

	private static Integer createSamples() {
		String type = "Recessive disease probability";
		Sample sample = new Sample();
		sample.setSource(SourceType.SUMMARY.getValue());
		sample.setEtype(ExperimentType.SUMMARY_TRACK.getValue());
		sample.setDeleted(0);
		sample.setSettype("Recessive disease probability");
		sample.setCell(type);
		sample.setDetail("");
		sample.setLab("MacArthur Lab");
		sample.setPubmedUrl("http://www.ncbi.nlm.nih.gov/pubmed/22344438");
		sample.setUrl("http://macarthurlab.org/lof/");
		sample.setReadCount(14142);
		Integer sampleId = sampleDAO.getSequenceId(SourceType.SUMMARY);
		sample.setSampleId(sampleId);
		sample.setCreateTiemStamp(DateUtils.getNowDate());
		
		sampleNewDAO.create(sample);
		
		return sampleId;
	}

}
