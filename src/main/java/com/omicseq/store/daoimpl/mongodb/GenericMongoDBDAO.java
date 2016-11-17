package com.omicseq.store.daoimpl.mongodb;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bson.BSON;
import org.bson.BSONObject;
import org.bson.types.BSONTimestamp;
import org.bson.types.Binary;
import org.bson.types.Code;
import org.bson.types.CodeWScope;
import org.bson.types.MaxKey;
import org.bson.types.MinKey;
import org.bson.types.ObjectId;
import org.bson.types.Symbol;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRefBase;
import com.omicseq.annotation.NonPersistent;
import com.omicseq.common.DBGroupName;
import com.omicseq.common.DBName;
import com.omicseq.common.SortType;
import com.omicseq.configuration.TableGroupMapping;
import com.omicseq.domain.BaseDomain;
import com.omicseq.exception.DataAccessException;
import com.omicseq.store.dbgroup.TableGroupMappingCache;
import com.omicseq.store.helper.MongodbHelper;
import com.omicseq.utils.DateTimeUtils;

public abstract class GenericMongoDBDAO<T> {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    private final Class<T> clz;
    private String tableType;

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public String getTableName() {
        String name = clz.getName();
        String[] array = name.split("\\.");
        String originalName = array[array.length - 1];
        if (StringUtils.isNoneBlank(this.tableType)) {
            return originalName + this.tableType;
        } else {
            return originalName;
        }
    }

    @SuppressWarnings("unchecked")
    public GenericMongoDBDAO() {
        clz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public void create(T obj) {
        List<T> list = new ArrayList<T>(1);
        list.add(obj);
        create(list);
    }

    public void update(T obj) {
        SmartDBObject dbObject = convertToDBObject(obj);
        if (logger.isDebugEnabled()) {
            logger.debug("table name :{};update {} , {} ", getTableName(), obj, dbObject);
        }
        getCollection().save(dbObject);
    }

    public void create(List<T> list) {
        DateTime dt = DateTime.now();
        doCreate(list);
        if (logger.isDebugEnabled()) {
            String used = DateTimeUtils.diff(dt, DateTime.now());
            logger.debug("create:{}, write {} records,used {} ", getTableName(), list.size(), used);
        }
    }

    public <V> List<V> find(SmartDBObject query, Class<V> cls) {
        return this.find(query, null, null, cls);
    }

    public List<T> find(SmartDBObject query) {
        return this.find(query, null, null, clz);
    }

    public List<T> find(SmartDBObject query, Integer start, Integer limit) {
        return find(query, start, limit, clz);
    }

    public <V> List<V> find(SmartDBObject query, Integer start, Integer limit, Class<V> cls) {
        DateTime dt = DateTime.now();
        if (query == null) {
            query = new SmartDBObject();
        }
        try {
            if (logger.isDebugEnabled()) {
                if (null != limit) {
                    logger.debug("table name:{};query:{};start:{},limit:{}", getTableName(), query, start, limit);
                } else {
                    logger.debug("table name:{};query:{}", getTableName(), query);
                }
            }
            DBCursor dbCursor = buildDBCursor(query, start, limit);
            List<V> list = new ArrayList<V>();
            while (dbCursor.hasNext()) {
                DBObject obj = dbCursor.next();
                list.add(convertFromDBObject(obj, cls));
            }
            if (logger.isDebugEnabled()) {
                String used = DateTimeUtils.diff(dt, DateTime.now());
                logger.debug("table name:{};query {} records ;used {}", getTableName(), list.size(), used);
            }
            return list;
        } catch (Exception e) {
            throw new DataAccessException("Failed to convert " + clz.getName(), e);
        }

    }

    /**
     * @param query
     * @param start
     * @param limit
     * @param coll
     * @return
     */
    private DBCursor buildDBCursor(SmartDBObject query, Integer start, Integer limit) {
        DBCollection coll = getCollection();
        DBCursor dbCursor = null;
        if (CollectionUtils.isEmpty(query.getReturnFieldList())) {
            dbCursor = coll.find(query);
        } else {
            SmartDBObject columnObject = new SmartDBObject();
            for (String column : query.getReturnFieldList()) {
                columnObject.put(column, 1);
            }
            dbCursor = coll.find(query, columnObject);
        }
        if (CollectionUtils.isNotEmpty(query.getSortList())) {
            SmartDBObject orderBy = new SmartDBObject();
            for (DBObject sortObject : query.getSortList()) {
                for (String sortKey : sortObject.keySet()) {
                	orderBy.put(sortKey, sortObject.get(sortKey));
                }
            }
            dbCursor.sort(orderBy);
        }
        if (start != null) {
            dbCursor.skip(start);
        } else {
            dbCursor.skip(0);
        }
        if (limit != null) {
            dbCursor.limit(limit);
        }
        return dbCursor;
    }

    public List<T> findAll() {
        return this.find(null);
    }

    /**
     * @param obj
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    protected SmartDBObject convertToDBObject(Object obj) {
        Field[] fields = obj.getClass().getDeclaredFields();
        Field[] parentFields = obj.getClass().getSuperclass().getDeclaredFields();
        SmartDBObject dbObject = new SmartDBObject();
        Method[] methods = obj.getClass().getMethods();
        Method[] parentMethods = obj.getClass().getSuperclass().getMethods();

        Field[] mergedFields = (Field[]) ArrayUtils.addAll(fields, parentFields);
        Method[] mergedMethods = (Method[]) ArrayUtils.addAll(methods, parentMethods);

        for (Field field : mergedFields) {
            Method getter = getGetter(mergedMethods, field);
            if (getter != null && !field.isAnnotationPresent(NonPersistent.class)) {
                try {
                    Object value = getter.invoke(obj);
                    if ("_id".equals(field.getName())) {
                        if (value != null && StringUtils.isNotBlank(value.toString())) {
                            dbObject.put("_id", new ObjectId(value.toString()));
                        }
                    } else if (isList(value)) {
                        @SuppressWarnings("unchecked")
                        List<?> list = (List<Object>) value;
                        Object[] objs = new Object[list.size()];
                        for (int i = 0; i < list.size(); i++) {
                            Object _obj = list.get(i);
                            objs[i] = isSimpleField(null, _obj) ? _obj : convertToDBObject(_obj);
                        }
                        dbObject.put(field.getName(), objs);
                    } else if (isSimpleField(field, value)) {
                        dbObject.put(field.getName(), value);
                    } else {
                        dbObject.put(field.getName(), convertToDBObject(value));
                    }
                } catch (Exception e) {
                    throw new DataAccessException("Error in invoking getter of Field [" + field.getName()
                            + "] of class [" + clz.getName() + "]", e);
                }

            }
        }
        return dbObject;
    }

    /**
     * 字符，数字，日期类型
     * 
     * @param clazz
     * @return
     */
    private static boolean isSimpleClass(Class<? extends Object> clazz) {
        return CharSequence.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz)
                || Date.class.isAssignableFrom(clazz);
    }

    /**
     * 对于关联集合特殊处理
     * 
     * @param val
     * @return
     */
    private boolean isList(Object value) {
        if (null == value) {
            return false;
        }
        return value instanceof List<?>;
    }

    /**
     * @param field
     * @param val
     * @return
     */
    private boolean isSimpleField(Field field, Object val) {
        if (val == null) {
            return true;
        }
        return (val instanceof Date || val instanceof Number || val instanceof Character || val instanceof String
                || val instanceof ObjectId || val instanceof BSONObject || val instanceof Boolean
                || val instanceof Pattern || val instanceof Map || val instanceof Iterable || val instanceof byte[]
                || val instanceof Binary || val instanceof UUID || val.getClass().isArray() || val instanceof Symbol
                || val instanceof BSONTimestamp || val instanceof CodeWScope || val instanceof Code
                || val instanceof DBRefBase || val instanceof MinKey || val instanceof MaxKey);
    }

    private void doCreate(List<T> objs) {
        List<DBObject> list = new ArrayList<DBObject>();
        for (T obj : objs) {
            list.add(convertToDBObject(obj));
        }
        getCollection().insert(list);
        for (int i = 0; i < list.size(); i++) {
            DBObject db = list.get(i);
            T t = objs.get(i);
            if (t instanceof BaseDomain) {
                BaseDomain domain = (BaseDomain) t;
                Object _id = db.get("_id");
                if (_id instanceof ObjectId) {
                    ObjectId pk = (ObjectId) _id;
                    domain.set_id(pk.toString());
                }
            }
        }
    }

    private Method getGetter(Method[] methods, Field field) {
        for (Method m : methods) {
            String mName = m.getName().toLowerCase();
            String fName = field.getName().toLowerCase();
            if (mName.equals("get" + fName) || mName.equals("is" + fName)) {
                return m;
            }
        }
        return null;
    }

    private Method getSetter(Method[] methods, Field field) {
        for (Method m : methods) {
            String mName = m.getName().toLowerCase();
            String fName = field.getName().toLowerCase();
            if (mName.equals("set" + fName)) {
                return m;
            }
        }
        return null;
    }

    protected int count(SmartDBObject dbObject) {
        return (int) getCollection().count(dbObject);
    }

    public T findOne(SmartDBObject dbObject) {
        if (logger.isDebugEnabled()) {
            logger.debug("table name :{};findOne:{};", getTableName(), dbObject);
        }
        DBObject resultObject = getCollection().findOne(dbObject);
        return convertFromDBObject(resultObject, clz);
    }

    /**
     * 自增长序列表实现,默认以表名作为id
     * 
     * @return
     */
    protected Integer sequence() {
        return sequence(null, 1);
    }

    /**
     * 支持添加后缀及默认值
     * 
     * @param suffix
     * @param def
     * @return
     */
    protected Integer sequence(String suffix, Integer def) {
        MongoDBManager instance = MongoDBManager.getInstance();
        DBCollection coll = instance.getCollection(DBGroupName.manage.name(), DBName.manage.name(), "sequence");
        String seqName = getTableName().toLowerCase();
        if (StringUtils.isNoneBlank(suffix)) {
            seqName = String.format("%s_%s", seqName, suffix.trim().toLowerCase());
        }
        DBObject query = new SmartDBObject("_id", seqName);
        String field = "seq";
        DBObject update = MongodbHelper.inc(new SmartDBObject(field, 1));
        DBObject obj = coll.findAndModify(query, null, null, false, update, true, false);
        if (null == obj) {
            query = new SmartDBObject("_id", seqName);
            update = MongodbHelper.inc(new SmartDBObject(field, def == 0 ? 1 : def));
            obj = coll.findAndModify(query, null, null, false, update, true, true);
        }
        return BSON.toInt(obj.get(field));
    }

    private DBCollection getCollection() {
        String tableName = getTableName();
        TableGroupMapping tableGroupMapping = TableGroupMappingCache.getInstance().getTableGroupMapping(
                tableName.toLowerCase());
        Validate.notNull(tableGroupMapping);
        return MongoDBManager.getInstance().getCollection(tableGroupMapping.getGroupName().toLowerCase(),
                tableGroupMapping.getDbName().toLowerCase(), getTableName().toLowerCase());
    }

    @SuppressWarnings("unchecked")
    private <V> V convertFromDBObject(DBObject dbObject, Class<V> clazz) {
        if (dbObject == null) {
            return null;
        }
        V t = null;
        String[] keys = dbObject.keySet().toArray(new String[] {});
        if (isSimpleClass(clazz)) {
            for (String key : keys) {
                if ("_id".equalsIgnoreCase(key)) {
                    continue;
                }
                return (V) dbObject.get(key);
            }
        }
        try {
            t = clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        Field[] selfFields = clazz.getDeclaredFields();
        Field[] parentFields = clazz.getSuperclass().getDeclaredFields();
        Method[] selfMethods = clazz.getMethods();
        Method[] parentMethods = clazz.getSuperclass().getMethods();

        Field[] fields = (Field[]) ArrayUtils.addAll(selfFields, parentFields);
        Method[] methods = (Method[]) ArrayUtils.addAll(selfMethods, parentMethods);

        for (String key : keys) {
            for (Field field : fields) {
                if (!needSetValue(key, field, methods)) {
                    continue;
                }

                Method setter = getSetter(methods, field);
                Object val = dbObject.get(key);
                try {
                    if (val == null) {
                        setter.invoke(t, val);
                        continue;
                    }

                    if (val instanceof ObjectId) {
                        if ("_id".equals(field.getName())) {
                            setter.invoke(t, ((ObjectId) val).toString());
                        }
                    } else if (field.getType().isAssignableFrom(List.class) && val instanceof BasicDBList) {
                        BasicDBList dbs = (BasicDBList) val;
                        if (0 == dbs.size()) {
                            continue;
                        }
                        ParameterizedType type = (ParameterizedType) field.getGenericType();
                        @SuppressWarnings("rawtypes")
                        Class<?> cls = (Class) type.getActualTypeArguments()[0];
                        List<Object> list = new ArrayList<Object>(dbs.size());
                        for (Iterator<Object> iterator = dbs.iterator(); iterator.hasNext();) {
                            Object obj = iterator.next();
                            if (obj instanceof DBObject) {
                                list.add(convertFromDBObject((DBObject) obj, cls));
                            }
                        }
                        setter.invoke(t, list);
                    } else if (val instanceof DBObject) {
                        setterOfDBObject(dbObject, clazz, t, key, field, setter, val);
                    } else {
                        Object convertedVal = null;
                        if (val instanceof Number) {
                            convertedVal = convertToNumber(field, val);
                        } else if (val instanceof Timestamp) {
                            convertedVal = Timestamp.valueOf(val.toString());
                        } else {
                            convertedVal = val;
                        }
                        setter.invoke(t, convertedVal);
                    }
                    break;
                } catch (Exception e) {
                    throw new DataAccessException("Error in invoking getter of Field [" + field.getName()
                            + "] of class [" + clz.getName() + "]" + ", value in DB is [" + val + "]", e);
                }
            }
        }
        return t;
    }

    /**
     * @param field
     * @param val
     * @param convertedVal
     * @return
     */
    private Object convertToNumber(Field field, Object val) {
        Object convertedVal = null;
        if (field.getType().isAssignableFrom(Long.class) || field.getType() == long.class) {
            convertedVal = ((Number) val).longValue();
        } else if (field.getType().isAssignableFrom(Double.class) || field.getType() == double.class) {
            convertedVal = ((Number) val).doubleValue();
        } else if (field.getType().isAssignableFrom(Float.class) || field.getType() == float.class) {
            convertedVal = ((Number) val).floatValue();
        } else if (field.getType().isAssignableFrom(Integer.class) || field.getType() == int.class) {
            convertedVal = ((Number) val).intValue();
        } else if (field.getType().isAssignableFrom(Byte.class) || field.getType() == byte.class) {
            convertedVal = ((Number) val).byteValue();
        } else if (field.getType().isAssignableFrom(Short.class) || field.getType() == short.class) {
            convertedVal = ((Number) val).shortValue();
        }
        return convertedVal;
    }

    /**
     * @param dbObject
     * @param clazz
     * @param t
     * @param key
     * @param field
     * @param setter
     * @param val
     * @throws InvocationTargetException
     */
    private void setterOfDBObject(DBObject dbObject, Class<? extends Object> clazz, Object t, String key, Field field,
            Method setter, Object val) throws InvocationTargetException {
        Class<? extends Object> fieldClassType = field.getType();
        if (fieldClassType.isAssignableFrom(BasicDBObject.class)) {
            try {
                setter.invoke(t, dbObject.get(key));
            } catch (IllegalAccessException e) {
                throw new DataAccessException("Failed to convert " + clazz.getClass().getSimpleName() + ", key = "
                        + key + ", value = " + val + ", setter = " + setter.getName(), e);
            }
        } else {
            Object dob = dbObject.get(key);
            if (dob != null) {
                try {
                    setter.invoke(t, convertFromDBObject((DBObject) dob, field.getType()));
                } catch (IllegalAccessException e) {
                    throw new DataAccessException("Failed to convert " + clazz.getClass().getSimpleName() + ", key = "
                            + key + ", value = " + val + ", setter = " + setter.getName(), e);
                }
            }
        }
    }

    /**
     * @param key
     * @param field
     * @return
     */
    private boolean needSetValue(String key, Field field, Method[] methods) {
        /*
         * if (field.isAnnotationPresent(NonPersistent.class)) { //
         * 如果标记了NonPersistent，则不转换 return false; }
         */
        if (!field.getName().toLowerCase().equals(key.toLowerCase())) { // 如果没有匹配的字段，则不转换
            return false;
        }

        Method setter = getSetter(methods, field);
        if (setter == null) { // 如果没有setter方法，则不转换
            return false;
        }

        return true;
    }

    protected void update(SmartDBObject query, SmartDBObject update) {
        this.update(query, update, false);
    }

    protected void increase(SmartDBObject query, SmartDBObject update) {
        getCollection().update(query, new BasicDBObject("$inc", update), false, true);
    }

    protected void update(SmartDBObject query, SmartDBObject update, boolean createIfNotExists) {
        getCollection().update(query, new BasicDBObject("$set", update), createIfNotExists, true);
    }

    public void delete(SmartDBObject query) {
        DateTime dt = DateTime.now();
        getCollection().remove(query);
        if (logger.isDebugEnabled()) {
            logger.debug("delete:{}, query:{},used {} ", getTableName(), query, DateTimeUtils.diff(dt, DateTime.now()));
        }
    }

    protected SmartDBObject inc(Object value) {
        return new SmartDBObject("$inc", value);
    }

    protected T max(String key, SmartDBObject query) {
        if (query == null) {
            query = new SmartDBObject();
        }
        query.addSort(key, SortType.DESC);
        return findOne(query);
    }

    protected T min(String key, SmartDBObject query) {
        if (query == null) {
            query = new SmartDBObject();
        }
        query.addSort(key, SortType.ASC);
        return findOne(query);
    }

    /*
     * protected int sum(String key, SmartDBObject query) { query =
     * nullCheck(query);
     * 
     * String reduce = "function(doc, prev) {prev._sum += doc." + key + ";}";
     * DBObject dbObject = getCollection().group(new SmartDBObject("_sum",
     * "sum(" + key + ")"), new SmartDBObject(), new SmartDBObject(), reduce);
     * System.out.println(dbObject); BasicDBList list = (BasicDBList) dbObject;
     * 
     * DBObject o = (DBObject) list.get(0); return
     * Integer.valueOf(o.get("_sum").toString()); }
     */

    //
    /*
     * private SmartDBObject nullCheck(SmartDBObject query) { return query ==
     * null ? new SmartDBObject() : query; }
     */

}
