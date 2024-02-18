package cn.dbj.framework.starter.common.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.collections.MapUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
public final class MyException extends RuntimeException {
    private final ErrorCode code;
    private final Map<String, Object> data = new HashMap<>();
    private String message;
    private final String userMessage;

    public MyException(ErrorCode code, String userMessage) {
        this.code = code;
        this.userMessage = userMessage;
        this.message = message(userMessage);
    }

    public MyException(ErrorCode code, String userMessage,
                       String key, Object value) {
        this.code = code;
        addData(key, value);
        this.userMessage = userMessage;
        this.message = message(userMessage);
    }

    public MyException(ErrorCode code, String userMessage,
                       String key1, Object value1,
                       String key2, Object value2) {
        this.code = code;
        addData(key1, value1);
        addData(key2, value2);
        this.userMessage = userMessage;
        this.message = message(userMessage);
    }

    public MyException(ErrorCode code, String userMessage,
                       String key1, Object value1,
                       String key2, Object value2,
                       String key3, Object value3) {
        this.code = code;
        addData(key1, value1);
        addData(key2, value2);
        addData(key3, value3);
        this.userMessage = userMessage;
        this.message = message(userMessage);
    }

    public MyException(ErrorCode code, String userMessage,
                       String key1, Object value1,
                       String key2, Object value2,
                       String key3, Object value3,
                       String key4, Object value4) {
        this.code = code;
        addData(key1, value1);
        addData(key2, value2);
        addData(key3, value3);
        addData(key4, value4);
        this.userMessage = userMessage;
        this.message = message(userMessage);
    }

    public MyException(ErrorCode code, String userMessage, Map<String, Object> data) {
        this.code = code;
        this.data.putAll(data);
        this.userMessage = userMessage;
        this.message = message(userMessage);
    }



    private String message(String userMessage) {
        StringBuilder stringBuilder = new StringBuilder().append("[").append(this.code.toString()).append("]");

        if (isNotBlank(userMessage)) {
            stringBuilder.append(userMessage);
        }

        if (isNotEmpty(this.data)) {
            stringBuilder.append("Data: ").append(this.data);
        }

        return stringBuilder.toString();
    }

    public static MyException requestValidationException(Map<String, Object> data) {
        return new MyException(ErrorCode.REQUEST_VALIDATION_FAILED, "请求数据验证失败。", data);
    }

    public static MyException requestValidationException(String key, Object value) {
        return new MyException(ErrorCode.REQUEST_VALIDATION_FAILED, "请求数据验证失败。", key, value);
    }

    public static MyException requestValidationException(String message) {
        return new MyException(ErrorCode.REQUEST_VALIDATION_FAILED, message);
    }

    public static MyException accessDeniedException() {
        return new MyException(ErrorCode.ACCESS_DENIED, "权限不足。");
    }

    public static MyException accessDeniedException(String userMessage) {
        return new MyException(ErrorCode.ACCESS_DENIED, userMessage);
    }

    public static MyException authenticationException() {
        return new MyException(ErrorCode.AUTHENTICATION_FAILED, "登录失败。");
    }

    public void addData(String key, Object value) {
        this.data.put(key, value);
        this.message = message(this.userMessage);
    }

}
