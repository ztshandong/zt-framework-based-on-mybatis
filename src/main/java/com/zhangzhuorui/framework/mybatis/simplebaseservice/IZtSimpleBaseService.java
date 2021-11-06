package com.zhangzhuorui.framework.mybatis.simplebaseservice;

import com.zhangzhuorui.framework.core.ZtPropertyFunc;
import com.zhangzhuorui.framework.core.ZtQueryConditionEntity;
import com.zhangzhuorui.framework.core.ZtResBeanEx;
import com.zhangzhuorui.framework.mybatis.core.ZtParamEntity;
import com.zhangzhuorui.framework.mybatis.core.ZtQueryWrapper;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.List;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate : 2017-01-01
 * @description :
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
public interface IZtSimpleBaseService<T> {

    ZtParamEntity<T> getInitZtParamEntity(T obj);

    ZtParamEntity<T> getInitZtParamEntity(T obj, SqlCommandType sqlCommandType);

    ZtQueryWrapper<T> getInitZtQueryWrapper(T obj);

    ZtParamEntity<T> getInitZtParamEntityWithOutCount(T obj);

    ZtParamEntity<T> initSimpleWrapper(ZtParamEntity<T> ztParamEntity, SqlCommandType sqlCommandType);

    ZtPropertyFunc<T, ?> getTimeScopeField();

    ZtParamEntity<T> afterUseCommonZtQueryWrapper(ZtParamEntity<T> ztParamEntity, SqlCommandType sqlCommandType);

    ZtParamEntity<T> afterInitSimpleWrapper(ZtParamEntity<T> ztParamEntity, SqlCommandType sqlCommandType);

    // ZtQueryWrapper<T> getQueryWrapper(T obj, boolean writeMapNullValue, SqlCommandType sqlCommandType);

    // ZtQueryWrapper<T> afterGetQueryWrapper(ZtQueryWrapper<T> ztQueryWrapper, SqlCommandType sqlCommandType);

    /**
     * Service对应的表名，因为没有加额外的注解，所以只能用这种方法记录表名
     *
     * @return
     */
    String getTableName();

    /**
     * 乐观锁字段名。数据库必须是bigint，java必须是Long。一律从1开始
     *
     * @return
     */
    default String getVersionFieldName() {
        return null;
    }

    /**
     * 逻辑删除字段
     *
     * @return
     */
    default String getLogicDeleteFieldName() {
        return null;
    }

    /**
     * 代表删除的值
     *
     * @return
     */
    default Boolean getLogicDeleteFlag() {
        return true;
    }

    ;

    /**
     * 是否手动生成主键 默认否
     *
     * @return
     */
    default Boolean getManualId() {
        return false;
    }

    //根据字段名获取数据库列名
    String getColumnName(String fieldName);

    //根据字段名获取数据库列名
    String getColumnName(ZtPropertyFunc<T, ?> fieldName);

    void setZtQueryConditionEntity(ZtQueryConditionEntity entity, ZtPropertyFunc<T, ?> func);

    T getObj(ZtParamEntity<T> ztParamEntity);

    List<T> getList(ZtParamEntity<T> ztParamEntity);

    List<T> ztSimpleGetList(T t) throws Exception;

    T ztSimpleInsert(T t) throws Exception;

    List<T> ztSimpleInsertBatch(List<T> list) throws Exception;

    ZtResBeanEx ztSimpleDelete(T t) throws Exception;

    ZtResBeanEx ztSimpleUpdate(T t) throws Exception;

    /**
     * 查询所有数据。适用于一些数据量比较少的基础信息类的表，理论上这种数据都可以用缓存。业务表尽量不要用
     *
     * @return
     */
    ZtParamEntity<T> ztSimpleSelectAll() throws Exception;

    void refreshCache() throws Exception;

    void refreshCache(String cacheName);

    void refreshCacheByCurUserId(Long userId);

    Integer ztSimpleSelectProviderCount(ZtParamEntity<T> ztParamEntity);

    /**
     * 查询。可打印比较全的SQL。动态拼接查询条件
     *
     * @param ztParamEntity
     * @return
     */
    ZtParamEntity<T> ztSimpleSelectProviderWithoutCount(ZtParamEntity<T> ztParamEntity) throws Exception;

    /**
     * 单表查询。可打印比较全的SQL。动态拼接查询条件
     *
     * @param ztParamEntity
     * @return
     */
    ZtParamEntity<T> ztSimpleSelectProvider(ZtParamEntity<T> ztParamEntity) throws Exception;

    ZtParamEntity<T> ztBeforeSimpleSelectProvider(ZtParamEntity<T> ztParamEntity) throws Exception;

    // ZtParamEntity<T> ztDoSimpleSelectProvider(ZtParamEntity<T> ztParamEntity) throws Exception;

    ZtParamEntity<T> ztAfterSimpleSelectProvider(ZtParamEntity<T> ztParamEntity) throws Exception;

    /**
     * 联表查询
     *
     * @param ztParamEntity
     * @return
     * @throws Exception
     */
    ZtParamEntity ztDoSimpleJoinSelectProvider(ZtParamEntity ztParamEntity) throws Exception;

    /**
     * 根据id查询
     *
     * @param ztParamEntity
     * @return
     */
    ZtParamEntity<T> ztSimpleSelectByPrimaryKey(ZtParamEntity<T> ztParamEntity) throws Exception;

    ZtParamEntity<T> ztAfterSimpleSelectByPrimaryKey(ZtParamEntity<T> ztParamEntity) throws Exception;

    /**
     * 动态更新，只更不为null的字段。注意：""也不为null
     *
     * @param ztParamEntity
     * @return
     */
    ZtParamEntity<T> ztSimpleUpdateByPrimaryKey(ZtParamEntity<T> ztParamEntity) throws Exception;

    ZtParamEntity<T> ztBeforeSimpleUpdateByPrimaryKey(ZtParamEntity<T> ztParamEntity) throws Exception;

    // ZtParamEntity<T> ztDoSimpleUpdateByPrimaryKey(ZtParamEntity<T> ztParamEntity) throws Exception;

    ZtParamEntity<T> ztAfterSimpleUpdateByPrimaryKey(ZtParamEntity<T> ztParamEntity) throws Exception;

    ZtParamEntity<T> ztCannotSimpleUpdateByPrimaryKey(ZtParamEntity<T> ztParamEntity) throws Exception;

    /**
     * 根据主键删除
     *
     * @param ztParamEntity
     * @return
     */
    ZtParamEntity<T> ztSimpleDeleteByPrimaryKey(ZtParamEntity<T> ztParamEntity) throws Exception;

    ZtParamEntity<T> ztBeforeSimpleDeleteByPrimaryKey(ZtParamEntity<T> ztParamEntity) throws Exception;

    // ZtParamEntity<T> ztDoSimpleDeleteByPrimaryKey(ZtParamEntity<T> ztParamEntity) throws Exception;

    ZtParamEntity<T> ztAfterSimpleDeleteByPrimaryKey(ZtParamEntity<T> ztParamEntity) throws Exception;

    ZtParamEntity<T> ztCannotSimpleDeleteByPrimaryKey(ZtParamEntity<T> ztParamEntity) throws Exception;

    /**
     * 根据主键批量删除
     *
     * @param ztParamEntity
     * @return
     */
    ZtParamEntity<T> ztSimpleDeleteByPrimaryKeyBatch(ZtParamEntity<T> ztParamEntity) throws Exception;

    ZtParamEntity<T> ztBeforeSimpleDeleteByPrimaryKeyBatch(ZtParamEntity<T> ztParamEntity) throws Exception;

    // ZtParamEntity<T> ztDoSimpleDeleteByPrimaryKeyBatch(ZtParamEntity<T> ztParamEntity) throws Exception;

    ZtParamEntity<T> ztAfterSimpleDeleteByPrimaryKeyBatch(ZtParamEntity<T> ztParamEntity) throws Exception;

    ZtParamEntity<T> ztCannotSimpleDeleteByPrimaryKeyBatch(ZtParamEntity<T> ztParamEntity) throws Exception;

    /**
     * 单条新增。只保存有值的字段，其余字段使用数据库默认值。
     *
     * @param ztParamEntity
     * @return
     */
    ZtParamEntity<T> ztSimpleInsert(ZtParamEntity<T> ztParamEntity) throws Exception;

    ZtParamEntity<T> ztBeforeSimpleInsert(ZtParamEntity<T> ztParamEntity) throws Exception;

    // ZtParamEntity<T> ztDoSimpleInsert(ZtParamEntity<T> ztParamEntity) throws Exception;

    ZtParamEntity<T> ztAfterSimpleInsert(ZtParamEntity<T> ztParamEntity) throws Exception;

    ZtParamEntity<T> ztCannotSimpleInsert(ZtParamEntity<T> ztParamEntity) throws Exception;

    /**
     * 批量新增。保存所有字段
     *
     * @param ztParamEntity
     * @return
     */
    ZtParamEntity<T> ztSimpleInsertBatch(ZtParamEntity<T> ztParamEntity) throws Exception;

    ZtParamEntity<T> ztBeforeSimpleInsertBatch(ZtParamEntity<T> ztParamEntity) throws Exception;

    // ZtParamEntity<T> ztDoSimpleInsertBatch(ZtParamEntity<T> ztParamEntity) throws Exception;

    ZtParamEntity<T> ztAfterSimpleInsertBatch(ZtParamEntity<T> ztParamEntity) throws Exception;

    ZtParamEntity<T> ztCannotSimpleInsertBatch(ZtParamEntity<T> ztParamEntity) throws Exception;

}
