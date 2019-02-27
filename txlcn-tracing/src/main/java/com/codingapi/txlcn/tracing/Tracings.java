package com.codingapi.txlcn.tracing;

import com.codingapi.txlcn.common.util.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Description: DTX Tracing 工具
 * Date: 19-2-11 下午1:02
 *
 * @author ujued
 */
@Slf4j
@Component
public class Tracings implements ApplicationContextAware {

    private static List<TracingTrigger> tracingTriggers = new LinkedList<>();

    public static void registerTracingTriggers(Collection<TracingTrigger> tracingTriggers) {
        Tracings.tracingTriggers.addAll(tracingTriggers);
    }

    /**
     * 私有构造器
     */
    private Tracings() {
    }

    /**
     * 传输Tracing信息
     *
     * @param tracingSetter Tracing信息设置器
     */
    public static void transmit(TracingSetter tracingSetter) {
        if (TracingContext.tracing().hasGroup()) {
            log.debug("tracing transmit group:{}", TracingContext.tracing().groupId());
            tracingSetter.set(TracingConstants.HEADER_KEY_GROUP_ID, TracingContext.tracing().groupId());
            tracingSetter.set(TracingConstants.HEADER_KEY_APP_MAP,
                    Base64Utils.encodeToString(TracingContext.tracing().appMapString().getBytes(StandardCharsets.UTF_8)));
            tracingTriggers.forEach(TracingTrigger::onTracingTransmit);
        }
    }

    /**
     * 获取传输的Tracing信息
     *
     * @param tracingGetter Tracing信息获取器
     */
    public static boolean apply(TracingGetter tracingGetter) {
        String groupId = Optional.ofNullable(tracingGetter.get(TracingConstants.HEADER_KEY_GROUP_ID)).orElse("");
        String appList = Optional.ofNullable(tracingGetter.get(TracingConstants.HEADER_KEY_APP_MAP)).orElse("");
        TracingContext.init(Maps.newHashMap(TracingConstants.GROUP_ID, groupId, TracingConstants.APP_MAP,
                StringUtils.isEmpty(appList) ? appList : new String(Base64Utils.decodeFromString(appList), StandardCharsets.UTF_8)));
        if (TracingContext.tracing().hasGroup()) {
            log.debug("tracing apply group:{}, app map:{}", groupId, appList);
            tracingTriggers.forEach(TracingTrigger::onTracingApply);
            return true;
        }
        return false;
    }

    public static void applyBack(boolean isApplied) {
        if (isApplied) {
            tracingTriggers.forEach(TracingTrigger::onTracingApplyBack);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, TracingTrigger> txPropagationTriggerMap = applicationContext.getBeansOfType(TracingTrigger.class);
        if (!txPropagationTriggerMap.isEmpty()) {
            registerTracingTriggers(txPropagationTriggerMap.values());
        }
    }

    /**
     * Tracing信息设置器
     */
    public interface TracingSetter {
        /**
         * 设置tracing属性
         *
         * @param key   key
         * @param value value
         */
        void set(String key, String value);
    }

    /**
     * Tracing信息获取器
     */
    public interface TracingGetter {
        /**
         * 获取tracing属性
         *
         * @param key key
         * @return tracing value
         */
        String get(String key);
    }
}
