package com.zhangzhuorui.framework.mybatis.simplebasemapper;

import com.zhangzhuorui.framework.mybatis.core.ZtQueryWrapper;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate : 2017-01-01
 * @description :
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
public class ZtTableInfoHelperStr {

    public static String DB_PRE = "";
    public static String DB_PRE_MYSQL = "`";
    public static String DB_PRE_MSSQL = "[";
    public static String DB_PRE_ORACLE = "\"";

    public static String DB_END = "";
    public static String DB_END_MYSQL = "`";
    public static String DB_END_MSSQL = "]";
    public static String DB_END_ORACLE = "\"";

    public static final String BASE_RESULT_MAP = "BaseResultMap";

    public static final String PARAM_NAME = "qw";

    private static final HashMap<String, String> TABLE_SELECT_SQL_MAP = new HashMap<>();

    //数据库保留字，关键字
    private static final List<String> ILLEGAL_NAMES = Arrays.asList("add", "analyze", "asc", "between", "blob", "call", "change", "check", "condition", "continue", "cross", "current_timestamp", "database", "day_microsecond", "dec", "default", "desc", "distinct", "double", "each", "enclosed", "exit", "fetch", "float8", "foreign", "goto", "having", "hour_minute", "ignore", "infile", "insensitive", "int1", "int4", "interval", "iterate", "keys", "leading", "like", "lines", "localtimestamp", "longblob", "low_priority", "mediumint", "minute_microsecond", "modifies", "no_write_to_binlog", "on", "optionally", "out", "precision", "purge", "read", "references", "rename", "require", "revoke", "schema", "select", "set", "spatial", "sqlexception", "sql_big_result", "ssl", "table", "tinyblob", "to", "true", "unique", "update", "using", "utc_timestamp", "varchar", "when", "with", "xor", "alter", "as", "before", "binary", "by", "case", "character", "column", "constraint", "create", "current_time", "cursor", "day_hour", "day_second", "declare", "delete", "deterministic", "div", "dual", "elseif", "exists", "false", "float4", "force", "fulltext", "group", "hour_microsecond", "if", "index", "inout", "int", "int3", "integer", "is", "key", "label", "left", "linear", "localtime", "long", "loop", "mediumblob", "middleint", "mod", "not", "numeric", "option", "order", "outfile", "procedure", "range", "real", "release", "replace", "return", "rlike", "second_microsecond", "separator", "smallint", "sql", "sqlwarning", "sql_small_result", "straight_join", "then", "tinytext", "trigger", "union", "unsigned", "use", "utc_time", "varbinary", "varying", "while", "x509", "zerofill", "all", "and", "asensitive", "bigint", "both", "cascade", "char", "collate", "connection", "convert", "current_date", "current_user", "databases", "day_minute", "decimal", "delayed", "describe", "distinctrow", "drop", "else", "escaped", "explain", "float", "for", "from", "grant", "high_priority", "hour_second", "in", "inner", "insert", "int2", "int8", "into", "join", "kill", "leave", "limit", "load", "lock", "longtext", "match", "mediumtext", "minute_second", "natural", "null", "optimize", "or", "outer", "primary", "raid0", "reads", "regexp", "repeat", "restrict", "right", "schemas", "sensitive", "show", "specific", "sqlstate", "sql_calc_found_rows", "starting", "terminated", "tinyint", "trailing", "undo", "unlock", "usage", "utc_date", "values", "varcharacter", "where", "write", "year_month", "count", "status", "comment");

    public static List<String> getIllegalNames() {
        return ILLEGAL_NAMES;
    }

    /**
     * @param columnName :
     * @return :  java.lang.String
     * @author :  zhangtao
     * @createDate :  2020-01-01
     * @description :  查询语句添加分隔符，防止保留字关键字报错
     * @updateUser :
     * @updateDate :
     * @updateRemark :
     */
    public static String getLegalColumnName(String columnName) {
        if (!columnName.startsWith(ZtTableInfoHelperStr.DB_PRE)) {
            columnName = ZtTableInfoHelperStr.DB_PRE + columnName + ZtTableInfoHelperStr.DB_END;
        }
        return columnName;
    }

    public static String getSelectColumnSql(ZtQueryWrapper qw) {
        String selectSql = TABLE_SELECT_SQL_MAP.get(qw.getTableName());
        if (StringUtils.isEmpty(selectSql)) {
            StringBuilder sb = new StringBuilder();
            List<ResultMapping> collect = ((ResultMap) qw.getResultMap()).getResultMappings().stream().filter(t -> t.getNestedQueryId() == null && t.getNestedResultMapId() == null && t.getColumn() != null).collect(Collectors.toList());
            Collections.sort(collect, new Comparator<ResultMapping>() {
                @Override
                public int compare(ResultMapping o1, ResultMapping o2) {
                    return o1.getColumn().compareTo(o2.getColumn());
                }
            });
            for (ResultMapping resultMapping : collect) {
                String property = resultMapping.getProperty();
                String columnName = ZtTableInfoHelperStr.getLegalColumnName(resultMapping.getColumn());
                sb.append(qw.getTableName()).append(".").append(columnName);
                if (!ILLEGAL_NAMES.contains(property)) {
                    // sb.append(" AS ").append(resultMapping.getProperty());
                }
                sb.append(", ");
            }
            sb.deleteCharAt(sb.length() - 2);
            selectSql = sb.toString();
            TABLE_SELECT_SQL_MAP.put(qw.getTableName(), selectSql);
        }
        return selectSql;
    }

    public static String getSelectByIdSql(ZtQueryWrapper qw) {
        String selectByIdSql = TABLE_SELECT_SQL_MAP.get(qw.getTableName() + "id");
        if (StringUtils.isEmpty(selectByIdSql)) {
            String selectColumnSql = getSelectColumnSql(qw);
            ResultMapping resultMapping = ((ResultMap) qw.getResultMap()).getIdResultMappings().get(0);
            selectByIdSql = "SELECT " + selectColumnSql + " FROM " + qw.getTableName() + " WHERE " + qw.getTableName() + "." + resultMapping.getColumn() + " = #{" + ZtTableInfoHelperStr.PARAM_NAME + ".obj." + resultMapping.getProperty() + "}";
            TABLE_SELECT_SQL_MAP.put(qw.getTableName() + "id", selectByIdSql);
        }
        return selectByIdSql;
    }
}
