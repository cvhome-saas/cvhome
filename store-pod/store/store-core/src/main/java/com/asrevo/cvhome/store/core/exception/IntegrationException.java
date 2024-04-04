package com.asrevo.cvhome.store.core.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class IntegrationException extends ServiceException {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final int ERROR_VALIDATION_SAVE = 100;
	public static final int TRANSACTION_EXCEPTION = 99;
	
	private List<String> errorFields;
	
	private int errorCode = 0;

    public IntegrationException(Exception e) {
		super(e);
	}
	
	public IntegrationException(String message, Exception e) {
		super(message,e);
	}
	
	public IntegrationException(int code, String message) {
		
		super(message);
		this.errorCode = code;
	}
	
	public IntegrationException(int code) {
		
		this.errorCode = code;
	}

	public IntegrationException(String message) {
		super(message);
	}

}
