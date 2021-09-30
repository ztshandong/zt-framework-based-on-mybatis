package com.zhangzhuorui.framework.mybatis.core;

import com.alibaba.fastjson.JSON;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author :  zhangtao
 * @version :  1.0
 * @createDate : 2020-01-01
 * @description :
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */

// @MappedTypes({Object.class})
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.LONGNVARCHAR})
public class ZtFastjsonObjectTypeHandler<T extends Object> extends ZtAbstractJsonTypeHandler<T> {
    private static final Logger log = LoggerFactory.getLogger(ZtFastjsonObjectTypeHandler.class);
    private Class<T> type;

    public ZtFastjsonObjectTypeHandler(Class<T> type) throws Exception {
        if (log.isTraceEnabled()) {
            log.trace("FastjsonTypeHandler(" + type + ")");
        }
        if (type == null) {
            throw new Exception(type + "不能为空");
        }
        this.type = type;
    }

    @Override
    protected T parse(String json) {
        return JSON.parseObject(json, this.type);
    }

    @Override
    protected String toJson(T obj) {
        // return JSON.toJSONString(obj, new SerializerFeature[]{SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullStringAsEmpty});
        return JSON.toJSONString(obj);
    }
}
