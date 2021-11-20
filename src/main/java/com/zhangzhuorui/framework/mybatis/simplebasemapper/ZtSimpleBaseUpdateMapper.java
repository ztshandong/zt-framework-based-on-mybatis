package com.zhangzhuorui.framework.mybatis.simplebasemapper;

import com.zhangzhuorui.framework.core.ZtBasicEntity;
import com.zhangzhuorui.framework.mybatis.core.ZtQueryWrapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.UpdateProvider;

import java.io.Serializable;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate : 2017-01-01
 * @description :
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
public interface ZtSimpleBaseUpdateMapper<T extends ZtBasicEntity> extends Serializable {

    @UpdateProvider(
            type = ZtSimpleBaseUpdateProvider.class,
            method = "ztSimpleUpdateByPrimaryKey"
    )
    Integer ztSimpleUpdateByPrimaryKey(@Param(ZtTableInfoHelperStr.PARAM_NAME) ZtQueryWrapper qw);

    @UpdateProvider(
            type = ZtSimpleBaseUpdateProvider.class,
            method = "ztSimpleUpdateByParam"
    )
    Integer ztSimpleUpdateByParam(@Param("dest") ZtQueryWrapper dest, @Param(ZtTableInfoHelperStr.PARAM_NAME) ZtQueryWrapper qw);

}
