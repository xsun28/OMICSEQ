package com.omicseq.robot.mouse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;

/**
 * @author Min.Wang
 *
 */
public class mouseGeneImport {
	private static IGeneDAO geneDAO = DAOFactory.getDAOByTableType(IGeneDAO.class, "mouse");
	 
	public static void main(String[] args) {
		try {
//			BufferedReader bufferReader = new BufferedReader(new FileReader("/home/tomcat/mouse/genes.gtf"));
			BufferedReader bufferReader = new BufferedReader(new FileReader("E:/mouse/genes.gtf"));
			String line = "";
			List<Gene> genefList = new ArrayList<Gene>();
			Map<String, Gene> geneMap = new HashMap<String, Gene>();
//			Set<String> geneIdSet = new HashSet<String>();
			//"project=wgEncode; grant=Myers; lab=HudsonAlpha; composite=wgEncodeHaibTfbs; dataType=ChipSeq; view=Alignments; cell=U87; treatment=None; antibody=RevXlinkChromatin; protocol=PCR2x; replicate=1; submittedDataVersion=V2 - remapped U87 to correct reference
//			String value = "objStatus=revoked - Original experiment was mapped to wrong sex; project=wgEncode; grant=Myers; lab=HudsonAlpha; composite=wgEncodeHaibTfbs; dataType=ChipSeq; view=Alignments; cell=U87; treatment=None; antibody=RevXlinkChromatin; protocol=PCR2x; replicate=1; dataVersion=ENCODE Jan 2011 Freeze; dccAccession=wgEncodeEH001559; dateSubmitted=2010-08-06; dateUnrestricted=2011-05-06; subId=1848; geoSampleAccession=GSM803375; setType=input; controlId=SL103; labExpId=SL103; type=bam; md5sum=1f298401c2b86540cdbd7baa4146c8e5; size=642M";
			int i = 0;
			while(StringUtils.isNoneBlank(line = bufferReader.readLine())) {
				String[] values = line.split("\t");
				String seqName = values[0];
				String start =  values[3];
				String end = values[4];
				Integer width = Integer.valueOf(end) - Integer.valueOf(start);
				String strand = values[6];
				String txName = values[8].split("; ")[1].split(" ")[1].replaceAll("\"", "");
				String geneName = values[8].split("; ")[2].split(" ")[1].replaceAll("\"", "");
				
				Gene mapGene = geneMap.get(txName);
				if(mapGene != null)
				{
					if(mapGene.getEnd() < Integer.valueOf(end))
					{
						mapGene.setEnd(Integer.valueOf(end));
						mapGene.setWidth(Integer.valueOf(end) - mapGene.getStart());
					}
				} else {
					Gene geneMouse = new Gene();
					geneMouse.setGeneId(100000 + i);
					geneMouse.setEnd(Integer.valueOf(end));
					geneMouse.setSeqName(seqName);
					geneMouse.setStart(Integer.valueOf(start));
					geneMouse.setStrand(strand);
					geneMouse.setTxName(txName);
					geneMouse.setWidth(width);
					geneMouse.setGeneName(geneName);
					
					geneMap.put(txName, geneMouse);
					i++;
				}
			}
			
			Iterator<Entry<String, Gene>> it = geneMap.entrySet().iterator();
			
			while(it.hasNext())
			{
				Gene gene = it.next().getValue();
				genefList.add(gene);
			}
			
			mouseGeneImport mg = new mouseGeneImport();
			
			mg.sortList(genefList);
			
			geneDAO.create(genefList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private void sortList(List<Gene> genefList) {
		Collections.sort(genefList, new Comparator<Gene>() {
			@Override
			public int compare(Gene o1, Gene o2) {
				return o1.getGeneId().compareTo(o2.getGeneId());
			}
		});
	}
	
}
