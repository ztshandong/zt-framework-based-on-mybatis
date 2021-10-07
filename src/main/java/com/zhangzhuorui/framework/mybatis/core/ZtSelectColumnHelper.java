package com.zhangzhuorui.framework.mybatis.core;

import org.apache.ibatis.mapping.ResultMapping;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate :  2021/10/7 下午5:25
 * @description : 是否允许select  字段级别权限控制
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
public class ZtSelectColumnHelper {

    private Boolean canSelect = true;

    ZtQueryWrapper qw;

    ResultMapping resultMapping;

    public Boolean getCanSelect() {
        return canSelect;
    }

    public void setCanSelect(Boolean canSelect) {
        this.canSelect = canSelect;
    }

    public ZtQueryWrapper getQw() {
        return qw;
    }

    public void setQw(ZtQueryWrapper qw) {
        this.qw = qw;
    }

    public ResultMapping getResultMapping() {
        return resultMapping;
    }

    public void setResultMapping(ResultMapping resultMapping) {
        this.resultMapping = resultMapping;
    }
}
