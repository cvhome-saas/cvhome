package com.asrevo.cvhome.store.controller.exception;

import java.io.Serial;

public class ConstraintException extends GenericRuntimeException {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private static final String CONSTRAINT_ERROR_CODE = "506";

	public ConstraintException(String message) {
		super(CONSTRAINT_ERROR_CODE, message);
	}

}
