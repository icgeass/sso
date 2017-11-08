package com.zeroq6.common.base;

import java.io.Serializable;

/**
 * @author icgeass@hotmail.com
 * @date 2017-07-08
 */
public class BaseResponse<T> implements Serializable{
    private static final long serialVersionUID = 1L;
    public static final String SUCCESS = "成功";
    public static final String FAILED = "失败";
    private boolean success;
    private String message;
    private T body;
    public BaseResponse(){}

    public BaseResponse(boolean success, T body) {
        this(success, success ? SUCCESS : FAILED, body);
    }

    public BaseResponse(boolean success, String message, T body) {
        this.success = success;
        this.message = message;
        this.body = body;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
