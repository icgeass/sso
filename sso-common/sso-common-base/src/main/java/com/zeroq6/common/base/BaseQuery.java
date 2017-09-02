
package com.zeroq6.common.base;

import java.io.Serializable;


public abstract class BaseQuery<T extends BaseQuery> implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer startIndex;// 开始索引（前台分页查询用到）

    private Integer endIndex;// 结束索引

    private String orderField = "id";

    private String orderFieldType = "DESC";

    public Integer getStartIndex() {
        return startIndex;
    }

    public T setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
        return (T)this;
    }

    public Integer getEndIndex() {
        return endIndex;
    }

    public T setEndIndex(Integer endIndex) {
        this.endIndex = endIndex;
        return (T)this;
    }

    public String getOrderField() {
        return orderField;
    }
    public T setOrderField(String orderField) {
        if(null == orderField || orderField.replace("_", "").trim().length() == 0){
            return (T)this;
        }
        for (int i = 0; i < orderField.length(); i++) {
            if (!Character.isLetter(orderField.charAt(i)) && orderField.charAt(i) != '_') {
                return (T)this;
            }
        }
        this.orderField = orderField;
        return (T)this;
    }

    public String getOrderFieldType() {
        return orderFieldType;
    }

    public T setOrderFieldType(String orderFieldType) {
        if(!"ASC".equalsIgnoreCase(orderFieldType) && !"DESC".equalsIgnoreCase(orderFieldType)){
            return (T)this;
        }
        this.orderFieldType = orderFieldType;
        return (T)this;
    }

    /**
     * 获取分页
     * @return
     */
    public Integer getPageSize() {
        if (endIndex != null && startIndex != null) {
            return endIndex - startIndex;
        }
        return null;
    }
}
