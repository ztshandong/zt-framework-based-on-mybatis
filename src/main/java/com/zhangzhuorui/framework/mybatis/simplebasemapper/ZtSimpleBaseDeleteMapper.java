package com.zhangzhuorui.framework.mybatis.simplebasemapper;

import com.zhangzhuorui.framework.core.ZtBasicEntity;
import com.zhangzhuorui.framework.mybatis.core.ZtQueryWrapper;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.Param;

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
public interface ZtSimpleBaseDeleteMapper<T extends ZtBasicEntity> extends Serializable {

    @DeleteProvider(
            type = ZtSimpleBaseDeleteProvider.class,
            method = "ztSimpleDeleteByPrimaryKey"
    )
    Integer ztSimpleDeleteByPrimaryKey(@Param(ZtTableInfoHelperStr.PARAM_NAME) ZtQueryWrapper qw);

    @DeleteProvider(
            type = ZtSimpleBaseDeleteProvider.class,
            method = "ztSimpleDeleteByPrimaryKeyBatch"
    )
    Integer ztSimpleDeleteByPrimaryKeyBatch(@Param(ZtTableInfoHelperStr.PARAM_NAME) ZtQueryWrapper qw);
}
