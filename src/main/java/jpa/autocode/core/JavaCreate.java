package jpa.autocode.core;

import com.squareup.javapoet.*;
import jpa.autocode.bean.CodeModel;
import jpa.autocode.bean.Parms;
import jpa.autocode.bean.Table;
import jpa.autocode.util.DateUtils;
import jpa.autocode.util.ParmsUtil;
import jpa.autocode.util.StringUtil;
import jpa.autocode.util.UUIDUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.criteria.Predicate;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

@Data
public class JavaCreate implements CreateCode {
    private final static Logger LOGGER = LoggerFactory.getLogger(JavaCreate.class);

    private EntityManager entityManager;
    protected String dataBaseName;
    protected CodeModel codeModel = new CodeModel();
    protected String tableName;// 表名
    protected String version = "V 1.0.5";// 版本
    protected String doMainPackage = "com.liubx.bean";// 实体类路径
    protected String servicePackage = "com.liubx.web.service";// service路径
    protected String serviceImplPackage = "com.liubx.web.service.impl";// service实现类路径
    protected String repositoryPackage = "com.liubx.web.repository";// repository类路径
    protected String controllerPackage = "com.liubx.web.controller";// controller类路径
    protected String dataBaseType;// 数据库类型
    protected String basePath;// 绝对路径前缀
    protected List<Parms> parm;// 参数
    protected List<String> createInstance;// 创建实例

    public JavaCreate(EntityManager entityManager, String dataBaseName, String tableName, String doMainPackage,
                      String servicePackage, String serviceImplPackage, String repositoryPackage, String controllerPackage,
                      String dataBaseType, List<Parms> parm) {
        Assert.notNull(dataBaseName, "数据库名不能为空！");
        Assert.notNull(tableName, "表不能为空！");
        Assert.notNull(doMainPackage, "实体类路径不能为空！");
        Assert.notNull(servicePackage, "service 路径不能为空！");
        Assert.notNull(serviceImplPackage, "service 实现类路径不能为空！");
        Assert.notNull(repositoryPackage, "repository 包路径不能为空！");
        Assert.notNull(controllerPackage, "controller 包路径不能为空！");
        Assert.notNull(dataBaseType, "数据库类型不能为空！");
        this.entityManager = entityManager;
        this.dataBaseName = dataBaseName;
        this.tableName = tableName;
        this.doMainPackage = doMainPackage;
        this.servicePackage = servicePackage;
        this.serviceImplPackage = serviceImplPackage;
        this.repositoryPackage = repositoryPackage;
        this.controllerPackage = controllerPackage;
        this.dataBaseType = dataBaseType;
        this.parm = parm;
        this.createInstance = ParmsUtil.getValueByKey(this.parm, "type_c");
        this.initBasePath();
    }

    public JavaCreate(EntityManager entityManager, String tableName, String dataBaseName) {
        this.entityManager = entityManager;
        this.tableName = tableName;
        this.dataBaseName = dataBaseName;
        this.initBasePath();
    }

    @Override
    public void create() {
        String sql = this.getSql();
        List<Object[]> resultList = entityManager.createNativeQuery(sql).getResultList();

        // 查询数据库
        List<Table> tableList = new ArrayList<>();
        resultList.forEach(t -> {
            Table table = new Table();
            table.setName(StringUtil.objToStr(t[0]));
            table.setComment(StringUtil.objToStr(t[1]));
            table.setDataType(StringUtil.objToStr(t[2]));
            table.setIsPri(StringUtil.objToStr(t[3]));
            tableList.add(table);
        });

        // 准备相关名
        codeModel.setBeanName(StringUtil.firstLetterUppercase(tableName.split("_")[1]));
        codeModel.setRepositoryName(codeModel.getBeanName() + "Repository");
        codeModel.setServerName(codeModel.getBeanName() + "Server");
        codeModel.setServerImplName(codeModel.getServerName() + "Impl");
        codeModel.setControllerName(codeModel.getBeanName() + "Controller");

        // 生成代码
        try {
            this.newThreadCreateCode(tableName, tableList);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void newThreadCreateCode(String tableName, List<Table> tableList) throws InterruptedException {
        // 生成domain
        this.createDomainClass(tableName, tableList);
        Thread.sleep(1000);

        if (createInstance.contains("repository")) {
            // 生成repository
            this.createRepository();
            Thread.sleep(1000);
        }

        if (createInstance.contains("server")) {
            // 生成service接口
            this.createServiceClass();
            Thread.sleep(1000);
        }

        if (createInstance.contains("serverImpl")) {
            // 生成service接口实现类
            this.createServiceClassImpl();
            Thread.sleep(1000);
        }
        if (createInstance.contains("controller")) {
            // 生成controller
            this.createController();
        }
    }

    public boolean createDomainClass(String tableName, List<Table> tableList) {
        /** 读取mysql转Java类型配置 **/
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("mysqlToJava.properties");
        ResourceBundle resourceBundle = null;
        try {
            resourceBundle = new PropertyResourceBundle(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        TypeSpec.Builder builder = TypeSpec.classBuilder(codeModel.getBeanName());
        ResourceBundle finalResourceBundle = resourceBundle;
        tableList.forEach(t -> {
            /** 属性上面的注解 **/
            AnnotationSpec annotationSpecColumn = AnnotationSpec.builder(Column.class)
                    .addMember("name", "$S", t.getName())
                    .build();
            /** 主键 **/
            if (t.getIsPri().equals("true")) {
                annotationSpecColumn = AnnotationSpec.builder(Id.class).build();
            }

            Class clazz = String.class;
            if (finalResourceBundle != null) {
                try {
                    clazz = Class.forName(finalResourceBundle.getString(t.getDataType()));
                    if (clazz == Date.class) {
                        // 处理日期格式化
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            /** 添加属性 **/
            FieldSpec fieldSpec = FieldSpec.builder(clazz, t.getName(), Modifier.PRIVATE)
                    .addJavadoc(t.getComment())
                    .addAnnotation(annotationSpecColumn)
                    .build();
            builder.addField(fieldSpec);
            LOGGER.info("bean生成成功！");
        });

        /** 生成注解 **/
        AnnotationSpec annotationSpecTable = AnnotationSpec.builder(javax.persistence.Table.class)
                .addMember("name", "$S", tableName)
                .build();
        AnnotationSpec annotationSpecEntity = AnnotationSpec.builder(javax.persistence.Entity.class).build();
        AnnotationSpec annotationSpecData = AnnotationSpec.builder(Data.class).build();

        TypeSpec typeSpec = builder
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(annotationSpecData)
                .addAnnotation(annotationSpecEntity)
                .addAnnotation(annotationSpecTable)
                .addJavadoc(" @Author:LiuBingXu\n" +
                        " @Description: \n" +
                        " @Date: " + DateUtils.formateDate("yyyy/MM/dd") + ".\n" +
                        " @Modified by\n")
                .build();
        JavaFile javaFile = JavaFile.builder(doMainPackage, typeSpec).build();

        outFile(javaFile);
        return true;
    }

    private void createRepository() {
        ClassName superClass = ClassName.bestGuess("jpa.repository.BaseRepository");

        ClassName paramOne = ClassName.bestGuess(doMainPackage + "." + codeModel.getBeanName());// 泛型第一个参数
        ClassName paramTwo = ClassName.bestGuess("java.lang.String");// 泛型第二个参数
        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(superClass, paramOne, paramTwo);

        TypeSpec typeSpec = TypeSpec.interfaceBuilder(codeModel.getRepositoryName())
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(parameterizedTypeName)
                .addJavadoc("@Author:LiuBingXu\n@Date: " + DateUtils.formateDate("yyyy/MM/dd") + "\n")
                .addAnnotation(Repository.class)
                .build();

        JavaFile javaFile = JavaFile.builder(repositoryPackage, typeSpec).build();
        outFile(javaFile);
        LOGGER.info("repository create success！");
    }

    public boolean createServiceClass() {
        ClassName beanClass = ClassName.bestGuess(doMainPackage + "." + codeModel.getBeanName());

        MethodSpec saveMethod = MethodSpec.methodBuilder("saveOrUpdate")
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)// 方法类型
                .addParameter(beanClass, codeModel.getBeanName().toLowerCase())// 方法参数
                .returns(beanClass)
                .build();

        MethodSpec getMethod = MethodSpec.methodBuilder("get" + codeModel.getBeanName() + "ById")
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .addParameter(String.class, "id")
                .returns(beanClass)
                .build();

        MethodSpec deleteMethod = MethodSpec.methodBuilder("delete" + codeModel.getBeanName() + "ByIds")
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .addParameter(String.class, "ids")
                .returns(boolean.class)
                .build();

        MethodSpec pageListMethod = MethodSpec.methodBuilder("pageList")
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .addParameter(beanClass, codeModel.getBeanName().toLowerCase())
                .addParameter(int.class, "page")
                .addParameter(int.class, "pageSize")
                .returns(Page.class)
                .build();

        TypeSpec typeSpec = TypeSpec.interfaceBuilder(codeModel.getServerName())
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("@Author:LiuBingXu\n@Date: " + DateUtils.formateDate("yyyy/MM/dd") + "\n")
                .addMethod(saveMethod)
                .addMethod(getMethod)
                .addMethod(deleteMethod)
                .addMethod(pageListMethod)
                .build();

        JavaFile javaFile = JavaFile.builder(servicePackage, typeSpec).build();
        outFile(javaFile);
        LOGGER.info("service create success！");
        return true;
    }

    private void createServiceClassImpl() {
        ClassName className = ClassName.bestGuess(servicePackage + "." + codeModel.getServerName());
        ClassName repositoryClass = ClassName.bestGuess(repositoryPackage + "." + codeModel.getRepositoryName());
        ClassName beanClass = ClassName.bestGuess(doMainPackage + "." + codeModel.getBeanName());

        FieldSpec fieldSpec = FieldSpec.builder(repositoryClass, StringUtil.firstLetterLowerCase(codeModel.getRepositoryName()), Modifier.PRIVATE)
                .addAnnotation(Autowired.class)
                .build();

        String beanParm = StringUtil.firstLetterLowerCase(codeModel.getBeanName());
        String repositoryName = StringUtil.firstLetterLowerCase(codeModel.getRepositoryName());

        MethodSpec saveMethod = MethodSpec.methodBuilder("saveOrUpdate")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(beanClass, StringUtil.firstLetterLowerCase(codeModel.getBeanName()))
                .addCode("  if ($T.isEmpty(" + beanParm + ".getId())) {\n" +
                        "  " + beanParm + ".setId($T.getUUID());\n" +
                        "  }\nreturn $N.save($N);\n", StringUtils.class, UUIDUtils.class, repositoryName, beanParm)
                .returns(beanClass)
                .build();

        MethodSpec getMethod = MethodSpec.methodBuilder("get" + codeModel.getBeanName() + "ById")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "id")
                .addStatement("  return " + repositoryName + ".findById(id ).get()")
                .returns(beanClass)
                .build();

        MethodSpec deleteMethod = MethodSpec.methodBuilder("delete" + codeModel.getBeanName() + "ByIds")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "ids")
                .addCode("  $T idArr = ids.split(\",\");\n" +
                        "  " + repositoryName + ".batchDelete($T.asList(idArr));\n" +
                        "  return true;\n", String[].class, Arrays.class)
                .returns(TypeName.BOOLEAN)
                .build();

        MethodSpec toPredicateMethod = MethodSpec.methodBuilder("toPredicate")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(beanClass, codeModel.getBeanName().toLowerCase())
                .addCode(" return ($T<" + codeModel.getBeanName() + ">) (root, criteriaQuery, criteriaBuilder) -> {\n" +
                        "     $T<$T> predicate = new $T<>();\n" +
                        "     if ($T.isNotBlank(" + StringUtil.firstLetterLowerCase(codeModel.getBeanName()) + ".getId())) {\n" +
                        "         predicate.add(criteriaBuilder.equal(root.get(\"id\"), " + StringUtil.firstLetterLowerCase(codeModel.getBeanName()) + ".getId()));\n" +
                        "     }\n" +
                        "     return criteriaQuery.where(predicate.toArray(new Predicate[predicate.size()])).getRestriction();\n" +
                        " };\n", Specification.class, List.class, Predicate.class, ArrayList.class, org.apache.commons.lang3.StringUtils.class)
                .returns(Specification.class)
                .build();

        MethodSpec pageListMethod = MethodSpec.methodBuilder("pageList")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(beanClass, codeModel.getBeanName().toLowerCase())
                .addParameter(int.class, "page")
                .addParameter(int.class, "pageSize")
                .addCode("  $T sort = Sort.by(Sort.Direction.DESC, \"id\");\n" +
                                "  $T pageable = $T.of(page, pageSize, sort);\n" +
                                "  return " + repositoryName + ".pageList(pageable, toPredicate(" + codeModel.getBeanName().toLowerCase() + "));\n",
                        Sort.class, Pageable.class, PageRequest.class)
                .returns(Page.class)
                .build();

        TypeSpec typeSpec = TypeSpec.classBuilder(codeModel.getServerImplName())
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("@Author:LiuBingXu\n@Date: " + DateUtils.formateDate("yyyy/MM/dd") + "\n")
                .addAnnotation(Service.class)
                .addAnnotation(Transactional.class)
                .addSuperinterface(className)
                .addField(fieldSpec)
                .addMethod(saveMethod)
                .addMethod(getMethod)
                .addMethod(deleteMethod)
                .addMethod(toPredicateMethod)
                .addMethod(pageListMethod)
                .build();

        JavaFile javaFile = JavaFile.builder(serviceImplPackage, typeSpec).build();
        outFile(javaFile);
        LOGGER.info("serviceImpl create success！");
    }

    private void createController() {
        ClassName serverClassName = ClassName.bestGuess(servicePackage + "." + codeModel.getServerName());
        ClassName domainClassName = ClassName.bestGuess(doMainPackage + "." + codeModel.getBeanName());
        Class saveReturnClass = ResponseEntity.class;

        String serverName = StringUtil.firstLetterLowerCase(codeModel.getServerName());
        String domainName = StringUtil.firstLetterLowerCase(codeModel.getBeanName());

        AnnotationSpec saveAnnotation = AnnotationSpec
                .builder(PostMapping.class)
                .addMember("value", "$S", "/" + domainName + "/add")
                .build();

        AnnotationSpec deleteAnnotation = AnnotationSpec
                .builder(PostMapping.class)
                .addMember("value", "$S", "/" + domainName + "/delByids")
                .build();

        AnnotationSpec infoAnnotation = AnnotationSpec
                .builder(PostMapping.class)
                .addMember("value", "$S", "/" + domainName + "/info/{id}")
                .build();

        AnnotationSpec pageListAnnotation = AnnotationSpec
                .builder(PostMapping.class)
                .addMember("value", "$S", "/" + domainName + "/pageList")
                .build();

        FieldSpec fieldSpec = FieldSpec.builder(serverClassName, serverName, Modifier.PUBLIC)
                .addAnnotation(Autowired.class)
                .build();

        ParameterSpec infoParm = ParameterSpec.builder(String.class, "id")
                .addAnnotation(PathVariable.class)
                .build();

        MethodSpec saveMethod = MethodSpec.methodBuilder("add")
                .addAnnotation(saveAnnotation)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(domainClassName, domainName)
                .addCode("return ResponseEntity.ok(" + serverName + ".saveOrUpdate(" + domainName + "));\n")
                .returns(saveReturnClass)
                .build();

        MethodSpec deleteMethod = MethodSpec.methodBuilder("delByids")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(deleteAnnotation)
                .addParameter(String.class, "ids")
                .addCode("return ResponseEntity.ok(" + serverName + ".delete" + codeModel.getBeanName() + "ByIds(ids));\n")
                .returns(saveReturnClass)
                .build();

        MethodSpec infoMethod = MethodSpec.methodBuilder("info")
                .addAnnotation(infoAnnotation)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(infoParm)
                .addCode("return ResponseEntity.ok(" + serverName + ".get" + codeModel.getBeanName() + "ById(id));\n")
                .returns(saveReturnClass)
                .build();

        MethodSpec pageListMethod = MethodSpec.methodBuilder("pageListMethod")
                .addAnnotation(pageListAnnotation)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(domainClassName, domainName)
                .addParameter(int.class, "page")
                .addParameter(int.class, "pageSize")
                .addCode("return " + serverName + ".pageList(" + domainName + ", page, pageSize);\n")
                .returns(Page.class)
                .build();

        TypeSpec className = TypeSpec.classBuilder(codeModel.getControllerName())
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("@Author:LiuBingXu\n@Date: " + DateUtils.formateDate("yyyy/MM/dd") + "\n")
                .addAnnotation(RestController.class)
                .addField(fieldSpec)
                .addMethod(saveMethod)
                .addMethod(deleteMethod)
                .addMethod(infoMethod)
                .addMethod(pageListMethod)
                .build();

        JavaFile javaFile = JavaFile.builder(controllerPackage, className).build();
        outFile(javaFile);
    }

    private void outFile(JavaFile javaFile) {
        try {
            File file = new File((basePath + File.separator + "src" + File.separator + "main" + File.separator + "java"));
            javaFile.writeTo(System.out);
            javaFile.writeTo(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getSql() {
        StringBuffer sb = new StringBuffer();
        if ("mysql".equals(dataBaseType)) {
            sb.append("select COLUMN_NAME as name,column_comment as comment, data_type as dataType, if(column_key='PRI','true','false') from INFORMATION_SCHEMA.Columns\n" +
                    " where table_name='" + tableName + "' and table_schema= '" + dataBaseName + "'");
        } else if ("oracle".equals(dataBaseType)) {
            sb.append("select\n" +
                    "  utc.column_name as 字段名,utc.data_default 默认值,ucc.comments 注释,utc.data_type 数据类型,\n" +
                    "  CASE utc.nullable WHEN 'N' THEN '否' ELSE '是' END 可空,\n" +
                    "  UTC.table_name 表名,\n" +
                    "  CASE UTC.COLUMN_NAME\n" +
                    "  WHEN (select\n" +
                    "          col.column_name\n" +
                    "        from\n" +
                    "          user_constraints con,user_cons_columns col\n" +
                    "        where\n" +
                    "          con.constraint_name=col.constraint_name and con.constraint_type='P'\n" +
                    "          and col.table_name='BAS_EVENT')   THEN 'true' ELSE 'false' END AS 主键,utc.data_length 最大长度\n" +
                    "from\n" +
                    "  user_tab_columns utc,user_col_comments ucc\n" +
                    "where\n" +
                    "  utc.table_name = ucc.table_name\n" +
                    "  and utc.column_name = ucc.column_name\n" +
                    "  and utc.table_name = '" + tableName + "'\n" +
                    "order by\n" +
                    "  column_id;");
        }
        return sb.toString();
    }

    private void initBasePath() {
        this.basePath = this.getClass().getClassLoader().getResource("").getPath();
        try {
            basePath = URLDecoder.decode(basePath, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        basePath = basePath.substring(1, basePath.indexOf("/target"));
    }
}
