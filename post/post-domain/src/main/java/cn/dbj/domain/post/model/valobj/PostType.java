package cn.dbj.domain.post.model.valobj;

public enum PostType {
    /*
    * 图文
    * */
    PICTURE("01","图文"),

    /**
     * 视频
     */
    VIDEO("00","视频");



    private String code;

    private String description;

    PostType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
