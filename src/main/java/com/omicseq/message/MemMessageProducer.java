package com.omicseq.message;


/**
 * @author Min.Wang
 *
 */
public class MemMessageProducer<T> implements IMessageProducer<T> {

	@Override
	public void produce(T t) {
		MemMessageTool.getInstance().sendMessage(getClass().getName(), t);
	}
}
