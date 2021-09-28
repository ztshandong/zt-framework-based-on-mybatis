package com.zhangzhuorui.framework.mybatis.simplebasemapper;

import com.zhangzhuorui.framework.core.ZtBasicEntity;
import com.zhangzhuorui.framework.mybatis.core.ZtQueryWrapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.SelectProvider;

import java.io.Serializable;
import java.util.List;
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
public interface ZtSimpleBaseSelectMapper<T extends ZtBasicEntity> extends Serializable {

    @SelectProvider(
            type = ZtSimpleBaseSelectProvider.class,
            method = "ztSimpleSelectProvider"
    )
    @ResultMap("BaseResultMap")
    List<T> ztSimpleSelectProvider(@Param(ZtTableInfoHelperStr.PARAM_NAME) ZtQueryWrapper qw);

    @SelectProvider(
            type = ZtSimpleBaseSelectProvider.class,
            method = "ztSimpleSelectProvider"
    )
    List<Map<String, Object>> ztSimpleJoinSelectProvider(@Param(ZtTableInfoHelperStr.PARAM_NAME) ZtQueryWrapper qw);

    @SelectProvider(
            type = ZtSimpleBaseSelectProvider.class,
            method = "ztSimpleSelectProviderMap"
    )
    @ResultMap("BaseResultMap")
    List<T> ztSimpleSelectProviderMap(Map<String, Object> map);

    @SelectProvider(
            type = ZtSimpleBaseSelectProvider.class,
            method = "ztSimpleSelectProvider"
    )
    Integer ztSimpleSelectProviderCount(@Param(ZtTableInfoHelperStr.PARAM_NAME) ZtQueryWrapper qw);

    @SelectProvider(
            type = ZtSimpleBaseSelectProvider.class,
            method = "ztSimpleSelectByPrimaryKey"
    )
    @ResultMap("BaseResultMap")
    T ztSimpleSelectByPrimaryKey(@Param(ZtTableInfoHelperStr.PARAM_NAME) ZtQueryWrapper qw);

}
