package com.zhangzhuorui.framework.mybatis.simplebasecontroller;

import com.zhangzhuorui.framework.core.ZtBasicEntity;
import com.zhangzhuorui.framework.core.ZtPage;
import com.zhangzhuorui.framework.core.ZtResBeanEx;
import com.zhangzhuorui.framework.core.ZtSpringUtil;
import com.zhangzhuorui.framework.core.ZtStrUtils;
import com.zhangzhuorui.framework.mybatis.core.ZtParamEntity;
import com.zhangzhuorui.framework.mybatis.core.ZtValidList;
import com.zhangzhuorui.framework.mybatis.simplebaseservice.IZtSimpleBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate : 2017-01-01
 * @description : Controller父类，标准接口，增删改查
 * 查询接口根据前端传入字段动态拼接查询语句
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
@Validated
public abstract class ZtSimpleBaseController<T extends ZtBasicEntity> {

    @Autowired
    IZtSimpleBaseService<T> service;

    @Autowired
    protected HttpServletRequest request;

    protected IZtSimpleBaseService<T> getIZtSimpleBaseService() {
        return service;
    }

    // @ApiOperation(value = "标准接口：获取所有枚举名。给前端看的，代码中用不到")
    @RequestMapping(value = ZtStrUtils.GET_ENUM_NAME, method = RequestMethod.GET)
    @ResponseBody
    public ZtResBeanEx getAllEnumName() {
        return ZtResBeanEx.ok(ZtSpringUtil.getEnumName());
    }

    // @ApiOperation(value = "标准接口：根据枚举名获取枚举内容，前端可以用")
    @RequestMapping(value = ZtStrUtils.GET_ENUM_INFO, method = RequestMethod.GET)
    @ResponseBody
    public ZtResBeanEx getEnumInfo(String enumName) {
        return ZtResBeanEx.ok(ZtSpringUtil.getEnumInfo(enumName));
    }

    // @ApiOperation(value = "标准接口：刷新本服务所有缓存")
    @RequestMapping(value = ZtStrUtils.REFRESH_CACHE, method = RequestMethod.POST)
    @ResponseBody
    public void refreshCache() throws Exception {
        getIZtSimpleBaseService().refreshCache();
    }

    // @ApiOperation(value = "标准接口：刷新本服务指定缓存")
    @RequestMapping(value = ZtStrUtils.REFRESH_CACHE_BY_NAME, method = RequestMethod.POST)
    @ResponseBody
    public void refreshCache(String cacheName) {
        getIZtSimpleBaseService().refreshCache(cacheName);
    }

    protected ZtParamEntity<T> beforeSelect(ZtParamEntity<T> ztParamEntity) {
        return ztParamEntity;
    }

    // @ApiOperation(value = "标准接口，查询。严格匹配查询条件，默认每页20条。默认返回所有列，可配合entity中的queryHelper返回指定列")
    @RequestMapping(value = ZtStrUtils.SELECT_SIMPLE, method = RequestMethod.POST)
    @ResponseBody
    public final ZtResBeanEx<ZtPage<T>> selectSimple(
            @RequestBody(required = false) T entity
    ) throws Exception {
        if (entity != null) {
            if (entity.getStart() == null) {
                entity.setStart(Long.valueOf(ZtStrUtils.START));
            }
            if (entity.getLimit() == null) {
                entity.setLimit(Long.valueOf(ZtStrUtils.LIMIT));
            }
        }
        ZtParamEntity<T> ztParamEntity = new ZtParamEntity<>();
        ztParamEntity.setZtResBeanEx(ZtResBeanEx.ok());
        ztParamEntity.setEntity(entity);
        ztParamEntity = this.beforeSelect(ztParamEntity);
        ztParamEntity = getIZtSimpleBaseService().ztSimpleSelectProvider(ztParamEntity);
        ztParamEntity = this.afterSelect(ztParamEntity);
        return ztParamEntity.getZtResBeanEx();
    }

    public ZtParamEntity<T> afterSelect(ZtParamEntity<T> ztParamEntity) {
        return ztParamEntity;
    }

    // @ApiOperation(value = "标准接口。根据id获取详情")
    @RequestMapping(value = ZtStrUtils.SELECT_ID_SIMPLE, method = RequestMethod.POST)
    @ResponseBody
    public final ZtResBeanEx<T> selectIdSimple(@RequestBody T t) throws Exception {
        ZtParamEntity<T> ztParamEntity = new ZtParamEntity<>();
        ztParamEntity.setZtResBeanEx(ZtResBeanEx.ok());
        ztParamEntity.setEntity(t);
        ztParamEntity = getIZtSimpleBaseService().ztSimpleSelectByPrimaryKey(ztParamEntity);
        ztParamEntity = this.afterSelectId(ztParamEntity);
        return ztParamEntity.getZtResBeanEx();
    }

    public ZtParamEntity<T> afterSelectId(ZtParamEntity<T> ztParamEntity) throws Exception {
        return ztParamEntity;
    }

    //这个默认不开放，需要的controller自己添加@RequestMapping
    // @Override
    // @RequestMapping(value = ZtStrUtils.SELECT_SIMPLE_ALL, method = RequestMethod.POST)
    // @ResponseBody
    // @ApiOperation(value = "标准接口。查询所有数据，最多1000条。适用于一些数据量比较少的基础信息类的表，理论上这种数据都可以用缓存。业务表尽量不要用。这个默认不开放，需要的controller自己添加@RequestMapping")
    public ZtResBeanEx<ZtPage<T>> ztSimpleSelectAll() throws Exception {
        ZtParamEntity<T> ztSimpleSelectAll = getIZtSimpleBaseService().ztSimpleSelectAll();
        return ztSimpleSelectAll.getZtResBeanEx();
    }

    //400 org.springframework.web.bind.MethodArgumentNotValidException
    // @ApiOperation(value = "标准接口：单条新增")
    @RequestMapping(value = ZtStrUtils.INSERT_SIMPLE, method = RequestMethod.POST)
    @ResponseBody
    public final ZtResBeanEx insertSimple(@Valid @RequestBody T entity) throws Exception {
        ZtParamEntity<T> ztParamEntity = new ZtParamEntity<>();
        ztParamEntity.setZtResBeanEx(ZtResBeanEx.ok(entity));
        ztParamEntity.setEntity(entity);
        ztParamEntity = this.beforeInsert(ztParamEntity);
        //controller层判断是否允许新增
        if (ztParamEntity.isCanInsert()) {
            ztParamEntity = getIZtSimpleBaseService().ztSimpleInsert(ztParamEntity);
            //service层还有判断，可能会不允许
            if (ztParamEntity.isCanInsert()) {
                ztParamEntity = this.afterInsert(ztParamEntity);
            } else {
                ztParamEntity = this.cannotInsert(ztParamEntity);
            }
        } else {
            ztParamEntity = this.cannotInsert(ztParamEntity);
        }
        return ztParamEntity.getZtResBeanEx();
    }

    //500 javax.validation.ConstraintViolationException
    // @ApiOperation(value = "标准接口：批量新增")
    @RequestMapping(value = ZtStrUtils.INSERT_BATCH_SIMPLE, method = RequestMethod.POST)
    @ResponseBody
    public final ZtResBeanEx insertBatchSimple(@Valid @RequestBody ZtValidList<T> entityList) throws Exception {
        ZtParamEntity<T> ztParamEntity = new ZtParamEntity<>();
        ztParamEntity.setZtResBeanEx(ZtResBeanEx.ok());
        ztParamEntity.setEntityList(entityList.getList());
        ztParamEntity = this.beforeInsertBatch(ztParamEntity);
        //controller层判断是否允许新增
        if (ztParamEntity.isCanInsert()) {
            ztParamEntity = getIZtSimpleBaseService().ztSimpleInsertBatch(ztParamEntity);
            //service层还有判断，可能会不允许
            if (ztParamEntity.isCanInsert()) {
                ztParamEntity = this.afterInsertBatch(ztParamEntity);
            } else {
                ztParamEntity = this.cannotInsertBatch(ztParamEntity);
            }
        } else {
            ztParamEntity = this.cannotInsertBatch(ztParamEntity);
        }
        return ztParamEntity.getZtResBeanEx();
    }

    // @ApiOperation(value = "标准接口：更新（只更新有值的列）")
    @RequestMapping(value = ZtStrUtils.UPDATE_SIMPLE, method = RequestMethod.POST)
    @ResponseBody
    public final ZtResBeanEx updateSimple(@RequestBody T entity) throws Exception {
        ZtParamEntity<T> ztParamEntity = new ZtParamEntity<>();
        ztParamEntity.setZtResBeanEx(ZtResBeanEx.ok(entity));
        ztParamEntity.setEntity(entity);
        ztParamEntity = this.beforeUpdate(ztParamEntity);
        if (ztParamEntity.isCanUpdate()) {
            ztParamEntity = this.doUpdate(ztParamEntity);
            if (ztParamEntity.isCanUpdate()) {
                ztParamEntity = this.afterUpdate(ztParamEntity);
            } else {
                ztParamEntity = this.cannotUpdate(ztParamEntity);
            }
        } else {
            ztParamEntity = this.cannotUpdate(ztParamEntity);
        }
        return ztParamEntity.getZtResBeanEx();
    }

    // @ApiOperation(value = "标准接口：根据id删除")
    @RequestMapping(value = ZtStrUtils.DELETE_SIMPLE, method = RequestMethod.POST)
    @ResponseBody
    public final ZtResBeanEx deleteSimple(@RequestBody T entity) throws Exception {
        ZtParamEntity<T> ztParamEntity = new ZtParamEntity<>();
        ztParamEntity.setZtResBeanEx(ZtResBeanEx.ok());
        ztParamEntity.setEntity(entity);
        ztParamEntity = this.beforeDelete(ztParamEntity);
        if (ztParamEntity.isCanDelete()) {
            ztParamEntity = getIZtSimpleBaseService().ztSimpleDeleteByPrimaryKey(ztParamEntity);
            if (ztParamEntity.isCanDelete()) {
                ztParamEntity = this.afterDelete(ztParamEntity);
            } else {
                ztParamEntity = this.cannotDelete(ztParamEntity);
            }
        } else {
            ztParamEntity = this.cannotDelete(ztParamEntity);
        }
        return ztParamEntity.getZtResBeanEx();
    }

    // @ApiOperation(value = "标准接口：根据ids批量删除")
    @RequestMapping(value = ZtStrUtils.DELETE_BATCH_SIMPLE, method = RequestMethod.POST)
    @ResponseBody
    public final ZtResBeanEx deleteBatchSimple(@RequestBody List<T> entityList) throws Exception {
        ZtParamEntity<T> ztParamEntity = new ZtParamEntity<>();
        ztParamEntity.setZtResBeanEx(ZtResBeanEx.ok());
        ztParamEntity.setEntityList(entityList);
        ztParamEntity = this.beforeDeleteBatch(ztParamEntity);
        if (ztParamEntity.isCanDelete()) {
            ztParamEntity = getIZtSimpleBaseService().ztSimpleDeleteByPrimaryKeyBatch(ztParamEntity);
            if (ztParamEntity.isCanDelete()) {
                ztParamEntity = this.afterDeleteBatch(ztParamEntity);
            } else {
                ztParamEntity = this.cannotDeleteBatch(ztParamEntity);
            }
        } else {
            ztParamEntity = this.cannotDeleteBatch(ztParamEntity);
        }
        return ztParamEntity.getZtResBeanEx();
    }

    protected ZtParamEntity<T> beforeInsert(ZtParamEntity<T> ztParamEntity) {
        return ztParamEntity;
    }

    protected ZtParamEntity<T> afterInsert(ZtParamEntity<T> ztParamEntity) {
        return ztParamEntity;
    }

    protected ZtParamEntity<T> cannotInsert(ZtParamEntity<T> ztParamEntity) {
        ztParamEntity.getZtResBeanEx().setCode(ZtStrUtils.FAIL_CODE);
        ztParamEntity.getZtResBeanEx().setMsg(ZtStrUtils.FAIL_MSG);
        return ztParamEntity;
    }

    protected ZtParamEntity<T> beforeUpdate(ZtParamEntity<T> ztParamEntity) {
        return ztParamEntity;
    }

    protected ZtParamEntity<T> doUpdate(ZtParamEntity<T> ztParamEntity) throws Exception {
        return getIZtSimpleBaseService().ztSimpleUpdateByPrimaryKey(ztParamEntity);
    }

    protected ZtParamEntity<T> afterUpdate(ZtParamEntity<T> ztParamEntity) {
        return ztParamEntity;
    }

    protected ZtParamEntity<T> cannotUpdate(ZtParamEntity<T> ztParamEntity) {
        ztParamEntity.getZtResBeanEx().setCode(ZtStrUtils.FAIL_CODE);
        ztParamEntity.getZtResBeanEx().setMsg(ZtStrUtils.FAIL_MSG);
        return ztParamEntity;
    }

    protected ZtParamEntity<T> beforeDelete(ZtParamEntity<T> ztParamEntity) {
        return ztParamEntity;
    }

    protected ZtParamEntity<T> afterDelete(ZtParamEntity<T> ztParamEntity) {
        return ztParamEntity;
    }

    protected ZtParamEntity<T> cannotDelete(ZtParamEntity<T> ztParamEntity) {
        ztParamEntity.getZtResBeanEx().setCode(ZtStrUtils.FAIL_CODE);
        ztParamEntity.getZtResBeanEx().setMsg(ZtStrUtils.FAIL_MSG);
        return ztParamEntity;
    }

    protected ZtParamEntity<T> beforeInsertBatch(ZtParamEntity<T> ztParamEntity) {
        return ztParamEntity;
    }

    protected ZtParamEntity<T> afterInsertBatch(ZtParamEntity<T> ztParamEntity) {
        return ztParamEntity;
    }

    protected ZtParamEntity<T> cannotInsertBatch(ZtParamEntity<T> ztParamEntity) {
        ztParamEntity.getZtResBeanEx().setCode(ZtStrUtils.FAIL_CODE);
        ztParamEntity.getZtResBeanEx().setMsg(ZtStrUtils.FAIL_MSG);
        return ztParamEntity;
    }

    protected ZtParamEntity<T> beforeDeleteBatch(ZtParamEntity<T> ztParamEntity) {
        return ztParamEntity;
    }

    protected ZtParamEntity<T> afterDeleteBatch(ZtParamEntity<T> ztParamEntity) {
        return ztParamEntity;
    }

    protected ZtParamEntity<T> cannotDeleteBatch(ZtParamEntity<T> ztParamEntity) {
        ztParamEntity.getZtResBeanEx().setCode(ZtStrUtils.FAIL_CODE);
        ztParamEntity.getZtResBeanEx().setMsg(ZtStrUtils.FAIL_MSG);
        return ztParamEntity;
    }

}
