package com.zhangzhuorui.framework.mybatis.simplebasemapper;

import com.zhangzhuorui.framework.core.ZtBasicEntity;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate : 2017-01-01
 * @description :
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
public interface ZtSimpleBaseMapper<T extends ZtBasicEntity> extends ZtSimpleBaseSelectMapper<T>, ZtSimpleBaseInsertMapper<T>, ZtSimpleBaseDeleteMapper<T>, ZtSimpleBaseUpdateMapper<T> {

}
