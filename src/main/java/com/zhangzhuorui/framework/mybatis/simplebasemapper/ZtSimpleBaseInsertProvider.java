package com.zhangzhuorui.framework.mybatis.simplebasemapper;

import com.zhangzhuorui.framework.core.ZtQueryConditionEntity;
import com.zhangzhuorui.framework.mybatis.core.ZtQueryWrapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;

import java.util.LinkedList;
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
public class ZtSimpleBaseInsertProvider {

    public String ztSimpleInsert(@Param(ZtTableInfoHelperStr.PARAM_NAME) ZtQueryWrapper qw) {
        LinkedList<ZtQueryConditionEntity> conditons = qw.getConditons();
        ResultMapping idResultMapping = ((ResultMap) qw.getResultMap()).getIdResultMappings().get(0);
        List<ResultMapping> resultMappings = ((ResultMap) qw.getResultMap()).getResultMappings();

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(qw.getTableName()).append(" ( ");
        StringBuilder columnNames = new StringBuilder();
        StringBuilder values = new StringBuilder();
        for (int i = 0; i < conditons.size(); i++) {
            ZtQueryConditionEntity conditon = conditons.get(i);
            String fieldName = conditon.getFieldName();
            String columnName = conditon.getColumnName();
            String idColumn = idResultMapping.getColumn();
            if (!qw.isManualId()) {
                if (columnName.equals(idColumn)) {
                    continue;
                }
            }
            ResultMapping resultMapping = resultMappings.stream().filter(t -> t.getColumn().equalsIgnoreCase(columnName)).findAny().get();
            String s = resultMapping.getTypeHandler().getClass().getName();
            columnNames.append(ZtTableInfoHelperStr.getLegalColumnName(columnName)).append(", ");
            values.append("#{" + ZtTableInfoHelperStr.PARAM_NAME + ".obj.").append(fieldName).append(", typeHandler = ").append(s).append(" }").append(", ");
        }
        columnNames.deleteCharAt(columnNames.length() - 2);
        values.deleteCharAt(values.length() - 2);
        sb.append(columnNames);
        sb.append(" ) VALUES ( ");
        sb.append(values);
        sb.append(" ) ;");
        return sb.toString();
    }

    public String ztSimpleInsertBatch(@Param("list") List list, @Param(ZtTableInfoHelperStr.PARAM_NAME) ZtQueryWrapper qw) {
        LinkedList<ZtQueryConditionEntity> conditons = qw.getConditons();
        ResultMapping idResultMapping = ((ResultMap) qw.getResultMap()).getIdResultMappings().get(0);
        List<ResultMapping> resultMappings = ((ResultMap) qw.getResultMap()).getResultMappings();

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(qw.getTableName()).append(" ( ");
        StringBuilder columnNames = new StringBuilder();
        StringBuilder values = new StringBuilder();
        for (int i = 0; i < conditons.size(); i++) {
            ZtQueryConditionEntity conditon = conditons.get(i);
            String columnName = conditon.getColumnName();
            String idColumn = idResultMapping.getColumn();
            String fieldName = conditon.getFieldName();
            if (!qw.isManualId()) {
                if (columnName.equals(idColumn)) {
                    continue;
                }
            }
            ResultMapping resultMapping = resultMappings.stream().filter(t -> t.getColumn().equalsIgnoreCase(columnName)).findAny().get();
            String s = resultMapping.getTypeHandler().getClass().getName();

            columnNames.append(ZtTableInfoHelperStr.getLegalColumnName(columnName)).append(", ");
            values.append("#{list[(INDEX)].").append(fieldName).append(", typeHandler = ").append(s).append(" }").append(", ");
        }
        columnNames.deleteCharAt(columnNames.length() - 2);

        sb.append(columnNames);
        sb.append(" ) VALUES ");

        values.deleteCharAt(values.length() - 2);
        values.insert(0, " ( ");
        values.append(" ), ");
        String tmp = values.toString();
        values = new StringBuilder();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            values.append(tmp.replace("(INDEX)", String.valueOf(i)));
        }
        values.deleteCharAt(values.length() - 2);

        sb.append(values);
        return sb.toString();
    }

}
