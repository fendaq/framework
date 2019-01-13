package jpa.autocode.util;

import java.util.UUID;

/**
 * @Author:LiuBingXu
 * @Description: 随机uuid
 * @Date: 2018/6/27.
 * @Modified by
 */
public class UUIDUtils {

    /**
     * @return 纯字符32位
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
