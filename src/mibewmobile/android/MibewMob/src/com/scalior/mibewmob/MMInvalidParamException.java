package com.scalior.mibewmob;

public class MMInvalidParamException extends MibewMobException {
	private static final long serialVersionUID = -5362101572378281235L;

    private String message = null;
    
    public MMInvalidParamException() {
        super();
    }
 
    public MMInvalidParamException(String message) {
        super(message);
        this.message = message;
    }
 
    @Override
    public String toString() {
        return message;
    }
 
    @Override
    public String getMessage() {
        return message;
    }	
}
