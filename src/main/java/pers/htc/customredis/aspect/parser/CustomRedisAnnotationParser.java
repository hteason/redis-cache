package pers.htc.customredis.aspect.parser;

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
import pers.htc.customredis.aspect.model.CustomRedisModel;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 解析注解数据
 *
 * @author huangtingcheng
 * @since 2020-08-24
 **/
@Component
@Slf4j
public class CustomRedisAnnotationParser {

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
            log.warn("缓存层没有入参,默认使用key的字符串形式 => " + defaultKey);
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

}
