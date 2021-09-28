package com.zhangzhuorui.framework.mybatis.core;

import com.zhangzhuorui.framework.core.ZtColumnUtil;
import com.zhangzhuorui.framework.core.ZtPropertyFunc;
import com.zhangzhuorui.framework.core.ZtQueryConditionEntity;
import com.zhangzhuorui.framework.core.ZtQueryTypeEnum;
import com.zhangzhuorui.framework.core.ZtQueryWrapperEnum;
import com.zhangzhuorui.framework.core.ZtUtils;
import com.zhangzhuorui.framework.mybatis.simplebasemapper.ZtTableInfoHelperStr;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate : 2017-01-01
 * @description :
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
public class ZtQueryWrapper<T> implements Serializable {

    //新增时是否需要手动插入id，默认使用数据库自增id
    private boolean manualId;

    //是否是查询count
    private boolean count;

    //数据库表名
    private String tableName;

    //乐观锁字段名
    private String versionFieldName;

    //逻辑删除字段名
    private String logicDeleteFieldName;

    //代表被删除
    private Boolean logicDeleteFlag;

    private Long current;

    private Long size;

    //单个操作（增删改查）
    private Object obj;

    //批量保存
    private List objList;

    private Object resultMap;

    private boolean distinctFlag;

    private String orderBy;

    private String selectColumn = "*";

    private String nativeSql;

    //单表查询条件
    private LinkedList<ZtQueryConditionEntity> conditons = new LinkedList<>();

    //内嵌查询条件
    private List<ZtQueryWrapperInner<T>> ztInnerQueryWrapperList = new LinkedList<>();

    //联表查询条件
    private List<ZtJoinWrapper> joinWrapperList = new LinkedList<>();

    public void andInnerQueryWrapper(ZtQueryWrapper<T> ztInnerQueryWrapper) {
        ZtQueryWrapperInner<T> ztQueryWrapperInner = new ZtQueryWrapperInner<>();
        ztQueryWrapperInner.setZtQueryTypeEnum(ZtQueryTypeEnum.AND);
        ztQueryWrapperInner.setZtInnerQueryWrapper(ztInnerQueryWrapper);
        ztInnerQueryWrapperList.add(ztQueryWrapperInner);
    }

    public void orInnerQueryWrapper(ZtQueryWrapper<T> ztInnerQueryWrapper) {
        ZtQueryWrapperInner<T> ztQueryWrapperInner = new ZtQueryWrapperInner<>();
        ztQueryWrapperInner.setZtQueryTypeEnum(ZtQueryTypeEnum.OR);
        ztQueryWrapperInner.setZtInnerQueryWrapper(ztInnerQueryWrapper);
        ztInnerQueryWrapperList.add(ztQueryWrapperInner);
    }

    public <T> String getColumnName(ZtJoinWrapper<T> ztJoinWrapper, ZtPropertyFunc<T, ?> fieldName) {
        String column;
        String getFieldName = ZtColumnUtil.getFieldName(fieldName);

        if (ztJoinWrapper == null) {
            ResultMapping resultMapping = ((ResultMap) this.resultMap).getResultMappings().stream().filter(t -> t.getProperty().equals(getFieldName)).findAny().get();
            column = ZtTableInfoHelperStr.getLegalColumnName(resultMapping.getColumn());
        } else {
            Object joinResultMap = ztJoinWrapper.getZtQueryWrapper().getResultMap();
            ResultMapping resultMapping = ((ResultMap) joinResultMap).getResultMappings().stream().filter(t -> t.getProperty().equals(getFieldName)).findAny().get();
            column = ZtTableInfoHelperStr.getLegalColumnName(resultMapping.getColumn());
        }
        return column;
    }

    public <T, T1> void innerJoin(ZtJoinWrapper<T> leftWrapper, ZtPropertyFunc<T, ?> leftField, ZtJoinWrapper<T1> rightWrapper, ZtPropertyFunc<T1, ?> rightField) {
        leftWrapper.setJoinType(" INNER JOIN ");
        doJoin(leftWrapper, leftField, rightWrapper, rightField);
    }

    public <T, T1> void rightJoin(ZtJoinWrapper<T> leftWrapper, ZtPropertyFunc<T, ?> leftField, ZtJoinWrapper<T1> rightWrapper, ZtPropertyFunc<T1, ?> rightField) {
        leftWrapper.setJoinType(" RIGHT JOIN ");
        doJoin(leftWrapper, leftField, rightWrapper, rightField);
    }

    public <T, T1> void leftJoin(ZtJoinWrapper<T> leftWrapper, ZtPropertyFunc<T, ?> leftField, ZtJoinWrapper<T1> rightWrapper, ZtPropertyFunc<T1, ?> rightField) {
        leftWrapper.setJoinType(" LEFT JOIN ");
        doJoin(leftWrapper, leftField, rightWrapper, rightField);
    }

    /**
     * @param leftWrapper  : 被连接表的wrapper
     * @param leftField    : 被连接表的join列
     * @param rightWrapper : 连接表的wrapper
     * @param rightField   : 连接表的join列
     * @return :  void
     * @author :  zhangtao
     * @createDate :  2021/9/25 上午9:50
     * @description :
     * @updateUser :
     * @updateDate :
     * @updateRemark :
     */
    public <T, T1> void doJoin(ZtJoinWrapper<T> leftWrapper, ZtPropertyFunc<T, ?> leftField, ZtJoinWrapper<T1> rightWrapper, ZtPropertyFunc<T1, ?> rightField) {
        String leftAliase = leftWrapper.getTableAliase();
        leftWrapper.setOnLeftColumn(" " + leftAliase + "." + getColumnName(leftWrapper, leftField) + " ");

        String rightAliase = this.getTableName();
        if (rightWrapper != null) {
            rightAliase = rightWrapper.getTableAliase();
        }
        leftWrapper.setOnRightColumn(" " + rightAliase + "." + getColumnName(rightWrapper, rightField) + " ");
        joinWrapperList.add(leftWrapper);
    }

    Map<String, String> selectSqlMap = new TreeMap<>();

    /**
     * @param ztJoinWrapper : 数据库表对应的wrapper
     * @return :  void
     * @author :  zhangtao
     * @createDate :  2021/9/25 上午9:53
     * @description :  添加某个表的所有列
     * @updateUser :
     * @updateDate :
     * @updateRemark :
     */
    public <T> void addAllColumn(ZtJoinWrapper<T> ztJoinWrapper) {
        String tableNameTmp = this.getTableName();
        List<ResultMapping> resultMappings = ((ResultMap) this.resultMap).getResultMappings();
        if (ztJoinWrapper != null) {
            tableNameTmp = ztJoinWrapper.getTableAliase();
            resultMappings = ((ResultMap) ztJoinWrapper.getZtQueryWrapper().getResultMap()).getResultMappings();
        }
        for (ResultMapping resultMapping : resultMappings) {
            if (resultMapping.getNestedQueryId() == null && resultMapping.getNestedResultMapId() == null && resultMapping.getColumn() != null) {
                String columnName = ZtTableInfoHelperStr.getLegalColumnName(resultMapping.getColumn());
                String s = tableNameTmp + "." + columnName + ", ";//" AS " + resultMapping.getProperty() +
                selectSqlMap.put(tableNameTmp + resultMapping.getColumn(), s);
            }
        }
    }

    /**
     * @param ztJoinWrapper : 对应数据库的查询条件wrapper
     * @param fieldName     : java字段名
     * @param aliasName     : sql字段别名 AS
     * @return :  void
     * @author :  zhangtao
     * @createDate :  2021/9/25 上午9:47
     * @description :  增加选择的列  select
     * @updateUser :
     * @updateDate :
     * @updateRemark :
     */
    public <T, T1> void addSelectColumn(ZtJoinWrapper<T> ztJoinWrapper, ZtPropertyFunc<T, ?> fieldName, ZtPropertyFunc<T1, ?> aliasName) {
        String aliasNameStr = null;
        if (aliasName != null) {
            aliasNameStr = ZtColumnUtil.getFieldName(aliasName);
        }
        addSelectColumn(ztJoinWrapper, fieldName, aliasNameStr);
    }

    private <T> void addSelectColumn(ZtJoinWrapper<T> ztJoinWrapper, ZtPropertyFunc<T, ?> fieldName, String aliasName) {
        String tableNameTmp = this.getTableName();
        String getFieldName = ZtColumnUtil.getFieldName(fieldName);
        ResultMapping resultMapping = null;

        if (ztJoinWrapper == null) {
            resultMapping = ((ResultMap) this.resultMap).getResultMappings().stream().filter(t -> t.getProperty().equals(getFieldName)).findAny().get();
        } else {
            resultMapping = ((ResultMap) ztJoinWrapper.getZtQueryWrapper().getResultMap()).getResultMappings().stream().filter(t -> t.getProperty().equals(getFieldName)).findAny().get();
            tableNameTmp = ztJoinWrapper.getTableAliase();
        }

        String aliasNameTmp = aliasName;
        if (StringUtils.isEmpty(aliasNameTmp)) {
            aliasNameTmp = getFieldName;
        }

        String columnName = ZtTableInfoHelperStr.getLegalColumnName(resultMapping.getColumn());
        String s = tableNameTmp + "." + columnName;
        if (!ZtTableInfoHelperStr.getIllegalNames().contains(aliasNameTmp)) {
            // s = s + " AS " + aliasNameTmp;
        }
        s = s + ", ";
        selectSqlMap.put(tableNameTmp + resultMapping.getColumn(), s);
    }

    /**
     * @param ztJoinWrapper :
     * @param fieldName     :
     * @return :  void
     * @author :  zhangtao
     * @createDate :  2021/9/25 上午10:31
     * @description :  从select中删除一个不需要的列
     * @updateUser :
     * @updateDate :
     * @updateRemark :
     */
    public <T> void removeSelectColumn(ZtJoinWrapper<T> ztJoinWrapper, ZtPropertyFunc<T, ?> fieldName) {
        String tableNameTmp = this.getTableName();
        String getFieldName = ZtColumnUtil.getFieldName(fieldName);
        ResultMapping resultMapping = null;

        if (ztJoinWrapper == null) {
            resultMapping = ((ResultMap) this.resultMap).getResultMappings().stream().filter(t -> t.getProperty().equals(getFieldName)).findAny().get();
        } else {
            resultMapping = ((ResultMap) ztJoinWrapper.getZtQueryWrapper().getResultMap()).getResultMappings().stream().filter(t -> t.getProperty().equals(getFieldName)).findAny().get();
            tableNameTmp = ztJoinWrapper.getTableAliase();
        }
        selectSqlMap.remove(tableNameTmp + resultMapping.getColumn());
    }

    public void buildSelectColumn() {
        StringBuilder selectSqlBuiler = new StringBuilder();
        Collection<String> values = selectSqlMap.values();
        for (String value : values) {
            selectSqlBuiler.append(value);
        }
        selectSqlBuiler.deleteCharAt(selectSqlBuiler.length() - 2);
        this.selectColumn = selectSqlBuiler.toString();
        selectSqlMap.clear();
    }

    public boolean isManualId() {
        return manualId;
    }

    public void setManualId(boolean manualId) {
        this.manualId = manualId;
    }

    public boolean isCount() {
        return count;
    }

    public void setCount(boolean count) {
        this.count = count;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getVersionFieldName() {
        return versionFieldName;
    }

    public void setVersionFieldName(String versionFieldName) {
        this.versionFieldName = versionFieldName;
    }

    public String getLogicDeleteFieldName() {
        return logicDeleteFieldName;
    }

    public void setLogicDeleteFieldName(String logicDeleteFieldName) {
        this.logicDeleteFieldName = logicDeleteFieldName;
    }

    public Boolean getLogicDeleteFlag() {
        return logicDeleteFlag;
    }

    public void setLogicDeleteFlag(Boolean logicDeleteFlag) {
        this.logicDeleteFlag = logicDeleteFlag;
    }

    public LinkedList<ZtQueryConditionEntity> getConditons() {
        return conditons;
    }

    public void setConditons(LinkedList<ZtQueryConditionEntity> conditons) {
        this.conditons = conditons;
    }

    public Long getCurrent() {
        return current;
    }

    public void setCurrent(Long current) {
        this.current = current;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public List getObjList() {
        return objList;
    }

    public void setObjList(List objList) {
        this.objList = objList;
    }

    public Object getResultMap() {
        return resultMap;
    }

    public void setResultMap(Object resultMap) {
        this.resultMap = resultMap;
    }

    public boolean isDistinctFlag() {
        return distinctFlag;
    }

    public void setDistinctFlag(boolean distinctFlag) {
        this.distinctFlag = distinctFlag;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getSelectColumn() {
        return selectColumn;
    }

    public void setSelectColumn(String selectColumn) {
        this.selectColumn = selectColumn;
    }

    public String getNativeSql() {
        return nativeSql;
    }

    public void setNativeSql(String nativeSql) {
        this.nativeSql = nativeSql;
    }

    public List<ZtJoinWrapper> getJoinWrapperList() {
        return joinWrapperList;
    }

    public void setJoinWrapperList(List<ZtJoinWrapper> joinWrapperList) {
        this.joinWrapperList = joinWrapperList;
    }

    public List<ZtQueryWrapperInner<T>> getZtInnerQueryWrapperList() {
        return ztInnerQueryWrapperList;
    }

    public void setZtInnerQueryWrapperList(List<ZtQueryWrapperInner<T>> ztInnerQueryWrapperList) {
        this.ztInnerQueryWrapperList = ztInnerQueryWrapperList;
    }

    //---------------------高能操作，优化体验-----------------------

    //默认是相等
    public ZtQueryWrapper<T> andEquals(ZtPropertyFunc<T, ?> func) {
        return opt(func, null, null, ZtQueryTypeEnum.AND, ZtQueryWrapperEnum.EQUALS);
    }

    public ZtQueryWrapper<T> orEquals(ZtPropertyFunc<T, ?> func) {
        return opt(func, null, null, ZtQueryTypeEnum.OR, ZtQueryWrapperEnum.EQUALS);
    }

    public ZtQueryWrapper<T> andLike(ZtPropertyFunc<T, ?> func) {
        return opt(func, null, null, ZtQueryTypeEnum.AND, ZtQueryWrapperEnum.LIKE);
    }

    public ZtQueryWrapper<T> orLike(ZtPropertyFunc<T, ?> func) {
        return opt(func, null, null, ZtQueryTypeEnum.OR, ZtQueryWrapperEnum.LIKE);
    }

    public ZtQueryWrapper<T> andNotLike(ZtPropertyFunc<T, ?> func) {
        return opt(func, null, null, ZtQueryTypeEnum.AND, ZtQueryWrapperEnum.NOT_LIKE);
    }

    public ZtQueryWrapper<T> orNotLike(ZtPropertyFunc<T, ?> func) {
        return opt(func, null, null, ZtQueryTypeEnum.OR, ZtQueryWrapperEnum.NOT_LIKE);
    }

    public ZtQueryWrapper<T> andNotEquals(ZtPropertyFunc<T, ?> func) {
        return opt(func, null, null, ZtQueryTypeEnum.AND, ZtQueryWrapperEnum.NOT_EQUALS);
    }

    public ZtQueryWrapper<T> orNotEquals(ZtPropertyFunc<T, ?> func) {
        return opt(func, null, null, ZtQueryTypeEnum.OR, ZtQueryWrapperEnum.NOT_EQUALS);
    }

    public ZtQueryWrapper<T> andGreatThan(ZtPropertyFunc<T, ?> func) {
        return opt(func, null, null, ZtQueryTypeEnum.AND, ZtQueryWrapperEnum.GREAT_THAN);
    }

    public ZtQueryWrapper<T> orGreatThan(ZtPropertyFunc<T, ?> func) {
        return opt(func, null, null, ZtQueryTypeEnum.OR, ZtQueryWrapperEnum.GREAT_THAN);
    }

    public ZtQueryWrapper<T> andLessThan(ZtPropertyFunc<T, ?> func) {
        return opt(func, null, null, ZtQueryTypeEnum.AND, ZtQueryWrapperEnum.LESS_THAN);
    }

    public ZtQueryWrapper<T> orLessThan(ZtPropertyFunc<T, ?> func) {
        return opt(func, null, null, ZtQueryTypeEnum.OR, ZtQueryWrapperEnum.LESS_THAN);
    }

    public ZtQueryWrapper<T> andGreatEquals(ZtPropertyFunc<T, ?> func) {
        return opt(func, null, null, ZtQueryTypeEnum.AND, ZtQueryWrapperEnum.GREAT_EQUALS);
    }

    public ZtQueryWrapper<T> orGreatEquals(ZtPropertyFunc<T, ?> func) {
        return opt(func, null, null, ZtQueryTypeEnum.OR, ZtQueryWrapperEnum.GREAT_EQUALS);
    }

    public ZtQueryWrapper<T> andLessEquals(ZtPropertyFunc<T, ?> func) {
        return opt(func, null, null, ZtQueryTypeEnum.AND, ZtQueryWrapperEnum.LESS_EQUALS);
    }

    public ZtQueryWrapper<T> orLessEquals(ZtPropertyFunc<T, ?> func) {
        return opt(func, null, null, ZtQueryTypeEnum.OR, ZtQueryWrapperEnum.LESS_EQUALS);
    }

    public ZtQueryWrapper<T> andIsNull(ZtPropertyFunc<T, ?> func) {
        return opt(func, null, null, ZtQueryTypeEnum.AND, ZtQueryWrapperEnum.IS_NULL);
    }

    public ZtQueryWrapper<T> orIsNull(ZtPropertyFunc<T, ?> func) {
        return opt(func, null, null, ZtQueryTypeEnum.OR, ZtQueryWrapperEnum.IS_NULL);
    }

    public ZtQueryWrapper<T> andIsNotNull(ZtPropertyFunc<T, ?> func) {
        return opt(func, null, null, ZtQueryTypeEnum.AND, ZtQueryWrapperEnum.IS_NOT_NULL);
    }

    public ZtQueryWrapper<T> orIsNotNull(ZtPropertyFunc<T, ?> func) {
        return opt(func, null, null, ZtQueryTypeEnum.OR, ZtQueryWrapperEnum.IS_NOT_NULL);
    }

    public ZtQueryWrapper<T> andIn(ZtPropertyFunc<T, ?> func, Object list) {
        return opt(func, list, null, ZtQueryTypeEnum.AND, ZtQueryWrapperEnum.IN);
    }

    public ZtQueryWrapper<T> orIn(ZtPropertyFunc<T, ?> func, Object list) {
        return opt(func, list, null, ZtQueryTypeEnum.OR, ZtQueryWrapperEnum.IN);
    }

    public ZtQueryWrapper<T> andNotIn(ZtPropertyFunc<T, ?> func, Object list) {
        return opt(func, list, null, ZtQueryTypeEnum.AND, ZtQueryWrapperEnum.NOT_IN);
    }

    public ZtQueryWrapper<T> orNotIn(ZtPropertyFunc<T, ?> func, Object list) {
        return opt(func, list, null, ZtQueryTypeEnum.OR, ZtQueryWrapperEnum.NOT_IN);
    }

    public ZtQueryWrapper<T> andBetween(ZtPropertyFunc<T, ?> func, Object firstValue, Object secondValue) {
        return opt(func, firstValue, secondValue, ZtQueryTypeEnum.AND, ZtQueryWrapperEnum.BETWEEN);
    }

    public ZtQueryWrapper<T> orBetween(ZtPropertyFunc<T, ?> func, Object firstValue, Object secondValue) {
        return opt(func, firstValue, secondValue, ZtQueryTypeEnum.OR, ZtQueryWrapperEnum.BETWEEN);
    }

    public ZtQueryWrapper<T> andNotBetween(ZtPropertyFunc<T, ?> func, Object firstValue, Object secondValue) {
        return opt(func, firstValue, secondValue, ZtQueryTypeEnum.AND, ZtQueryWrapperEnum.NOT_BETWEEN);
    }

    public ZtQueryWrapper<T> orNotBetween(ZtPropertyFunc<T, ?> func, Object firstValue, Object secondValue) {
        return opt(func, firstValue, secondValue, ZtQueryTypeEnum.OR, ZtQueryWrapperEnum.NOT_BETWEEN);
    }

    public ZtQueryWrapper<T> opt(ZtPropertyFunc<T, ?> func, Object firstValue, Object secondValue, ZtQueryTypeEnum ztQueryTypeEnum, ZtQueryWrapperEnum ztQueryWrapperEnum) {
        String fieldName = ZtColumnUtil.getFieldName(func);
        ZtQueryConditionEntity entity = new ZtQueryConditionEntity();

        Optional<ZtQueryConditionEntity> any = conditons.stream().filter(t -> t.getFieldName().equals(fieldName)).findAny();
        if (any.isPresent()) {
            entity = any.get();
        } else {
            entity.setFieldName(fieldName);
            conditons.add(entity);
        }
        entity.setQueryWrapper(ztQueryWrapperEnum);
        entity.setQueryType(ztQueryTypeEnum);

        if (firstValue instanceof List || firstValue instanceof ZtQueryWrapper) {
            entity.setList(firstValue);
        }

        if (ztQueryWrapperEnum.equals(ZtQueryWrapperEnum.LIKE) || ztQueryWrapperEnum.equals(ZtQueryWrapperEnum.NOT_LIKE)) {
            try {
                Field field = ZtUtils.getField(obj, fieldName);
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value == null) {
                    value = "";
                }
                String likeValue = "%" + value + "%";
                field.set(obj, likeValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (ztQueryWrapperEnum.equals(ZtQueryWrapperEnum.BETWEEN) || ztQueryWrapperEnum.equals(ZtQueryWrapperEnum.NOT_BETWEEN)) {
            entity.setBetweenStart(firstValue);
            entity.setBetweenEnd(secondValue);
        }
        return (ZtQueryWrapper<T>) this;
    }

}
