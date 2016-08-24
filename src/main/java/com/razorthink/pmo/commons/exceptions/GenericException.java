package com.razorthink.pmo.commons.exceptions;

public class GenericException extends Exception {

    private static final int INTERNAL_SERVER_ERROR = 500;

    private int errorCode;
    private String message;

    public GenericException(String message, String details )
    {
        this.message = message;
        this.errorCode = INTERNAL_SERVER_ERROR;
    }

    public GenericException(String msg, Throwable cause )
    {
        super(cause);
        this.message = msg;
    }

    public GenericException(String msg, int code, Throwable cause )
    {
        super(cause);
        this.errorCode = code;
        this.message = msg;
    }

    public GenericException(int errorCode, String message )
    {
        this.errorCode = errorCode;
        this.message = message;
    }

    public GenericException(Exception exception, String message )
    {
        super(exception);
        this.errorCode = INTERNAL_SERVER_ERROR;
        this.message = message;
    }

    public GenericException(Exception exception )
    {
        super(exception);
        this.errorCode = INTERNAL_SERVER_ERROR;
        this.message = exception.getMessage();
    }

    public GenericException(String message )
    {
        this.errorCode = INTERNAL_SERVER_ERROR;
        this.message = message;
    }

    public GenericException(Exception e, String message, int code )
    {
        super(e);
        this.errorCode = code;
        this.message = message;
    }

    public GenericException(String message, int code )
    {
        this.errorCode = code;
        this.message = message;
    }

    public int getErrorCode()
    {
        return errorCode;
    }

    public void setErrorCode( int errorCode )
    {
        this.errorCode = errorCode;
    }

    @Override
    public String getMessage()
    {
        return message;
    }

    public void setMessage( String message )
    {
        this.message = message;
    }
}
