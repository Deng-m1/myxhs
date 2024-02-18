package cn.dbj.framework.starter.common.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public final class User {
    public static final User NOUSER = new User(null, null,  null);
    public static final User ANONYMOUS_USER = NOUSER;
    private final Long id;
    private final String name;


    private final String role;
    private User(Long id, String name, String role) {
        this.id= id;
        this.name = name;
        this.role = role;
    }



}
