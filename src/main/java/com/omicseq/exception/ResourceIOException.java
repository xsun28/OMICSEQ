package com.omicseq.exception;

public class ResourceIOException extends OmicSeqException {

	private static final long serialVersionUID = -2015833792883538687L;

	public ResourceIOException(final String msg) {
		super(msg);
	}

	public ResourceIOException(final String msg, final Throwable ex) {
		super(msg, ex);
	}
	
}
