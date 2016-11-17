package com.omicseq.message;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;

import com.omicseq.common.MessageContainerName;
import com.omicseq.common.MessageTopic;
import com.omicseq.utils.ThreadUtils;

/**
 * @author Min.Wang
 * 
 */
public class MemMessageTool {

    private static MemMessageTool memMessageTool = new MemMessageTool();
    private ConcurrentMap<String, DefaultMemMessageContainer> containerMap = new ConcurrentHashMap<String, DefaultMemMessageContainer>();
    // first key is producer class name, the value is container name.
    private ConcurrentMap<String, String> containerNameMap = new ConcurrentHashMap<String, String>();

    public static MemMessageTool getInstance() {
        return memMessageTool;
    }

    public <T> void subscribe(MessageTopic messageTopic, Class<? extends IMessageProducer<T>> cls,
            IMessageConsumer<T> consumerClass) {
        subscribe(MessageContainerName.defaultContainer, messageTopic, cls.getName(), consumerClass);
    }

    public <T> void subscribe(MessageContainerName containerName, MessageTopic messageTopic, String producerClassName,
            IMessageConsumer<T> messageConsumerClass) {
        DefaultMemMessageContainer defaultMemMessageContainer = containerMap.get(containerName.name());
        if (defaultMemMessageContainer == null) {
            defaultMemMessageContainer = new DefaultMemMessageContainer();
            containerMap.put(containerName.name(), defaultMemMessageContainer);
            defaultMemMessageContainer.setName(containerName.name());
        }
        containerNameMap.put(producerClassName, containerName.name());
        defaultMemMessageContainer.subscribe(messageTopic, producerClassName, messageConsumerClass);
    }

    public void sendMessage(String className, Object t) {
        String containerName = containerNameMap.get(className);
        if (StringUtils.isNotBlank(containerName) && containerMap.containsKey(containerName)) {
            DefaultMemMessageContainer messageContainer = containerMap.get(containerName);
            messageContainer.produce(className, t);
        }
    }

    public Integer size(String className) {
        String containerName = containerNameMap.get(className);
        if (StringUtils.isNotBlank(containerName) && containerMap.containsKey(containerName)) {
            DefaultMemMessageContainer messageContainer = containerMap.get(containerName);
            return messageContainer.size(className);
        }
        return 0;
    }

    public void updateThreads(String className, Integer threads) {
        String containerName = containerNameMap.get(className);
        if (StringUtils.isNotBlank(containerName) && containerMap.containsKey(containerName)) {
            DefaultMemMessageContainer messageContainer = containerMap.get(containerName);
            if (messageContainer.updateThreads(threads)) {
                messageContainer.stop();
                ThreadUtils.sleep(1000);
                messageContainer.run();
            }
        }
    }

    public void runContainer() {
        Set<String> containerNameList = containerMap.keySet();
        for (String containerName : containerNameList) {
            containerMap.get(containerName).run();
        }
    }

    public void stop() {
        synchronized (MemMessageTool.class) {
            Set<String> containerNameList = containerMap.keySet();
            for (String containerName : containerNameList) {
                containerMap.get(containerName).stop();
            }
        }
    }

}
