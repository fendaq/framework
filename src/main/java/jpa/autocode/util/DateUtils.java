package jpa.autocode.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author:LiuBingXu
 * @Description:
 * @Date: 2018/8/11.
 * @Modified by
 */
public class DateUtils {

    /**
     * 格式化时间
     * @param formate
     * @param date
     * @return
     */
    public static String formateDate(String formate) {
        return formateDate(formate, new Date());
    }

    /**
     * 格式化时间
     * @param formate
     * @param date
     * @return
     */
    public static String formateDate(String formate, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(formate);
        return sdf.format(date == null ? new Date() : date);
    }
}
