package com.asrevo.cvhome.store.controller.exception;

import java.io.Serial;

public class OperationNotAllowedException extends ServiceRuntimeException {

	private static final String ERROR_CODE = "304";

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	public OperationNotAllowedException(String message) {
		super(ERROR_CODE, message);
	}

}
