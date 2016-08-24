package com.razorthink.pmo.commons.pojo;


import com.razorthink.pmo.commons.exceptions.WebappException;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

public class Response<T> implements Serializable {

    private T result;
    private OperationStatus status;
    private WebappException exception;
    private HttpStatus httpStatus;
    private Integer httpStatusCode;

    public Response() {
    }

    public Response(T result) {
        this.result = result;
    }

    public Response(T result, OperationStatus status) {
        this.result = result;
        this.status = status;
    }

    public Response(T result, OperationStatus status, HttpStatus httpStatus) {
        this.result = result;
        this.status = status;
        this.httpStatus = httpStatus;
    }

    public Response(WebappException wException) {
        this.exception = wException;
    }

    public Response(WebappException wException, OperationStatus status) {
        this.exception = wException;
        this.status = status;
    }

    public Response(Result result) {
        this.result = (T) result.getResult();
        this.status = result.getStatus();
        this.exception = result.getException() != null ? new WebappException(result.getException()) : null;
        this.httpStatus = HttpStatus.OK;
        this.httpStatusCode = 200;
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

    public WebappException getException() {
        return exception;
    }

    public void setException(WebappException WebappException) {
        this.exception = WebappException;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }
}