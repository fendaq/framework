package jpa.autocode.bean;

import lombok.Data;

/**
 * @Author:LiuBingXu
 * @Description:代码自动生成相关名称
 * @Date: 2018/8/18.
 * @Modified by
 */
@Data
public class CodeModel {

    private String beanName;// 实体类名
    private String repositoryName;// repository名
    private String serverName;// server名
    private String serverImplName;// server实现类名
    private String controllerName;// 控制器名

}
