package com.omicseq.test;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.omicseq.common.SourceType;
import com.omicseq.core.GeneCache;
import com.omicseq.core.MouseGeneCache;
import com.omicseq.core.MouseTxrRefCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.MiRNA;
import com.omicseq.domain.MiRNARank;
import com.omicseq.domain.MiRNASample;
import com.omicseq.domain.Sample;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.ImiRNADAO;
import com.omicseq.store.dao.ImiRNARankDAO;
import com.omicseq.store.dao.ImiRNASampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateUtils;

public class TwoColumnFile {
	String path = "C:\\Users\\Administrator\\Desktop\\file\\";
	ISampleDAO samplenewDao = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
	ISampleDAO sampleDao = DAOFactory.getDAO(ISampleDAO.class);
	IGeneRankDAO rankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	ImiRNASampleDAO miRNASampleDAO = DAOFactory.getDAO(ImiRNASampleDAO.class);
	ImiRNADAO miRNADAO = DAOFactory.getDAOByTableType(ImiRNADAO.class,"new");
	ImiRNARankDAO miRNARankDAO = DAOFactory.getDAO(ImiRNARankDAO.class);
	DecimalFormat df = new DecimalFormat("#.00000");
	public void parse(){
		File file = new File(path);
		if(file.isDirectory()){
			String [] names = file.list();
			List<Sample> samples = samplenewDao.find(new SmartDBObject("sampleId",1300016));
			Sample sample = samples.get(0);
			/*Sample sa = new Sample();
			int sampleId = sampleDao.getSequenceId(SourceType.SUPPLEMENTTARY);
			sa.setSampleId(sampleId);
			System.out.println(sampleId);
			sa.setAntibody(sample.getAntibody());
			sa.setCell(sample.getCell());
			sa.setCreateTiemStamp(sample.getCreateTiemStamp());
			sa.setDeleted(0);
			sa.setDescription(sample.getDescription());
			sa.setDetail(sample.getDetail());
			sa.setEtype(sample.getEtype());
			sa.setFactor(sample.getFactor());
			sa.setFromType(sample.getFromType());
			sa.setInputSampleIds(sample.getInputSampleIds());;
			sa.setInstrument(sample.getInstrument());
			sa.setLab(sample.getLab());
			sa.setPubmedUrl(sample.getPubmedUrl());
			sa.setReadCount(sample.getReadCount());
			sa.setSampleCode(sample.getSampleCode());
			sa.setSource(sample.getSource());
			sa.setSourceUrl(sample.getSourceUrl());
			sa.setTimeStamp(sample.getTimeStamp());
			sa.setUrl("http://genome.cshlp.org/content/suppl/2013/11/18/gr.157743.113.DC1/Supplemental_Data3_DEgenes.xlsx");
			samplenewDao.create(sa);*/
			for(String name : names){
				try {
					BufferedReader br = new BufferedReader(new FileReader(new File(path+name)));
					String line = null;
					int row = 1;
					Set<Integer> geneSet = new HashSet<Integer>();
					List<GeneRank> ranklist = new ArrayList<GeneRank>();
					while((line=br.readLine())!=null){
						if(1==row++) continue;
						String [] tmp = line.split("\t");
						String symbol = tmp[0];
						Double read = Double.valueOf(tmp[1]);
						Gene gene = getGeneId(symbol);
						if(gene==null) continue;
						int geneId = gene.getGeneId();
						if(!geneSet.add(geneId)) continue;
						GeneRank generank = new GeneRank();
						generank.setCreatedTimestamp(System.currentTimeMillis());
						generank.setGeneId(geneId);
						if(name.contains("1")){
							generank.setSampleId(sample.getSampleId());
						}else{
							generank.setSampleId(1300220);
						}
						generank.setSource(sample.getSource());
						generank.setEtype(sample.getEtype());
						generank.setTssTesCount(read);
						ranklist.add(generank);
					}
					
					Collections.sort(ranklist, new Comparator<GeneRank>(){
						@Override
						public int compare(GeneRank o1, GeneRank o2) {
							return ( o1.getTssTesCount().compareTo(o2.getTssTesCount()) ) * (-1);
						}
					});
					
					for(GeneRank gr : ranklist){
						gr.setTotalCount(32745);
						gr.setMixturePerc(Double.valueOf(df.format((double)(ranklist.indexOf(gr)+1)/32745)));
					}
					rankDAO.create(ranklist);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public Gene getGeneId(String symbol){
		//根据symbol找对应的refseq
		//List<TxrRef> txrRefList = txrRefDAO.findByGeneSymbol(symbol);
	List<TxrRef> txrRefList = TxrRefCache.getInstance().getTxrRefBySymbol(symbol.toLowerCase());
	if(txrRefList ==null ||txrRefList.size()== 0){
	}else{
		boolean flag = true; 
		for(TxrRef tr : txrRefList){
			String refseq = tr.getRefseq();
			if(refseq !=null && !"".equals(refseq)){
					//根据refseq对应gene表txName字段 找geneId
					//Gene gene = geneDAO.getByName(refseq); 
				Gene gene = GeneCache.getInstance().getGeneByName(refseq);
				if(gene != null){
					return gene;
				}
			}
		}
	}
		return null;
	}
	
	public Gene getGeneId_mouse(String symbol){
		//根据symbol找对应的refseq
		//List<TxrRef> txrRefList = txrRefDAO.findByGeneSymbol(symbol);
	List<TxrRef> txrRefList = MouseTxrRefCache.getInstance().getTxrRefBySymbol(symbol.toLowerCase());
	if(txrRefList ==null ||txrRefList.size()== 0){
	}else{
		boolean flag = true; 
		for(TxrRef tr : txrRefList){
			String refseq = tr.getRefseq();
			if(refseq !=null && !"".equals(refseq)){
					//根据refseq对应gene表txName字段 找geneId
					//Gene gene = geneDAO.getByName(refseq); 
				Gene gene = MouseGeneCache.getInstance().getGeneByName(refseq);
				if(gene != null){
					return gene;
				}
			}
		}
	}
		return null;
	}
	
	public MiRNASample convertSampleToMiRNASample(Sample sample){
		MiRNASample miSample = new MiRNASample();
		miSample.setMiRNASampleId(miRNASampleDAO.getSequenceId(SourceType.TCGA));
		miSample.setBarCode(sample.getSampleCode());
		miSample.setSetType(sample.getSettype());
		miSample.setCell(sample.getCell());
		miSample.setSource(sample.getSource());
		miSample.setEtype(sample.getEtype());
		miSample.setCreateTimeStamp(sample.getCreateTiemStamp());
		miSample.setDeleted(0);
		miSample.setFactor(sample.getFactor());
		miSample.setLab(sample.getLab());
		miSample.setUrl(sample.getUrl());
		miRNASampleDAO.create(miSample);
		return miSample;
	}
	public static void main(String[] args) {
		GeneCache.getInstance().doInit();TxrRefCache.getInstance().doInit();
		new TwoColumnFile().parse();
	}

	public void parseMiRNA(){
		File file = new File(path);
		if(file.isDirectory()){
			String [] names = file.list();
			List<Sample> samples = samplenewDao.find(new SmartDBObject("sampleId",1300063));
			Sample sample1 = samples.get(0);
			MiRNASample  sample = convertSampleToMiRNASample(sample1);
			for(String name : names){
				try {
					BufferedReader br = new BufferedReader(new FileReader(new File(path+name)));
					String line = null;
					int row = 1;
					Set<Integer> geneSet = new HashSet<Integer>();
					List<MiRNARank> ranklist = new ArrayList<MiRNARank>();
					while((line=br.readLine())!=null){
						if(1==row++) continue;
						String [] tmp = line.split("\t");
						String symbol = tmp[0].replace("-5p", "").toLowerCase();
						Double read = Double.valueOf(tmp[1]);
						MiRNA gene = miRNADAO.findByName(symbol);
						if(gene==null) continue;
						int geneId = gene.getMiRNAId();
						if(!geneSet.add(geneId)) continue;
						MiRNARank generank = new MiRNARank();
						generank.setCreateTimeStamp(DateUtils.format(new Date(), DateUtils.FT_DATE));
						generank.setMiRNAId(geneId);
						if(name.contains("1")){
							generank.setMiRNASampleId(sample.getMiRNASampleId());
						}
						generank.setSource(sample.getSource());
						generank.setEtype(sample.getEtype());
						generank.setRead(read);
						ranklist.add(generank);
					}
					
					Collections.sort(ranklist, new Comparator<MiRNARank>(){
						@Override
						public int compare(MiRNARank o1, MiRNARank o2) {
							return ( o1.getRead().compareTo(o2.getRead()) ) * (-1);
						}
					});
					
					for(MiRNARank gr : ranklist){
						gr.setTotalCount(1046);
						gr.setMixtureperc(Double.valueOf(df.format((double)(ranklist.indexOf(gr)+1)/1046)));
					}
					miRNARankDAO.create(ranklist);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
