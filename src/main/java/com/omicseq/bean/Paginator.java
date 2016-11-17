package com.omicseq.bean;

public class Paginator {
    public static int DEFAULTPAGESIZE = 20;
    // number of total records
    private Integer totalRecords = Integer.valueOf(0);
    // the current page
    private int pageIndex =  Integer.valueOf(1);
    // number of records on current page
    private int pageSize = DEFAULTPAGESIZE;
    // the sorted column 
    private String sort;
    // direction of sorted column
    private String dir;
    // 
    private String alias;

    /**
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @param alias the alias to set
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * @return the sort
     */
    public String getSort() {
        return sort;
    }

    /**
     * @param sort the sort to set
     */
    public void setSort(String sort) {
        this.sort = sort;
    }

    /**
     * @return the dir
     */
    public String getDir() {
        return dir;
    }

    /**
     * @param dir the dir to set
     */
    public void setDir(String dir) {
        this.dir = dir;
    }

    /**
     * @return the totalRecords
     */
    public Integer getTotalRecords() {
        return totalRecords;
    }

    /**
     * @param totalRecords the totalRecords to set
     */
    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    /**
     * @return the startIndex
     */
    public int getStartIndex() {
        return (this.pageIndex - 1) * pageSize;
    }

    /** 
    * @return the endIndex
    */
    public int getEndIndex() {
        int end = this.pageIndex * pageSize;
        return end > totalRecords ? totalRecords : end;
    }

    /**
     * @return the pageSize
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * @param pageSize the pageSize to set
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    /** 
     * 总页数
    * @return
    */
    public Integer getTotalPages() {
        Integer mod = (totalRecords % pageSize);
        Integer pages = totalRecords / pageSize;
        return mod == 0 ? pages : pages + 1;
    }
}
