package com.omicseq.store.dao;

import java.util.List;

import com.omicseq.domain.VariationGene;

public interface IVariationGeneDAO extends IGenericDAO<VariationGene> {

	VariationGene getById(String key);

	List<VariationGene> loadGeneList(Integer start, Integer limit);

}
