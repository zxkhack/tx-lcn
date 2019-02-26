package com.codingapi.txlcn.tracing;

import org.springframework.core.Ordered;

/**
 * Description:
 * Date: 19-2-26 下午3:26
 *
 * @author ujued
 */
public interface TracingTrigger extends Ordered {

    default void onTracingTransmit() {
    }

    default void onTracingApply() {
    }

    default void onTracingApplyBack() {
    }

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
