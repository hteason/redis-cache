package pers.htc.customredis.aspect.handler;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 嵌套泛型解析
 *
 * @author huangtingcheng
 * @since 2020-08-28 17:13
 **/
@Slf4j
public class ClassTypeHandler {
    private Type[] types;

    /**
     * 获取嵌套泛型最外层的类,如List<String>的最外层类为List
     * 默认在下标0位置
     */
    public Class<?> getOuterClass() {
        return types != null && types.length != 0 ? (Class<?>) types[0] : null;
    }

    /**
     * 嵌套泛型解析
     * 如:List<String>解析为数组[List.class, String.class]
     *
     * @return 下标[0]为包裹泛型最外部的类, 其余为嵌套泛型类
     */
    public ClassTypeHandler(Method method) {
        List<Type> typeList = new ArrayList<>();
        //获取最外层类
        Type clz = method.getReturnType();

        typeList.add(clz);
        // 获取返回值类型
        Type type = method.getGenericReturnType();
        // 判断获取的类型是否是参数类型
        if (type instanceof ParameterizedType) {
            // 强制转型为带参数的泛型类型
            Type[] typeArgs = ((ParameterizedType) type).getActualTypeArguments();
            typeList.addAll(Arrays.asList(typeArgs));
        }
        //转数组
        types = typeList.toArray(new Type[0]);
    }


    /**
     * 将数据转换为返回类型对象
     *
     * @param obj 数据
     * @return 转换后的对象数据
     */
    public <T> T transToClassTypeObject(Object obj) {
        String jsonStr = JSONObject.toJSONString(obj);
        return JSONObject.parseObject(jsonStr, buildType());
    }

    /**
     * 构建泛型的嵌套关系
     */
    private Type buildType() {
        ParameterizedTypeImpl beforeType = null;
        if (types != null && types.length > 0) {
            if (types.length == 1) {
                return types[0];
            }
            for (int i = types.length - 1; i > 0; i--) {
                beforeType = new ParameterizedTypeImpl(new Type[]{beforeType == null ? types[i] : beforeType}, null, types[i - 1]);
            }
        }
        return beforeType;
    }
}
