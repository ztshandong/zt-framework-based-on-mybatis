package com.zhangzhuorui.framework.mybatis.simplebasemapper;

import com.zhangzhuorui.framework.mybatis.core.ZtQueryWrapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.springframework.util.StringUtils;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate : 2017-01-01
 * @description :
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
public class ZtSimpleBaseDeleteProvider {

    public String ztSimpleDeleteByPrimaryKey(@Param(ZtTableInfoHelperStr.PARAM_NAME) ZtQueryWrapper qw) {
        String delSql = null;
        ResultMapping idResultMapping = ((ResultMap) qw.getResultMap()).getIdResultMappings().get(0);
        String idColumn = ZtTableInfoHelperStr.getLegalColumnName(idResultMapping.getColumn());

        String logicDeleteFieldName = qw.getLogicDeleteFieldName();
        if (!StringUtils.isEmpty(logicDeleteFieldName)) {
            ResultMap resultMap = (ResultMap) qw.getResultMap();
            ResultMapping resultMapping = resultMap.getResultMappings().stream().filter(t -> t.getProperty().equals(logicDeleteFieldName)).findAny().get();
            String columnName = ZtTableInfoHelperStr.getLegalColumnName(resultMapping.getColumn());
            delSql = "UPDATE " + qw.getTableName() + " SET " + columnName + " = " + qw.getLogicDeleteFlag() + " WHERE " + idColumn + " = #{" + ZtTableInfoHelperStr.PARAM_NAME + ".obj." + idResultMapping.getProperty() + "}";
        } else {
            delSql = "DELETE FROM " + qw.getTableName() + " WHERE " + idColumn + " = #{" + ZtTableInfoHelperStr.PARAM_NAME + ".obj." + idResultMapping.getProperty() + "}";
        }
        System.out.println(delSql);
        return delSql;
    }

    public String ztSimpleDeleteByPrimaryKeyBatch(@Param(ZtTableInfoHelperStr.PARAM_NAME) ZtQueryWrapper qw) {
        ResultMapping idResultMapping = ((ResultMap) qw.getResultMap()).getIdResultMappings().get(0);
        String idColumn = ZtTableInfoHelperStr.getLegalColumnName(idResultMapping.getColumn());

        String logicDeleteFieldName = qw.getLogicDeleteFieldName();
        StringBuilder delSb = new StringBuilder();
        StringBuilder delIdsStr = new StringBuilder();
        for (int i = 0; i < qw.getObjList().size(); i++) {
            delIdsStr.append(" #{" + ZtTableInfoHelperStr.PARAM_NAME + ".objList[").append(i).append("].").append(idResultMapping.getProperty()).append("}").append(", ");
        }
        delIdsStr.deleteCharAt(delIdsStr.length() - 2);

        if (!StringUtils.isEmpty(logicDeleteFieldName)) {
            ResultMap resultMap = (ResultMap) qw.getResultMap();
            ResultMapping resultMapping = resultMap.getResultMappings().stream().filter(t -> t.getProperty().equals(logicDeleteFieldName)).findAny().get();
            String columnName = ZtTableInfoHelperStr.getLegalColumnName(resultMapping.getColumn());

            delSb.append("UPDATE ").append(qw.getTableName()).append(" SET ").append(columnName).append(" = ").append(qw.getLogicDeleteFlag()).append(" WHERE ").append(idColumn).append(" IN ( ").append(delIdsStr).append(")");
        } else {
            delSb.append("DELETE FROM ").append(qw.getTableName()).append(" WHERE ").append(idColumn).append(" IN ( ").append(delIdsStr).append(")");
        }

        System.out.println(delSb.toString());
        return delSb.toString();
    }

}
