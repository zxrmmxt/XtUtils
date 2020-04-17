package com.xt.common;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * @author xt on 2019/6/10 12:08
 */
public class MyTextUtils {
    public static boolean containTexts(String string, CharSequence subString) {
        if (!TextUtils.isEmpty(string)) {
            return string.contains(subString);
        }
        return false;
    }

    public static String subString(String string, String startString, String endString) {
        if (containTexts(string, startString) && containTexts(string, endString)) {
            int start = string.indexOf(startString) + startString.length();
            int end   = string.indexOf(endString);
            if (end > start) {
                return string.substring(start, end);
            }
        }
        return "";
    }

    public static String lastSubString(String string, String lastStartString, String endString) {
        if (containTexts(string, lastStartString) && containTexts(string, endString)) {
            int start = string.lastIndexOf(lastStartString) + lastStartString.length();
            int end   = string.indexOf(endString);
            if (end > start) {
                return string.substring(start, end);
            }
        }
        return "";
    }

    public static String lastSubString(String string, String lastStartString) {
        if (containTexts(string, lastStartString)) {
            int start = string.lastIndexOf(lastStartString) + lastStartString.length();
            return string.substring(start);
        }
        return "";
    }

    public static boolean isNumer(String str) {
        Pattern pattern = compile("[0-9]*");
        Matcher isNum   = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public static boolean isNegativeNumer(String str) {
        Pattern pattern = compile("^-?[0-9]+");
        Matcher isNum   = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public static boolean isAllNumer(String str) {
        Pattern pattern = compile("-?[0-9]+.?[0-9]+");
        Matcher isNum   = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 不区分大小写比较字符串(16进制字符串比较大小时会用到)
     *
     * @param text1
     * @param text2
     * @return
     */
    public static boolean equalsIgnoreCase(String text1, String text2) {
        if (!TextUtils.isEmpty(text1)) {
            text1 = text1.toUpperCase();
        }
        if (!TextUtils.isEmpty(text2)) {
            text2 = text2.toUpperCase();
        }
        return TextUtils.equals(text1, text2);
    }

    /**
     * 是否是手机号判断
     * @param phoneNumber
     * @return
     */
    public static boolean isPhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return false;
        } else {
            String regex = "[1][34578]\\d{9}";
            return phoneNumber.matches(regex);
        }
    }
}
