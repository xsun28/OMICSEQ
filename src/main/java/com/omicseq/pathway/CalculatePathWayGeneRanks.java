package com.omicseq.pathway;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBList;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.IPathWayDAO;
import com.omicseq.store.dao.IPathWaySampleDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class CalculatePathWayGeneRanks {

	private Logger logger = LoggerFactory.getLogger(CalculatePathWayGeneRanks.class);
	private ITxrRefDAO txrRefDAO = DAOFactory.getDAO(ITxrRefDAO.class);
	
	private IGeneDAO geneDAO = DAOFactory.getDAO(IGeneDAO.class);
	
	private static IPathWayDAO pathWayDao = DAOFactory.getDAO(IPathWayDAO.class);
	
	private IPathWaySampleDAO pathWaySampleDao = DAOFactory.getDAO(IPathWaySampleDAO.class);
	
	private IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	
	private static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
	
	private List<TxrRef> lazyLoadTxrRef(String key) {
        return txrRefDAO.findByGeneSymbol(key.toUpperCase());
    }
	
	static Integer[] geneIds;
	static int n = 0;
	
//	static int m = 1000;
	
	private Gene lazyLoadGene(String key) {
        return geneDAO.getByName(key);
    }
	
	public Gene getGeneByGeneSymbol(String key)
	{
		List<TxrRef> txrRefList = this.lazyLoadTxrRef(key);
		
		if (CollectionUtils.isEmpty(txrRefList)) {
            return null;
        }
		Gene gene = null;
		for (TxrRef txrRef : txrRefList) {
            if (StringUtils.isBlank(txrRef.getRefseq())) {
                continue;
            }
            gene = lazyLoadGene(txrRef.getRefseq());
            if (gene == null) {
                logger.warn(" can't find gene for name : " + txrRef.getRefseq() + "; genesymbol : " + key);
                continue;
            } else {
            	break;
            }
		}
		
		return gene;
	}
	
	public List<GeneRank> geneRanksOfPathWay(String geneNames, Integer pathId, String geneIdStr)
	{
		List<GeneRank> geneRanks = new ArrayList<GeneRank>();
		String[] symbloNames = geneNames.split(",");
		n = symbloNames.length;
		BasicDBList values = new BasicDBList();
		
		if(geneIdStr != null && !"".equals(geneIdStr) && geneIdStr.length() > 0)
		{
			for(int i=0; i<geneIdStr.split(",").length; i++)
			{
				if(!"null".equals(geneIdStr.split(",")[i]))
				{
					values.add(Integer.parseInt(geneIdStr.split(",")[i]));
				}
			}
		} else {
			geneIds = new Integer[symbloNames.length];
			
			for(int i=0; i<symbloNames.length;i++)
			{
				Gene gene = getGeneByGeneSymbol(symbloNames[i]);
				if(gene == null)
				{
					logger.warn(" can't find gene for genesymbol : " + symbloNames[i]);
					continue;
				}
				geneIds[i] = gene.getGeneId();
				values.add(gene.getGeneId());
			}
			
			pathWayDao.updatePathWayById(pathId, geneIds);
		}
		
		
		SmartDBObject query = new SmartDBObject("geneId", new SmartDBObject("$in", values));
//		query.append("sampleId", 101758);
//		query.addSort("mixturePerc", SortType.DESC);
		geneRanks = geneRankDAO.find(query);
		
		return geneRanks;
	}
	
	public static void main(String[] args) {
		
		CalculatePathWayGeneRanks c = new CalculatePathWayGeneRanks();
		
		List<PathWay> pathWayList = c.findAllPathWay();
		for(PathWay pw : pathWayList){
			String geneNames = pw.getGeneNames();
			List<GeneRank> geneRanks = c.geneRanksOfPathWay(geneNames, pw.getPathId(), pw.getGeneIds());
			double d = Math.sqrt(12*n);
			
			HashMap<Integer, Double> mapA = new HashMap<Integer, Double>();
			HashMap<Integer, Integer> mapB = new HashMap<Integer, Integer>();
			HashMap<Integer, Double> mapC = new HashMap<Integer, Double>();
			HashMap<Integer, Integer> mapSource = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> mapEtype = new HashMap<Integer, Integer>();
			
			for(GeneRank g : geneRanks)
			{
				if (mapA.containsKey(g.getSampleId())) {
					if(g.getMixturePerc() != null)
					{
						mapA.put(g.getSampleId(), mapA.get(g.getSampleId())+g.getMixturePerc());
						mapB.put(g.getSampleId(), mapB.get(g.getSampleId()) +1);
						mapC.put(g.getSampleId(), mapC.get(g.getSampleId())+g.getMixturePerc());
					}
					
				}else {
					if(g.getMixturePerc() != null) {
						mapA.put(g.getSampleId(), g.getMixturePerc());
						mapB.put(g.getSampleId(), 1);
						mapC.put(g.getSampleId(), g.getMixturePerc());
					}
				}
				if(!mapSource.containsKey(g.getSampleId()))
				{
					mapSource.put(g.getSampleId(), g.getSource());
				}
				if(!mapEtype.containsKey(g.getSampleId()))
				{
					mapEtype.put(g.getSampleId(), g.getEtype());
				}
			}
			
			
			Iterator<Integer> itC = mapC.keySet().iterator();
			while(itC.hasNext()) {
				int sampleId = itC.next();
				if(mapB.get(sampleId) < n/2)
				{
					if(mapA.get(sampleId) != null)
					{
						mapA.remove(sampleId);
					}
				}
			}
			Iterator<Integer> it = mapA.keySet().iterator();
			List<PathWaySample> psList = new ArrayList<PathWaySample>();
			while(it.hasNext()) {
				Integer sampleId = (Integer)it.next();
				double avgR = mapA.get(sampleId)/mapB.get(sampleId);
				double b = d*(avgR - 0.5);
				
				double test = NORMSDIST(b);
				
				java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.0000000"); 
				
				try {
//					Sample sample = sampleDAO.find(new SmartDBObject("sampleId", sampleId)).get(0);
					PathWaySample ps = new PathWaySample();
					ps.setPathId(pw.getPathId());
					ps.setPathWayName(pw.getPathwayName());
					ps.setSampleId(sampleId);
					ps.setAvgA(avgR);
					ps.setB(b);
					ps.setRank(Double.valueOf(df.format(test)));
					ps.setSource(mapSource.get(sampleId));
					ps.setEtype(mapEtype.get(sampleId));
					psList.add(ps);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
			
			//根据rank值排序
			Collections.sort(psList, new Comparator<PathWaySample>() {
				@Override
				public int compare(PathWaySample o1, PathWaySample o2) {
					if(null != o1.getRank() && null != o2.getRank())
					{
						return o1.getRank().compareTo(o2.getRank());
					}
					return 0;
				}
			});
			
			c.insertInfoToDB(psList);
			System.out.println(pw.getPathId());
			Short status = 1;  //初始计算完成
			pathWayDao.updateStatus(pw.getPathId(), status);
		}
		
	}

	private void insertInfoToDB(List<PathWaySample> psList) {
		pathWaySampleDao.create(psList);
	}

	private List<PathWay> findAllPathWay() {
		SmartDBObject s = new SmartDBObject();
		s.put("status", (short)0);
//		s.put("pathId", 1606);
		return pathWayDao.find(s);
	}
	
	public static double NORMSDIST(double b)
	    {
	        double p = 0.2316419;
	        double b1 = 0.31938153;
	        double b2 = -0.356563782;
	        double b3 = 1.781477937;
	        double b4 = -1.821255978;
	        double b5 = 1.330274429;
	         
	        double x = Math.abs(b);
	        double t = 1/(1+p*x);
	         
	        double val = 1 - (1/(Math.sqrt(2*Math.PI))  * Math.exp(-1*Math.pow(b, 2)/2)) 
							* (b1*t + b2 * Math.pow(t,2) + b3*Math.pow(t,3) + b4 * Math.pow(t,4) + b5 * Math.pow(t,5));
	        if(b < 0)
	        {
	        	val = 1 - val;
	        }
	        return val;
	    }

	
}
