
package com.zeroq6.common.counter;


public class CounterConfig extends AbstractCounterConfig{
    // counter类型
    private String type;
    // 重试失败提示
    private String msgTryFailed;
    // 被锁定提示
    private String msgLock;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public String getMsgTryFailed() {
        return msgTryFailed;
    }

    public void setMsgTryFailed(String msgTryFailed) {
        this.msgTryFailed = msgTryFailed;
    }

    public String getMsgLock() {
        return msgLock;
    }

    public void setMsgLock(String msgLock) {
        this.msgLock = msgLock;
    }

}