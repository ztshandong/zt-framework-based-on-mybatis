package com.zhangzhuorui.framework.mybatis.core;

import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate : 2017-01-01
 * @description :
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
public class ZtCodeGenerator {

    public static void main(String[] args) throws Exception {
        // ztGenCode();
    }

    private static void ztGenCode() throws Exception {
        String now = LocalDateTime.now().toString();
        String projectPath = System.getProperty("user.dir");

        //先根据其他代码生成器生成entity与xml。
        // 注意entity继承com.zhangzhuorui.framework.core.ZtBasicEntity<>并且添加范型
        // mapper继承com.zhangzhuorui.framework.mybatis.simplebasemapper.ZtSimpleBaseMapper
        // 然后改这几个变量后，生成mapper.java，service，serviceImpl，controller
        String entityName = "ZtFrameUse3";//已有的java实体名称
        String tableName = "zt_frame_use3";//表名
        String packageName = "com.zhangzhuorui.framework.mybatis";//包名
        String author = "zhangtao";//姓名
        String comment = "框架演示";//表注释

        Map<String, Object> map = new HashMap<>();
        map.put("packageName", packageName);
        map.put("entityName", entityName);
        map.put("tableName", tableName);
        map.put("date", now);
        map.put("author", author);
        map.put("comment", comment);

        String controllerStr = compileTemplate("templates/ZtCodeGen/controller.java.vm", map);
        String controllerPath = projectPath + "/src/main/java/com/zhangzhuorui/framework/mybatis/usedemo/" + entityName + "Controller.java";
        File controllerFile = new File(controllerPath);
        PrintStream ps = new PrintStream(new FileOutputStream(controllerFile));
        ps.println(controllerStr);

        String serviceStr = compileTemplate("templates/ZtCodeGen/service.java.vm", map);
        String servicePath = projectPath + "/src/main/java/com/zhangzhuorui/framework/mybatis/usedemo/I" + entityName + "Service.java";
        File serviceFile = new File(servicePath);
        ps = new PrintStream(new FileOutputStream(serviceFile));
        ps.println(serviceStr);

        String serviceImplStr = compileTemplate("templates/ZtCodeGen/serviceImpl.java.vm", map);
        String serviceImplPath = projectPath + "/src/main/java/com/zhangzhuorui/framework/mybatis/usedemo/" + entityName + "ServiceImpl.java";
        File serviceImplFile = new File(serviceImplPath);
        ps = new PrintStream(new FileOutputStream(serviceImplFile));
        ps.println(serviceImplStr);

        String mapperStr = compileTemplate("templates/ZtCodeGen/mapper.java.vm", map);
        String mapperPath = projectPath + "/src/main/java/com/zhangzhuorui/framework/mybatis/usedemo/" + entityName + "Mapper.java";
        File mapperFile = new File(mapperPath);
        ps = new PrintStream(new FileOutputStream(mapperFile));
        ps.println(mapperStr);

    }

    private static String compileTemplate(String resourceName, Map<String, Object> context)
            throws IOException {
        ClassPathResource cp = new ClassPathResource(resourceName);
        if (!cp.exists()) {
            return "";
        }

        return "";

        /*
        添加依赖使用代码生成器
        <dependency>
            <groupId>org.apache.velocity</groupId>
            <artifactId>velocity-engine-core</artifactId>
            <version>2.2</version>
        </dependency>

        VelocityEngine vEngine = new VelocityEngine();
        vEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        vEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        vEngine.setProperty(Velocity.ENCODING_DEFAULT, "UTF-8");
        vEngine.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
        vEngine.init();

        org.apache.velocity.Template template = vEngine.getTemplate(resourceName);
        VelocityContext velocityContext = new VelocityContext(context);

        Writer out = new StringWriter();
        template.merge(velocityContext, out);
        out.flush();
        return out.toString();
         */

    }
}
