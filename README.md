# 张涛-基于Mybatis的极速开发框架。防脱发神器
### ```是故圣人不治已病治未病，不治已乱治未乱，此之谓也。夫病已成而后药之，乱已成而后治之，譬犹渴而穿井，斗而铸锥，不亦晚乎```
#
### ```好的开发，不治脱发，而是防止脱发```
#

# 项目开发首选，我要打十个
* ## 基于Mybatis的各种Provider实现各种功能，对原有架构零侵入，不添加任何依赖，仍然支持原有功能
* ## 模版模式，父类标准实现，子类只需要定义，杜绝复制粘贴
* ## 开闭原则，before，do，after，仿spring三部曲，杜绝开发人员随意发挥
* ## 范型约束，杜绝低级错误，避免使用Map交互，没有硬编码
* ## 可支持JOIN查询，生成极其复杂SQL，层次分明，逻辑清晰，没有各种眼花缭乱的XML，也不是堆在一起的一串建造者模式
* ## 开发效率高，开发成本低，代码可读性好，维护成本低，随时适应各种需求变化
* ## 项目代码量至少降低60%，一行顶过去十行，一口气做六个需求
* ## 数据库表建好，功能就完成一大半
#
# 常规项目痛点

* ### 后台开发：全是复制粘贴的代码？！新增修改删除的逻辑都在一个方法里？！sql这么复杂，看花眼了，还有拼错的？！到底这个表新增之后要做那些事？！这个方法是干嘛的？到底是在哪个节点会执行？！真不想维护这堆破烂，还一天到晚改需求 ... ...
* ### 前端开发：后台就不能统一一下调用方式和方法名吗？我也好封装，名字起的乱七八糟，调用方式五花八门，对接个接口真费劲！ ... ...
* ### 技术经理：让他们统一一下规范怎么就这么难呢？这个需求真的这么复杂吗？就一个单表的操作，怎么这么半天还没做出来？代码写的这么烂，以后怎么复用，都快没法维护了! ... ...
* ### 项目经理：项目不会又要延期了吧？我顶着这么多压力，给他们争取了这么多时间，这么多资源，这么多开发，天天加班，进度怎么还这么慢？ ... ...
* ### 产品经理：不就是加了个字段吗？不就是多了个查询条件吗？这都是常识，开发人员应该可以想到的！怎么要这么复杂？ ... ...
* ### 商务售前：之前那个项目怎么就不能拿过来用呢，这不是差不多吗？都答应客户了！ ... ...
* ### CTO & 架构师：唉，心累
* ### 老板：这帮家伙一天到晚都在干嘛？这个月还做不出来统统给老子滚蛋！
* ### 客户：我怎么感觉被他们忽悠了？感觉技术根本就不行！
#
### 使用方式
        <dependency>
            <groupId>com.zhangzhuorui.framework</groupId>
            <artifactId>mybatis</artifactId>
            <version>1.1.RELEASE</version>
        </dependency>
* # 只需要六个定义文件，即可实现增删改查的功能。entity和mapper.xml可以用现有的任何一款mybatis代码生成器生成。只需要mapper.xml中有resultMap id="BaseResultMap"就行。
* # mapper.java，service.java，serviceImpl.java，controller.java可以由项目中提供的代码生成器生成。就算不用代码生成器，也只是创建四个文件而已，花不了一分钟
* # 下面的代码就是实际在示例文档中的代码，只有定义，没有一行复制粘贴的冗余代码，可实现示例中增删改查所有功能
#
* ### entity类，继承ZtBasicEntity，`记得加主键范型`
```java
public class ZtFrameUse1 extends ZtBasicEntity<Long> {}
```

* ### mapper.java，继承ZtSimpleBaseMapper接口，`记得加entity范型`
```java
public interface ZtFrameUse1Mapper extends ZtSimpleBaseMapper<ZtFrameUse1> {}
```

* ### service接口，继承IZtSimpleBaseService，`记得加entity范型`
```java
public interface IZtFrameUse1Service extends IZtSimpleBaseService<ZtFrameUse1> {}
```

* ### serviceImpl，继承ZtSimpleBaseServiceImpl，实现相关service接口，`记得加entity范型`
```java
public class ZtFrameUse1ServiceImpl extends ZtSimpleBaseServiceImpl<ZtFrameUse1> implements IZtFrmeUse1Service {
    @Override
    public String getTableName() {
        return "zt_frame_use1";
    }
}
```

* ### controller 继承ZtSimpleBaseController，，`记得加entity范型`
```java
public class ZtFrameUse1Controller extends ZtSimpleBaseController<ZtFrameUse1> {}
```

* ### mapper.xml 可根据需要，继承ZtBaseResultMapMapper中相关ResultMap
```xml
 <resultMap id="BaseResultMap" type="com.zhangzhuorui.framework.mybatis.ZtFrameUse1"
extends="com.zhangzhuorui.framework.mybatis.simplebasemapper.ZtBaseResultMapMapper.ZtBaseLongIdResultMap">
```
#
# [打个招呼](https://ztshandong.github.io/)
# ```水平有限，也恳请各路大神指点```
## 微信 = 手机 = 邮箱
## 17091648421@126.com
#
# [示例项目代码github](https://github.com/ztshandong/zt-framework-mybatis-use-demo.git)
# [示例项目代码gitee](https://gitee.com/ztshandong/zt-framework-mybatis-use-demo.git)
# simpleuse为常规用法，正常的通用功能。```只需要定义，不需要实现```
```java
public class ZtFrameUse1Controller extends ZtSimpleBaseController<ZtFrameUse1> {}

public class ZtFrameUse1ServiceImpl extends ZtSimpleBaseServiceImpl<ZtFrameUse1> implements IZtFrameUse1Service {
    @Override
    public String getTableName() {return "zt_frame_use1";}
}
```
#
# advancduse为高级用法，有乐观锁与逻辑删除字段。拼接复杂SQL。```不是硬编码，不是在代码中写native sql```
* # 详见ZtFrameUse3ServiceImpl.advancedInstructions。
```sql
    SELECT DISTINCT
        ca57f_zt_frame_use2.`created_by`,
        ca57f_zt_frame_use2.`created_by_name`,
        ca57f_zt_frame_use2.`updated_by_name`,
        t1.`created_by`,
        t1.`created_by_name`,
        t1.`del_flag`,
        t1.`enable_flag`,
        t1.`enum_1`,
        t1.`gmt_create`,
        t1.`id`,
        t1.`remark`,
        t1.`udf_1`,
        t1.`udf_2`,
        t1.`updated_by`,
        t1.`updated_by_name`,
        zt_frame_use3.`created_by`,
        zt_frame_use3.`created_by_name`,
        zt_frame_use3.`delete_flag`,
        zt_frame_use3.`enum_1`,
        zt_frame_use3.`gmt_update`,
        zt_frame_use3.`id`,
        zt_frame_use3.`remark`,
        zt_frame_use3.`udf_1`,
        zt_frame_use3.`udf_2`,
        zt_frame_use3.`updated_by`,
        zt_frame_use3.`updated_by_name`,
        zt_frame_use3.`version`
    FROM
        zt_frame_use3
        LEFT JOIN zt_frame_use1 AS t1 ON t1.`udf_1` = zt_frame_use3.`udf_1`
        INNER JOIN zt_frame_use2 AS ca57f_zt_frame_use2 ON ca57f_zt_frame_use2.`id` = t1.`udf_2`
    WHERE
        1 = 1
        AND ( zt_frame_use3.`delete_flag` = FALSE )
        AND ( zt_frame_use3.`enum_1` = 'ONE1' )
        AND ( zt_frame_use3.`id` IN ( SELECT id FROM zt_frame_use2 WHERE 1 = 1 ) )
        AND (
            1 = 1
            AND ( zt_frame_use3.`delete_flag` = FALSE )
            OR ( zt_frame_use3.`remark` LIKE '%%备注%%' )
            OR ( zt_frame_use3.`created_by` IN ( '张三', '李四' ) )
        )
        OR (
            1 = 1
            AND ( zt_frame_use3.`delete_flag` = FALSE )
            AND ( zt_frame_use3.`remark` LIKE '%%备注%%' )
            AND ( zt_frame_use3.`created_by` IN ( '张三', '李四' ) )
        )
        AND ( t1.`gmt_update` IS NOT NULL )
        OR ( t1.`remark` LIKE '%aaa%' )
        OR ( t1.`udf_1` IN ( 'bbb', 'ccc' ) )
        AND (
            1 = 1
            AND ( t1.`gmt_update` IS NOT NULL )
            OR ( t1.`remark` LIKE '%inneraaa%' )
            OR ( t1.`udf_1` IN ( 'innerbbb', 'innerccc' ) )
            OR ( t1.`id` NOT IN ( SELECT id FROM zt_frame_use2 WHERE 1 = 1 ) )
        )
        AND ( ca57f_zt_frame_use2.`remark` IN ( 'fff', 'ggg' ) )
        OR ( ca57f_zt_frame_use2.`udf_1` LIKE '%d%' )
        LIMIT 0,20
```
#
# extenduse为扩展用法，可扩展需要的功能。```4个Extend父类```