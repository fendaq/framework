package jpa.autocode.core;

import com.alibaba.fastjson.JSONObject;
import com.squareup.javapoet.*;
import jpa.autocode.bean.CodeModel;
import jpa.autocode.bean.Table;
import jpa.autocode.util.DateUtils;
import jpa.autocode.util.StringUtil;
import jpa.autocode.util.UUIDUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.lang.model.element.Modifier;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;

@Data
public class JavaCreate implements CreateCode {
    private final static Logger LOGGER = LoggerFactory.getLogger(JavaCreate.class);

    private EntityManager entityManager;
    protected String dataBaseName;
    protected CodeModel codeModel = new CodeModel();
    protected String tableName;// 表名
    protected String version = "V 1.0";// 版本
    protected String doMainPackage = "com.com.liubx.bean";// 实体类路径
    protected String servicePackage = "com.com.liubx.web.server";// service路径
    protected String serviceImplPackage = "com.com.liubx.web.server.impl";// service实现类路径
    protected String repositoryPackage = "com.com.liubx.web.repository";// repository类路径
    protected String controllerPackage = "com.com.liubx.web.controller";// controller类路径
    protected String basePath;// 绝对路径前缀

    public JavaCreate(EntityManager entityManager, String dataBaseName, String tableName, String doMainPackage,
                      String servicePackage, String serviceImplPackage, String repositoryPackage, String controllerPackage) {
        Assert.notNull(dataBaseName, "数据库名不能为空！");
        Assert.notNull(tableName, "表不能为空！");
        Assert.notNull(doMainPackage, "实体类路径不能为空！");
        Assert.notNull(servicePackage, "service 路径不能为空！");
        Assert.notNull(serviceImplPackage, "service 实现类路径不能为空！");
        Assert.notNull(repositoryPackage, "repository 包路径不能为空！");
        Assert.notNull(controllerPackage, "controller 包路径不能为空！");
        this.entityManager = entityManager;
        this.dataBaseName = dataBaseName;
        this.tableName = tableName;
        this.doMainPackage = doMainPackage;
        this.servicePackage = servicePackage;
        this.serviceImplPackage = serviceImplPackage;
        this.repositoryPackage = repositoryPackage;
        this.controllerPackage = controllerPackage;
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
        // 生成repository
        this.createRepository();

        Thread.sleep(1000);
        // 生成service接口
        this.createServiceClass();

        Thread.sleep(1000);
        // 生成service接口实现类
        this.createServiceClassImpl();

        Thread.sleep(1000);
        // 生成controller
        // this.createController();
    }

    /**
     * 生成domain
     *
     * @param tableName
     * @param tableList
     * @return
     */
    public boolean createDomainClass(String tableName, List<Table> tableList) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(codeModel.getBeanName());

        tableList.forEach(t -> {
            AnnotationSpec annotationSpecColumn = AnnotationSpec.builder(Column.class)// 属性上面的注解
                    .addMember("name", "$S", t.getName())
                    .build();
            if ("id".equals(t.getName())) {
                annotationSpecColumn = AnnotationSpec.builder(Id.class).build();
            }

            FieldSpec fieldSpec = FieldSpec.builder(String.class, t.getName(), Modifier.PRIVATE)// 添加属性
                    .addJavadoc(t.getComment())
                    .addAnnotation(annotationSpecColumn)
                    .build();
            builder.addField(fieldSpec);
            LOGGER.info("bean生成成功！");
        });

        AnnotationSpec annotationSpecTable = AnnotationSpec.builder(javax.persistence.Table.class)// 生成注解
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

    /**
     * 生成repository
     */
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

    /**
     * 生成service
     *
     * @return
     */
    public boolean createServiceClass() {
        ClassName beanClass = ClassName.bestGuess(doMainPackage + "." + codeModel.getBeanName());

        MethodSpec saveMethod = MethodSpec.methodBuilder("saveOrUpdate")
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)// 方法类型
                .addParameter(beanClass, codeModel.getBeanName().toLowerCase())// 方法参数
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
                .returns(JSONObject.class)
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
                .addCode("if ($T.isEmpty(" + beanParm + ".getId())) {\n" +
                        "  " + beanParm + ".setId($T.getUUID());\n" +
                        "}\n", StringUtils.class, UUIDUtils.class)
                .addStatement("$N.save($N)", repositoryName, beanParm)
                .returns(TypeName.VOID)
                .build();

        MethodSpec getMethod = MethodSpec.methodBuilder("get" + codeModel.getBeanName() + "ById")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "id")
                .addStatement("return  (\"1\").equals(id ) ? null : " + repositoryName + ".findById(id ).get()")
                .returns(beanClass)
                .build();

        MethodSpec deleteMethod = MethodSpec.methodBuilder("delete" + codeModel.getBeanName() + "ByIds")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "ids")
                .addCode("$T idArr = ids.split(\",\");\n" +
                        "    " + repositoryName + ".batchDelete($T.asList(idArr));\n" +
                        "return true;\n", String[].class, Arrays.class)
                .returns(TypeName.BOOLEAN)
                .build();

        MethodSpec addWhereMethod = MethodSpec.methodBuilder("addWhere")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(beanClass, codeModel.getBeanName().toLowerCase())
                .addParameter(List.class, "pamrms")
                .addCode("StringBuffer hql = new StringBuffer();\n" +
                        "return hql.toString();\n", StringBuffer.class)
                .returns(String.class)
                .build();

        MethodSpec pageListMethod = MethodSpec.methodBuilder("pageList")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(beanClass, codeModel.getBeanName().toLowerCase())
                .addParameter(int.class, "page")
                .addParameter(int.class, "pageSize")
                .addCode("$T result = new $T();\n" +
                        "$T pamrms = new $T();\n" +
                        "String appendHql = addWhere(" + codeModel.getBeanName().toLowerCase() + ", pamrms);\n" +
                        "result.put(\"result\",  " + repositoryName + ".listPageHql(\"from " + codeModel.getBeanName() + " where 1=1\" + appendHql, page, pageSize, pamrms));\n" +
                        "result.put(\"count\",  " + repositoryName + ".countHql(\"select count(*) from " + codeModel.getBeanName() + " where 1=1\" + appendHql, pamrms));\n" +
                        "return result;\n", JSONObject.class, JSONObject.class, List.class, ArrayList.class)
                .returns(JSONObject.class)
                .build();

        TypeSpec typeSpec = TypeSpec.classBuilder(codeModel.getServerImplName())
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("@Author:LiuBingXu\n@Date: " + DateUtils.formateDate("yyyy/MM/dd") + "\n")
                .addAnnotation(Service.class)
                .addSuperinterface(className)
                .addField(fieldSpec)
                .addMethod(saveMethod)
                .addMethod(getMethod)
                .addMethod(deleteMethod)
                .addMethod(addWhereMethod)
                .addMethod(pageListMethod)
                .build();

        JavaFile javaFile = JavaFile.builder(serviceImplPackage, typeSpec).build();
        outFile(javaFile);
        LOGGER.info("serviceImpl create success！");
    }

    private void createController() {
        ClassName serverClassName = ClassName.bestGuess(servicePackage + "." + codeModel.getServerName());
        ClassName domainClassName = ClassName.bestGuess(doMainPackage + "." + codeModel.getBeanName());
        Class saveReturnClass = Map.class;

        String serverName = StringUtil.firstLetterLowerCase(codeModel.getServerName());
        String domainName = StringUtil.firstLetterLowerCase(codeModel.getBeanName());

        AnnotationSpec saveAnnotation = AnnotationSpec
                .builder(RequestMapping.class)
                .addMember("name", "$S", "/" + domainName + "/add")
                .build();

        AnnotationSpec deleteAnnotation = AnnotationSpec
                .builder(PostMapping.class)
                .addMember("name", "$S", "/" + domainName + "/delByids")
                .build();

        AnnotationSpec infoAnnotation = AnnotationSpec
                .builder(RequestMapping.class)
                .addMember("name", "$S", "/" + domainName + "/info/{id}")
                .build();

        AnnotationSpec editAnnotation = AnnotationSpec
                .builder(RequestMapping.class)
                .addMember("name", "$S", "/" + domainName + "/edit/{id}")
                .build();

        FieldSpec fieldSpec = FieldSpec.builder(serverClassName, serverName, Modifier.PUBLIC)
                .addAnnotation(Autowired.class)
                .build();

        ParameterSpec infoParm = ParameterSpec.builder(String.class, "id")
                .addAnnotation(PathVariable.class)
                .build();

        MethodSpec saveMethod = MethodSpec.methodBuilder("add")
                .addAnnotation(saveAnnotation)
                .addAnnotation(ResponseBody.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(domainClassName, domainName)
                .addCode(" $T m = new $T();\n" +
                        "" + serverName + ".save(" + domainName + ");\n" +
                        " m.put(\"success\", true);\n" +
                        " return m;\n", Map.class, HashMap.class)
                .returns(saveReturnClass)
                .build();

        MethodSpec deleteMethod = MethodSpec.methodBuilder("delByids")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(deleteAnnotation)
                .addAnnotation(ResponseBody.class)
                .addParameter(String.class, "ids")
                .addCode("  $T m = new $T();\n" +
                        "   m.put(\"success\", " + serverName + ".delete" + codeModel.getBeanName() + "ByIds(ids));\n" +
                        "   return m;\n", Map.class, HashMap.class)
                .returns(saveReturnClass)
                .build();

        MethodSpec infoMethod = MethodSpec.methodBuilder("info")
                .addAnnotation(infoAnnotation)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(infoParm)
                .addParameter(Model.class, "model")
                .addCode("model.addAttribute(\"info\", " + serverName + ".get" + codeModel.getBeanName() + "ById(id));\n" +
                        " return \"user/info.html\";\n")
                .returns(String.class)
                .build();

        MethodSpec editMethod = MethodSpec.methodBuilder("edit")
                .addAnnotation(editAnnotation)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(infoParm)
                .addParameter(Model.class, "model")
                .addCode(" model.addAttribute(\"info\", " + serverName + ".get" + codeModel.getBeanName() + "ById(id));\n" +
                        "  return \"user/edit.html\";")
                .returns(String.class)
                .build();

        TypeSpec className = TypeSpec.classBuilder(codeModel.getControllerName())
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("@Author:LiuBingXu\n@Date: " + DateUtils.formateDate("yyyy/MM/dd") + "\n")
                .addAnnotation(Controller.class)
                .addField(fieldSpec)
                .addMethod(saveMethod)
                .addMethod(deleteMethod)
                .addMethod(infoMethod)
                .addMethod(editMethod)
                .build();

        JavaFile javaFile = JavaFile.builder(controllerPackage, className).build();
        outFile(javaFile);
    }

    /**
     * @param javaFile
     */
    private void outFile(JavaFile javaFile) {
        try {
            File file = new File((basePath + File.separator + "src" + File.separator + "main" + File.separator + "java"));
            javaFile.writeTo(System.out);
            javaFile.writeTo(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return
     */
    private String getSql() {
        return "select COLUMN_NAME as name,column_comment as comment from INFORMATION_SCHEMA.Columns\n" +
                " where table_name='" + tableName + "' and table_schema= '" + dataBaseName + "'";
    }

    private void initBasePath() {
        this.basePath = this.getClass().getClassLoader().getResource("").getPath();
        basePath = URLDecoder.decode(basePath);
        basePath = basePath.substring(1, basePath.indexOf("/target"));
    }
}
