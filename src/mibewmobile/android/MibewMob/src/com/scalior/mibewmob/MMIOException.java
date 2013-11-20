package com.scalior.mibewmob;

public class MMIOException extends MibewMobException {
	private static final long serialVersionUID = 3954666005446788427L;

    private String message = null;
    
    public MMIOException() {
        super();
    }
 
    public MMIOException(String message) {
        super(message);
        this.message = message;
    }
 
    public MMIOException(String message, Throwable cause) {
    	super(message, cause);
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
