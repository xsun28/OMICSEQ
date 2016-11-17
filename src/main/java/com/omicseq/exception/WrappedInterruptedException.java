package com.omicseq.exception;

@SuppressWarnings("serial")
public class WrappedInterruptedException extends RuntimeException {

	public WrappedInterruptedException(InterruptedException e) {
		super(e);
		// TODO Auto-generated constructor stub
	}
	
	public WrappedInterruptedException(final String msg, final Throwable ex) {
		super(msg, ex);
	}

}
