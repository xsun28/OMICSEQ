package com.omicseq.exception;

public class StatisticException extends OmicSeqException {

	private static final long serialVersionUID = 5319938532965813451L;

	public StatisticException(final String msg) {
		super(msg);
	}

	public StatisticException(final String msg, final Throwable ex) {
		super(msg, ex);
	}
	
}
