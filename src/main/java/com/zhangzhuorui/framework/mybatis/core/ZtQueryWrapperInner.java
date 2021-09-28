package com.zhangzhuorui.framework.mybatis.core;

import com.zhangzhuorui.framework.core.ZtQueryTypeEnum;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate : 2017-01-01
 * @description : 需要拼接复杂SQL 例如 AND ( A OR B )
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
public class ZtQueryWrapperInner<T> {

    private ZtQueryTypeEnum ztQueryTypeEnum;

    private ZtQueryWrapper<T> ztInnerQueryWrapper;

    public ZtQueryTypeEnum getZtQueryTypeEnum() {
        return ztQueryTypeEnum;
    }

    public void setZtQueryTypeEnum(ZtQueryTypeEnum ztQueryTypeEnum) {
        this.ztQueryTypeEnum = ztQueryTypeEnum;
    }

    public ZtQueryWrapper<T> getZtInnerQueryWrapper() {
        return ztInnerQueryWrapper;
    }

    public void setZtInnerQueryWrapper(ZtQueryWrapper<T> ztInnerQueryWrapper) {
        this.ztInnerQueryWrapper = ztInnerQueryWrapper;
    }
}
