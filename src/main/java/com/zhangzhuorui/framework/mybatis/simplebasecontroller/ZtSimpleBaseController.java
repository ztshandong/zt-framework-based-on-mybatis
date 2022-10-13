package com.zhangzhuorui.framework.mybatis.simplebasecontroller;

import com.zhangzhuorui.framework.core.ZtBasicEntity;
import com.zhangzhuorui.framework.core.ZtBasicNumberIdEntity4Swagger;
import com.zhangzhuorui.framework.core.ZtPage;
import com.zhangzhuorui.framework.core.ZtResBeanEx;
import com.zhangzhuorui.framework.core.ZtResBeanExConfig;
import com.zhangzhuorui.framework.core.ZtSpringUtil;
import com.zhangzhuorui.framework.core.ZtStrUtils;
import com.zhangzhuorui.framework.mybatis.core.ZtInsertBatchEntity;
import com.zhangzhuorui.framework.mybatis.core.ZtParamEntity;
import com.zhangzhuorui.framework.mybatis.core.ZtValidList;
import com.zhangzhuorui.framework.mybatis.simplebaseservice.IZtSimpleBaseService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ClassUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.beans.Introspector;
import java.util.LinkedList;
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
    ZtResBeanExConfig ztResBeanExConfig;

    public ZtResBeanExConfig getZtResBeanExConfig() {
        return ztResBeanExConfig;
    }

    @Autowired
    protected HttpServletRequest request;

    public HttpServletRequest getRequest() {
        return request;
    }

    @Autowired
    IZtSimpleBaseService<T> iZtSimpleBaseService;

    protected IZtSimpleBaseService<T> getIZtSimpleBaseService() {
        return iZtSimpleBaseService;
    }

    protected ZtSimpleBaseController<T> getThisController() {
        String shortClassName = ClassUtils.getShortName(this.getClass());
        String beanName = Introspector.decapitalize(shortClassName);
        Object bean1 = ZtSpringUtil.getBean(beanName);
        return (ZtSimpleBaseController<T>) bean1;
    }

    @ApiOperation(value = "标准枚举接口:获取所有枚举名及描述。", notes = ZtStrUtils.GET_ENUM_NAME_RES_SWAGGER)
    @RequestMapping(value = ZtStrUtils.GET_ENUM_NAME, method = {RequestMethod.GET})
    @ResponseBody
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ZtStrUtils.APIRESPONSE_MESSAGE, response = Object.class)
    })
    @ApiOperationSupport(order = 6000)
    public ZtResBeanEx getAllEnumName() {
        return ZtResBeanEx.ok(ZtSpringUtil.getEnumName());
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "enumName", value = "枚举名称。getEnumName接口返回的数据", paramType = "query", required = true)})
    @ApiOperation(value = "标准枚举接口:根据枚举名获取枚举内容，前端可以用", notes = ZtStrUtils.GET_ENUM_INFO_RES_SWAGGER)
    @RequestMapping(value = ZtStrUtils.GET_ENUM_INFO, method = {RequestMethod.GET})
    @ResponseBody
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ZtStrUtils.APIRESPONSE_MESSAGE, response = Object.class)
    })
    @ApiOperationSupport(order = 6001)
    public ZtResBeanEx getEnumInfo(@RequestParam String enumName) {
        return ZtResBeanEx.ok(ZtSpringUtil.getEnumInfo(enumName));
    }

    @ApiOperation(value = "标准缓存接口:刷新本服务所有缓存。无返回值")
    @RequestMapping(value = ZtStrUtils.REFRESH_CACHE, method = RequestMethod.POST)
    @ResponseBody
    @ApiOperationSupport(order = 5000)
    public void refreshCache() throws Exception {
        getIZtSimpleBaseService().refreshCache();
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "cacheName", value = "缓存名称", paramType = "query", required = true)})
    @ApiOperation(value = "标准缓存接口:刷新本服务指定缓存。无返回值")
    @RequestMapping(value = ZtStrUtils.REFRESH_CACHE_BY_NAME, method = RequestMethod.GET)
    @ResponseBody
    @ApiOperationSupport(order = 5001)
    public void refreshCache(@RequestParam String cacheName) {
        getIZtSimpleBaseService().refreshCache(cacheName);
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "userInfo", value = "用户信息", paramType = "body", required = true, dataType = "ZtBasicNumberIdEntity4Swagger")})
    @ApiOperation(value = "标准缓存接口:刷新当前用户所有相关缓存。无返回值")
    @RequestMapping(value = ZtStrUtils.REFRESH_CACHE_BY_CUR_USER_ID, method = RequestMethod.POST)
    @ResponseBody
    @ApiOperationSupport(order = 5002)
    public void refreshCacheByCurUserId(@RequestBody T userInfo) {
        getIZtSimpleBaseService().refreshCacheByCurUserId((Long) userInfo.getId());
    }

    protected ZtParamEntity<T> beforeSelect(ZtParamEntity<T> ztParamEntity) {
        return ztParamEntity;
    }

    // @ApiImplicitParams({@ApiImplicitParam(name = "entity", value = "查询条件", paramType = "body", required = false, dataTypeClass = ZtBasicEntity.class)})
    @ApiOperation(value = "标准查询接口，分页查询。严格匹配查询条件，不传查询条件则查询所有数据，默认每页大小可配置。默认返回所有列，可配合entity中的queryHelper返回指定列", notes = ZtStrUtils.SELECT_SIMPLE_RES_SWAGGER, position = 0)
    @RequestMapping(value = ZtStrUtils.SELECT_SIMPLE, method = RequestMethod.POST)
    @ResponseBody
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ZtStrUtils.APIRESPONSE_MESSAGE, response = Object.class)
    })
    @ApiOperationSupport(order = 1000)
    public ZtResBeanEx<ZtPage<T>> selectSimple(
            @RequestBody(required = false) T entity
    ) throws Exception {
        if (entity != null) {
            if (entity.getStart() == null) {
                entity.setStart(Long.valueOf(getZtResBeanExConfig().getStart()));
            }
            if (entity.getLimit() == null) {
                entity.setLimit(Long.valueOf(getZtResBeanExConfig().getLimit()));
            }
        }
        ZtParamEntity<T> ztParamEntity = new ZtParamEntity<>();
        ztParamEntity.setZtResBeanEx(ZtResBeanEx.ok());
        ztParamEntity.setEntity(entity);
        ztParamEntity = getThisController().beforeSelect(ztParamEntity);
        ztParamEntity = getIZtSimpleBaseService().ztSimpleSelectProvider(ztParamEntity);
        ztParamEntity = getThisController().afterSelect(ztParamEntity);
        return ztParamEntity.getZtResBeanEx();
    }

    public ZtParamEntity<T> afterSelect(ZtParamEntity<T> ztParamEntity) {
        return ztParamEntity;
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "entity", value = "查询对象", paramType = "body", required = true, dataType = "ZtBasicNumberIdEntity4Swagger")})
    //@Example无效 @ApiImplicitParams({@ApiImplicitParam(name = "entity", value = "查询条件", paramType = "body", required = true, dataTypeClass = ZtBasicNumberIdEntity4Swagger.class, examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"id\":100}")))})
    @ApiOperation(value = "标准查询接口。根据id获取详情", notes = ZtStrUtils.SELECT_ID_SIMPLE_RES_SWAGGER)
    @RequestMapping(value = ZtStrUtils.SELECT_ID_SIMPLE, method = RequestMethod.POST)
    @ResponseBody
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ZtStrUtils.APIRESPONSE_MESSAGE, response = Object.class)
    })
    @ApiOperationSupport(order = 1001)
    public ZtResBeanEx<T> selectIdSimple(@RequestBody T entity) throws Exception {
        ZtParamEntity<T> ztParamEntity = new ZtParamEntity<>();
        ztParamEntity.setZtResBeanEx(ZtResBeanEx.ok());
        ztParamEntity.setEntity(entity);
        ztParamEntity = getThisController().beforeSelectId(ztParamEntity);
        ztParamEntity = getIZtSimpleBaseService().ztSimpleSelectByPrimaryKey(ztParamEntity);
        ztParamEntity = getThisController().afterSelectId(ztParamEntity);
        return ztParamEntity.getZtResBeanEx();
    }

    private ZtParamEntity<T> beforeSelectId(ZtParamEntity<T> ztParamEntity) {
        return ztParamEntity;
    }

    public ZtParamEntity<T> afterSelectId(ZtParamEntity<T> ztParamEntity) throws Exception {
        return ztParamEntity;
    }

    @ApiOperation(value = "标准查询接口。根据多个id批量获取详情", notes = ZtStrUtils.SELECT_SIMPLE_RES_SWAGGER)
    @RequestMapping(value = ZtStrUtils.SELECT_ID_SIMPLE_BATCH, method = RequestMethod.POST)
    @ResponseBody
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ZtStrUtils.APIRESPONSE_MESSAGE, response = Object.class)
    })
    @ApiOperationSupport(order = 1002)
    public final ZtResBeanEx<T> selectIdSimpleBatch(@RequestBody List<Long> idList) throws Exception {
        ZtParamEntity<T> ztParamEntity = new ZtParamEntity<>();
        ztParamEntity.setZtResBeanEx(ZtResBeanEx.ok());
        ztParamEntity.setIdList(idList);
        ztParamEntity = getIZtSimpleBaseService().ztSimpleSelectByPrimaryKeyBatch(ztParamEntity);
        return ztParamEntity.getZtResBeanEx();
    }

    @ApiOperation(value = "标准查询接口。根据某个字段批量查询")
    @RequestMapping(value = ZtStrUtils.SELECT_ONE_FIELD_SIMPLE_BATCH + "/{fieldName}", method = RequestMethod.POST)
    @ResponseBody
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ZtStrUtils.APIRESPONSE_MESSAGE, response = Object.class)
    })
    @ApiOperationSupport(order = 1003)
    public ZtResBeanEx<ZtPage<T>> ztSimpleGetListByOneField(@PathVariable String fieldName, @RequestBody List<Object> fieldValueList) throws Exception {
        ZtParamEntity<T> ztParamEntity = new ZtParamEntity<>();
        ztParamEntity.setZtResBeanEx(ZtResBeanEx.ok());
        ztParamEntity.setFieldName(fieldName);
        ztParamEntity.setFieldValueList(fieldValueList);
        ztParamEntity = getIZtSimpleBaseService().ztSimpleSelectByOneFieldValue(ztParamEntity);
        return ztParamEntity.getZtResBeanEx();
    }

    //这个默认不开放，需要的controller自己添加@RequestMapping
    //如果需要数据权限还要记得添加数据权限注解
    // @Override
    // @RequestMapping(value = ZtStrUtils.SELECT_SIMPLE_ALL, method = RequestMethod.POST)
    // @ResponseBody
    @ApiOperation(value = "标准查询接口。查询所有数据，最多数据量可配置。适用于一些数据量比较少的基础信息类的表，理论上这种数据都可以用缓存。业务表尽量不要用。这个默认不开放，需要的controller自己添加@RequestMapping", notes = ZtStrUtils.SELECT_SIMPLE_RES_SWAGGER)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ZtStrUtils.APIRESPONSE_MESSAGE, response = Object.class)
    })
    @ApiOperationSupport(order = 1100)
    public ZtResBeanEx<ZtPage<T>> ztSimpleSelectAll() throws Exception {
        ZtParamEntity<T> ztSimpleSelectAll = getIZtSimpleBaseService().ztSimpleSelectAll();
        return ztSimpleSelectAll.getZtResBeanEx();
    }

    //400 org.springframework.web.bind.MethodArgumentNotValidException
    // @ApiImplicitParams({@ApiImplicitParam(name = "entity", value = "需要保存的数据", paramType = "body", required = true)})
    @ApiOperation(value = "标准新增接口：单条新增", notes = ZtStrUtils.SELECT_ID_SIMPLE_RES_SWAGGER)
    @RequestMapping(value = ZtStrUtils.INSERT_SIMPLE, method = RequestMethod.POST)
    @ResponseBody
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ZtStrUtils.APIRESPONSE_MESSAGE, response = Object.class)
    })
    @ApiOperationSupport(order = 2000)
    public final ZtResBeanEx insertSimple(@Valid @RequestBody T entity, BindingResult bindingResult) throws Exception {
        ZtParamEntity<T> ztParamEntity = new ZtParamEntity<>();
        ztParamEntity.setZtResBeanEx(ZtResBeanEx.ok(entity));
        ztParamEntity.setEntity(entity);
        ztParamEntity = getThisController().beforeInsert(ztParamEntity);
        //controller层判断是否允许新增
        if (ztParamEntity.isCanInsert()) {
            ztParamEntity = getIZtSimpleBaseService().ztSimpleInsert(ztParamEntity);
            //service层还有判断，可能会不允许
            if (ztParamEntity.isCanInsert()) {
                ztParamEntity = getThisController().afterInsert(ztParamEntity);
            } else {
                ztParamEntity = getThisController().cannotInsert(ztParamEntity);
            }
        } else {
            ztParamEntity = getThisController().cannotInsert(ztParamEntity);
        }
        return ztParamEntity.getZtResBeanEx();
    }

    //500 javax.validation.ConstraintViolationException
    // @ApiImplicitParams({@ApiImplicitParam(name = "entityList", value = "需要保存的数据列表", paramType = "body", required = true)})
    @ApiOperation(value = "标准新增接口：批量新增", notes = ZtStrUtils.NORMAL_RES_SWAGGER)
    @RequestMapping(value = ZtStrUtils.INSERT_BATCH_SIMPLE, method = RequestMethod.POST)
    @ResponseBody
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ZtStrUtils.APIRESPONSE_MESSAGE, response = Object.class)
    })
    @ApiOperationSupport(order = 2001)
    public final ZtResBeanEx insertBatchSimple(@Valid @RequestBody ZtInsertBatchEntity<T> ztInsertBatchEntity, BindingResult bindingResult) throws Exception {
        ZtValidList<T> entityList = ztInsertBatchEntity.getEntityList();
        ZtParamEntity<T> ztParamEntity = new ZtParamEntity<>();
        ztParamEntity.setZtResBeanEx(ZtResBeanEx.ok());
        ztParamEntity.setEntityList(entityList.getList());
        ztParamEntity = getThisController().beforeInsertBatch(ztParamEntity);
        //controller层判断是否允许新增
        if (ztParamEntity.isCanInsert()) {
            ztParamEntity = getIZtSimpleBaseService().ztSimpleInsertBatch(ztParamEntity);
            //service层还有判断，可能会不允许
            if (ztParamEntity.isCanInsert()) {
                ztParamEntity = getThisController().afterInsertBatch(ztParamEntity);
            } else {
                ztParamEntity = getThisController().cannotInsertBatch(ztParamEntity);
            }
        } else {
            ztParamEntity = getThisController().cannotInsertBatch(ztParamEntity);
        }
        return ztParamEntity.getZtResBeanEx();
    }

    // @ApiImplicitParams({@ApiImplicitParam(name = "entity", value = "需要更新的数据", paramType = "body", required = true)})
    @ApiOperation(value = "标准更新接口：动态更新（只更新传入的列）", notes = ZtStrUtils.NORMAL_RES_SWAGGER)
    @RequestMapping(value = ZtStrUtils.UPDATE_SIMPLE, method = RequestMethod.POST)
    @ResponseBody
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ZtStrUtils.APIRESPONSE_MESSAGE, response = Object.class)
    })
    @ApiOperationSupport(order = 3000)
    public final ZtResBeanEx updateSimple(@RequestBody T entity) throws Exception {
        ZtParamEntity<T> ztParamEntity = new ZtParamEntity<>();
        ztParamEntity.setZtResBeanEx(ZtResBeanEx.ok(entity));
        ztParamEntity.setEntity(entity);
        ztParamEntity = getThisController().beforeUpdate(ztParamEntity);
        if (ztParamEntity.isCanUpdate()) {
            ztParamEntity = getThisController().doUpdate(ztParamEntity);
            if (ztParamEntity.isCanUpdate()) {
                ztParamEntity = getThisController().afterUpdate(ztParamEntity);
            } else {
                ztParamEntity = getThisController().cannotUpdate(ztParamEntity);
            }
        } else {
            ztParamEntity = getThisController().cannotUpdate(ztParamEntity);
        }
        return ztParamEntity.getZtResBeanEx();
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "entity", value = "操作对象", paramType = "body", required = true, dataType = "ZtBasicNumberIdEntity4Swagger")})
    @ApiOperation(value = "标准删除接口：根据id单条删除", notes = ZtStrUtils.NORMAL_RES_SWAGGER)
    @RequestMapping(value = ZtStrUtils.DELETE_SIMPLE, method = RequestMethod.POST)
    @ResponseBody
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ZtStrUtils.APIRESPONSE_MESSAGE, response = Object.class)
    })
    @ApiOperationSupport(order = 4000)
    public final ZtResBeanEx deleteSimple(@RequestBody T entity) throws Exception {
        ZtParamEntity<T> ztParamEntity = new ZtParamEntity<>();
        ztParamEntity.setZtResBeanEx(ZtResBeanEx.ok());
        ztParamEntity.setEntity(entity);
        ztParamEntity = getThisController().beforeDelete(ztParamEntity);
        if (ztParamEntity.isCanDelete()) {
            ztParamEntity = getIZtSimpleBaseService().ztSimpleDeleteByPrimaryKey(ztParamEntity);
            if (ztParamEntity.isCanDelete()) {
                ztParamEntity = getThisController().afterDelete(ztParamEntity);
            } else {
                ztParamEntity = getThisController().cannotDelete(ztParamEntity);
            }
        } else {
            ztParamEntity = getThisController().cannotDelete(ztParamEntity);
        }
        return ztParamEntity.getZtResBeanEx();
    }

    @ApiOperation(value = "标准删除接口：根据ids批量删除", notes = ZtStrUtils.NORMAL_RES_SWAGGER)
    @RequestMapping(value = ZtStrUtils.DELETE_BATCH_SIMPLE, method = RequestMethod.POST)
    @ResponseBody
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ZtStrUtils.APIRESPONSE_MESSAGE, response = Object.class)
    })
    @ApiOperationSupport(order = 4001)
    public final ZtResBeanEx deleteBatchSimple(@RequestBody List<ZtBasicNumberIdEntity4Swagger> entityList) throws Exception {

        List<T> collect = new LinkedList<>();
        for (ZtBasicNumberIdEntity4Swagger ztBasicNumberIdEntity4Swagger : entityList) {
            T tmp = getIZtSimpleBaseService().getEntityClass().newInstance();
            tmp.setId(ztBasicNumberIdEntity4Swagger.getId());
            collect.add(tmp);
        }

        ZtParamEntity<T> ztParamEntity = new ZtParamEntity<>();
        ztParamEntity.setZtResBeanEx(ZtResBeanEx.ok());
        ztParamEntity.setEntityList(collect);
        ztParamEntity = getThisController().beforeDeleteBatch(ztParamEntity);
        if (ztParamEntity.isCanDelete()) {
            ztParamEntity = getIZtSimpleBaseService().ztSimpleDeleteByPrimaryKeyBatch(ztParamEntity);
            if (ztParamEntity.isCanDelete()) {
                ztParamEntity = getThisController().afterDeleteBatch(ztParamEntity);
            } else {
                ztParamEntity = getThisController().cannotDeleteBatch(ztParamEntity);
            }
        } else {
            ztParamEntity = getThisController().cannotDeleteBatch(ztParamEntity);
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
        ztParamEntity.getZtResBeanEx().setCode(getZtResBeanExConfig().getFailCode());
        ztParamEntity.getZtResBeanEx().setMsg(getZtResBeanExConfig().getFailMsg());
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
        ztParamEntity.getZtResBeanEx().setCode(getZtResBeanExConfig().getFailCode());
        ztParamEntity.getZtResBeanEx().setMsg(getZtResBeanExConfig().getFailMsg());
        return ztParamEntity;
    }

    protected ZtParamEntity<T> beforeDelete(ZtParamEntity<T> ztParamEntity) {
        return ztParamEntity;
    }

    protected ZtParamEntity<T> afterDelete(ZtParamEntity<T> ztParamEntity) {
        return ztParamEntity;
    }

    protected ZtParamEntity<T> cannotDelete(ZtParamEntity<T> ztParamEntity) {
        ztParamEntity.getZtResBeanEx().setCode(getZtResBeanExConfig().getFailCode());
        ztParamEntity.getZtResBeanEx().setMsg(getZtResBeanExConfig().getFailMsg());
        return ztParamEntity;
    }

    protected ZtParamEntity<T> beforeInsertBatch(ZtParamEntity<T> ztParamEntity) {
        return ztParamEntity;
    }

    protected ZtParamEntity<T> afterInsertBatch(ZtParamEntity<T> ztParamEntity) {
        return ztParamEntity;
    }

    protected ZtParamEntity<T> cannotInsertBatch(ZtParamEntity<T> ztParamEntity) {
        ztParamEntity.getZtResBeanEx().setCode(getZtResBeanExConfig().getFailCode());
        ztParamEntity.getZtResBeanEx().setMsg(getZtResBeanExConfig().getFailMsg());
        return ztParamEntity;
    }

    protected ZtParamEntity<T> beforeDeleteBatch(ZtParamEntity<T> ztParamEntity) {
        return ztParamEntity;
    }

    protected ZtParamEntity<T> afterDeleteBatch(ZtParamEntity<T> ztParamEntity) {
        return ztParamEntity;
    }

    protected ZtParamEntity<T> cannotDeleteBatch(ZtParamEntity<T> ztParamEntity) {
        ztParamEntity.getZtResBeanEx().setCode(getZtResBeanExConfig().getFailCode());
        ztParamEntity.getZtResBeanEx().setMsg(getZtResBeanExConfig().getFailMsg());
        return ztParamEntity;
    }

}
