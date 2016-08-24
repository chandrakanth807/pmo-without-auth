package com.razorthink.pmo.commons.exceptions;

public class DataException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private String errorCode;
    private String errorMessage;

    public DataException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;

    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
