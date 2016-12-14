package com.omicseq.store.daoimpl.mongodb;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.omicseq.domain.SymbolSynonyms;
import com.omicseq.store.dao.ISynonymsDAO;
import com.omicseq.store.helper.MongodbHelper;

public class SynonymsDAO extends GenericMongoDBDAO<SymbolSynonyms> implements ISynonymsDAO {


	@SuppressWarnings("unchecked")
	@Override
	public List<SymbolSynonyms> fuzzyQuery(String feild, String query,
			Integer limit) {
		if (StringUtils.isBlank(feild)) {
			return Collections.EMPTY_LIST;
	    }
	    return super.find(MongodbHelper.startLike(feild, query), 0, limit);
	}
}
