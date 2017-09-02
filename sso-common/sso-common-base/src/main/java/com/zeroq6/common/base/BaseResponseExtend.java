
package com.zeroq6.common.base;

public class BaseResponseExtend<T> extends BaseResponse<T> {

    private static final long serialVersionUID = 1L;

    // 默认为null，当业务确实关心失败原因时使用
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
