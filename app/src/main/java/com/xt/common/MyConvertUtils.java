package com.xt.common;

/**
 * @author xt on 2020/3/27 14:05
 */
public class MyConvertUtils {
    /**
     * @param number    数字
     * @param byteCount 字节个数
     * @return
     */
    public static String numberToHex(Number number, int byteCount) {
        if (number == null) {
            return "";
        }
        //2表示需要两个16进制位
        return String.format("%0" + byteCount * 2 + "x", number);
    }
}
