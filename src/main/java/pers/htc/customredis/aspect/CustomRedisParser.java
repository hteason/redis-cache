package pers.htc.customredis.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import pers.htc.customredis.annotation.CustomRedis;
import pers.htc.customredis.annotation.RedisKeyParam;
import pers.htc.customredis.model.CustomRedisModel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CustomRedisParser {

    private final ExpressionParser parser = new SpelExpressionParser();
    private final LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();

    public CustomRedisModel parse(ProceedingJoinPoint pjp) {
        CustomRedisModel customRedisModel = resolveMethodCustomRedisAnnotationConfig(pjp);
        String key = customRedisModel.getKey();
        String redisKey = parseSpel(pjp, key, key);
        customRedisModel.setRedisKey(redisKey);
        return customRedisModel;
    }

    private CustomRedisModel resolveMethodCustomRedisAnnotationConfig(ProceedingJoinPoint pjp) {
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        CustomRedis customRedis = methodSignature.getMethod().getAnnotation(CustomRedis.class);
        String spel = customRedis.key();
        long expireTme = customRedis.expireTme();
        int failCount = customRedis.failCount();
        TimeUnit timeUnit = customRedis.expireTimeUnit();
        long extendTime = customRedis.extendTime();
        TimeUnit extendTimeUnit = customRedis.extendTimeUnit();

        CustomRedisModel model = new CustomRedisModel();
        model.setKey(spel);
        model.setExpireTme(expireTme);
        model.setFailCount(failCount);
        model.setExpireTimeUnit(timeUnit);
        model.setExtendTime(extendTime);
        model.setExtendTimeUnit(extendTimeUnit);
        return model;
    }

    /**
     * 解析 spel 表达式
     *
     * @param spel       表达式
     * @param defaultKey 默认结果
     * @return 执行spel表达式后的结果
     */
    private String parseSpel(ProceedingJoinPoint pjp, String spel, String defaultKey) {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        String[] params = discoverer.getParameterNames(method);
        if (params == null || params.length == 0) {
            log.warn("缓存层没有入参,默认使用key的字符串形式:" + defaultKey);
            return defaultKey;
        }
        Object[] arguments = pjp.getArgs();
        EvaluationContext context = new StandardEvaluationContext();
        for (int index = 0; index < params.length; index++) {
            context.setVariable(params[index], arguments[index]);
        }
        try {
            ParserContext templateParserContext = new TemplateParserContext("{", "}");
            Expression expression = parser.parseExpression(spel, templateParserContext);
            return expression.getValue(context, String.class);
        } catch (Exception e) {
            return defaultKey;
        }
    }

    private void resolveArgs(ProceedingJoinPoint pjp, CustomRedisModel customRedisModel) {
        Object[] args = pjp.getArgs();
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        Annotation[][] parameterAnnotations = methodSignature.getMethod().getParameterAnnotations();
        if (parameterAnnotations.length == 0) {
            return;
        }

        String key = customRedisModel.getKey();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] parameterAnnotation = parameterAnnotations[i];
            for (Annotation annotation : parameterAnnotation) {
                if (annotation instanceof RedisKeyParam) {
                    RedisKeyParam redisKeyParam = (RedisKeyParam) annotation;
                    String keyItem = "#" + redisKeyParam.value();
//                    if (Objects.equals(keyItem, "")) {
                    //没有设置key名时，key默认为参数名
                    String keyValue = String.valueOf(args[i]);
                    System.out.println(keyValue);
                    key = key.replaceAll(keyItem, keyValue);
                    break;//只会有一个注解，立即退出
                }
            }
        }
        System.out.println("key -> " + key);
    }

}
