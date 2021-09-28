package com.zhangzhuorui.framework.mybatis.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

@MappedTypes({List.class})
@MappedJdbcTypes({JdbcType.VARCHAR})
public class ZtFastjsonTypeHandler extends ZtAbstractJsonTypeHandler<List> {
    private static final Logger log = LoggerFactory.getLogger(ZtFastjsonTypeHandler.class);
    private Class<List> type;

    public ZtFastjsonTypeHandler(Class<List> type) throws Exception {
        if (log.isTraceEnabled()) {
            log.trace("FastjsonTypeHandler(" + type + ")");
        }
        if (type == null) {
            throw new Exception(type + "不能为空");
        }
        this.type = type;
    }

    @Override
    protected List parse(String json) {
        return JSON.parseObject(json, this.type);
    }

    @Override
    protected String toJson(List obj) {
        return JSON.toJSONString(obj, new SerializerFeature[]{SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullStringAsEmpty});
    }
}
