package com.omicseq.store.daoimpl.mongodb;

import java.util.List;

import com.omicseq.domain.Comment;
import com.omicseq.store.dao.ICommentDAO;

public class CommentDAO extends GenericMongoDBDAO<Comment> implements ICommentDAO{

	@Override
	public void create(Comment comment) {
		super.create(comment);
		
	}

	@Override
	public List<Comment> findBySampleId(Integer sampleId) {
		return super.find(new SmartDBObject("sampleId",sampleId));
	}

	@Override
	public Integer getSequenceId(String sampleName) {
		return super.sequence(sampleName, 1);
	}
	
	@Override
	public List<Comment> find(SmartDBObject query){
		return super.find(query);
	}
}
