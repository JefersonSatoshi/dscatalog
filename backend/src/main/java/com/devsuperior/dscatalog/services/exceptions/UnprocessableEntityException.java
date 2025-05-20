package com.devsuperior.dscatalog.services.exceptions;

public class UnprocessableEntityException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public UnprocessableEntityException(String msg) {
		super(msg);
	}
}