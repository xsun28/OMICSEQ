package com.omicseq.store.helper;

import java.util.Arrays;
import java.util.List;

import com.mongodb.BasicDBList;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

/**
 * @author Min.Wang
 * 
 */
public class MongodbHelper {

    /**
     * >
     * 
     * @param val
     * @return
     */
    public static SmartDBObject gt(Object val) {
        return new SmartDBObject("$gt", val);
    }

    public static SmartDBObject gt(String key, Object val) {
        return new SmartDBObject(key, new SmartDBObject("$gt", val));
    }

    public static SmartDBObject gte(String key, Object val) {
        return new SmartDBObject(key, new SmartDBObject("$gte", val));
    }

    /**
     * >=
     * 
     * @param val
     * @return
     */
    public static SmartDBObject gte(Object val) {
        return new SmartDBObject("$gte", val);
    }

    /**
     * <
     * 
     * @param val
     * @return
     */
    public static SmartDBObject lt(Object val) {
        return new SmartDBObject("$lt", val);
    }

    public static SmartDBObject lt(String key, Object val) {
        return new SmartDBObject(key, new SmartDBObject("$lt", val));
    }

    /**
     * <=
     * 
     * @param val
     * @return
     */
    public static SmartDBObject lte(Object val) {
        return new SmartDBObject("$lte", val);
    }

    public static SmartDBObject lte(String key, Object val) {
        return new SmartDBObject(key, new SmartDBObject("$lte", val));
    }

    /**
     * !=
     * 
     * @param val
     * @return
     */
    public static SmartDBObject ne(Object val) {
        return new SmartDBObject("$ne", val);
    }

    public static SmartDBObject ne(String key, Object val) {
        return new SmartDBObject(key, new SmartDBObject("$ne", val));
    }

    /**
     * construct and object
     * 
     * @param smartDBObjects
     * @return
     */
    public static SmartDBObject and(SmartDBObject... smartDBObjects) {
        SmartDBObject andDBObject = new SmartDBObject();
        BasicDBList list = new BasicDBList();
        for (SmartDBObject obj : smartDBObjects) {
            list.add(obj);
        }
        andDBObject.put("$and", list);
        return andDBObject;
    }

    /**
     * construct or object.
     * 
     * @param smartDBObjects
     * @return
     */
    public static SmartDBObject or(SmartDBObject... smartDBObjects) {
        SmartDBObject orDBObject = new SmartDBObject();
        BasicDBList list = new BasicDBList();
        for (SmartDBObject obj : smartDBObjects) {
            list.add(obj);
        }
        orDBObject.put("$or", list);
        return orDBObject;
    }

    /**
     * put and condition
     * 
     * @param parentDBObject
     * @param smartDBObjects
     */
    public static void putAnd(SmartDBObject parentDBObject, SmartDBObject... smartDBObjects) {
        BasicDBList list = (BasicDBList) (parentDBObject.get("$and") == null ? new BasicDBList() : parentDBObject
                .get("$and"));
        for (SmartDBObject obj : smartDBObjects) {
            list.add(obj);
        }
        parentDBObject.put("$and", list);
    }

    public static void putOr(SmartDBObject parentDBObject, SmartDBObject... smartDBObjects) {
        BasicDBList list = (BasicDBList) (parentDBObject.get("$or") == null ? new BasicDBList() : parentDBObject
                .get("$or"));
        for (SmartDBObject obj : smartDBObjects) {
            list.add(obj);
        }
        parentDBObject.put("$or", list);
    }

    public static SmartDBObject inc(Object value) {
        return new SmartDBObject("$inc", value);
    }
    public static SmartDBObject in(Object... values) {
        return in(Arrays.asList(values));
    }
    public static SmartDBObject in(List<?> values) {
        BasicDBList list = new BasicDBList();
        for (Object obj : values) {
            list.add(obj);
        }
        return new SmartDBObject("$in", list);
    }

    public static SmartDBObject in(String key, Object... values) {
        List<Object> coll = Arrays.asList(values);
        return in(key, coll);
    }

    public static SmartDBObject in(String key, List<Object> values) {
        SmartDBObject dbObject = new SmartDBObject();
        dbObject.put(key, in(values));
        return dbObject;
    }

    public static void putIn(SmartDBObject parentDBObject, String key, Object... values) {
        SmartDBObject keyObject = (SmartDBObject) parentDBObject.get(key);
        BasicDBList list = new BasicDBList();
        if (keyObject == null) {
            keyObject = new SmartDBObject("$in", list);
            parentDBObject.put(key, keyObject);
        } else {
            if (keyObject.get("$in") == null) {
                keyObject.put("$in", list);
            } else {
                list = (BasicDBList) keyObject.get("$in");
            }
        }

        for (Object obj : values) {
            list.add(obj);
        }
    }

    /**
     * like '%value%'
     * 
     * @param key
     * @param value
     * @return
     */
    public static SmartDBObject like(String key, String value) {
        SmartDBObject q = new SmartDBObject();
        q.put(key, java.util.regex.Pattern.compile(value));
        return q;
    }

    /**
     * like 'value%'
     * 
     * @param key
     * @param value
     * @return
     */
    public static SmartDBObject endLike(String key, String value) {
        SmartDBObject q = new SmartDBObject();
        value = value + "$";
        q.put(key, java.util.regex.Pattern.compile(value));
        return q;
    }

    /**
     * like '%value'
     * 
     * @param key
     * @param value
     * @return
     */
    public static SmartDBObject startLike(String key, String value) {
        SmartDBObject q = new SmartDBObject();
        value = "^" + value;
        q.put(key, java.util.regex.Pattern.compile(value));
        return q;
    }

}
