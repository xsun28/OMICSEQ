package com.omicseq.relationship;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.domain.GeneRank;
import com.omicseq.pathway.CalculatePathWayGeneRanks;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class DataSetRelation {

	private Logger logger = LoggerFactory.getLogger(CalculatePathWayGeneRanks.class);
	private IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);
	static int max = 30000;
	
	public static void main(String[] args) {
		DataSetRelation re = new DataSetRelation();
		double r = re.compareR(308693, 303191);
		System.out.println(r);
	}
	
	public double compareR(Integer sampleId1, Integer sampleId2)
	{
		double r = 0.0;
		SmartDBObject query1 = new SmartDBObject();
		query1.put("sampleId", sampleId1);
		List<GeneRank> genrankList1 = geneRankDAO.find(query1, 0, max);
		
		SmartDBObject query2 = new SmartDBObject();
		query2.put("sampleId", sampleId2);
		List<GeneRank> genrankList2 = geneRankDAO.find(query2, 0, max);
		if(genrankList1.size() < max || genrankList2.size() < max)
		{
			if(genrankList1.size() < genrankList2.size())
			{
				max = genrankList1.size();
			} else {
				max = genrankList2.size();
			}
		}
		double[] x = new double[max];
		double[] y = new double[max];
		double sum_x = 0.0;
		double sum_y = 0.0;
		for(int i=0; i<max; i++)
		{
			if(genrankList1.get(i) != null && genrankList1.get(i).getMixturePerc() != null)
			{
				x[i] = genrankList1.get(i).getMixturePerc();
			} else {
				x[i] = 0.00;
			}
			sum_x += x[i];
			
			if(genrankList2.get(i) != null && genrankList2.get(i).getMixturePerc() != null)
			{
				y[i] = genrankList2.get(i).getMixturePerc();
			} else {
				y[i] = 0.00;
			}
			
			sum_y += y[i];
		}
		
		double up = 0.0;
		double sumDown_x = 0.0;
		double sumDown_y = 0.0;
		double down = 0.0;
		for(int i=0; i<max; i++)
		{
			up += (x[i] - sum_x/max)*(y[i] - sum_y/max);
			sumDown_x += Math.pow((x[i]- sum_x/max),2);
			sumDown_y += Math.pow((y[i]- sum_y/max),2);
		}
		down = Math.sqrt(sumDown_x*sumDown_y);
		
		r = up/down;
		return r;
	}

}
