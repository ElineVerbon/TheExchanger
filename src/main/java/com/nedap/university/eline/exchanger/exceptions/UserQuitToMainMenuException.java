package com.nedap.university.eline.exchanger.exceptions;

public class UserQuitToMainMenuException extends Exception { 
    
	private static final long serialVersionUID = 1L;

	public UserQuitToMainMenuException(String errorMessage) {
        super(errorMessage);
    }
	
	public UserQuitToMainMenuException() {
        super();
    }
}
