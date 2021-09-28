package com.zhangzhuorui.framework.mybatis.simplebasemapper;

import com.zhangzhuorui.framework.core.ZtBasicEntity;
import com.zhangzhuorui.framework.mybatis.core.ZtQueryWrapper;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
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
public interface ZtSimpleBaseInsertMapper<T extends ZtBasicEntity> extends Serializable {

    @InsertProvider(
            type = ZtSimpleBaseInsertProvider.class,
            method = "ztSimpleInsert"
    )
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = ZtTableInfoHelperStr.PARAM_NAME + ".obj.id")
    Integer ztSimpleInsert(@Param(ZtTableInfoHelperStr.PARAM_NAME) ZtQueryWrapper qw);

    @InsertProvider(
            type = ZtSimpleBaseInsertProvider.class,
            method = "ztSimpleInsertBatch"
    )
    @Options(useGeneratedKeys = true, keyProperty = "list.id")
    Integer ztSimpleInsertBatch(@Param("list") List<T> list, @Param(ZtTableInfoHelperStr.PARAM_NAME) ZtQueryWrapper qw);

}
