package cn.dbj.framework.starter.common.toolkit;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NullOrZeroUtils {

    public static boolean isNullOrEmptyOrZero(String str) {
        return str == null || str.isEmpty() || str.trim().equals("0");
    }

    public static boolean isNullOrEmptyOrZero(Integer num) {
        return num == null || num == 0;
    }

    public static boolean isNullOrEmptyOrZero(Long num) {
        return num == null || num == 0L;
    }

    public static boolean isNullOrEmptyOrZero(Short num) {
        return num == null || num == 0;
    }

    public static boolean isNullOrEmptyOrZero(Double num) {
        return num == null || num == 0.0;
    }

    public static boolean isNullOrEmptyOrZero(Float num) {
        return num == null || num == 0.0f;
    }

    public static boolean isNullOrEmptyOrZero(BigDecimal num) {
        return num == null || num.compareTo(BigDecimal.ZERO) == 0;
    }

    public static boolean isNullOrEmptyOrZero(BigInteger num) {
        return num == null || num.compareTo(BigInteger.ZERO) == 0;
    }
}

