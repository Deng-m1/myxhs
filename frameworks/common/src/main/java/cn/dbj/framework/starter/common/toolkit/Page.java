package cn.dbj.framework.starter.common.toolkit;

import java.util.List;

public class Page<T> {
    private int page; // 当前页数
    private int size; // 每页数量
    private long totalElements; // 总元素数量
    private int totalPages; // 总页数
    private List<T> content; // 分页内容

    public Page() {
        // 默认构造函数
    }

    public Page(int page, int size, long totalElements, int totalPages, List<T> content) {
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.content = content;
    }

    // getter和setter方法

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }
}
