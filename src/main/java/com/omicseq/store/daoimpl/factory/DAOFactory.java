package com.omicseq.store.daoimpl.factory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;

import com.omicseq.store.dao.ICellDescDao;
import com.omicseq.store.dao.ICommentDAO;
import com.omicseq.store.dao.ICronTaskDAO;
import com.omicseq.store.dao.IExampleDAO;
import com.omicseq.store.dao.IFactorDescDao;
import com.omicseq.store.dao.IFileInfoDAO;
import com.omicseq.store.dao.IGeneDAO;
import com.omicseq.store.dao.IGeneIdMappingDAO;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.dao.IHashDBDAO;
import com.omicseq.store.dao.IHistoryResultDAO;
import com.omicseq.store.dao.IMultigeneDAO;
import com.omicseq.store.dao.IPathWayDAO;
import com.omicseq.store.dao.IPathWaySampleDAO;
import com.omicseq.store.dao.ISampleCountDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.dao.ISearchHistoryDAO;
import com.omicseq.store.dao.IStatisticInfoDAO;
import com.omicseq.store.dao.ISummaryTrackDataDao;
import com.omicseq.store.dao.ISynonymsDAO;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.dao.IUserDAO;
import com.omicseq.store.dao.IVariationGeneDAO;
import com.omicseq.store.dao.IVariationRankDAO;
import com.omicseq.store.dao.ImiRNADAO;
import com.omicseq.store.dao.ImiRNARankDAO;
import com.omicseq.store.dao.ImiRNASampleDAO;
import com.omicseq.store.daoimpl.mongodb.CellDescDaoImpl;
import com.omicseq.store.daoimpl.mongodb.CommentDAO;
import com.omicseq.store.daoimpl.mongodb.CronTaskDAO;
import com.omicseq.store.daoimpl.mongodb.ExampleDAOMongoDBImpl;
import com.omicseq.store.daoimpl.mongodb.FactorDescDaoImpl;
import com.omicseq.store.daoimpl.mongodb.FileInfoDAOImpl;
import com.omicseq.store.daoimpl.mongodb.GeneDAO;
import com.omicseq.store.daoimpl.mongodb.GeneIdMappingDAO;
import com.omicseq.store.daoimpl.mongodb.GeneRankDAOImpl;
import com.omicseq.store.daoimpl.mongodb.HashDBDAO;
import com.omicseq.store.daoimpl.mongodb.HistoryResultDAO;
import com.omicseq.store.daoimpl.mongodb.MiRNADAOImpl;
import com.omicseq.store.daoimpl.mongodb.MiRNASampleDAO;
import com.omicseq.store.daoimpl.mongodb.MiRankDAO;
import com.omicseq.store.daoimpl.mongodb.MultigeneDAOImpl;
import com.omicseq.store.daoimpl.mongodb.PathWayDAOImpl;
import com.omicseq.store.daoimpl.mongodb.PathWaySampleDAOImpl;
import com.omicseq.store.daoimpl.mongodb.SampleCountDAOImpl;
import com.omicseq.store.daoimpl.mongodb.SampleDAO;
import com.omicseq.store.daoimpl.mongodb.SearchHistoryDAO;
import com.omicseq.store.daoimpl.mongodb.StatisticInfoDAO;
import com.omicseq.store.daoimpl.mongodb.SummaryTrackDataDaoImpl;
import com.omicseq.store.daoimpl.mongodb.SynonymsDAO;
import com.omicseq.store.daoimpl.mongodb.TxrRefDAO;
import com.omicseq.store.daoimpl.mongodb.UserDAOImpl;
import com.omicseq.store.daoimpl.mongodb.VariationGeneDAOImpl;
import com.omicseq.store.daoimpl.mongodb.VariationRankDAOImpl;

/**
 * @author Min.Wang
 * 
 */
public class DAOFactory {

    private static volatile DAOFactory instance = new DAOFactory();

    @SuppressWarnings("rawtypes")
    private static Map<String, Class> classMap = new HashMap<String, Class>();
    private static ConcurrentMap<String, Object> instanceMap = new ConcurrentHashMap<String, Object>();
    private static ConcurrentMap<String, Object> tableTypeInstanceMap = new ConcurrentHashMap<String, Object>();

    private DAOFactory() {
    }

    public static DAOFactory getInstance() {
        return instance;
    }

    static {
        classMap.put(IUserDAO.class.getName(), UserDAOImpl.class);
        classMap.put(IFileInfoDAO.class.getName(), FileInfoDAOImpl.class);
        classMap.put(IGeneRankDAO.class.getName(), GeneRankDAOImpl.class);
        classMap.put(IExampleDAO.class.getName(), ExampleDAOMongoDBImpl.class);
        classMap.put(IGeneDAO.class.getName(), GeneDAO.class);
        classMap.put(ITxrRefDAO.class.getName(), TxrRefDAO.class);
        classMap.put(ISampleDAO.class.getName(), SampleDAO.class);
        classMap.put(IGeneIdMappingDAO.class.getName(), GeneIdMappingDAO.class);
        classMap.put(ISearchHistoryDAO.class.getName(), SearchHistoryDAO.class);
        classMap.put(IHistoryResultDAO.class.getName(), HistoryResultDAO.class);
        classMap.put(ISampleCountDAO.class.getName(), SampleCountDAOImpl.class);
        classMap.put(IHashDBDAO.class.getName(), HashDBDAO.class);
        classMap.put(IStatisticInfoDAO.class.getName(), StatisticInfoDAO.class);
        classMap.put(ICronTaskDAO.class.getName(), CronTaskDAO.class);
        classMap.put(IPathWayDAO.class.getName(), PathWayDAOImpl.class);
        classMap.put(IPathWaySampleDAO.class.getName(), PathWaySampleDAOImpl.class);
        classMap.put(IMultigeneDAO.class.getName(), MultigeneDAOImpl.class);
        classMap.put(IStatisticInfoDAO.class.getName(), StatisticInfoDAO.class);
        classMap.put(ICommentDAO.class.getName(), CommentDAO.class);
        classMap.put(IFactorDescDao.class.getName(), FactorDescDaoImpl.class);
        classMap.put(ICellDescDao.class.getName(), CellDescDaoImpl.class);
        classMap.put(ImiRNADAO.class.getName(), MiRNADAOImpl.class);
        classMap.put(ImiRNASampleDAO.class.getName(), MiRNASampleDAO.class);
        classMap.put(ImiRNARankDAO.class.getName(), MiRankDAO.class);
        classMap.put(ISummaryTrackDataDao.class.getName(), SummaryTrackDataDaoImpl.class);
        classMap.put(ISynonymsDAO.class.getName(), SynonymsDAO.class);
        classMap.put(IVariationGeneDAO.class.getName(), VariationGeneDAOImpl.class);
        classMap.put(IVariationRankDAO.class.getName(), VariationRankDAOImpl.class);
    }

    public static <T> T getDAO(Class<T> clz) {
        return getDAO(clz.getName(), clz);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getDAO(String name, Class<T> clz) {
        Object dao = instanceMap.get(clz);
        // to avoid lock the original name
        String lockName = ("daolock." + name).intern();
        if (dao == null) {
            // 注意点，对name进行锁，等于
            synchronized (lockName) {
                if (dao == null) {
                    Class<T> daoImplClass = classMap.get(name);
                    try {
                        dao = daoImplClass.newInstance();
                    } catch (InstantiationException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                    instanceMap.putIfAbsent(clz.getName(), dao);
                }
            }
        }
        return (T) dao;
    }

    public static <T> T getDAOByTableType(Class<T> clz, String tableType) {
        return getDAOByTableType(clz.getName(), clz, tableType);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getDAOByTableType(String name, Class<T> clz, String tableType) {
        if (StringUtils.isBlank(tableType)) {
            return getDAO(clz);
        }
        String key = (name + "-" + tableType).intern();
        Object dao = tableTypeInstanceMap.get(key);
        if (dao == null) {
            synchronized (key) {
                if (dao == null) {
                    Class<T> daoImplClass = classMap.get(name);
                    try {
                        dao = daoImplClass.newInstance();
                        Method setTableType = dao.getClass().getMethod("setTableType", String.class);
                        setTableType.invoke(dao, tableType);
                    } catch (InstantiationException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    tableTypeInstanceMap.put(key, dao);
                }
            }
        }
        return (T) dao;
    }
}
