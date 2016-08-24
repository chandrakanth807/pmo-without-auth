package com.razorthink.pmo.commons.exceptions;

import org.springframework.http.HttpStatus;

public class WebappException extends Exception {

    private HttpStatus errorCode;
    private String message;
    private String details;

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public WebappException() {
    }

    public WebappException(String message, String details) {
        // this.exception = exception;
        this.details = details;
        this.message = message;
        this.errorCode = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public WebappException(String msg, Throwable cause) {
        super(cause);
        this.message = msg;
    }

    public WebappException(String msg, HttpStatus code, Throwable cause) {
        super(cause);
        this.errorCode = code;
        this.message = msg;
    }

    public WebappException(HttpStatus errorCode, String message) {
        // this.exception = exception;
        this.errorCode = errorCode;
        this.message = message;
    }

    public WebappException(Exception exception, String message) {
        // this.exception = exception;
        super(exception);
        this.errorCode = HttpStatus.INTERNAL_SERVER_ERROR;
        this.message = message;
    }

    public WebappException(Exception exception) {
        // this.exception = exception;
        super(exception);
        this.errorCode = HttpStatus.INTERNAL_SERVER_ERROR;
        this.message = exception.getMessage();
    }

    public WebappException(String message) {
        // this.exception = null;
        this.errorCode = HttpStatus.INTERNAL_SERVER_ERROR;
        this.message = message;
    }

    public WebappException(Exception e, String message, HttpStatus code) {
        // this.exception = null;
        super(e);
        this.errorCode = code;
        this.message = message;
    }

    public WebappException(String message, HttpStatus code) {
        // this.exception = null;
        this.errorCode = code;
        this.message = message;
    }

    public HttpStatus getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(HttpStatus errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
