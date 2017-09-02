
package com.zeroq6.common.base;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yuuki asuna on 2016/10/23.
 *
 * 根据总记录数，当前页，分页大小计算
 */
public class Page<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    public final static int INIT_PAGE_SIZE = 20;
    public final static int INIT_FIRST_PAGE = 1;
    private int pageSize = INIT_PAGE_SIZE;
    private int totalCount;
    private int currentPage;
    private List<T> data;

    public Page() {
        this(INIT_FIRST_PAGE, INIT_PAGE_SIZE);
    }

    public Page(int currentPage) {
        this(currentPage, INIT_PAGE_SIZE);
    }

    public Page(int currentPage, int pageSize) {
        setCurrentPage(currentPage);
        setPageSize(pageSize);
    }

    /**
     * 获取开始索引，从0开始
     *
     * @return
     */
    public int getStartIndex() {
        return (getCurrentPage() - 1) * this.pageSize;
    }

    /**
     * 获取结束索引+1
     *
     * @return
     */
    public int getEndIndex() {
        return getCurrentPage() * this.pageSize;
    }

    /**
     * 是否第一页
     * http://stackoverflow.com/questions/7155491/can-you-explain-the-isxxx-method-names-in-java
     *
     * @return
     */
    public boolean getIsFirstPage() {
        return getCurrentPage() <= INIT_FIRST_PAGE;
    }

    /**
     * 是否末页
     *
     * @return
     */
    public boolean getIsLastPage() {
        return getCurrentPage() >= getPageCount();
    }

    /**
     * 获取下一页页码
     *
     * @return
     */
    public int getNextPage() {
        if (getIsLastPage()) {
            return getCurrentPage();
        }
        return getCurrentPage() + 1;
    }

    /**
     * 获取上一页页码
     *
     * @return
     */
    public int getPreviousPage() {
        if (getIsFirstPage()) {
            return INIT_FIRST_PAGE;
        }
        return getCurrentPage() - 1;
    }

    /**
     * 获取当前页页码
     *
     * @return
     */
    public int getCurrentPage() {
        if (currentPage <= 0) {
            currentPage = INIT_FIRST_PAGE;
        }
        return currentPage;
    }

    /**
     * 取得总页数
     *
     * @return
     */
    public int getPageCount() {
        return totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
    }

    /**
     * 取总记录数.
     *
     * @return
     */
    public int getTotalCount() {
        return this.totalCount;
    }

    /**
     * 设置当前页
     *
     * @param currentPage
     */
    public void setCurrentPage(int currentPage) {
        if (currentPage <= 0) {
            this.currentPage = INIT_FIRST_PAGE;
        } else {
            this.currentPage = currentPage;
        }
    }

    /**
     * 获取每页数据容量.
     *
     * @return
     */
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        if (pageSize <= 0) {
            this.pageSize = INIT_PAGE_SIZE;
        } else {
            this.pageSize = pageSize;
        }
    }

    /**
     * 该页是否有下一页.
     *
     * @return
     */
    public boolean getHasNextPage() {
        return getCurrentPage() < getPageCount();
    }

    /**
     * 该页是否有上一页.
     *
     * @return
     */
    public boolean getHasPreviousPage() {
        return getCurrentPage() > INIT_FIRST_PAGE;
    }

    /**
     * 获取数据集
     *
     * @return
     */
    public List<T> getData() {
        return data;
    }

    /**
     * 设置数据集
     *
     * @param data
     */
    public void setData(List<T> data) {
        this.data = data;
    }

    /**
     * 设置总记录条数
     *
     * @param totalCount
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }


}
