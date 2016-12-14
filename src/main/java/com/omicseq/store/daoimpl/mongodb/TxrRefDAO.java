package com.omicseq.store.daoimpl.mongodb;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import com.omicseq.common.SortType;
import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.helper.MongodbHelper;

public class TxrRefDAO extends GenericMongoDBDAO<TxrRef> implements ITxrRefDAO {

	@SuppressWarnings("unchecked")
    @Override
	public List<TxrRef> fuzzyQuery(String feild, String query, Integer limit) {
		if (StringUtils.isBlank(feild)) {
			return Collections.EMPTY_LIST;
		}
		return super.find(MongodbHelper.startLike(feild, query), 0, limit);
	}

	@Override
	public List<TxrRef> loadTxrRefList(Integer start, Integer limit) {
		SmartDBObject smartDBObject = new SmartDBObject();
		smartDBObject.put("refseq", MongodbHelper.ne(null));
//		smartDBObject.put("geneSymbol", "ANKRD30BL");
		smartDBObject.addSort("_id", SortType.ASC);
		return super.find(smartDBObject, start, limit);
	}

    @Override
    public List<TxrRef> findByGeneSymbol(String geneSymbol) {
        return super.find(new SmartDBObject("geneSymbol",geneSymbol));
    }

    @Override
    public void delete(List<TxrRef> list) {
        for (TxrRef ref : list) {
            super.delete(new SmartDBObject("_id", new ObjectId(ref.get_id())));
        }
    }
	
	
}
