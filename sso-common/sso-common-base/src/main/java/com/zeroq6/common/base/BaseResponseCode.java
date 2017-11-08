package com.zeroq6.common.base;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * @author icgeass@hotmail.com
 * @date 2017-07-08
 */
public class BaseResponseCode<T> implements Serializable{

    private static final long serialVersionUID = 1L;

    public final static String MESSAGE_SUCCESS = "成功";
    public final static String MESSAGE_HANDING = "处理中";
    public final static String MESSAGE_FAILED = "失败";

    public final static String CODE_SUCCESS = "000000";
    public final static String CODE_HANDING = "888888";
    public final static String CODE_FAILED = "999999";


    private String code;

    private String message;

    private T body;

    public BaseResponseCode(){}

    public BaseResponseCode(String code, T body) {
        this(code, null, body);
        String message = null;
        if(CODE_SUCCESS.equals(code)){
            message = MESSAGE_SUCCESS;
        }else if(CODE_HANDING.equals(code)){
            message = MESSAGE_HANDING;
        }else if(CODE_FAILED.equals(code)){
            message = MESSAGE_FAILED;
        }
        this.message = message;
    }

    public BaseResponseCode(String code, String message, T body) {
        if (null == code || !Pattern.matches("\\d{6}", code)) {
            throw new RuntimeException("构造传入code非法, " + code);
        }
        this.code = code;
        this.message = message;
        this.body = body;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
