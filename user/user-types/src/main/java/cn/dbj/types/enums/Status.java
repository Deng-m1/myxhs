package cn.dbj.types.enums;

public enum Status {
    DRAFT("Draft"),          // 草稿
    PUBLISHED("Published"),  // 已发布
    ARCHIVED("Archived"),    // 已归档
    DELETED("Deleted"),      // 已删除
    UNDELETED("Undeleted");  // 未删除

    private final String status;

    Status(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
