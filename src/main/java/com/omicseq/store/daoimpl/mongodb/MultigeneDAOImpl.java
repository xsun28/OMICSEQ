package com.omicseq.store.daoimpl.mongodb;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.omicseq.domain.Multigene;
import com.omicseq.store.dao.IMultigeneDAO;
import com.omicseq.store.helper.MongodbHelper;

public class MultigeneDAOImpl extends GenericMongoDBDAO<Multigene> implements IMultigeneDAO {

	@Override
	public Integer getSequenceId(String source) {
		// TODO Auto-generated method stub
		return super.sequence(source, 1);
	}

	@Override
	public void updateMultigeneById(Integer multigeneId, Integer[] geneIds) {
		// TODO Auto-generated method stub
		String geneIdStr = "";
		for(int geneId : geneIds){
			geneIdStr += geneId + ",";
		}
		geneIdStr = geneIdStr.substring(0, geneIdStr.length()-2);
		SmartDBObject query = new SmartDBObject();
		Multigene multigene = findOne(query);
		multigene.setGeneIds(geneIdStr);
		this.update(multigene);
	}

	@Override
	public List<Multigene> fuzzyQuery(String field, String upperCase,
			int maxValue) {
		// TODO Auto-generated method stub
		if (StringUtils.isBlank(field)) {
			return Collections.EMPTY_LIST;
		}
		SmartDBObject query = MongodbHelper.and(MongodbHelper.startLike(field, upperCase), new SmartDBObject("status", 1));
		return super.find(query, 0, maxValue);
	}

	@Override
	public void updateStatus(Integer multigeneId, Short status, String remark) {
		// TODO Auto-generated method stub
		SmartDBObject b = new SmartDBObject("status",status);
		b.put("lastmodified",System.currentTimeMillis());
		super.update(new SmartDBObject("multigeneId", multigeneId), b, false);
	}

	@Override
	public Integer updateSearchTimes() {
		// TODO Auto-generated method stub
		return null;
	}



}
