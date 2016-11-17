package com.omicseq.store.dao;

import java.util.List;

import com.omicseq.domain.Comment;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public interface ICommentDAO {
	
	void create(Comment comment);
	
	List<Comment> findBySampleId(Integer sampleId);
	
	Integer getSequenceId(String sampleName);
	
	List<Comment> find(SmartDBObject query);
}
