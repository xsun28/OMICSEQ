package com.omicseq.store.daoimpl.mongodb;

import java.util.List;

import com.omicseq.domain.Example;
import com.omicseq.store.dao.IExampleDAO;
import com.omicseq.store.helper.MongodbHelper;

/**
 * @author Min.Wang
 * 
 */
public class ExampleDAOMongoDBImpl extends GenericMongoDBDAO<Example> implements IExampleDAO {

    // select * from example where type=1 or (age > 4 and type>7);
    @Override
    public List<Example> complexFind1() {
        SmartDBObject secondDBObject = MongodbHelper.and(MongodbHelper.gt("age", 97), MongodbHelper.gt("type", 7));
        return super.find(MongodbHelper.or(new SmartDBObject("type", 1), secondDBObject));
    }

    public List<Example> inFind() {
        return super.find(MongodbHelper.in("type", 1, 2, 3));
    }

    @Override
    public List<Example> complexFind2() {
        // TODO Auto-generated method stub
        return super.find(MongodbHelper.endLike("name", "wang98"));
    }
    
    public List<Example> loadExamples() {
    	SmartDBObject queryObject = new SmartDBObject();
    	return super.find(queryObject, 0, 20);
    }
    
}
