package com.zhangzhuorui.framework.mybatis.simplebaseservice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.zhangzhuorui.framework.core.ZtBasicEntity;
import com.zhangzhuorui.framework.core.ZtColumnUtil;
import com.zhangzhuorui.framework.core.ZtPage;
import com.zhangzhuorui.framework.core.ZtPropertyFunc;
import com.zhangzhuorui.framework.core.ZtQueryConditionEntity;
import com.zhangzhuorui.framework.core.ZtResBeanEx;
import com.zhangzhuorui.framework.core.ZtSpringUtil;
import com.zhangzhuorui.framework.core.ZtStrUtils;
import com.zhangzhuorui.framework.core.ZtUtils;
import com.zhangzhuorui.framework.mybatis.core.ZtParamEntity;
import com.zhangzhuorui.framework.mybatis.core.ZtQueryWrapper;
import com.zhangzhuorui.framework.mybatis.simplebasemapper.ZtSimpleBaseMapper;
import com.zhangzhuorui.framework.mybatis.simplebasemapper.ZtTableInfoHelperStr;
import org.apache.ibatis.binding.MapperProxy;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate :  2017-01-01
 * @description :
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
public abstract class ZtSimpleBaseServiceImpl<T extends ZtBasicEntity> implements IZtSimpleBaseService<T> {

    protected static final Logger log = LoggerFactory.getLogger(ZtSimpleBaseServiceImpl.class);

    @Autowired
    protected HttpServletRequest request;

    public HttpServletRequest getRequest() {
        return request;
    }

    @Autowired
    ZtSimpleBaseMapper<T> ztSimpleBaseMapper;

    public ZtSimpleBaseMapper<T> getZtSimpleBaseMapper() {
        return ztSimpleBaseMapper;
    }

    private static Set<ResultMap> resultMapSet = new HashSet<>();

    public static Set<ResultMap> getResultMapSet() {
        return resultMapSet;
    }

    private ResultMap resultMap;

    public ResultMap getResultMap() {
        return resultMap;
    }

    @Autowired
    DataSource dataSource;

    public DataSource getDataSource() {
        return dataSource;
    }

    ThreadLocal<AtomicInteger> safeLock = ThreadLocal.withInitial(() -> new AtomicInteger(0));

    public ThreadLocal<AtomicInteger> getSafeLock() {
        return safeLock;
    }

    protected IZtSimpleBaseService<T> getThisService() {
        String shortClassName = ClassUtils.getShortName(this.getClass());
        String beanName = Introspector.decapitalize(shortClassName);
        Object bean1 = ZtSpringUtil.getBean(beanName);
        return (IZtSimpleBaseService<T>) bean1;
    }

    private static String dbProduct;

    @PostConstruct
    protected void init() throws Exception {
        if (dbProduct == null) {
            dbProduct = getDataSource().getConnection().getMetaData().getDatabaseProductName();
        }
        if ("mysql".equalsIgnoreCase(dbProduct)) {
            ZtTableInfoHelperStr.DB_PRE = ZtTableInfoHelperStr.DB_PRE_MYSQL;
            ZtTableInfoHelperStr.DB_END = ZtTableInfoHelperStr.DB_END_MYSQL;
        } else if ("mssql".equalsIgnoreCase(dbProduct)) {
            ZtTableInfoHelperStr.DB_PRE = ZtTableInfoHelperStr.DB_PRE_MSSQL;
            ZtTableInfoHelperStr.DB_END = ZtTableInfoHelperStr.DB_END_MSSQL;
        } else if ("oracle".equalsIgnoreCase(dbProduct)) {
            ZtTableInfoHelperStr.DB_PRE = ZtTableInfoHelperStr.DB_PRE_ORACLE;
            ZtTableInfoHelperStr.DB_END = ZtTableInfoHelperStr.DB_END_ORACLE;
        }
        SqlSessionFactory sqlSessionFactory = ZtSpringUtil.getBean(SqlSessionFactory.class);
        Collection resultMaps = sqlSessionFactory.getConfiguration().getResultMaps();
        for (Object resultMap : resultMaps) {
            if (resultMap instanceof ResultMap) {
                getResultMapSet().add((ResultMap) resultMap);
            }
        }
        Field h = getZtSimpleBaseMapper().getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        MapperProxy<T> mapperProxy = (MapperProxy<T>) h.get(getZtSimpleBaseMapper());
        Field mapperInterface = mapperProxy.getClass().getDeclaredField("mapperInterface");
        mapperInterface.setAccessible(true);
        Class<T> baseSelectMapper = (Class<T>) mapperInterface.get(mapperProxy);
        String name = baseSelectMapper.getName();
        ParameterizedType parameterizedType = (ParameterizedType) this.getClass().getGenericSuperclass();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        Class<T> entityClass = (Class<T>) actualTypeArguments[0];
        Optional<ResultMap> mybatisResultMap = getResultMapSet().stream().filter(t -> (t.getIdResultMappings() != null && t.getIdResultMappings().size() > 0) && t.getId().equals(name + ".BaseResultMap") && t.getType().equals(entityClass)).findAny();
        if (mybatisResultMap.isPresent()) {
            ResultMap resultMap = mybatisResultMap.get();
            this.resultMap = resultMap;
        } else {
            Optional<ResultMap> mybatisPlusResultMap = getResultMapSet().stream().filter(t -> (t.getResultMappings() != null && t.getResultMappings().size() > 0) && t.getId().equals(name + ".mybatis-plus_" + entityClass.getSimpleName()) && t.getType().equals(entityClass)).findAny();
            if (mybatisPlusResultMap.isPresent()) {
                this.resultMap = mybatisPlusResultMap.get();
            } else {
                log.error("-----------------" + this.getClass().getName() + "没有resultmap");
            }

        }
    }

    /**
     * @param :
     * @return :  void
     * @author :  zhangtao
     * @createDate :  2021/9/26 下午10:02
     * @description :  刷新缓存
     * @updateUser :
     * @updateDate :
     * @updateRemark :
     */
    @Override
    public void refreshCache() throws Exception {

    }

    /**
     * @param cacheName :
     * @return :  void
     * @author :  zhangtao
     * @createDate :  2021/9/26 下午10:02
     * @description :  刷新指定缓存
     * @updateUser :
     * @updateDate :
     * @updateRemark :
     */
    @Override
    public void refreshCache(String cacheName) {

    }

    /**
     * 刷新当前用户相关缓存
     *
     * @param userId :
     * @return :  void
     * @author :  zhangtao
     * @createDate :  2021/10/6 下午1:13
     * @description :
     * @updateUser :
     * @updateDate :
     * @updateRemark :
     */
    @Override
    public void refreshCacheByCurUserId(Long userId) {

    }

    /**
     * @param fieldName :
     * @return :  java.lang.String
     * @author :  zhangtao
     * @createDate :  2021/9/26 下午10:01
     * @description :  根据字段名获取数据库列名
     * @updateUser :
     * @updateDate :
     * @updateRemark :
     */
    @Override
    public final String getColumnName(String fieldName) {
        Optional<ResultMapping> any = getResultMap().getResultMappings().stream().filter(t -> t.getProperty().equals(fieldName)).findAny();
        return any.map(ResultMapping::getColumn).orElse(null);
    }

    @Override
    public final String getColumnName(ZtPropertyFunc<T, ?> fieldName) {
        String fieldName1 = ZtColumnUtil.getFieldName(fieldName);
        Optional<ResultMapping> any = getResultMap().getResultMappings().stream().filter(t -> t.getProperty().equals(fieldName1)).findAny();
        return any.map(ResultMapping::getColumn).orElse(null);
    }

    @Override
    public T getObj(ZtParamEntity<T> ztParamEntity) {
        T obj = (T) ztParamEntity.getZtResBeanEx().getData();
        return obj;
    }

    @Override
    public List<T> getList(ZtParamEntity<T> ztParamEntity) {
        ZtPage<T> page = (ZtPage<T>) ztParamEntity.getZtResBeanEx().getData();
        List<T> list = page.getResults();
        return list;
    }

    /**
     * @param obj :
     * @return :  com.antscity.any_call.base.ztframe.basicentity.ZtParamEntity<T>
     * @author :  zhangtao
     * @createDate :  2021/9/23 上午11:12
     * @description :  根据obj返回一个初始化的ZtParamEntity
     * @updateUser :
     * @updateDate :
     * @updateRemark :
     */
    @Override
    public ZtParamEntity<T> getInitZtParamEntity(T obj) {
        return getInitZtParamEntity(obj, SqlCommandType.SELECT);
    }

    @Override
    public ZtParamEntity<T> getInitZtParamEntity(T obj, SqlCommandType sqlCommandType) {
        ZtParamEntity<T> ztParamEntity = new ZtParamEntity<>();
        ztParamEntity.setEntity(obj);
        ztParamEntity = this.initSimpleWrapper(ztParamEntity, sqlCommandType);
        ztParamEntity.setUseCommonZtQueryWrapper(false);
        return ztParamEntity;
    }

    @Override
    public ZtQueryWrapper<T> getInitZtQueryWrapper(T obj) {
        ZtParamEntity<T> initZtParamEntity = getInitZtParamEntity(obj);
        return initZtParamEntity.getZtQueryWrapper();
    }

    /**
     * @param obj :
     * @return :  com.antscity.any_call.base.ztframe.basicentity.ZtParamEntity<T>
     * @author :  zhangtao
     * @createDate :  2021/9/23 上午11:12
     * @description :  根据obj返回一个不需要select count的初始化的ZtParamEntity
     * @updateUser :
     * @updateDate :
     * @updateRemark :
     */
    @Override
    public ZtParamEntity<T> getInitZtParamEntityWithOutCount(T obj) {
        if (obj.getLimit() == null) {
            obj.setLimit(Long.valueOf(ZtStrUtils.MAX_PAGE));
        }
        ZtParamEntity<T> ztParamEntity = getInitZtParamEntity(obj);
        ztParamEntity.setNeedCount(false);
        return ztParamEntity;
    }

    /**
     * 动态生成需要的条件。核心方法之一
     *
     * @param obj               :
     * @param writeMapNullValue :
     * @return :  com.zhangzhuorui.framework.core.ZtQueryWrapper<T>
     * @author :  zhangtao
     * @createDate :  2021/9/26 下午9:58
     * @description :  组装查询条件，增删改查都会用到。一改全改，这个不可随意修改
     * @updateUser :
     * @updateDate :
     * @updateRemark :
     */
    // @Override
    private final ZtQueryWrapper<T> getQueryWrapper(T obj, boolean writeMapNullValue, SqlCommandType sqlCommandType) {
        ZtQueryWrapper<T> wrapper = new ZtQueryWrapper<>();
        String logicDeleteFieldName = getLogicDeleteFieldName();
        wrapper.setTableName(getTableName());
        wrapper.setVersionFieldName(getVersionFieldName());
        wrapper.setLogicDeleteFieldName(logicDeleteFieldName);
        wrapper.setLogicDeleteFlag(getLogicDeleteFlag());
        wrapper.setManualId(getManualId());
        wrapper.setResultMap(getResultMap());
        wrapper.setCurrent(Long.valueOf(ZtStrUtils.START));
        wrapper.setSize(Long.valueOf(ZtStrUtils.LIMIT));
        if (obj.getLimit() != null) {
            wrapper.setSize(obj.getLimit());
        }
        if (obj.getStart() != null) {
            wrapper.setCurrent(obj.getStart());
        }
        String orderByField = obj.getOrderBy();
        if (!StringUtils.isEmpty(orderByField)) {
            String orderByColumn = getColumnName(orderByField);
            if (!StringUtils.isEmpty(orderByColumn)) {
                if (obj.getAscFlag() == null || obj.getAscFlag()) {
                    wrapper.setOrderBy(" " + orderByColumn + " ASC ");
                } else {
                    wrapper.setOrderBy(" " + orderByColumn + " DESC ");
                }
            }
        }

        if (!StringUtils.isEmpty(logicDeleteFieldName)) {
            Field field = ZtUtils.getField(obj, logicDeleteFieldName);
            field.setAccessible(true);
            try {
                field.set(obj, !getLogicDeleteFlag());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        LinkedList<ZtQueryConditionEntity> entityList = wrapper.getConditons();
        JSONObject jsonObject = new JSONObject();
        if (writeMapNullValue) {
            jsonObject = JSONObject.parseObject(JSON.toJSONString(obj, SerializerFeature.WriteMapNullValue));
        } else {
            jsonObject = JSONObject.parseObject(JSON.toJSONString(obj));
        }

        wrapper.setObj(obj);

        for (String propertyName : jsonObject.keySet()) {
            Optional<ResultMapping> any = getResultMap().getResultMappings().stream().filter(t -> t.getProperty().equals(propertyName)).findAny();
            if (any.isPresent()) {
                ResultMapping resultMapping = any.get();
                if (resultMapping.getNestedQueryId() == null && resultMapping.getNestedResultMapId() == null && resultMapping.getColumn() != null) {
                    ZtQueryConditionEntity entity = new ZtQueryConditionEntity();
                    entity.setFieldName(propertyName);
                    String column = resultMapping.getColumn();
                    entity.setColumnName(column);
                    entityList.add(entity);
                }
            }
        }

        // wrapper = afterGetQueryWrapper(wrapper, sqlCommandType);

        return wrapper;
    }

    // @Override
    // public ZtQueryWrapper<T> afterGetQueryWrapper(ZtQueryWrapper<T> ztQueryWrapper, SqlCommandType sqlCommandType) {
    //     return ztQueryWrapper;
    // }

    /**
     * @param entity :
     * @param func   :
     * @return :  void
     * @author :  zhangtao
     * @createDate :  2021/9/26 下午10:00
     * @description :  单独设置一个查询条件
     * @updateUser :
     * @updateDate :
     * @updateRemark :
     */
    @Override
    public final void setZtQueryConditionEntity(ZtQueryConditionEntity entity, ZtPropertyFunc<T, ?> func) {
        String fieldName = ZtColumnUtil.getFieldName(func);
        entity.setFieldName(fieldName);
        ResultMapping resultMapping = getResultMap().getResultMappings().stream().filter(t -> t.getProperty().equals(fieldName)).findAny().get();
        entity.setColumnName(resultMapping.getColumn());
    }

    /**
     * @param :
     * @return :  com.zhangzhuorui.framework.core.ZtParamEntity<T>
     * @author :  zhangtao
     * @createDate :  2021/9/26 下午10:01
     * @description :  查询全部。有些配置表可用。可配合缓存
     * @updateUser :
     * @updateDate :
     * @updateRemark :
     */
    @Override
    public ZtParamEntity<T> ztSimpleSelectAll() throws Exception {
        ZtParamEntity<T> ztParamEntity = new ZtParamEntity<>();
        ztParamEntity = getThisService().initSimpleWrapper(ztParamEntity, SqlCommandType.SELECT);
        ztParamEntity.setNeedCount(false);
        ztParamEntity.setUseCommonZtQueryWrapper(false);
        ztParamEntity = getThisService().ztSimpleSelectProvider(ztParamEntity);
        return ztParamEntity;
    }

    /**
     * @param ztParamEntity :
     * @return :  com.zhangzhuorui.framework.core.ZtParamEntity<T>
     * @author :  zhangtao
     * @createDate :  2021/9/26 下午10:00
     * @description :  初始化通用条件，就是所有的接口都要走的接口
     * 如果需要手动调用initSimpleWrapper后调用ztSimpleSelectProvider，需要setUseCommonZtQueryWrapper(false)
     * @updateUser :
     * @updateDate :
     * @updateRemark :
     */
    @Override
    public final ZtParamEntity<T> initSimpleWrapper(ZtParamEntity<T> ztParamEntity, SqlCommandType sqlCommandType) {
        if (ztParamEntity.getZtResBeanEx() == null) {
            ztParamEntity.setZtResBeanEx(ZtResBeanEx.ok());
        }

        if (ztParamEntity.isUseCommonZtQueryWrapper()) {
            if (ztParamEntity.getEntity() != null) {
                ztParamEntity.setZtQueryWrapper(this.getQueryWrapper(ztParamEntity.getEntity(), false, sqlCommandType));
            } else if (ztParamEntity.getEntityList() != null && ztParamEntity.getEntityList().size() > 0) {
                ztParamEntity.setZtQueryWrapper(this.getQueryWrapper(ztParamEntity.getEntityList().get(0), false, sqlCommandType));
            } else {
                //默认全表查询 ztSimpleSelectAll
                try {
                    T obj = (T) this.getResultMap().getType().newInstance();
                    obj.setLimit(Long.valueOf(ZtStrUtils.MAX_PAGE));
                    ztParamEntity.setEntity(obj);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                ztParamEntity.setZtQueryWrapper(this.getQueryWrapper(ztParamEntity.getEntity(), false, sqlCommandType));
            }
            T entity = ztParamEntity.getEntity();
            if (entity != null) {
                Date startDate = entity.getStartDate();
                Date endDate = entity.getEndDate();
                Date startTime = entity.getStartTime();
                Date endTime = entity.getEndTime();

                Date start = null, end = null;
                if (startDate != null) {
                    start = ZtUtils.getEarliestTimeOfTheDay(startDate);
                }
                if (startTime != null) {
                    start = startTime;
                }
                if (endDate != null) {
                    end = ZtUtils.getLatestTimeOfTheDay(endDate);
                }
                if (endTime != null) {
                    end = endTime;
                }

                if (start != null && end != null) {
                    ztParamEntity.getZtQueryWrapper().andBetween(ZtBasicEntity::getGmtCreate, start, end);
                } else if (start != null) {
                    entity.setGmtCreate(start);
                    ztParamEntity.getZtQueryWrapper().andGreatEquals(ZtBasicEntity::getGmtCreate);
                } else if (end != null) {
                    entity.setGmtCreate(end);
                    ztParamEntity.getZtQueryWrapper().andLessEquals(ZtBasicEntity::getGmtCreate);
                }
            }
            //这个在UseCommonZtQueryWrapper里面，只有UseCommonZtQueryWrapper才会进入
            ztParamEntity = afterUseCommonZtQueryWrapper(ztParamEntity, sqlCommandType);
        }
        //一定会走这个逻辑，只是预留一个接口，一般用不到
        ztParamEntity = afterInitSimpleWrapper(ztParamEntity, sqlCommandType);
        LinkedList<ZtQueryConditionEntity> conditons = ztParamEntity.getZtQueryWrapper().getConditons();
        conditons.sort(Comparator.comparing(o -> o.getQueryType().getIntValue()));

        return ztParamEntity;
    }

    /**
     * 对ZtQueryWrapper预留的扩展接口，例如权限系统的权限字段赋值
     *
     * @param ztParamEntity  :
     * @param sqlCommandType :
     * @return :  com.zhangzhuorui.framework.mybatis.core.ZtParamEntity<T>
     * @author :  zhangtao
     * @createDate :  2021/10/2 上午10:58
     * @description :
     * @updateUser :
     * @updateDate :
     * @updateRemark :
     */
    @Override
    public ZtParamEntity<T> afterUseCommonZtQueryWrapper(ZtParamEntity<T> ztParamEntity, SqlCommandType sqlCommandType) {
        return ztParamEntity;
    }

    /**
     * 预留一个接口，一般用不到
     *
     * @param ztParamEntity  :
     * @param sqlCommandType :
     * @return :  com.zhangzhuorui.framework.mybatis.core.ZtParamEntity<T>
     * @author :  zhangtao
     * @createDate :  2021/10/2 上午11:30
     * @description :
     * @updateUser :
     * @updateDate :
     * @updateRemark :
     */
    @Override
    public ZtParamEntity<T> afterInitSimpleWrapper(ZtParamEntity<T> ztParamEntity, SqlCommandType sqlCommandType) {
        return ztParamEntity;
    }

    @Override
    public List<T> ztSimpleGetList(T t) throws Exception {
        ZtParamEntity<T> ztParamEntity = new ZtParamEntity<T>();
        ztParamEntity.setEntity(t);
        ztParamEntity = this.ztSimpleSelectProviderWithoutCount(ztParamEntity);
        List<T> list = this.getList(ztParamEntity);
        return list;
    }

    @Override
    public T ztSimpleInsert(T t) throws Exception {
        ZtParamEntity<T> ztParamEntity = new ZtParamEntity<>();
        ztParamEntity.setZtResBeanEx(ZtResBeanEx.ok(t));
        ztParamEntity.setEntity(t);
        ztParamEntity = getThisService().ztSimpleInsert(ztParamEntity);
        T obj = getThisService().getObj(ztParamEntity);
        return obj;
    }

    @Override
    public List<T> ztSimpleInsertBatch(List<T> list) throws Exception {
        ZtParamEntity<T> ztParamEntity = new ZtParamEntity<>();
        ztParamEntity.setZtResBeanEx(ZtResBeanEx.ok());
        ztParamEntity.setEntityList(list);
        ztParamEntity = getThisService().ztSimpleInsertBatch(ztParamEntity);
        List<T> list1 = getThisService().getList(ztParamEntity);
        return list1;
    }

    @Override
    public ZtResBeanEx ztSimpleDelete(T t) throws Exception {
        ZtParamEntity<T> ztParamEntity = new ZtParamEntity<>();
        ztParamEntity.setZtResBeanEx(ZtResBeanEx.ok());
        ztParamEntity.setEntity(t);
        ztParamEntity = getThisService().ztSimpleDeleteByPrimaryKey(ztParamEntity);
        return ztParamEntity.getZtResBeanEx();
    }

    @Override
    public ZtResBeanEx ztSimpleUpdate(T t) throws Exception {
        ZtParamEntity<T> ztParamEntity = new ZtParamEntity<>();
        ztParamEntity.setZtResBeanEx(ZtResBeanEx.ok());
        ztParamEntity.setEntity(t);
        ztParamEntity = getThisService().ztSimpleUpdateByPrimaryKey(ztParamEntity);
        return ZtResBeanEx.ok();
    }

    @Override
    public Integer ztSimpleSelectProviderCount(ZtParamEntity<T> ztParamEntity) {
        this.initSimpleWrapper(ztParamEntity, SqlCommandType.SELECT);
        ZtQueryWrapper ztQueryWrapper = ztParamEntity.getZtQueryWrapper();
        ztQueryWrapper.setCount(true);
        return getZtSimpleBaseMapper().ztSimpleSelectProviderCount(ztQueryWrapper);
    }

    @Override
    public final ZtParamEntity<T> ztSimpleSelectProviderWithoutCount(ZtParamEntity<T> ztParamEntity) throws Exception {
        T entity = ztParamEntity.getEntity();
        if (entity.getLimit() == null) {
            entity.setLimit(Long.valueOf(ZtStrUtils.MAX_PAGE));
        }
        ztParamEntity = this.ztBeforeSimpleSelectProvider(ztParamEntity);
        ztParamEntity.setNeedCount(false);
        ztParamEntity = this.ztDoSimpleSelectProvider(ztParamEntity);
        ztParamEntity = this.ztAfterSimpleSelectProvider(ztParamEntity);
        return ztParamEntity;
    }

    //region 分页查询
    @Override
    public final ZtParamEntity<T> ztSimpleSelectProvider(ZtParamEntity<T> ztParamEntity) throws Exception {
        ztParamEntity = this.ztBeforeSimpleSelectProvider(ztParamEntity);
        ztParamEntity = this.ztDoSimpleSelectProvider(ztParamEntity);
        ztParamEntity = this.ztAfterSimpleSelectProvider(ztParamEntity);
        getSafeLock().set(new AtomicInteger(0));
        return ztParamEntity;
    }

    @Override
    public ZtParamEntity<T> ztBeforeSimpleSelectProvider(ZtParamEntity<T> ztParamEntity) throws Exception {
        ztParamEntity = this.initSimpleWrapper(ztParamEntity, SqlCommandType.SELECT);
        return ztParamEntity;
    }

    private final ZtParamEntity<T> ztDoSimpleSelectProvider(ZtParamEntity<T> ztParamEntity) throws Exception {
        ZtQueryWrapper ztQueryWrapper = ztParamEntity.getZtQueryWrapper();
        ztParamEntity.setUseCommonZtQueryWrapper(false);
        ZtPage<T> page = new ZtPage<>();
        page.setTotal(0);
        page.setResults(Collections.emptyList());
        ztParamEntity.getZtResBeanEx().setEmpty(true);
        ztParamEntity.getZtResBeanEx().setData(page);

        if (ztParamEntity.isNeedCount()) {
            Integer count = getThisService().ztSimpleSelectProviderCount(ztParamEntity);
            if (count > 0) {
                ztQueryWrapper.setCount(false);
                // Map map = new HashMap<>();
                // map.put(ZtTableInfoHelperStr.PARAM_NAME, ztQueryWrapper);
                List<T> list = getZtSimpleBaseMapper().ztSimpleSelectProvider(ztQueryWrapper);
                page.setResults(list);
                page.setTotal(count);
                ztParamEntity.getZtResBeanEx().setEmpty(false);
            }
        } else {
            ztQueryWrapper.setCount(false);
            // Map map = new HashMap<>();
            // map.put(ZtTableInfoHelperStr.PARAM_NAME, ztQueryWrapper);
            List<T> list = getZtSimpleBaseMapper().ztSimpleSelectProvider(ztQueryWrapper);
            page.setResults(list);
            page.setTotal(list.size());
            if (list.size() > 0) {
                ztParamEntity.getZtResBeanEx().setEmpty(false);
            }
        }
        return ztParamEntity;
    }

    @Override
    public ZtParamEntity<T> ztAfterSimpleSelectProvider(ZtParamEntity<T> ztParamEntity) throws Exception {
        return ztParamEntity;
    }
    //endregion

    /**
     * @param ztParamEntity :
     * @return :  com.zhangzhuorui.framework.core.ZtParamEntity
     * @author :  zhangtao
     * @createDate :  2021/9/26 下午10:05
     * @description :  联表查询
     * @updateUser :
     * @updateDate :
     * @updateRemark :
     */
    @Override
    public ZtParamEntity ztDoSimpleJoinSelectProvider(ZtParamEntity ztParamEntity) throws Exception {
        ZtQueryWrapper ztQueryWrapper = ztParamEntity.getZtQueryWrapper();
        ztParamEntity.setUseCommonZtQueryWrapper(false);
        ztQueryWrapper.setDistinctFlag(true);
        ztQueryWrapper.buildSelectColumn();
        // ztQueryWrapper.setCurrent(null);
        // ztQueryWrapper.setSize(null);
        // ztQueryWrapper.setSize(Long.valueOf(ZtStrUtils.MAX_PAGE));

        ZtPage page = new ZtPage<>();
        page.setTotal(0);
        page.setResults(Collections.emptyList());
        ztParamEntity.getZtResBeanEx().setEmpty(true);
        ztParamEntity.getZtResBeanEx().setData(page);
        ztQueryWrapper.setCount(false);
        List list = getZtSimpleBaseMapper().ztSimpleJoinSelectProvider(ztQueryWrapper);
        Class dtoClass = ztParamEntity.getDtoClass();
        List dtoList = new ArrayList<>();
        for (Object o : list) {
            Map tmp = (Map) o;
            Object dtoObj = JSON.parseObject(JSON.toJSONString(tmp), dtoClass);
            dtoList.add(dtoObj);
        }
        page.setResults(dtoList);
        page.setTotal(list.size());
        if (list.size() > 0) {
            ztParamEntity.getZtResBeanEx().setEmpty(false);
        }
        return ztParamEntity;
    }

    //region 根据主键查询
    @Override
    public final ZtParamEntity<T> ztSimpleSelectByPrimaryKey(ZtParamEntity<T> ztParamEntity) throws Exception {
        ztParamEntity = this.initSimpleWrapper(ztParamEntity, SqlCommandType.SELECT);
        ZtQueryWrapper ztQueryWrapper = ztParamEntity.getZtQueryWrapper();
        T t = getZtSimpleBaseMapper().ztSimpleSelectByPrimaryKey(ztQueryWrapper);
        ztParamEntity.getZtResBeanEx().setData(t);
        if (t == null) {
            ztParamEntity.getZtResBeanEx().setEmpty(true);
        } else {
            ztParamEntity.getZtResBeanEx().setEmpty(false);
        }
        ztParamEntity = this.ztAfterSimpleSelectByPrimaryKey(ztParamEntity);
        return ztParamEntity;
    }

    @Override
    public ZtParamEntity<T> ztAfterSimpleSelectByPrimaryKey(ZtParamEntity<T> ztParamEntity) throws Exception {
        return ztParamEntity;
    }
    //endregion

    //region 根据主键动态更新
    @Override
    @Transactional(rollbackFor = Exception.class)
    public final ZtParamEntity<T> ztSimpleUpdateByPrimaryKey(ZtParamEntity<T> ztParamEntity) throws Exception {
        ztParamEntity = getThisService().ztBeforeSimpleUpdateByPrimaryKey(ztParamEntity);
        if (ztParamEntity.isCanUpdate()) {
            // ztParamEntity = getThisService().ztDoSimpleUpdateByPrimaryKey(ztParamEntity);
            ZtQueryWrapper ztQueryWrapper = ztParamEntity.getZtQueryWrapper();
            Integer integer = getZtSimpleBaseMapper().ztSimpleUpdateByPrimaryKey(ztQueryWrapper);
            ztParamEntity.setUpdateRow(integer);
            if (integer > 0) {
                ztParamEntity.setUpdateRes(true);
            }
            if (ztParamEntity.isCanUpdate() && ztParamEntity.isUpdateRes()) {
                ztParamEntity = getThisService().ztAfterSimpleUpdateByPrimaryKey(ztParamEntity);
            } else {
                ztParamEntity = getThisService().ztCannotSimpleUpdateByPrimaryKey(ztParamEntity);
            }
        } else {
            ztParamEntity = getThisService().ztCannotSimpleUpdateByPrimaryKey(ztParamEntity);
        }
        return ztParamEntity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZtParamEntity<T> ztBeforeSimpleUpdateByPrimaryKey(ZtParamEntity<T> ztParamEntity) throws Exception {
        T entity = ztParamEntity.getEntity();
        Date now = new Date();
        entity.setGmtCreate(null);
        entity.setGmtUpdate(now);
        entity.setCreatedBy(null);
        entity.setCreatedByName(null);
        ztParamEntity = this.initSimpleWrapper(ztParamEntity, SqlCommandType.UPDATE);
        return ztParamEntity;
    }

    // @Override
    // @Transactional(rollbackFor = Exception.class)
    // public final ZtParamEntity<T> ztDoSimpleUpdateByPrimaryKey(ZtParamEntity<T> ztParamEntity) throws Exception {
    //     ZtQueryWrapper ztQueryWrapper = ztParamEntity.getZtQueryWrapper();
    //     Integer integer = getZtSimpleBaseMapper().ztSimpleUpdateByPrimaryKey(ztQueryWrapper);
    //     ztParamEntity.setUpdateRow(integer);
    //     if (integer > 0) {
    //         ztParamEntity.setUpdateRes(true);
    //     }
    //     return ztParamEntity;
    // }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZtParamEntity<T> ztAfterSimpleUpdateByPrimaryKey(ZtParamEntity<T> ztParamEntity) throws Exception {
        return ztParamEntity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZtParamEntity<T> ztCannotSimpleUpdateByPrimaryKey(ZtParamEntity<T> ztParamEntity) throws Exception {
        ztParamEntity.getZtResBeanEx().setCode(ZtStrUtils.FAIL_CODE);
        ztParamEntity.getZtResBeanEx().setMsg(ZtStrUtils.FAIL_MSG);
        return ztParamEntity;
    }
    //endregion

    //region 根据主键删除
    @Override
    @Transactional(rollbackFor = Exception.class)
    public final ZtParamEntity<T> ztSimpleDeleteByPrimaryKey(ZtParamEntity<T> ztParamEntity) throws Exception {
        ztParamEntity = getThisService().ztBeforeSimpleDeleteByPrimaryKey(ztParamEntity);
        if (ztParamEntity.isCanDelete()) {
            // ztParamEntity = getThisService().ztDoSimpleDeleteByPrimaryKey(ztParamEntity);
            ZtQueryWrapper ztQueryWrapper = ztParamEntity.getZtQueryWrapper();
            Integer integer = getZtSimpleBaseMapper().ztSimpleDeleteByPrimaryKey(ztQueryWrapper);
            ztParamEntity.setDeleteRow(integer);
            if (integer > 0) {
                ztParamEntity.setDeleteRes(true);
            }
            if (ztParamEntity.isCanDelete() && ztParamEntity.isDeleteRes()) {
                ztParamEntity = getThisService().ztAfterSimpleDeleteByPrimaryKey(ztParamEntity);
            } else {
                ztParamEntity = getThisService().ztCannotSimpleDeleteByPrimaryKey(ztParamEntity);
            }
        } else {
            ztParamEntity = getThisService().ztCannotSimpleDeleteByPrimaryKey(ztParamEntity);
        }
        return ztParamEntity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZtParamEntity<T> ztBeforeSimpleDeleteByPrimaryKey(ZtParamEntity<T> ztParamEntity) throws Exception {
        ztParamEntity = this.initSimpleWrapper(ztParamEntity, SqlCommandType.DELETE);
        return ztParamEntity;
    }

    // @Override
    // @Transactional(rollbackFor = Exception.class)
    // public final ZtParamEntity<T> ztDoSimpleDeleteByPrimaryKey(ZtParamEntity<T> ztParamEntity) throws Exception {
    //     ZtQueryWrapper ztQueryWrapper = ztParamEntity.getZtQueryWrapper();
    //     Integer integer = getZtSimpleBaseMapper().ztSimpleDeleteByPrimaryKey(ztQueryWrapper);
    //     ztParamEntity.setDeleteRow(integer);
    //     if (integer > 0) {
    //         ztParamEntity.setDeleteRes(true);
    //     }
    //     return ztParamEntity;
    // }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZtParamEntity<T> ztAfterSimpleDeleteByPrimaryKey(ZtParamEntity<T> ztParamEntity) throws Exception {
        return ztParamEntity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZtParamEntity<T> ztCannotSimpleDeleteByPrimaryKey(ZtParamEntity<T> ztParamEntity) throws Exception {
        ztParamEntity.getZtResBeanEx().setCode(ZtStrUtils.FAIL_CODE);
        ztParamEntity.getZtResBeanEx().setMsg(ZtStrUtils.FAIL_MSG);
        return ztParamEntity;
    }
    //endregion

    //region 根据主键批量删除
    @Override
    @Transactional(rollbackFor = Exception.class)
    public final ZtParamEntity<T> ztSimpleDeleteByPrimaryKeyBatch(ZtParamEntity<T> ztParamEntity) throws Exception {
        ztParamEntity = getThisService().ztBeforeSimpleDeleteByPrimaryKeyBatch(ztParamEntity);
        if (ztParamEntity.isCanDelete()) {
            // ztParamEntity = getThisService().ztDoSimpleDeleteByPrimaryKey(ztParamEntity);
            ZtQueryWrapper ztQueryWrapper = ztParamEntity.getZtQueryWrapper();
            Integer integer = getZtSimpleBaseMapper().ztSimpleDeleteByPrimaryKeyBatch(ztQueryWrapper);
            ztParamEntity.setDeleteRow(integer);
            if (integer > 0) {
                ztParamEntity.setDeleteRes(true);
            }
            if (ztParamEntity.isCanDelete() && ztParamEntity.isDeleteRes()) {
                ztParamEntity = getThisService().ztAfterSimpleDeleteByPrimaryKeyBatch(ztParamEntity);
            } else {
                ztParamEntity = getThisService().ztCannotSimpleDeleteByPrimaryKeyBatch(ztParamEntity);
            }
        } else {
            ztParamEntity = getThisService().ztCannotSimpleDeleteByPrimaryKeyBatch(ztParamEntity);
        }
        return ztParamEntity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZtParamEntity<T> ztBeforeSimpleDeleteByPrimaryKeyBatch(ZtParamEntity<T> ztParamEntity) throws Exception {
        ztParamEntity = this.initSimpleWrapper(ztParamEntity, SqlCommandType.DELETE);
        ZtQueryWrapper ztQueryWrapper = ztParamEntity.getZtQueryWrapper();
        ztQueryWrapper.setObjList(ztParamEntity.getEntityList());
        return ztParamEntity;
    }

    // @Override
    // @Transactional(rollbackFor = Exception.class)
    // public final ZtParamEntity<T> ztDoSimpleDeleteByPrimaryKeyBatch(ZtParamEntity<T> ztParamEntity) throws Exception {
    //     ZtQueryWrapper ztQueryWrapper = ztParamEntity.getZtQueryWrapper();
    //     Integer integer = getZtSimpleBaseMapper().ztSimpleDeleteByPrimaryKeyBatch(ztQueryWrapper);
    //     ztParamEntity.setDeleteRow(integer);
    //     if (integer > 0) {
    //         ztParamEntity.setDeleteRes(true);
    //     }
    //     return ztParamEntity;
    // }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZtParamEntity<T> ztAfterSimpleDeleteByPrimaryKeyBatch(ZtParamEntity<T> ztParamEntity) throws Exception {
        return ztParamEntity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZtParamEntity<T> ztCannotSimpleDeleteByPrimaryKeyBatch(ZtParamEntity<T> ztParamEntity) throws Exception {
        ztParamEntity.getZtResBeanEx().setCode(ZtStrUtils.FAIL_CODE);
        ztParamEntity.getZtResBeanEx().setMsg(ZtStrUtils.FAIL_MSG);
        return ztParamEntity;
    }
    //endregion

    //region 单个新增
    @Override
    @Transactional(rollbackFor = Exception.class)
    public final ZtParamEntity<T> ztSimpleInsert(ZtParamEntity<T> ztParamEntity) throws Exception {
        ztParamEntity = getThisService().ztBeforeSimpleInsert(ztParamEntity);
        if (ztParamEntity.isCanInsert()) {

            Integer integer = getZtSimpleBaseMapper().ztSimpleInsert(ztParamEntity.getZtQueryWrapper());
            ztParamEntity.setInsertRow(integer);
            if (integer > 0) {
                ztParamEntity.setInsertRes(true);
            }

            // ztParamEntity = getThisService().ztDoSimpleInsert(ztParamEntity);

            if (ztParamEntity.isCanInsert() && ztParamEntity.isInsertRes()) {
                ztParamEntity = getThisService().ztAfterSimpleInsert(ztParamEntity);
            } else {
                ztParamEntity = getThisService().ztCannotSimpleInsert(ztParamEntity);
            }
        } else {
            ztParamEntity = getThisService().ztCannotSimpleInsert(ztParamEntity);
        }
        return ztParamEntity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZtParamEntity<T> ztBeforeSimpleInsert(ZtParamEntity<T> ztParamEntity) throws Exception {
        T entity = ztParamEntity.getEntity();
        Date now = new Date();
        entity.setGmtCreate(now);
        entity.setGmtUpdate(now);
        if (!getManualId()) {
            entity.setId(null);
        }
        if (!StringUtils.isEmpty(getVersionFieldName())) {
            Field versionField = ZtUtils.getField(entity, getVersionFieldName());
            versionField.setAccessible(true);
            versionField.set(entity, 1L);
        }

        if (!StringUtils.isEmpty(getLogicDeleteFieldName())) {
            Field logicDeleteField = ZtUtils.getField(entity, getLogicDeleteFieldName());
            logicDeleteField.setAccessible(true);
            logicDeleteField.set(entity, !getLogicDeleteFlag());
        }

        if (StringUtils.isEmpty(entity.getRemark())) {
            entity.setRemark("");
        }
        ztParamEntity = this.initSimpleWrapper(ztParamEntity, SqlCommandType.INSERT);
        return ztParamEntity;
    }

    // @Override
    // @Transactional(rollbackFor = Exception.class)
    // public final ZtParamEntity<T> ztDoSimpleInsert(ZtParamEntity<T> ztParamEntity) throws Exception {
    //     ZtQueryWrapper ztQueryWrapper = ztParamEntity.getZtQueryWrapper();
    //     Integer integer = getZtSimpleBaseMapper().ztSimpleInsert(ztQueryWrapper);
    //     ztParamEntity.setInsertRow(integer);
    //     if (integer > 0) {
    //         ztParamEntity.setInsertRes(true);
    //     }
    //     return ztParamEntity;
    // }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZtParamEntity<T> ztAfterSimpleInsert(ZtParamEntity<T> ztParamEntity) throws Exception {
        return ztParamEntity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZtParamEntity<T> ztCannotSimpleInsert(ZtParamEntity<T> ztParamEntity) throws Exception {
        ztParamEntity.getZtResBeanEx().setCode(ZtStrUtils.FAIL_CODE);
        ztParamEntity.getZtResBeanEx().setMsg(ZtStrUtils.FAIL_MSG);
        return ztParamEntity;
    }
    //endregion

    //region 批量新增
    @Override
    @Transactional(rollbackFor = Exception.class)
    public final ZtParamEntity<T> ztSimpleInsertBatch(ZtParamEntity<T> ztParamEntity) throws Exception {
        ztParamEntity = getThisService().ztBeforeSimpleInsertBatch(ztParamEntity);
        if (ztParamEntity.isCanInsert()) {
            // ztParamEntity = getThisService().ztDoSimpleInsertBatch(ztParamEntity);
            ZtQueryWrapper ztQueryWrapper = ztParamEntity.getZtQueryWrapper();
            Integer integer = getZtSimpleBaseMapper().ztSimpleInsertBatch(ztParamEntity.getEntityList(), ztQueryWrapper);
            ztParamEntity.setInsertRow(integer);
            if (integer > 0) {
                ztParamEntity.setInsertRes(true);
            }
            if (ztParamEntity.isCanInsert() && ztParamEntity.isInsertRes()) {
                ztParamEntity = getThisService().ztAfterSimpleInsertBatch(ztParamEntity);
            } else {
                ztParamEntity = getThisService().ztCannotSimpleInsertBatch(ztParamEntity);
            }
        } else {
            ztParamEntity = getThisService().ztCannotSimpleInsertBatch(ztParamEntity);
        }
        return ztParamEntity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZtParamEntity<T> ztBeforeSimpleInsertBatch(ZtParamEntity<T> ztParamEntity) throws Exception {
        List<T> entityList = ztParamEntity.getEntityList();
        Date now = new Date();

        for (T entity : entityList) {
            if (!getManualId()) {
                entity.setId(null);
            }
            if (!StringUtils.isEmpty(getVersionFieldName())) {
                Field versionField = ZtUtils.getField(entity, getVersionFieldName());
                versionField.setAccessible(true);
                versionField.set(entity, 1L);
            }

            if (!StringUtils.isEmpty(getLogicDeleteFieldName())) {
                Field logicDeleteField = ZtUtils.getField(entity, getLogicDeleteFieldName());
                logicDeleteField.setAccessible(true);
                logicDeleteField.set(entity, !getLogicDeleteFlag());
            }

            entity.setGmtCreate(now);
            entity.setGmtUpdate(now);

            if (StringUtils.isEmpty(entity.getRemark())) {
                entity.setRemark("");
            }
        }
        if (ztParamEntity.getZtResBeanEx() == null) {
            ztParamEntity.setZtResBeanEx(ZtResBeanEx.ok());
        }
        if (ztParamEntity.isUseCommonZtQueryWrapper()) {

            ztParamEntity.setZtQueryWrapper(this.getQueryWrapper(ztParamEntity.getEntityList().get(0), true, SqlCommandType.INSERT));
            ztParamEntity.getZtQueryWrapper().setObjList(ztParamEntity.getEntityList());
        }
        return ztParamEntity;
    }

    // @Override
    // @Transactional(rollbackFor = Exception.class)
    // public final ZtParamEntity<T> ztDoSimpleInsertBatch(ZtParamEntity<T> ztParamEntity) throws Exception {
    //     ZtQueryWrapper ztQueryWrapper = ztParamEntity.getZtQueryWrapper();
    //     Integer integer = getZtSimpleBaseMapper().ztSimpleInsertBatch(ztParamEntity.getEntityList(), ztQueryWrapper);
    //     ztParamEntity.setInsertRow(integer);
    //     if (integer > 0) {
    //         ztParamEntity.setInsertRes(true);
    //     }
    //     return ztParamEntity;
    // }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZtParamEntity<T> ztAfterSimpleInsertBatch(ZtParamEntity<T> ztParamEntity) throws Exception {
        return ztParamEntity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZtParamEntity<T> ztCannotSimpleInsertBatch(ZtParamEntity<T> ztParamEntity) throws Exception {
        ztParamEntity.getZtResBeanEx().setCode(ZtStrUtils.FAIL_CODE);
        ztParamEntity.getZtResBeanEx().setMsg(ZtStrUtils.FAIL_MSG);
        return ztParamEntity;
    }
    //endregion
}
