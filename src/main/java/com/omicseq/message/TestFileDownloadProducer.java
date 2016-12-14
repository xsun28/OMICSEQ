package com.omicseq.message;

import com.omicseq.common.MessageContainerName;
import com.omicseq.common.MessageTopic;
import com.omicseq.concurrent.ThreadTaskPoolsFactory;
import com.omicseq.domain.Chunk;

/**
 * @author Min.Wang
 * 
 */
public class TestFileDownloadProducer<T> extends MemMessageProducer<T> {

    @Override
    public void produce(T t) {
        super.produce(t);
    }

    public static void main(String[] args) {
        TestStatsConsumer testStatsConsumer = new TestStatsConsumer();
        TestFileDownloadProducer<Chunk> testFileDownloadProducer = new TestFileDownloadProducer<Chunk>();

        ThreadTaskPoolsFactory.getThreadTaskPoolsExecutor().init();
        MemMessageTool.getInstance().subscribe(MessageContainerName.defaultContainer, MessageTopic.down,
                TestFileDownloadProducer.class.getName(), testStatsConsumer);
        MemMessageTool.getInstance().runContainer();
        Chunk chunk = new Chunk(10L, 12L);
        testFileDownloadProducer.produce(chunk);

        Chunk chunk2 = new Chunk(13L, 14L);
        testFileDownloadProducer.produce(chunk2);
        System.out.println("size is : ");
    }

}
