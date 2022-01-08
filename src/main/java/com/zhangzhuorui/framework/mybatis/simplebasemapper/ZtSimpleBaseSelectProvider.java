package com.zhangzhuorui.framework.mybatis.simplebasemapper;

import com.zhangzhuorui.framework.core.ZtQueryConditionEntity;
import com.zhangzhuorui.framework.core.ZtQueryInHelper;
import com.zhangzhuorui.framework.core.ZtQueryWrapperEnum;
import com.zhangzhuorui.framework.core.ZtStrUtils;
import com.zhangzhuorui.framework.mybatis.core.ZtJoinWrapper;
import com.zhangzhuorui.framework.mybatis.core.ZtQueryWrapper;
import com.zhangzhuorui.framework.mybatis.core.ZtQueryWrapperInner;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate : 2017-01-01
 * @description :
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
public class ZtSimpleBaseSelectProvider {

    protected static final Logger log = LoggerFactory.getLogger(ZtSimpleBaseSelectProvider.class);

    public String ztSimpleSelectByPrimaryKey(@Param(ZtTableInfoHelperStr.PARAM_NAME) ZtQueryWrapper qw) {
        String selectByIdSql = ZtTableInfoHelperStr.getSelectByIdSql(qw);
        return selectByIdSql;
    }

    public String ztSimpleSelectProviderMap(Map<String, Object> map) {
        ZtQueryWrapper qw = (ZtQueryWrapper) map.get(ZtTableInfoHelperStr.PARAM_NAME);
        return ztSimpleSelectProvider(qw);
    }

    /**
     * @param qw :
     * @return :  java.lang.String
     * @author :  张涛 zhangtao
     * @createDate :  2017-01-01
     * @description :  核心方法。拼接查询语句，并且进行了分页查询优化
     * @updateUser :
     * @updateDate :
     * @updateRemark :
     */
    public String ztSimpleSelectProvider(@Param(ZtTableInfoHelperStr.PARAM_NAME) ZtQueryWrapper qw) {
        StringBuilder finalSelectSql = new StringBuilder();
        finalSelectSql.append("SELECT ");
        if (qw.isCount()) {
            finalSelectSql.append("COUNT( ");
        }
        if (qw.isDistinctFlag()) {
            finalSelectSql.append("DISTINCT ");
        }
        String selectColumnStr = qw.getSelectColumn();
        if (!qw.isCount()) {
            if ("*".equals(selectColumnStr)) {
                selectColumnStr = ZtTableInfoHelperStr.getSelectColumnSql(qw);
            }
        }
        if (qw.isCount()) {
            selectColumnStr = selectColumnStr + " )";
        }
        StringBuilder fromWhereStr = new StringBuilder();
        fromWhereStr.append(" FROM ").append(qw.getTableName());

        List<String> joinWhereList = new LinkedList<>();
        List<ZtJoinWrapper> joinWrapperList = qw.getJoinWrapperList();
        for (int i = 0; i < joinWrapperList.size(); i++) {
            ZtJoinWrapper ztJoinWrapper = joinWrapperList.get(i);
            fromWhereStr.append(ztJoinWrapper.getJoinType()).append(" ");
            fromWhereStr.append(ztJoinWrapper.getZtQueryWrapper().getTableName()).append(" ");
            if (!StringUtils.isEmpty(ztJoinWrapper.getTableAliase())) {
                fromWhereStr.append(" AS ").append(ztJoinWrapper.getTableAliase());
            }
            fromWhereStr.append(" ON ").append(ztJoinWrapper.getOnLeftColumn()).append(" = ").append(ztJoinWrapper.getOnRightColumn());
            String whereSql1 = getWhereSql(ztJoinWrapper, i);
            joinWhereList.add(whereSql1);
        }

        fromWhereStr.append(" WHERE 1 = 1 ");
        String whereSql = getWhereSql(qw, null, null);
        fromWhereStr.append(whereSql);
        for (String joinWhere : joinWhereList) {
            fromWhereStr.append(joinWhere);
        }
        if (!qw.isCount()) {
            if (null != qw.getGroupBy()) {
                fromWhereStr.append(" GROUP BY ").append(qw.getTableName()).append(".").append(qw.getGroupBy());
            }
            if (null != qw.getOrderBy()) {
                fromWhereStr.append(" ORDER BY ").append(qw.getTableName()).append(".").append(qw.getOrderBy());
            } else {
                ResultMapping idResultMapping = ((ResultMap) qw.getResultMap()).getIdResultMappings().get(0);
                fromWhereStr.append(" ORDER BY ").append(qw.getTableName()).append(".").append(idResultMapping.getColumn()).append(" DESC ");
            }
            if (null != qw.getCurrent() && null != qw.getSize()) {
                if (qw.getCurrent().compareTo(1L) < 0) {
                    qw.setCurrent(1L);
                }
                if (qw.getSize().compareTo(1L) < 0) {
                    qw.setSize(20L);
                }
                if (ZtTableInfoHelperStr.DB_PRE.equalsIgnoreCase(ZtTableInfoHelperStr.DB_PRE_MYSQL)) {
                    fromWhereStr.append(" LIMIT ").append(((qw.getCurrent() - 1) * qw.getSize())).append(" , ").append(qw.getSize());
                } else if (ZtTableInfoHelperStr.DB_PRE.equalsIgnoreCase(ZtTableInfoHelperStr.DB_PRE_MSSQL)) {
                    fromWhereStr.append(" OFFSET ").append(((qw.getCurrent() - 1) * qw.getSize())).append(" ROWS FETCH NEXT ").append(qw.getSize()).append(" ROWS ONLY");
                } else if (ZtTableInfoHelperStr.DB_PRE.equalsIgnoreCase(ZtTableInfoHelperStr.DB_PRE_ORACLE)) {
                    fromWhereStr.append(" OFFSET ").append(((qw.getCurrent() - 1) * qw.getSize())).append(" ROWS FETCH FIRST ").append(qw.getSize()).append(" ROWS ONLY");
                } else {
                    //默认使用mysql语法
                    fromWhereStr.append(" LIMIT ").append(((qw.getCurrent() - 1) * qw.getSize())).append(" , ").append(qw.getSize());
                }
            }
        }

        if (joinWrapperList.size() <= 0
                && null != qw.getCurrent()
                && qw.getCurrent().compareTo(1L) > 0
                && !qw.isCount() && null == qw.getGroupBy()) {
            ResultMapping idResultMapping = ((ResultMap) qw.getResultMap()).getIdResultMappings().get(0);
            String idColumn = idResultMapping.getColumn();
            finalSelectSql.append(selectColumnStr).append(" FROM ").append(qw.getTableName()).append(" ")
                    .append(" INNER JOIN ( SELECT ").append(idColumn).append(" ").append(fromWhereStr.toString()).append(" ) AS b ")
                    .append(" ON ").append(qw.getTableName()).append(".").append(idColumn).append(" = b.").append(idColumn);
        } else {
            finalSelectSql.append(selectColumnStr).append(fromWhereStr.toString());
        }
        if (log.isDebugEnabled()) {
            log.debug(finalSelectSql.toString());
        }
        return finalSelectSql.toString();
    }

    public String getWhereSql(ZtJoinWrapper ztJoinWrapper, Integer index) {
        ZtQueryWrapper qw = ztJoinWrapper.getZtQueryWrapper();
        return getWhereSql(qw, index, ztJoinWrapper.getTableAliase());
    }

    public String getWhereSql(ZtQueryWrapper qw, Integer index, String tableAliase) {
        ResultMap resultMap = (ResultMap) qw.getResultMap();
        StringBuilder sb = new StringBuilder();
        String tbName = tableAliase;
        if (StringUtils.isEmpty(tbName)) {
            tbName = qw.getTableName();
        }
        LinkedList<ZtQueryConditionEntity> conditons = qw.getConditons();
        conditons.sort(new Comparator<ZtQueryConditionEntity>() {
            @Override
            public int compare(ZtQueryConditionEntity o1, ZtQueryConditionEntity o2) {
                return o1.getQueryType().getIntValue().compareTo(o2.getQueryType().getIntValue());
            }
        });
        for (int i = 0; i < conditons.size(); i++) {
            ZtQueryConditionEntity conditon = conditons.get(i);
            if (StringUtils.isEmpty(conditon.getColumnName())) {
                Optional<ResultMapping> any = resultMap.getResultMappings().stream().filter(t -> t.getProperty().equals(conditon.getFieldName())).findAny();
                if (any.isPresent()) {
                    conditon.setColumnName(any.get().getColumn());
                } else {
                    continue;
                }
            }

            sb.append(" ").append(conditon.getQueryType().name()).append(" ");
            sb.append(" ( ");
            String columnName = ZtTableInfoHelperStr.getLegalColumnName(conditon.getColumnName());
            sb.append(tbName).append(".").append(columnName).append(" ").append(conditon.getQueryWrapper().getStrValue());
            String join = "";
            if (index != null) {
                join = ".joinWrapperList[" + index + "].ztQueryWrapper";
            }
            if (conditon.getQueryWrapper().equals(ZtQueryWrapperEnum.IN) || conditon.getQueryWrapper().equals(ZtQueryWrapperEnum.NOT_IN)) {
                Object inCondition = conditon.getList();
                if (inCondition instanceof List) {
                    List list = (List) inCondition;
                    StringBuilder sb1 = new StringBuilder();
                    for (int i1 = 0; i1 < list.size(); i1++) {
                        sb1.append("#{" + ZtTableInfoHelperStr.PARAM_NAME + join + ".conditons[").append(i).append("].list[").append(i1).append("]} ,");
                    }
                    if (sb1.length() > 1) {
                        sb1.deleteCharAt(sb1.length() - 1);
                    } else {
                        sb1.append(ZtStrUtils.FALSE_SQL);
                    }
                    sb.append(" ( ").append(sb1.toString()).append(" ) ");
                } else if (inCondition instanceof ZtQueryWrapper) {
                    ZtQueryWrapper ztQueryWrapper = (ZtQueryWrapper) inCondition;
                    ztQueryWrapper.setCount(false);
                    ztQueryWrapper.setCurrent(null);
                    ztQueryWrapper.setSize(null);
                    String whereSql = ztSimpleSelectProvider(ztQueryWrapper);
                    sb.append(" ( ").append(whereSql).append(" ) ");
                } else if (inCondition instanceof ZtQueryInHelper) {
                    sb.append(" ( ").append(((ZtQueryInHelper) inCondition).getInSqlStr()).append(" ) ");
                }
            } else if (conditon.getQueryWrapper().equals(ZtQueryWrapperEnum.BETWEEN) || conditon.getQueryWrapper().equals(ZtQueryWrapperEnum.NOT_BETWEEN)) {
                sb.append(" #{" + ZtTableInfoHelperStr.PARAM_NAME + join + ".conditons[").append(i).append("].betweenStart}").append(" AND ").append("#{" + ZtTableInfoHelperStr.PARAM_NAME + ".conditons[").append(i).append("].betweenEnd} ");
            } else if (conditon.getQueryWrapper().equals(ZtQueryWrapperEnum.IS_NULL) || conditon.getQueryWrapper().equals(ZtQueryWrapperEnum.IS_NOT_NULL)) {

            } else {
                sb.append(" #{" + ZtTableInfoHelperStr.PARAM_NAME + join + ".obj.").append(conditon.getFieldName()).append("} ");
            }
            sb.append(" ) ");
        }

        //再来一遍
        List<ZtQueryWrapperInner> ztInnerQueryWrapperList = qw.getZtInnerQueryWrapperList();
        int ztInnerQueryWrapperSize = ztInnerQueryWrapperList.size();
        StringBuilder innerSb = new StringBuilder();

        for (int innerIndex = 0; innerIndex < ztInnerQueryWrapperSize; innerIndex++) {
            ZtQueryWrapperInner ztQueryWrapperInner = ztInnerQueryWrapperList.get(innerIndex);
            innerSb.append(" ").append(ztQueryWrapperInner.getZtQueryTypeEnum().name()).append(" ( 1 = 1 ");
            ZtQueryWrapper innerQw = ztQueryWrapperInner.getZtInnerQueryWrapper();
            ResultMap innerResultMap = (ResultMap) innerQw.getResultMap();

            LinkedList<ZtQueryConditionEntity> innerConditons = innerQw.getConditons();
            innerConditons.sort(new Comparator<ZtQueryConditionEntity>() {
                @Override
                public int compare(ZtQueryConditionEntity o1, ZtQueryConditionEntity o2) {
                    return o1.getQueryType().getIntValue().compareTo(o2.getQueryType().getIntValue());
                }
            });
            for (int i = 0; i < innerConditons.size(); i++) {
                ZtQueryConditionEntity innerConditon = innerConditons.get(i);
                if (StringUtils.isEmpty(innerConditon.getColumnName())) {
                    Optional<ResultMapping> any = innerResultMap.getResultMappings().stream().filter(t -> t.getProperty().equals(innerConditon.getFieldName())).findAny();
                    if (any.isPresent()) {
                        innerConditon.setColumnName(any.get().getColumn());
                    } else {
                        continue;
                    }
                }

                innerSb.append(" ").append(innerConditon.getQueryType().name()).append(" ");
                innerSb.append(" ( ");
                String innerColumnName = ZtTableInfoHelperStr.getLegalColumnName(innerConditon.getColumnName());
                innerSb.append(tbName).append(".").append(innerColumnName).append(" ").append(innerConditon.getQueryWrapper().getStrValue());
                String join = "";
                if (index != null) {
                    join = ".joinWrapperList[" + index + "].ztQueryWrapper";
                }
                if (innerConditon.getQueryWrapper().equals(ZtQueryWrapperEnum.IN) || innerConditon.getQueryWrapper().equals(ZtQueryWrapperEnum.NOT_IN)) {
                    Object innerInConditon = innerConditon.getList();
                    if (innerInConditon instanceof List) {
                        List innerList = (List) innerInConditon;
                        StringBuilder innerSb1 = new StringBuilder();
                        for (int i1 = 0; i1 < innerList.size(); i1++) {
                            innerSb1.append("#{" + ZtTableInfoHelperStr.PARAM_NAME + join + ".ztInnerQueryWrapperList[" + innerIndex + "].ztInnerQueryWrapper" + ".conditons[").append(i).append("].list[").append(i1).append("]} ,");
                        }
                        if (innerSb1.length() > 1) {
                            innerSb1.deleteCharAt(innerSb1.length() - 1);
                        } else {
                            innerSb1.append(ZtStrUtils.FALSE_SQL);
                        }
                        innerSb.append(" ( ").append(innerSb1.toString()).append(" ) ");
                    } else if (innerInConditon instanceof ZtQueryWrapper) {
                        ZtQueryWrapper ztQueryWrapper = (ZtQueryWrapper) innerInConditon;
                        ztQueryWrapper.setCount(false);
                        ztQueryWrapper.setCurrent(null);
                        ztQueryWrapper.setSize(null);
                        String whereSql = ztSimpleSelectProvider(ztQueryWrapper);
                        innerSb.append(" ( ").append(whereSql).append(" ) ");
                    } else if (innerInConditon instanceof ZtQueryInHelper) {
                        innerSb.append(" ( ").append(((ZtQueryInHelper) innerInConditon).getInSqlStr()).append(" ) ");
                    }
                } else if (innerConditon.getQueryWrapper().equals(ZtQueryWrapperEnum.BETWEEN) || innerConditon.getQueryWrapper().equals(ZtQueryWrapperEnum.NOT_BETWEEN)) {
                    innerSb.append(" #{" + ZtTableInfoHelperStr.PARAM_NAME + join + ".ztInnerQueryWrapperList[" + innerIndex + "].ztInnerQueryWrapper" + ".conditons[").append(i).append("].betweenStart}").append(" AND ").append("#{" + ZtTableInfoHelperStr.PARAM_NAME + ".conditons[").append(i).append("].betweenEnd} ");
                } else if (innerConditon.getQueryWrapper().equals(ZtQueryWrapperEnum.IS_NULL) || innerConditon.getQueryWrapper().equals(ZtQueryWrapperEnum.IS_NOT_NULL)) {

                } else {
                    innerSb.append(" #{" + ZtTableInfoHelperStr.PARAM_NAME + join + ".ztInnerQueryWrapperList[" + innerIndex + "].ztInnerQueryWrapper" + ".obj.").append(innerConditon.getFieldName()).append("} ");
                }
                innerSb.append(" ) ");
            }
            innerSb.append(" ) ");
        }
        sb.append(innerSb);
        if (null != qw.getNativeSql()) {
            sb.append(" ").append(qw.getNativeSql()).append(" ");
        }
        return sb.toString();
    }

}
