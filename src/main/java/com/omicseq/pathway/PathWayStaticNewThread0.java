package com.omicseq.pathway;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.omicseq.common.SortType;
import com.omicseq.common.SourceType;
import com.omicseq.concurrent.IThreadTaskPoolsExecutor;
import com.omicseq.concurrent.ThreadTaskPoolsFactory;
import com.omicseq.concurrent.WaitFutureTask;
import com.omicseq.core.AbstractLifeCycle;
import com.omicseq.core.SampleCache;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.IPathWayDAO;
import com.omicseq.store.dao.IPathWaySampleDAO;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateTimeUtils;

public class PathWayStaticNewThread0 extends AbstractLifeCycle {
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class,"new");
	private static IPathWayDAO pathWayDao = DAOFactory.getDAO(IPathWayDAO.class);
	private static IPathWaySampleDAO pathWaySampleDao = DAOFactory.getDAO(IPathWaySampleDAO.class);
	protected static DBCollection  collection;
	protected static SampleCache sampleCahe = SampleCache.getInstance();
	protected static List<Integer> sampleIds;
	private static PathWayStaticNewThread0 single = new PathWayStaticNewThread0();
	protected int threads = 8;
	
	private PathWayStaticNewThread0() {
		
	}
	static {
		try {
			Mongo mongo = new Mongo("112.25.20.155", 27017);
			DB db = mongo.getDB("manage");
			db.authenticate("root", "seqjava".toCharArray());
			collection = db.getCollection("sampleOfGeneReadCount");
			
			sampleCahe.doInit();
			
			sampleIds = sampleCahe.getSampleIds();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		
//		List<PathWay> pathWayList = pathWayDao.find(new SmartDBObject("status", 0));
//		PathWayStaticNewThread psTh = new PathWayStaticNewThread();
//		for(PathWay pw : pathWayList) {
//			psTh.calculate(pw);
//		}
		
		single.refresh();
		
	}
	
	public void calculate(PathWay pw) {
		System.out.println(pw.getPathwayName());
		SmartDBObject query = new SmartDBObject("pathId", pw.getPathId());
		List<PathWaySample> pathwayList = pathWaySampleDao.find(query, 0, 20);
		if(pathwayList != null && pathwayList.size() > 0)
		{
			if(pathwayList.get(0).getB() < 0 || pathwayList.get(0).getRank() ==0)
			{
				createPathaySampleRanks(pw);
			}
		} else {
			createPathaySampleRanks(pw);
		}
	
	}
	
	private void createPathaySampleRanks(PathWay pw) {
		List<PathWaySample> psList = new ArrayList<PathWaySample>();
		List<Integer> pathGeneIdList = new ArrayList<Integer>();
		String geneIds = pw.getGeneIds();
		String[] geneIdArray = geneIds.split(",");
		for(int i=0; i<geneIdArray.length; i++)
		{
			if(geneIdArray[i] != null && !"null".equals(geneIdArray[i])) {
				pathGeneIdList.add(Integer.parseInt(geneIdArray[i]));
			}
		}
		for(Integer sampleId : sampleIds)
		{
			Sample sample = sampleCahe.get(sampleId);
			if(sample.getEtype() == 0 || sample.getSource() == SourceType.GEO.getValue())
			{
				continue;
			}
			String genRanks = null;
			SmartDBObject queryRank = new SmartDBObject("sampleId", sampleId);
			try {
				DBObject  object = collection.findOne(queryRank);
				if(object != null)
				{
					genRanks = object.get("geneId_count").toString();
				}else {
					continue;
				}
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			
			if(genRanks == null)
			{
				continue;
			}
			
			try {
				List<GeneRank> genRankList = new ArrayList<GeneRank>();
				String[] geneId_Rank = genRanks.split(",");
				String[] values;
				for(int i=0; i<geneId_Rank.length; i++)
				{
					GeneRank geneRank = new GeneRank();
					values = geneId_Rank[i].split("=");
					if("".equals(values[0]) || "".equals(values[1]) || "非数字".equals(values[1]))
					{
						continue;
					}
					Integer geneId = Integer.parseInt(values[0]);
					Double rank = Double.parseDouble(values[1]);

					geneRank.setGeneId(geneId);
					geneRank.setTssTesCount(rank);
					
					genRankList.add(geneRank);
				}
				
				double totalX = 0.0;
				double totalY = 0.0;
				int totalNum = genRankList.size();
				int pathNumIn = 0;
				for(GeneRank geneRank : genRankList) {
					totalX += Math.abs(geneRank.getTssTesCount());
					Integer geneId = geneRank.getGeneId();
					if(pathGeneIdList.contains(geneId))
					{
						totalY += Math.abs(geneRank.getTssTesCount());
						pathNumIn++;
					}
				}
				if(pathNumIn == 0 || totalX == 0)
				{
					continue;
				}
				double result = (totalY/totalX)*(totalNum/pathNumIn);
				if("非数字".equals(result))
				{
					continue;
				}
				PathWaySample ps = new PathWaySample();
				ps.setPathId(pw.getPathId());
				ps.setSampleId(sampleId);
				ps.setPathWayName(pw.getPathwayName());
				ps.setAvgA(totalY/totalX);
				ps.setB(result);
				ps.setSource(sample.getSource());
				ps.setEtype(sample.getEtype());
				psList.add(ps);
	          
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		
		//根据rank值排序
		Collections.sort(psList, new Comparator<PathWaySample>() {
			@Override
			public int compare(PathWaySample o1, PathWaySample o2) {
				if(null != o1.getB() && null != o2.getB())
				{
					return o1.getB().compareTo(o2.getB()) * (-1);
				}
				return 0;
			}
		});
		
		java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00000");
		
		for(PathWaySample ps : psList)
		{
			ps.setRank(Double.parseDouble(df.format((double)(psList.indexOf(ps)+1)/psList.size())));
		}
		
		pathWaySampleDao.delete(new SmartDBObject("pathId", pw.getPathId()));
		
		pathWaySampleDao.create(psList);
		
		pathWayDao.updateStatus(pw.getPathId(), (short)1);
		
		System.out.println("runned pathId: " + pw.getPathId() + " pathName: " + pw.getPathwayName());
	}

	public void refresh() {
        final IThreadTaskPoolsExecutor executor = ThreadTaskPoolsFactory.getThreadTaskPoolsExecutor();
        FutureTask<Object> task = new FutureTask<Object>(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                try {
                    start();
                    List<WaitFutureTask<Object>> tasks = buildTasks();
                    executor.blockRun(tasks, 100l, TimeUnit.DAYS);
                } catch (Exception e) {
                    logger.error("文件生成出错", e);
                } finally {
                    stop();
                }
                return Boolean.TRUE;
            }
        });
        executor.run(task);
    }
	
	protected List<WaitFutureTask<Object>> buildTasks() {
        List<WaitFutureTask<Object>> tasks = new ArrayList<WaitFutureTask<Object>>(1);
        Semaphore semaphore = new Semaphore(3);
        int size = 1;
        int batch = size <= threads ? 1 : size % threads == 0 ? size / threads : size / threads + 1;
        for (int i = 0; i < threads; i++) {
            int fromIndex = i * batch;
            int toIndex = fromIndex + batch;
            toIndex = size < toIndex ? size : toIndex;
            WaitFutureTask<Object> task = new WaitFutureTask<Object>(getCallable(), semaphore);
            tasks.add(task);
            if (toIndex >= size) {
                break;
            }
        }
        return tasks;
    }
	
	protected Callable<Object> getCallable() {
        return new CountCallable(new PathWayStaticNewThread0());
    }
	
	static abstract class BaseCallable<T, R> implements Callable<T> {
        protected R ref;

        public BaseCallable(R ref) {
            this.ref = ref;
        }
    }
	
	static class CountCallable extends BaseCallable<Object, PathWayStaticNewThread0> {
        public CountCallable(PathWayStaticNewThread0 ref) {
            super(ref);
        }

        @Override
        public Object call() throws Exception {
            DateTime dt = DateTime.now();
            try {
                ref.start();
                SmartDBObject query = new SmartDBObject();
                query.put("pathId", new SmartDBObject("$gte", 4102));
                query.addSort("pathId", SortType.ASC);
                List<PathWay> pathWayList = pathWayDao.find(query);
                int thread = 8;
                final BlockingQueue<PathWay> queue = new LinkedBlockingQueue<PathWay>(pathWayList);
                
                Semaphore semaphore = new Semaphore(thread);
                List<WaitFutureTask<Object>> tasks = new ArrayList<WaitFutureTask<Object>>(thread);
                Callable<Object> callable = new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        while (true) {
                        	PathWay pw = queue.poll();
                            DateTime dt = DateTime.now();
                            ref.calculate(pw);
                            if (ref.logger.isDebugEnabled()) {
                            	ref.logger.debug("runned pathway:{}, {}, use time {}", pw.getPathId(), pw.getPathwayName(), DateTimeUtils.used(dt));
                            }
                            if (queue.isEmpty()) {
                                break;
                            }
                        }
                        return null;
                    }
                };
                for (int i = 0; i < thread; i++) {
                    tasks.add(new WaitFutureTask<Object>(callable, semaphore));
                }
                ThreadTaskPoolsFactory.getThreadTaskPoolsExecutor().blockRun(tasks, 10l, TimeUnit.DAYS);
            
            } catch (Exception e) {
                ref.logger.error("生成Excel文件出错:", e);
            } finally {
                ref.stop();
                ref.logger.debug("生成Excel用时:{}", DateTimeUtils.used(dt));
            }
            return Boolean.TRUE;
        }
    }

}
