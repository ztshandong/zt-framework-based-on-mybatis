package com.zhangzhuorui.framework.mybatis.enums;

import com.zhangzhuorui.framework.core.IZtBaseEnum;
import io.swagger.annotations.ApiModel;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate : 2017-01-01
 * @description :
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
// @ModuleName("框架枚举用法示例1")
@ApiModel(value = "框架枚举用法示例1", description = "框架枚举用法示例1")
public enum ZtFrameEnum implements IZtBaseEnum<ZtFrameEnum> {

    ONE1(11, "壹1"), TWO1(21, "贰1"), THREE1(31, "叁1");

    private final Integer intValue;

    private final String strValue;

    ZtFrameEnum(Integer intValue, String strValue) {
        this.intValue = intValue;
        this.strValue = strValue;
    }

    @Override
    public Integer getIntValue() {
        return intValue;
    }

    @Override
    public String getStrValue() {
        return strValue;
    }
}
