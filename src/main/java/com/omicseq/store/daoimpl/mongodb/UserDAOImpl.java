package com.omicseq.store.daoimpl.mongodb;

import java.util.List;

import com.omicseq.domain.User;
import com.omicseq.store.dao.IUserDAO;

public class UserDAOImpl extends GenericMongoDBDAO<User> implements IUserDAO {

    public List<User> findAll() {
        List<User> users = super.findAll();
        return users;
    }

    public User findByUserName(String userName) {
        SmartDBObject dbObject = new SmartDBObject();
        dbObject.put("name", userName);
        return super.findOne(dbObject);
    }
    
    @Override
    public void create(User user) {
        user.setUserId(sequence());
        super.create(user);
    }

}
