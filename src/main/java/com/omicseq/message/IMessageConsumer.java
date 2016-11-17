package com.omicseq.message;


/**
 * @author Min.Wang
 *
 */
public interface IMessageConsumer<T> {

	void consume(T t);

	
}
