package com.omicseq.store.daoimpl.mongodb;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.mongodb.BasicDBObject;
import com.omicseq.pathway.PathWay;
import com.omicseq.store.dao.IPathWayDAO;
import com.omicseq.store.helper.MongodbHelper;

public class PathWayDAOImpl extends GenericMongoDBDAO<PathWay> implements IPathWayDAO {

	@Override
	public Integer getSequenceId(String source) {
		return super.sequence(source, 1);
	}

	@Override
	public void updatePathWayById(Integer pathId, Integer[] geneIds) {
		String geneIdStr = "";
		for(int i=0; i<geneIds.length; i++)
		{
			if(i != geneIds.length -1)
			{
				geneIdStr += geneIds[i] + ",";
			}
			
			else {
				geneIdStr += geneIds[i];
			}
		}
		SmartDBObject queryCondition=new SmartDBObject();
		queryCondition.put("pathId", pathId);
		PathWay pathWay = findOne(queryCondition);
		pathWay.setGeneIds(geneIdStr);
		this.update(pathWay);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PathWay> fuzzyQuery(String feild, String upperCase,
			int maxValue) {
		if (StringUtils.isBlank(feild)) {
			return Collections.EMPTY_LIST;
		}
		if(upperCase.contains("$")){
			upperCase = upperCase.replace("$", "\\$");
		}
		SmartDBObject query = MongodbHelper.and(MongodbHelper.startLike(feild, upperCase), new SmartDBObject("status", 1));
		return super.find(query, 0, maxValue);
	}

	@Override
	public void updateStatus(Integer pathId, Short status) {
		SmartDBObject b = new SmartDBObject("status",status);
		b.put("lastmodified",System.currentTimeMillis());
		super.update(new SmartDBObject("pathId", pathId), b, false);
	}

	public static void main(String[] args) {
		PathWayDAOImpl pathwayDao = new PathWayDAOImpl();
		pathwayDao.updateStatus(23, (short)1);
	}

	@Override
	public PathWay findOne(String pathwayName) {
		return super.findOne(new SmartDBObject("pathwayName", pathwayName));
	}

}
