package com.omicseq.store.daoimpl.mongodb;

import java.util.List;

import com.omicseq.common.SortType;
import com.omicseq.domain.VariationGene;
import com.omicseq.store.dao.IVariationGeneDAO;

public class VariationGeneDAOImpl extends GenericMongoDBDAO<VariationGene> implements IVariationGeneDAO {

	@Override
	public VariationGene getById(String variationId) {
		SmartDBObject query = new SmartDBObject("variationId", variationId);
		return findOne(query);
	}

	@Override
	public List<VariationGene> loadGeneList(Integer start, Integer limit) {
		 SmartDBObject query = new SmartDBObject();
		 query.addSort("variationId", SortType.ASC);
		 return super.find(query, start, limit);
	}

}
