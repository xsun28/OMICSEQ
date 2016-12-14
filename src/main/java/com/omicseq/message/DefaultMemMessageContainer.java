package com.omicseq.message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.omicseq.common.MessageTopic;
import com.omicseq.concurrent.IThreadTaskPoolsExecutor;
import com.omicseq.concurrent.ThreadTaskPoolsFactory;
import com.omicseq.utils.ThreadUtils;

/**
 * @author Min.Wang
 * 
 */
public class DefaultMemMessageContainer {

    private ConcurrentMap<String, String> topicNameMap = new ConcurrentHashMap<String, String>();
    private ConcurrentMap<String, BlockingQueue<?>> queueMap = new ConcurrentHashMap<String, BlockingQueue<?>>();
    private ConcurrentMap<String, IMessageConsumer<?>> messageConumserMap = new ConcurrentHashMap<String, IMessageConsumer<?>>();
    private AtomicBoolean stopSignal = new AtomicBoolean(false);
    private AtomicBoolean startSignal = new AtomicBoolean(false);
    private List<String> topicList = new ArrayList<String>();
    private Integer queueSize = 20000;
    private Integer threadCount = 0;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public <T> void subscribe(MessageTopic messageTopic, String producerClassName, IMessageConsumer<?> messageConsumer) {
        messageConumserMap.put(messageTopic.name(), messageConsumer);
        BlockingQueue<?> queue = queueMap.get(messageTopic.name());
        if (queue == null) {
            queueMap.put(messageTopic.name(), new LinkedBlockingQueue<Object>(queueSize));
        }
        topicList.add(messageTopic.name());
        topicNameMap.put(producerClassName, messageTopic.name());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void produce(String className, Object t) {
        String messageTopic = topicNameMap.get(className);
        BlockingQueue messageQueue = queueMap.get(messageTopic);
        messageQueue.add(t);
    }

    public Integer size(String className) {
        String messageTopic = topicNameMap.get(className);
        BlockingQueue<?> messageQueue = queueMap.get(messageTopic);
        return null == messageQueue ? 0 : messageQueue.size();
    }

    public boolean updateThreads(Integer threads) {
        if (null != threads && threads.intValue() > 0 && threads.intValue() != this.threadCount) {
            this.threadCount = threads.intValue();
            return true;
        }
        return false;
    }

    public void run() {
        if (startSignal.get()) {
            return;
        }
        startSignal.compareAndSet(false, true);
        if (threadCount <= 0) {
            return;
        }
        IThreadTaskPoolsExecutor threadPoolsExecutor = ThreadTaskPoolsFactory.getThreadTaskPoolsExecutor();
        List<FutureTask<String>> taskList = new ArrayList<FutureTask<String>>();
        for (int i = 0; i < threadCount; i++) {
            FutureTask<String> consumMessageTask = new FutureTask<String>(new Runnable() {
                @SuppressWarnings({ "rawtypes", "unchecked" })
                @Override
                public void run() {
                    while (true) {
                        if (stopSignal.get()) {
                            break;
                        }
                        for (String messageTopic : topicList) {
                            BlockingQueue<?> blockingQueue = queueMap.get(messageTopic);
                            Object ob = blockingQueue.poll();
                            if (ob != null) {
                                IMessageConsumer messageConsumer = messageConumserMap.get(messageTopic);
                                messageConsumer.consume(ob);
                            }
                        }
                        ThreadUtils.sleep(500);
                    }
                }
            }, "");
            taskList.add(consumMessageTask);
        }
        threadPoolsExecutor.run("MessageContainer", taskList);
    }

    public void stop() {
        stopSignal.compareAndSet(true, false);
        startSignal.compareAndSet(true, false);
    }

}
