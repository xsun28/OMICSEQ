package com.omicseq.exception;


/**
 * @author Min.Wang
 *
 */
public class DataAccessException extends OmicSeqException {

	private static final long serialVersionUID = 5319938532965813451L;

	public DataAccessException(final String msg) {
		super(msg);
	}

	public DataAccessException(final String msg, final Throwable ex) {
		super(msg, ex);
	}
	
}
