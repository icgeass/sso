package com.zeroq6.sso.web.client.domain;

/**
 *
 * Created by icgeass on 2017/2/22.
 *
 * 通用的响应对象，当调用方需要根据不同的响应码做相应处理的时候使用（success不满足需求时）
 * @param <T>
 */
public class BaseResponseExtend<T> extends BaseResponse<T> {

    private static final long serialVersionUID = 1L;

    private String code;

    public BaseResponseExtend(){}

    public BaseResponseExtend(boolean success, String code, String message, T data) {
        super(success, message, data);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
