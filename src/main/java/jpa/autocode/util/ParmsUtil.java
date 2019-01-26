package jpa.autocode.util;

import jpa.autocode.bean.Parms;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author:LiuBingXu
 * @Description:
 * @Date: 2019/1/26.
 * @Modified by
 */
public class ParmsUtil {

    /**
     * 返回key值是names的values
     *
     * @param parms
     * @param names
     * @return
     */
    public static List<String> getValueByKey(List<Parms> parms, String names) {
        List<String> values = new ArrayList<>();
        parms.forEach(p -> {
            if (p.getName().equals(names)) {
                values.add(p.getValue());
            }
        });
        return values;
    }
}
