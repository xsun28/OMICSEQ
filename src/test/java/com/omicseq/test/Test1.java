package com.omicseq.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import com.omicseq.common.SortType;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class Test1 {
	private static IGeneRankDAO geneRankDao = DAOFactory.getDAO(IGeneRankDAO.class);
	private static IGeneDAO geneDAO = DAOFactory.getDAO(IGeneDAO.class);
	private static ITxrRefDAO txrRefDAO = DAOFactory.getDAO(ITxrRefDAO.class);
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		int [] sampleIds = {65133};
		int x = 0;
		for(int sid : sampleIds){
			SmartDBObject query = new SmartDBObject();
			query.put("sampleId", sid);
			SmartDBObject q = new SmartDBObject("$lte", 0.01);
			query.put("mixturePerc", q);
			query.addSort("mixturePerc", SortType.ASC);
			List<GeneRank> geneRankList = geneRankDao.find(query);
			for(GeneRank gr : geneRankList){
				File file1 = new File("F:/"); 
				if(!file1.exists() && !file1.isDirectory()){
					file1.mkdir();
				}
				File file = new File("F:"+File.separator+"recoder"+File.separator+"mutation_prad_rec.txt");    
				FileOutputStream fos;
				fos = new FileOutputStream(file,true);
				OutputStreamWriter osw = new OutputStreamWriter(fos);   
				BufferedWriter bw = new BufferedWriter(osw);
				List<Gene> gene= geneDAO.find(new SmartDBObject("geneId", gr.getGeneId()));

				 for(int i=0; i<gene.size(); i++)
				 {
					 String txName = gene.get(i).getTxName();
					 List<TxrRef>  refs = txrRefDAO.find(new SmartDBObject("refseq", txName));
					 
					 for(TxrRef r : refs){
						 System.out.println(r.getGeneSymbol());
						 bw.write("\r\n"+r.getGeneSymbol()+"	"+gr.getGeneId()+"	"+gr.getSampleId()+"	"+gr.getTssTesCount()+"	"+gr.getMixturePerc()); 
						 break;
					 }
					 break;
				 }
				bw.flush();   
				bw.close();  
				osw.close();  
				fos.close();
			}
			
		}
	}

}
