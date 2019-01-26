package jpa.autocode.controller;

import jpa.autocode.core.CreateCode;
import jpa.autocode.core.JavaCreate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;

@RestController
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
    @Value("${code-create.database-name}")
    private String dataTableName;

    @PostMapping(value = "/code/create")
    public ResponseEntity createCode(String table) {
        CreateCode createCode = new JavaCreate(entityManager, dataBaseName, table
                , doMainPackage, servicePackage, serviceImplPackag, repositoryPackage, controllerPackage);
        if (StringUtils.isEmpty(table)) {
            return ResponseEntity.ok("请输入表名，类似这样的dev_?");
        }
        if (!"true".equals(enable)) {
            return ResponseEntity.ok("未启用代码生成");
        }
        try {
            createCode.create();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok("代码生成成功！");
    }

    @PostMapping(value = "/loadtable")
    public ResponseEntity loadtable() {
        if (!"true".equals(enable)) {
            return ResponseEntity.ok("未启用代码生成");
        }
        String sql = "select table_name from information_schema.tables where table_schema=? and table_type='base table'";
        return ResponseEntity.ok(entityManager.createNativeQuery(sql)
                .setParameter(1, dataTableName).getResultList());
    }
}
