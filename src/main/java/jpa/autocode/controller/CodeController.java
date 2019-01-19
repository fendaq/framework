package jpa.autocode.controller;

import jpa.autocode.core.CreateCode;
import jpa.autocode.core.JavaCreate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author:LiuBingXu
 * @Description: 代码生成操作
 * @Date: 2019/1/13.
 * @Modified by
 */

@Controller
@RequestMapping(value = "/code")
public class CodeController {

    @Autowired
    private EntityManager entityManager;
    @Value("${code-create.database-name}")
    private String dataBaseName;
    @Value("${code-create.bean-package}")
    private String doMainPackage;
    @Value("${code-create.service-package}")
    private String servicePackage;
    @Value("${code-create.service-impl-package}")
    private String serviceImplPackag;
    @Value("${code-create.repository-package}")
    private String repositoryPackage;
    @Value("${code-create.controller-package}")
    private String controllerPackage;
    @Value("${code-create.enable}")
    private String enable;

    @GetMapping(value = "/create")
    @ResponseBody
    public Map createCode(String table) {
        Map m = new HashMap();
        CreateCode createCode = new JavaCreate(entityManager, dataBaseName, table
                , doMainPackage, servicePackage, serviceImplPackag, repositoryPackage, controllerPackage);
        if (StringUtils.isEmpty(table)) {
            m.put("success", false);
            m.put("message", "请输入表名dev_?");
            return m;
        }
        if (!"true".equals(enable)) {
            m.put("success", false);
            m.put("message", "未启用代码生成");
            return m;
        }
        try {
            createCode.create();
            m.put("success", true);
        } catch (Exception e) {
            e.printStackTrace();
            m.put("success", false);
            m.put("message", e.getMessage());
        }
        return m;
    }

}
