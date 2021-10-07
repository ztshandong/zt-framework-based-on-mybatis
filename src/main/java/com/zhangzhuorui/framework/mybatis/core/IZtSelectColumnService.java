package com.zhangzhuorui.framework.mybatis.core;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate :  2021/10/7 下午5:28
 * @description : 是否允许select  字段级别权限控制
 * 自行实现逻辑
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
public interface IZtSelectColumnService {

    ZtSelectColumnHelper calCanSelect(ZtSelectColumnHelper ztSelectColumnHelper);

}
