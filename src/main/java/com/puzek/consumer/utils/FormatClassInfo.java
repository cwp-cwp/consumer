package com.puzek.consumer.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 格式化类信息
 */
public class FormatClassInfo {

    public static String format(Object tClass) {
        StringBuilder result = new StringBuilder();
        try {
            // 获取当前类的全部属性
            Field[] fields = tClass.getClass().getDeclaredFields();
            for (Field field : fields) {
                // 遍历属性得到属性名
                String fieldName = field.getName();
                // 如果是用于序列化的直接过滤掉
                if ("serialVersionUID".equals(fieldName))
                    continue;
                // 判断属性的类型，主要是区分boolean和其他类型
                Class<?> type = field.getType();
                // boolean的取值是is,其他是get
                String methodName = (type.getTypeName().equals("boolean") ? "is" : "get") + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1, fieldName.length());
                Method method;
                Object resultObj;
                // 通过方法名得到方法对象
                method = tClass.getClass().getMethod(methodName);
                // 得到这个方法的返回值
                resultObj = method.invoke(tClass);

                // 将属性名和它对应的值进行匹配打印
                if (resultObj != null && !"".equals(resultObj)) {
                    if (type.getName().equals(Object.class.getName()))
                        result.append("[").append(fieldName).append("]").append("\n").append(format(resultObj)).append("\n");
                    else if (type.getName().equals(List.class.getName())) {
                        List<Object> list = (List<Object>) resultObj;
                        for (Object t : list)
                            result.append("[").append(fieldName).append("]").append("\n").append(format(t)).append("\n");
                    } else
                        result.append("[").append(fieldName).append("]").append(resultObj).append("\n");
                }
            }
        } catch (SecurityException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return result.toString();
    }
}
