package com.zhangzhuorui.framework.mybatis.core;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate : 2017-01-01
 * @description : join 查询的包装类
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
public class ZtJoinWrapper<T> implements Serializable {

    /**
     * INNER JOIN /  LEFT JOIN  / RIGHT JOIN
     */
    private String joinType;

    /**
     * 表的别名  tableName AS t1
     */
    private String tableAliase;

    /**
     * t1 JOIN t2 ON t1.id = t2.xx  这个就是id
     */
    private String onLeftColumn;

    /**
     * t1 JOIN t2 ON t1.id = t2.xx  这个就是xx
     */
    private String onRightColumn;

    private ZtQueryWrapper ztQueryWrapper;

    public ZtJoinWrapper(ZtQueryWrapper ztQueryWrapper) {
        this(UUID.randomUUID().toString().replace("-", "").substring(0, 5) + "_" + ztQueryWrapper.getTableName(), ztQueryWrapper);
    }

    public ZtJoinWrapper(String tableAliase, ZtQueryWrapper ztQueryWrapper) {
        this.tableAliase = tableAliase;
        this.ztQueryWrapper = ztQueryWrapper;
    }

    public String getJoinType() {
        return joinType;
    }

    public void setJoinType(String joinType) {
        this.joinType = joinType;
    }

    public String getTableAliase() {
        return tableAliase;
    }

    public void setTableAliase(String tableAliase) {
        this.tableAliase = tableAliase;
    }

    public String getOnLeftColumn() {
        return onLeftColumn;
    }

    public void setOnLeftColumn(String onLeftColumn) {
        this.onLeftColumn = onLeftColumn;
    }

    public String getOnRightColumn() {
        return onRightColumn;
    }

    public void setOnRightColumn(String onRightColumn) {
        this.onRightColumn = onRightColumn;
    }

    public ZtQueryWrapper getZtQueryWrapper() {
        return ztQueryWrapper;
    }

    public void setZtQueryWrapper(ZtQueryWrapper ztQueryWrapper) {
        this.ztQueryWrapper = ztQueryWrapper;
    }
}
