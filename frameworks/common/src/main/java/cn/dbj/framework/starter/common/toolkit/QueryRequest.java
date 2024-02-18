package cn.dbj.framework.starter.common.toolkit;

import lombok.Data;

@Data
public class QueryRequest {
    private int page; // 当前页码
    private int size; // 每页大小
    private String sortBy; // 排序字段
    private String sortOrder; // 排序顺序（升序/降序）等

    // 可以添加其他查询参数

    // 构造函数
    public QueryRequest() {
        // 默认构造函数
    }

    // Getter 和 Setter 方法
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

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}