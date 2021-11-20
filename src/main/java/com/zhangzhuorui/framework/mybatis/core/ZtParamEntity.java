package com.zhangzhuorui.framework.mybatis.core;

import com.zhangzhuorui.framework.core.ZtPropertyFunc;
import com.zhangzhuorui.framework.core.ZtResBeanEx;

import java.io.Serializable;
import java.util.List;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate : 2017-01-01
 * @description : 贯穿始终的类
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
public class ZtParamEntity<T> implements Serializable {

    //token信息
    private Object token;

    //根据token保存的用户信息
    private Object userInfo;

    //可以缓存一下id
    private Serializable id;

    //单个对象的实体类
    private T entity;

    //批量保存、批量删除
    private List<T> entityList;

    //备用
    private Object otherParams;

    //备用
    private List otherListParams;

    //返回前端
    private ZtResBeanEx ztResBeanEx;

    //增删改查操作结果
    private boolean canInsert = true;
    private boolean canDelAndSave = true;
    private boolean insertRes;
    private Integer insertRow;
    private boolean canUpdate = true;
    private boolean updateRes;
    private Integer updateRow;
    private boolean canDelete = true;
    private boolean deleteRes;
    private Integer deleteRow;
    private boolean canSave = true;
    private boolean saveRes;

    //自动生成增删改查条件
    private boolean useCommonZtQueryWrapper = true;
    //是否需要select count(*)
    private boolean needCount = true;
    //用于生成sql的包装类
    private ZtQueryWrapper<T> ztQueryWrapper;
    //join返回的dto
    private Class dtoClass;

    //条件更新的目标值
    private T destEntity;
    //条件更新的目标值wrapper 用于生成SQL中的SET语句
    private ZtQueryWrapper<T> destQueryWrapper;

    public T getDestEntity() {
        return destEntity;
    }

    public void setDestEntity(T destEntity) {
        this.destEntity = destEntity;
    }

    public ZtQueryWrapper<T> getDestQueryWrapper() {
        return destQueryWrapper;
    }

    public void setDestQueryWrapper(ZtQueryWrapper<T> destQueryWrapper) {
        this.destQueryWrapper = destQueryWrapper;
    }

    public Class getDtoClass() {
        return dtoClass;
    }

    public void setDtoClass(Class dtoClass) {
        this.dtoClass = dtoClass;
    }

    public Object getToken() {
        return token;
    }

    public void setToken(Object token) {
        this.token = token;
    }

    public Object getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(Object userInfo) {
        this.userInfo = userInfo;
    }

    public Serializable getId() {
        return id;
    }

    public void setId(Serializable id) {
        this.id = id;
    }

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }

    public List<T> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<T> entityList) {
        this.entityList = entityList;
    }

    public Object getOtherParams() {
        return otherParams;
    }

    public void setOtherParams(Object otherParams) {
        this.otherParams = otherParams;
    }

    public List getOtherListParams() {
        return otherListParams;
    }

    public void setOtherListParams(List otherListParams) {
        this.otherListParams = otherListParams;
    }

    public ZtResBeanEx getZtResBeanEx() {
        return ztResBeanEx;
    }

    public void setZtResBeanEx(ZtResBeanEx ztResBeanEx) {
        this.ztResBeanEx = ztResBeanEx;
    }

    public boolean isCanInsert() {
        return canInsert;
    }

    public void setCanInsert(boolean canInsert) {
        this.canInsert = canInsert;
    }

    public boolean isCanDelAndSave() {
        return canDelAndSave;
    }

    public void setCanDelAndSave(boolean canDelAndSave) {
        this.canDelAndSave = canDelAndSave;
    }

    public boolean isInsertRes() {
        return insertRes;
    }

    public void setInsertRes(boolean insertRes) {
        this.insertRes = insertRes;
    }

    public Integer getInsertRow() {
        return insertRow;
    }

    public void setInsertRow(Integer insertRow) {
        this.insertRow = insertRow;
    }

    public boolean isCanUpdate() {
        return canUpdate;
    }

    public void setCanUpdate(boolean canUpdate) {
        this.canUpdate = canUpdate;
    }

    public boolean isUpdateRes() {
        return updateRes;
    }

    public void setUpdateRes(boolean updateRes) {
        this.updateRes = updateRes;
    }

    public Integer getUpdateRow() {
        return updateRow;
    }

    public void setUpdateRow(Integer updateRow) {
        this.updateRow = updateRow;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }

    public boolean isDeleteRes() {
        return deleteRes;
    }

    public void setDeleteRes(boolean deleteRes) {
        this.deleteRes = deleteRes;
    }

    public Integer getDeleteRow() {
        return deleteRow;
    }

    public void setDeleteRow(Integer deleteRow) {
        this.deleteRow = deleteRow;
    }

    public boolean isCanSave() {
        return canSave;
    }

    public void setCanSave(boolean canSave) {
        this.canSave = canSave;
    }

    public boolean isSaveRes() {
        return saveRes;
    }

    public void setSaveRes(boolean saveRes) {
        this.saveRes = saveRes;
    }

    public boolean isUseCommonZtQueryWrapper() {
        return useCommonZtQueryWrapper;
    }

    public void setUseCommonZtQueryWrapper(boolean useCommonZtQueryWrapper) {
        this.useCommonZtQueryWrapper = useCommonZtQueryWrapper;
    }

    public boolean isNeedCount() {
        return needCount;
    }

    public void setNeedCount(boolean needCount) {
        this.needCount = needCount;
    }

    //-------------查询条件----------

    public ZtQueryWrapper<T> getZtQueryWrapper() {
        return ztQueryWrapper;
    }

    public void setZtQueryWrapper(ZtQueryWrapper<T> ztQueryWrapper) {
        this.ztQueryWrapper = ztQueryWrapper;
    }

    public ZtParamEntity<T> andLike(ZtPropertyFunc<T, ?> func) {
        ztQueryWrapper.andLike(func);
        return this;
    }

    public ZtParamEntity<T> orLike(ZtPropertyFunc<T, ?> func) {
        ztQueryWrapper.orLike(func);
        return this;
    }

    public ZtParamEntity<T> andNotLike(ZtPropertyFunc<T, ?> func) {
        ztQueryWrapper.andNotLike(func);
        return this;
    }

    public ZtParamEntity<T> orNotLike(ZtPropertyFunc<T, ?> func) {
        ztQueryWrapper.orNotLike(func);
        return this;
    }

    public ZtParamEntity<T> andNotEquals(ZtPropertyFunc<T, ?> func) {
        ztQueryWrapper.andNotEquals(func);
        return this;
    }

    public ZtParamEntity<T> orNotEquals(ZtPropertyFunc<T, ?> func) {
        ztQueryWrapper.orNotEquals(func);
        return this;
    }

    public ZtParamEntity<T> andGreatThan(ZtPropertyFunc<T, ?> func) {
        ztQueryWrapper.andGreatThan(func);
        return this;
    }

    public ZtParamEntity<T> orGreatThan(ZtPropertyFunc<T, ?> func) {
        ztQueryWrapper.orGreatThan(func);
        return this;
    }

    public ZtParamEntity<T> andLessThan(ZtPropertyFunc<T, ?> func) {
        ztQueryWrapper.andLessThan(func);
        return this;
    }

    public ZtParamEntity<T> orLessThan(ZtPropertyFunc<T, ?> func) {
        ztQueryWrapper.orLessThan(func);
        return this;
    }

    public ZtParamEntity<T> andGreatEquals(ZtPropertyFunc<T, ?> func) {
        ztQueryWrapper.andGreatEquals(func);
        return this;
    }

    public ZtParamEntity<T> orGreatEquals(ZtPropertyFunc<T, ?> func) {
        ztQueryWrapper.orGreatEquals(func);
        return this;
    }

    public ZtParamEntity<T> andLessEquals(ZtPropertyFunc<T, ?> func) {
        ztQueryWrapper.andLessEquals(func);
        return this;
    }

    public ZtParamEntity<T> orLessEquals(ZtPropertyFunc<T, ?> func) {
        ztQueryWrapper.orLessEquals(func);
        return this;
    }

    public ZtParamEntity<T> andIsNull(ZtPropertyFunc<T, ?> func) {
        ztQueryWrapper.andIsNull(func);
        return this;
    }

    public ZtParamEntity<T> orIsNull(ZtPropertyFunc<T, ?> func) {
        ztQueryWrapper.orIsNull(func);
        return this;
    }

    public ZtParamEntity<T> andIsNotNull(ZtPropertyFunc<T, ?> func) {
        ztQueryWrapper.andIsNotNull(func);
        return this;
    }

    public ZtParamEntity<T> orIsNotNull(ZtPropertyFunc<T, ?> func) {
        ztQueryWrapper.orIsNotNull(func);
        return this;
    }

    public ZtParamEntity<T> andIn(ZtPropertyFunc<T, ?> func, List list) {
        ztQueryWrapper.andIn(func, list);
        return this;
    }

    public ZtParamEntity<T> orIn(ZtPropertyFunc<T, ?> func, List list) {
        ztQueryWrapper.orIn(func, list);
        return this;
    }

    public ZtParamEntity<T> andNotIn(ZtPropertyFunc<T, ?> func, List list) {
        ztQueryWrapper.andNotIn(func, list);
        return this;
    }

    public ZtParamEntity<T> orNotIn(ZtPropertyFunc<T, ?> func, List list) {
        ztQueryWrapper.orNotIn(func, list);
        return this;
    }

    public ZtParamEntity<T> andBetween(ZtPropertyFunc<T, ?> func, Object firstValue, Object secondValue) {
        ztQueryWrapper.andBetween(func, firstValue, secondValue);
        return this;
    }

    public ZtParamEntity<T> orBetween(ZtPropertyFunc<T, ?> func, Object firstValue, Object secondValue) {
        ztQueryWrapper.orBetween(func, firstValue, secondValue);
        return this;
    }

    public ZtParamEntity<T> andNotBetween(ZtPropertyFunc<T, ?> func, Object firstValue, Object secondValue) {
        ztQueryWrapper.andNotBetween(func, firstValue, secondValue);
        return this;
    }

    public ZtParamEntity<T> orNotBetween(ZtPropertyFunc<T, ?> func, Object firstValue, Object secondValue) {
        ztQueryWrapper.orNotBetween(func, firstValue, secondValue);
        return this;
    }
}
