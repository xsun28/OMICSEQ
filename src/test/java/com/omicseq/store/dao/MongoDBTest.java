package com.omicseq.store.dao;

import com.omicseq.domain.User;
import com.omicseq.store.daoimpl.factory.DAOFactory;

public class MongoDBTest {

	
	public static void main(String[] args) {
		IUserDAO userDAO = DAOFactory.getInstance().getDAO(IUserDAO.class);
		User user = new User();
		user.setName("duzj5");
		user.setPassword("111111");
		userDAO.create(user);
		System.out.println(user.get_id());
	}

	
	
}
