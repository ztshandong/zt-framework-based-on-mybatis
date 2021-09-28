package com.zhangzhuorui.framework.mybatis.simplebasemapper;

import com.zhangzhuorui.framework.core.ZtQueryConditionEntity;
import com.zhangzhuorui.framework.core.ZtUtils;
import com.zhangzhuorui.framework.mybatis.core.ZtQueryWrapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.LinkedList;

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
        String idColumn = ZtTableInfoHelperStr.getLegalColumnName(idResultMapping.getColumn());

        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ").append(qw.getTableName()).append(" SET ");
        LinkedList<ZtQueryConditionEntity> conditons = qw.getConditons();
        String versionFieldName = qw.getVersionFieldName();
        String versionColumnName = null;
        StringBuilder setStr = new StringBuilder();
        for (int i = 0; i < conditons.size(); i++) {
            ZtQueryConditionEntity conditon = conditons.get(i);
            String fieldName = conditon.getFieldName();
            String columnName = ZtTableInfoHelperStr.getLegalColumnName(conditon.getColumnName());
            if (columnName.equals(idColumn)) {
                continue;
            }
            if (!StringUtils.isEmpty(versionFieldName) && fieldName.equals(versionFieldName)) {
                versionColumnName = columnName;
                Object obj = qw.getObj();
                Field versionField = ZtUtils.getField(obj, versionFieldName);
                versionField.setAccessible(true);
                Long versionPlus = (Long) versionField.get(obj);
                if (versionPlus == null) {
                    versionPlus = 1L;
                }
                versionPlus = versionPlus + 1;
                setStr.append(versionFieldName).append(" = " + versionPlus + ", ");
            } else {
                if (conditon.getUpdateFieldUseNativeSql()) {
                    setStr.append(columnName).append(" = " + conditon.getUpdateFieldNativeSql() + ", ");
                } else {
                    setStr.append(columnName).append(" = #{" + ZtTableInfoHelperStr.PARAM_NAME + ".obj.").append(fieldName).append("}, ");
                }
            }
        }
        setStr.deleteCharAt(setStr.length() - 2);
        sb.append(setStr);
        sb.append(" WHERE ").append(idColumn).append(" = #{" + ZtTableInfoHelperStr.PARAM_NAME + ".obj.").append(idResultMapping.getProperty()).append("}");
        if (!StringUtils.isEmpty(versionFieldName)) {
            sb.append(" AND ").append(versionColumnName).append(" = #{" + ZtTableInfoHelperStr.PARAM_NAME + ".obj.").append(versionFieldName).append("}");
        }
        System.out.println(sb.toString());
        return sb.toString();
    }

}
