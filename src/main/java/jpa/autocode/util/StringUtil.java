package jpa.autocode.util;

/**
 * @Author:LiuBingXu
 * @Description:
 * @Date: 2018/8/9.
 * @Modified by
 */
public class StringUtil {

    /**
     *Object转字符串
     * @param obj
     * @return
     */
    public static String objToStr(Object obj) {
        return obj == null ? null: obj.toString();
    }

    /**
     * 首字母大写
     * @param str 纯英文
     * @return
     */
    public static String firstLetterUppercase(String str) {
        StringBuilder sb = new StringBuilder();
        sb.append(str.substring(0, 1).toUpperCase() + str.substring(1));
        return sb.toString();
    }

    /**
     * 首字母小写
     * @param str 纯英文
     * @return
     */
    public static String firstLetterLowerCase(String str) {
        StringBuilder sb = new StringBuilder();
        sb.append(str.substring(0, 1).toLowerCase() + str.substring(1));
        return sb.toString();
    }

}
