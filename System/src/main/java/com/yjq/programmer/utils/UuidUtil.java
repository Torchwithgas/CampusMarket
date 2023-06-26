package com.yjq.programmer.utils;

import java.util.UUID;

/**
 * UUID生成工具类
 */
public class UuidUtil {

    public static String[] chars = new String[] { "a", "b", "c", "d", "e", "f",
            "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
            "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z" };


    /**
     * 获取短UUID
     * @return
     */
    public static String getShortUuid() {
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = UuidUtil.getUuid();
        return uuid.substring(0,8);

    }

    /**
     * 获得32位UUID
     */
    public static String getUuid(){
        String uuid = UUID.randomUUID().toString();
        //去掉“-”符号
        return uuid.replaceAll("-", "");
    }


}
