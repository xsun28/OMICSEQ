package com.omicseq.message;

/**
 * @author Min.Wang
 *
 */
public interface IMessageProducer<T> {

	void produce(T t);
	
}
