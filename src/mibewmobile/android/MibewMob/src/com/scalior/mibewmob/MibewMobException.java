package com.scalior.mibewmob;

public class MibewMobException extends Exception {
	private static final long serialVersionUID = 7451126154089765952L;

    private String message = null;
    private int errorCode = 0;
    
    public MibewMobException() {
        super();
    }
 
    public MibewMobException(String message) {
        super(message);
        this.message = message;
    }
 
    public MibewMobException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public MibewMobException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    public MibewMobException(String message, Throwable cause, int errorCode) {
        super(message, cause);
        this.message = message;
        this.errorCode = errorCode;
    }

    public MibewMobException(Throwable cause) {
        super(cause);
    }
 
    @Override
    public String toString() {
        return message;
    }
 
    @Override
    public String getMessage() {
        return message;
    }
    
    public int getErrorCode() {
    	return errorCode;
    }
}
