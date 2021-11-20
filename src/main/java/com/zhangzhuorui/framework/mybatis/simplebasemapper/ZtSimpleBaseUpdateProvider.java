package com.zhangzhuorui.framework.mybatis.simplebasemapper;

import com.zhangzhuorui.framework.core.ZtQueryConditionEntity;
import com.zhangzhuorui.framework.core.ZtQueryInHelper;
import com.zhangzhuorui.framework.core.ZtQueryWrapperEnum;
import com.zhangzhuorui.framework.core.ZtStrUtils;
import com.zhangzhuorui.framework.core.ZtUtils;
import com.zhangzhuorui.framework.mybatis.core.ZtQueryWrapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
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
public class ZtSimpleBaseUpdateProvider {

    public String ztSimpleUpdateByPrimaryKey(@Param(ZtTableInfoHelperStr.PARAM_NAME) ZtQueryWrapper qw) throws Exception {
        ResultMapping idResultMapping = ((ResultMap) qw.getResultMap()).getIdResultMappings().get(0);
        String idColumn = idResultMapping.getColumn();
        List<ResultMapping> resultMappings = ((ResultMap) qw.getResultMap()).getResultMappings();

        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ").append(qw.getTableName()).append(" SET ");
        LinkedList<ZtQueryConditionEntity> conditons = qw.getConditons();
        String versionFieldName = qw.getVersionFieldName();
        String versionColumnName = null;
        StringBuilder setStr = new StringBuilder();
        for (int i = 0; i < conditons.size(); i++) {
            ZtQueryConditionEntity conditon = conditons.get(i);
            String fieldName = conditon.getFieldName();
            String columnName = conditon.getColumnName();
            if (columnName.equals(idColumn)) {
                continue;
            }
            if (!StringUtils.isEmpty(versionFieldName) && fieldName.equals(versionFieldName)) {
                versionColumnName = columnName;
                Object obj = qw.getObj();
                Field versionField = ZtUtils.getField(obj, versionFieldName);
                versionField.setAccessible(true);
                if (versionField.getType().equals(Integer.class) || versionField.getType().equals(int.class)) {
                    Integer versionPlus = (Integer) versionField.get(obj);
                    if (versionPlus == null) {
                        versionPlus = 1;
                    }
                    versionPlus = versionPlus + 1;
                    setStr.append(ZtTableInfoHelperStr.getLegalColumnName(versionColumnName)).append(" = " + versionPlus + ", ");
                } else if (versionField.getType().equals(Long.class) || versionField.getType().equals(long.class)) {
                    Long versionPlus = (Long) versionField.get(obj);
                    if (versionPlus == null) {
                        versionPlus = 1L;
                    }
                    versionPlus = versionPlus + 1;
                    setStr.append(ZtTableInfoHelperStr.getLegalColumnName(versionColumnName)).append(" = " + versionPlus + ", ");
                }
            } else {
                if (conditon.getUpdateFieldUseNativeSql()) {
                    setStr.append(ZtTableInfoHelperStr.getLegalColumnName(columnName)).append(" = " + conditon.getUpdateFieldNativeSql() + ", ");
                } else {
                    ResultMapping resultMapping = resultMappings.stream().filter(t -> t.getColumn().equalsIgnoreCase(columnName)).findAny().get();
                    String s = resultMapping.getTypeHandler().getClass().getName();
                    setStr.append(ZtTableInfoHelperStr.getLegalColumnName(columnName)).append(" = #{" + ZtTableInfoHelperStr.PARAM_NAME + ".obj.").append(fieldName).append(", typeHandler = ").append(s).append(" }").append(", ");
                }
            }
        }
        setStr.deleteCharAt(setStr.length() - 2);
        sb.append(setStr);
        sb.append(" WHERE ").append(ZtTableInfoHelperStr.getLegalColumnName(idColumn)).append(" = #{" + ZtTableInfoHelperStr.PARAM_NAME + ".obj.").append(idResultMapping.getProperty()).append("}");
        if (!StringUtils.isEmpty(versionFieldName)) {
            sb.append(" AND ").append(ZtTableInfoHelperStr.getLegalColumnName(versionColumnName)).append(" = #{" + ZtTableInfoHelperStr.PARAM_NAME + ".obj.").append(versionFieldName).append("}");
        }
        return sb.toString();
    }

    public String ztSimpleUpdateByParam(ZtQueryWrapper dest, ZtQueryWrapper qw) throws Exception {
        ResultMapping idResultMapping = ((ResultMap) qw.getResultMap()).getIdResultMappings().get(0);
        List<ResultMapping> destResultMappings = ((ResultMap) dest.getResultMap()).getResultMappings();
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ").append(qw.getTableName()).append(" SET ");
        LinkedList<ZtQueryConditionEntity> destConditons = dest.getConditons();
        StringBuilder setStr = new StringBuilder();
        for (int i = 0; i < destConditons.size(); i++) {
            ZtQueryConditionEntity conditon = destConditons.get(i);
            String columnName = conditon.getColumnName();
            String fieldName = conditon.getFieldName();
            if (columnName.equals(idResultMapping.getColumn())) {
                continue;
            }
            if (conditon.getUpdateFieldUseNativeSql()) {
                setStr.append(columnName).append(" = " + conditon.getUpdateFieldNativeSql() + ", ");
            } else {
                ResultMapping resultMapping = destResultMappings.stream().filter(t -> t.getColumn().equalsIgnoreCase(columnName)).findAny().get();
                String s = resultMapping.getTypeHandler().getClass().getName();
                setStr.append(columnName).append(" = #{dest.obj.").append(fieldName).append(", typeHandler = ").append(s).append(" }").append(", ");
            }
        }
        setStr.deleteCharAt(setStr.length() - 2);
        sb.append(setStr);
        sb.append(" WHERE 1 = 1 ");

        String whereSql = getWhereSql(qw, null, null);
        sb.append(whereSql);

        System.out.println(sb.toString());
        return sb.toString();
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

            if (conditon.getQueryWrapper().equals(ZtQueryWrapperEnum.IN) || conditon.getQueryWrapper().equals(ZtQueryWrapperEnum.NOT_IN)) {
                Object inCondition = conditon.getList();
                if (inCondition instanceof List) {
                    List list = (List) inCondition;
                    StringBuilder sb1 = new StringBuilder();
                    for (int i1 = 0; i1 < list.size(); i1++) {
                        sb1.append("#{" + ZtTableInfoHelperStr.PARAM_NAME + ".conditons[").append(i).append("].list[").append(i1).append("]} ,");
                    }
                    if (sb1.length() > 1) {
                        sb1.deleteCharAt(sb1.length() - 1);
                    } else {
                        sb1.append(ZtStrUtils.FALSE_SQL);
                    }
                    sb.append(" ( ").append(sb1.toString()).append(" ) ");
                } else if (inCondition instanceof ZtQueryInHelper) {
                    sb.append(" ( ").append(((ZtQueryInHelper) inCondition).getInSqlStr()).append(" ) ");
                }
            } else if (conditon.getQueryWrapper().equals(ZtQueryWrapperEnum.BETWEEN) || conditon.getQueryWrapper().equals(ZtQueryWrapperEnum.NOT_BETWEEN)) {
                sb.append(" #{" + ZtTableInfoHelperStr.PARAM_NAME + ".conditons[").append(i).append("].betweenStart}").append(" AND ").append("#{" + ZtTableInfoHelperStr.PARAM_NAME + ".conditons[").append(i).append("].betweenEnd} ");
            } else if (conditon.getQueryWrapper().equals(ZtQueryWrapperEnum.IS_NULL) || conditon.getQueryWrapper().equals(ZtQueryWrapperEnum.IS_NOT_NULL)) {

            } else {
                sb.append(" #{" + ZtTableInfoHelperStr.PARAM_NAME + ".obj.").append(conditon.getFieldName()).append("} ");
            }
            sb.append(" ) ");
        }

        if (null != qw.getNativeSql()) {
            sb.append(" ").append(qw.getNativeSql()).append(" ");
        }
        return sb.toString();
    }

}
