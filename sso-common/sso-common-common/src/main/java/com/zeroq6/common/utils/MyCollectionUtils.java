
package com.zeroq6.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.util.TypeUtils;
import org.apache.commons.lang3.StringUtils;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by icgeass on 2017/1/3.
 */

public class MyCollectionUtils {

    /**
     * base Collection 中不能有空值
     *
     * @param base
     * @param now
     * @param targetType
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> ListDiffDomain<T> diffList(Collection base, Collection now, TypeReference<T> targetType, String methodName) throws Exception {
        if (null == base) {
            base = new ArrayList<T>();
        }
        if (null == now) {
            now = new ArrayList<T>();
        }
        Map<String, T> baseMap = transferMap(base, targetType, methodName, false);
        Map<String, T> nowMap = transferMap(now, targetType, methodName, true);
        List<T> modifiedList = new ArrayList<T>();
        List<T> newList = new ArrayList<T>();
        List<T> deleteList = new ArrayList<T>();
        for (String key : baseMap.keySet()) {
            if (nowMap.keySet().contains(key)) {
                modifiedList.add(nowMap.get(key)); // 均包含--修改
            } else {
                deleteList.add(baseMap.get(key)); // 原有包含，现有不包含---删除
            }
        }
        for (String key : nowMap.keySet()) {
            if (!baseMap.keySet().contains(key)) {
                newList.add(nowMap.get(key)); // 现有包含，原有不包含----新增，如果现有的有key---null，这里的key是null_0,null_1,null_2，对应的id主键为null，会添加到newList里面
            }
        }
        return new ListDiffDomain<T>(modifiedList, newList, deleteList);
    }


    /**
     *
     * @param collection
     * @param targetType
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> Map<String, T> transferMap(Collection<?> collection, TypeReference<T> targetType, String methodName, boolean acceptNull) throws Exception {
        long n = 0;
        Iterator<?> baseIterator = collection.iterator();
        Map<String, T> result = new HashMap<String, T>();
        while (baseIterator.hasNext()) {
            Object o = baseIterator.next();//getSuperclass()
            String id = String.valueOf(o.getClass().getMethod(methodName).invoke(o));
            if ("null".equals(id)) {
                if (acceptNull) {
                    id = id + "_" + n;
                    n++;
                } else {
                    throw new RuntimeException(methodName + "返回null");
                }
            }
            result.put(id, MyTypeUtils.transfer(o, targetType));
        }
        return result;
    }


    public static <T> List<T> transferList(Map<String, T> source) {
        List<T> result = new ArrayList<T>();
        for (String key : source.keySet()) {
            T t = source.get(key);
            if (null == t) {
                throw new RuntimeException("map中含有null值, key = " + key);
            }
            result.add(t);
        }
        return result;
    }

    public static <T> boolean contains(Collection<T> base, T key, String methodName) throws Exception {
        String id = String.valueOf(key.getClass().getMethod(methodName).invoke(key));
        if ("null".equals(id)) {
            throw new RuntimeException("key中" + methodName + "返回null");
        }
        List<String> baseId = new ArrayList<String>();
        Iterator<T> iterator = base.iterator();
        while (iterator.hasNext()) {
            T o = iterator.next();
            Object value = o.getClass().getMethod(methodName).invoke(o);
            if (null == o || null == value) {
                throw new RuntimeException("collection中元素为null或元素中" + methodName + "方法返回null");
            }
            baseId.add(String.valueOf(o.getClass().getMethod(methodName).invoke(o)));
        }
        return baseId.contains(id);
    }


    /**
     * 用map中的key-value填充collection中的bean
     * @param collection
     * @param prop2Value
     * @throws Exception
     */
    public static void fillProps(Collection<?> collection, Map<String, Object> prop2Value) throws Exception {
        if (null == collection || collection.isEmpty() || null == prop2Value || prop2Value.isEmpty()) {
            return;
        }
        Iterator<?> iterator = collection.iterator();
        while (iterator.hasNext()) {
            Object invokeObj = iterator.next();
            PropertyDescriptor[] des = Introspector.getBeanInfo(invokeObj.getClass()).getPropertyDescriptors();
            for (PropertyDescriptor d : des) {
                if (!prop2Value.containsKey(d.getName())) {
                    continue;
                }
                String type = d.getPropertyType().getCanonicalName();
                Method writeMethod = d.getWriteMethod();
                Object value = prop2Value.get(d.getName()); // if return null, no error occur
                if ("java.lang.String".equals(type)) {
                    writeMethod.invoke(invokeObj, TypeUtils.castToString(value));
                } else if ("java.math.BigDecimal".equals(type)) {
                    writeMethod.invoke(invokeObj, TypeUtils.castToBigDecimal(value));
                } else if ("java.util.Date".equals(type)) {
                    writeMethod.invoke(invokeObj, TypeUtils.castToDate(value));
                } else if ("java.lang.Long".equals(type)) {
                    writeMethod.invoke(invokeObj, TypeUtils.castToLong(value));
                } else if ("java.lang.Integer".equals(type)) {
                    writeMethod.invoke(invokeObj, TypeUtils.castToInt(value));
                } else if ("java.lang.Float".equals(type)) {
                    writeMethod.invoke(invokeObj, TypeUtils.castToFloat(value));
                } else if ("java.lang.Double".equals(type)) {
                    writeMethod.invoke(invokeObj, TypeUtils.castToDouble(value));
                } else if ("java.lang.Character".equals(type)) {
                    writeMethod.invoke(invokeObj, TypeUtils.castToChar(value));
                } else if ("java.lang.Boolean".equals(type)) {
                    writeMethod.invoke(invokeObj, TypeUtils.castToBoolean(value));
                } else if ("java.lang.Short".equals(type)) {
                    writeMethod.invoke(invokeObj, TypeUtils.castToShort(value));
                } else if ("java.lang.Byte".equals(type)) {
                    writeMethod.invoke(invokeObj, TypeUtils.castToByte(value));
                } else {
                    // silent skip
                }
            }
        }
    }


    /**
     * 1. sourceMap属性中对应某列值为空字符串 --- skip
     * 2. propValue2Null指定 --- null
     * @param sourceMap
     * @param clz
     * @param propValue2Null
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> List<T> transferListObject(Map<String, Object> sourceMap, Class<T> clz, Map<String, Object> propValue2Null) throws Exception {
        List<T> result = new ArrayList<T>();
        int length = -1;
        PropertyDescriptor[] des = Introspector.getBeanInfo(clz).getPropertyDescriptors();
        for (PropertyDescriptor d : des) {
            String fieldName = d.getName();
            Object object = sourceMap.get(d.getName());
            if (null != object && (object instanceof String || object instanceof String[]) && !"class".equalsIgnoreCase(fieldName)) {
                String[] cols = null;
                if (object instanceof String) {
                    cols = new String[]{(String) object};
                } else {
                    cols = (String[]) object;
                }
                if (null != cols && cols.length > 0) {
                    if (length == -1) {
                        length = cols.length;
                        for (int i = 0; i < length; i++) {
                            result.add(clz.newInstance());
                        }
                    } else {
                        if (length != cols.length) {
                            throw new RuntimeException("列长度不相同, " + fieldName + ", " + JSON.toJSONString(cols));
                        }
                    }
                    for (int i = 0; i < length; i++) {
                        if(StringUtils.isBlank(cols[i])){
                            continue; // skip blank value
                        }
                        String type = d.getPropertyType().getCanonicalName();
                        Method writeMethod = d.getWriteMethod();
                        Object invokeObj = result.get(i);
                        // now we first process field-value that implies null
                        if (null != propValue2Null && !propValue2Null.isEmpty() && propValue2Null.containsKey(fieldName)) {
                            String v = String.valueOf(propValue2Null.get(fieldName)); // if propValue2Null get(fieldName) return null then "null"
                            if (v.equals(cols[i])) {
                                writeMethod.invoke(invokeObj, new Object[]{null});
                                continue;
                            }
                        }
                        if ("java.lang.String".equals(type)) { // if cols[i] is null, no error occur
                            writeMethod.invoke(invokeObj, TypeUtils.castToString(cols[i]));
                        } else if ("java.math.BigDecimal".equals(type)) {
                            writeMethod.invoke(invokeObj, TypeUtils.castToBigDecimal(cols[i]));
                        } else if ("java.util.Date".equals(type)) {
                            writeMethod.invoke(invokeObj, TypeUtils.castToDate(cols[i]));
                        } else if ("java.lang.Long".equals(type)) {
                            writeMethod.invoke(invokeObj, TypeUtils.castToLong(cols[i]));
                        } else if ("java.lang.Integer".equals(type)) {
                            writeMethod.invoke(invokeObj, TypeUtils.castToInt(cols[i]));
                        } else if ("java.lang.Float".equals(type)) {
                            writeMethod.invoke(invokeObj, TypeUtils.castToFloat(cols[i]));
                        } else if ("java.lang.Double".equals(type)) {
                            writeMethod.invoke(invokeObj, TypeUtils.castToDouble(cols[i]));
                        } else if ("java.lang.Character".equals(type)) {
                            writeMethod.invoke(invokeObj, TypeUtils.castToChar(cols[i]));
                        } else if ("java.lang.Boolean".equals(type)) {
                            writeMethod.invoke(invokeObj, TypeUtils.castToBoolean(cols[i]));
                        } else if ("java.lang.Short".equals(type)) {
                            writeMethod.invoke(invokeObj, TypeUtils.castToShort(cols[i]));
                        } else if ("java.lang.Byte".equals(type)) {
                            writeMethod.invoke(invokeObj, TypeUtils.castToByte(cols[i]));
                        } else {
                            // silent skip
                        }
                    }
                }

            }
        }
        return result;
    }

    public static boolean containsAll(Collection<?> collection, Object... target) {
        if(null == collection || collection.isEmpty() || null == target || target.length == 0){
            throw new RuntimeException("collection, target不能为空");
        }
        for(Object object : target){
            if(!collection.contains(object)){
                return false;
            }
        }
        return true;
    }

}