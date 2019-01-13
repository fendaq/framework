package jpa.autocode.domain;

import lombok.Data;

/**
 * @Author:LiuBingXu
 * @Description: 数据库对应字段和注释
 * @Date: 2019/1/13.
 * @Modified by
 */
@Data
public class Table {
    private String name;// 字段
    private String comment;// 注释
}