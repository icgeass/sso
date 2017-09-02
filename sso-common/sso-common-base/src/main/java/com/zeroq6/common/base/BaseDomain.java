
package com.zeroq6.common.base;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseDomain<T extends BaseDomain> extends BaseQuery<T> {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Date createdTime;
    private Date modifiedTime;
    private String remark;
    private Integer yn;

    private Map<String, Object> extendMap = new HashMap<String, Object>();

    public Long getId() {
        return id;
    }

    public T setId(Long id) {
        this.id = id;
        return (T)this;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public T setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
        return (T)this;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public T setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
        return (T)this;
    }

    public String getRemark() {
        return remark;
    }

    public T setRemark(String remark) {
        this.remark = remark;
        return (T)this;
    }

    public Integer getYn() {
        return yn;
    }

    public T setYn(Integer yn) {
        this.yn = yn;
        return (T)this;
    }

    public Map<String, Object> getExtendMap() {
        return extendMap;
    }

    public T setExtendMap(Map<String, Object> extendMap) {
        this.extendMap = extendMap;
        return (T)this;
    }


}
