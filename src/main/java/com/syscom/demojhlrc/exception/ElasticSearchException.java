package com.syscom.demojhlrc.exception;

public class ElasticSearchException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ElasticSearchException(final String message) {
		super(message);
	}

	public ElasticSearchException(Exception exception) {
		super(exception);
	}

	public ElasticSearchException(final String message, Exception exception) {
		super(message, exception);
	}

}
