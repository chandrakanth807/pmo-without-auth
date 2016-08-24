package com.razorthink.pmo.commons.pojo;

public class Result<T> {

    private T result;
    private OperationStatus status;
    private Exception exception;

    public Result() {
    }

    public Result(T result, OperationStatus status) {
        this.result = result;
        this.status = status;
    }

    public Result(Exception exception) {
        this.exception = exception;
    }

    public Result(Exception exception, OperationStatus status) {
        this.exception = exception;
        this.status = status;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public OperationStatus getStatus() {
        return status;
    }

    public void setStatus(OperationStatus status) {
        this.status = status;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

}
