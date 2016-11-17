package com.omicseq.store.daoimpl.mongodb;

import java.util.List;

import com.omicseq.domain.VariationRank;
import com.omicseq.store.criteria.GeneRankCriteria;
import com.omicseq.store.dao.IVariationRankDAO;

public class VariationRankDAOImpl extends GenericMongoDBDAO<VariationRank>  implements IVariationRankDAO {

	@Override
	public void clean() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<VariationRank> findByCriteria(GeneRankCriteria criteria,
			Integer start, Integer limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<VariationRank> findByCriteria(GeneRankCriteria criteria) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer count(GeneRankCriteria criteria) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer count(Integer sampleId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Integer> listRankByCriteria(GeneRankCriteria criteria,
			String field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<VariationRank> listByGeneId(Integer geneId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeBySampleId(Integer sampleId) {
		super.delete(new SmartDBObject("sampleId", sampleId));
	}

	@Override
	public List<Double> percentile(GeneRankCriteria criteria) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
