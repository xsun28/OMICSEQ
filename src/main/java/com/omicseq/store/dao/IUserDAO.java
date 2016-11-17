package com.omicseq.store.dao;

import java.util.List;

import com.omicseq.domain.User;

/**
 * @author Min.Wang
 *
 */
public interface IUserDAO {

	void create(User user);
	
	List<User> findAll();
	
	User findByUserName(String userName);

    void update(User dbUser);
}
