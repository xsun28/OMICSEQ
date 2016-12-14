package com.omicseq.exception;

/**
 * The base exception of all exception in project omicseq
 * 
 * @author Min.Wang
 *
 */
public class OmicSeqException extends RuntimeException {

	private static final long serialVersionUID = -5365473166567994353L;

	public OmicSeqException(final String msg) {
		super(msg);
	}

	public OmicSeqException(final String msg, final Throwable ex) {
		super(msg, ex);
	}
	
	
}
