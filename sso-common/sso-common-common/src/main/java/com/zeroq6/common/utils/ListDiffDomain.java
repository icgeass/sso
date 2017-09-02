
package com.zeroq6.common.utils;

import java.util.List;

/**
 * Created by icgeass on 2017/1/3.
 */
public class ListDiffDomain<T> {

    private List<T> modifiedList;
    private List<T> newList;
    private List<T> deleteList;

    public ListDiffDomain() {
    }

    public ListDiffDomain(List<T> modifiedList, List<T> newList, List<T> deleteList) {
        this.modifiedList = modifiedList;
        this.newList = newList;
        this.deleteList = deleteList;
    }

    public List<T> getModifiedList() {
        return modifiedList;
    }

    public void setModifiedList(List<T> modifiedList) {
        this.modifiedList = modifiedList;
    }

    public List<T> getNewList() {
        return newList;
    }

    public void setNewList(List<T> newList) {
        this.newList = newList;
    }

    public List<T> getDeleteList() {
        return deleteList;
    }

    public void setDeleteList(List<T> deleteList) {
        this.deleteList = deleteList;
    }
}