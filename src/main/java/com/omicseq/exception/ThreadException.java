package com.omicseq.exception;

public class ThreadException  extends OmicSeqException {

	private static final long serialVersionUID = -2737778492029287L;

	public ThreadException(final String msg) {
		super(msg);
	}

	public ThreadException(final String msg, final Throwable ex) {
		super(msg, ex);
	}
	
}
