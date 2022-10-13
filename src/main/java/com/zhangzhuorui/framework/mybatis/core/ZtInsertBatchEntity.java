package com.zhangzhuorui.framework.mybatis.core;

import javax.validation.Valid;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate :  2022/10/10 上午11:14
 * @description :
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
public class ZtInsertBatchEntity<T> {

    @Valid
    ZtValidList<T> entityList;

    public ZtValidList<T> getEntityList() {
        return entityList;
    }

    public void setEntityList(ZtValidList<T> entityList) {
        this.entityList = entityList;
    }
}
