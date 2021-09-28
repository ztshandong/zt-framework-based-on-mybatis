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

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(qw.getTableName()).append(" ( ");
        StringBuilder columnNames = new StringBuilder();
        StringBuilder values = new StringBuilder();
        for (int i = 0; i < conditons.size(); i++) {
            ZtQueryConditionEntity conditon = conditons.get(i);
            String fieldName = conditon.getFieldName();
            String columnName = ZtTableInfoHelperStr.getLegalColumnName(conditon.getColumnName());
            String idColumn = ZtTableInfoHelperStr.getLegalColumnName(idResultMapping.getColumn());
            if (!qw.isManualId()) {
                if (columnName.equals(idColumn)) {
                    continue;
                }
            }
            columnNames.append(columnName).append(", ");
            values.append("#{" + ZtTableInfoHelperStr.PARAM_NAME + ".obj.").append(fieldName).append("}").append(", ");
        }
        columnNames.deleteCharAt(columnNames.length() - 2);
        values.deleteCharAt(values.length() - 2);
        sb.append(columnNames);
        sb.append(" ) VALUES ( ");
        sb.append(values);
        sb.append(" ) ;");
        System.out.println(sb.toString());
        return sb.toString();
    }

    public String ztSimpleInsertBatch(@Param("list") List list, @Param(ZtTableInfoHelperStr.PARAM_NAME) ZtQueryWrapper qw) {
        LinkedList<ZtQueryConditionEntity> conditons = qw.getConditons();
        ResultMapping idResultMapping = ((ResultMap) qw.getResultMap()).getIdResultMappings().get(0);

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(qw.getTableName()).append(" ( ");
        StringBuilder columnNames = new StringBuilder();
        StringBuilder values = new StringBuilder();
        for (int i = 0; i < conditons.size(); i++) {
            ZtQueryConditionEntity conditon = conditons.get(i);
            String columnName = ZtTableInfoHelperStr.getLegalColumnName(conditon.getColumnName());
            String idColumn = ZtTableInfoHelperStr.getLegalColumnName(idResultMapping.getColumn());
            String fieldName = conditon.getFieldName();
            if (!qw.isManualId()) {
                if (columnName.equals(idColumn)) {
                    continue;
                }
            }
            columnNames.append(columnName).append(", ");
            values.append("#{list[(INDEX)].").append(fieldName).append("}").append(", ");
        }
        columnNames.deleteCharAt(columnNames.length() - 2);

        sb.append(columnNames);
        sb.append(" ) VALUES ");

        values.deleteCharAt(values.length() - 2);
        values.insert(0, " ( ");
        values.append(" ), ");
        String tmp = values.toString();
        values = new StringBuilder();
        for (int i = 0; i < qw.getObjList().size(); i++) {
            values.append(tmp.replace("(INDEX)", String.valueOf(i)));
        }
        values.deleteCharAt(values.length() - 2);

        sb.append(values);
        System.out.println(sb.toString());
        return sb.toString();
    }

}
