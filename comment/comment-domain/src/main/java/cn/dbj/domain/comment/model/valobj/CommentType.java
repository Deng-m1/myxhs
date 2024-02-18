/**
 * 值对象；
 * 1. 用于描述对象属性的值，如一个库表中有json后者一个字段多个属性信息的枚举对象
 * 2. 对象名称如；XxxVO
 */
package cn.dbj.domain.comment.model.valobj;

public enum CommentType {
    // 图片评论
    PICTURE("01", "图片"),

    // 文字评论
    TEXT("02", "文字");

    private String type;
    private String name;

    CommentType(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}