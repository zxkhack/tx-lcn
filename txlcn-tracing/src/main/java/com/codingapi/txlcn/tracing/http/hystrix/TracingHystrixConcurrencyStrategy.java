package com.codingapi.txlcn.tracing.http.hystrix;

import com.codingapi.txlcn.tracing.TracingContext;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariable;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableLifecycle;
import com.netflix.hystrix.strategy.properties.HystrixProperty;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Description:
 * Date: 19-2-15 下午1:28
 *
 * @author ujued
 */
@Slf4j
public class TracingHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy {

    private HystrixConcurrencyStrategy delegate;

    public TracingHystrixConcurrencyStrategy(HystrixConcurrencyStrategy concurrencyStrategy) {
        this.delegate = concurrencyStrategy;
    }

    @Override
    public <T> Callable<T> wrapCallable(Callable<T> callable) {
        Map<String, String> fields = TracingContext.tracing().fields();
        return () -> {
            boolean isReInitTracingContext = true;
            try {
                HystrixConcurrencyStrategy strategy = TracingHystrixConcurrencyStrategy.this.delegate;
                if (TracingContext.tracing().hasGroup()) {
                    isReInitTracingContext = false;
                    return strategy == null ?
                            TracingHystrixConcurrencyStrategy.super.wrapCallable(callable).call() :
                            strategy.wrapCallable(callable).call();
                }
                log.debug("Hystrix transfer tracing.");
                TracingContext.init(fields);
                return strategy == null ?
                        TracingHystrixConcurrencyStrategy.super.wrapCallable(callable).call() :
                        strategy.wrapCallable(callable).call();
            } finally {
                if (isReInitTracingContext) {
                    TracingContext.tracing().destroy();
                }
            }
        };
    }

    @Override
    public ThreadPoolExecutor getThreadPool(final HystrixThreadPoolKey threadPoolKey, HystrixProperty<Integer> corePoolSize, HystrixProperty<Integer> maximumPoolSize, HystrixProperty<Integer> keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        return this.delegate == null ?
                super.getThreadPool(threadPoolKey, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue) :
                this.delegate.getThreadPool(threadPoolKey, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override
    public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey, HystrixThreadPoolProperties threadPoolProperties) {
        return this.delegate == null ?
                super.getThreadPool(threadPoolKey, threadPoolProperties) :
                this.delegate.getThreadPool(threadPoolKey, threadPoolProperties);
    }

    @Override
    public BlockingQueue<Runnable> getBlockingQueue(int maxQueueSize) {
        return this.delegate == null ?
                super.getBlockingQueue(maxQueueSize) :
                this.delegate.getBlockingQueue(maxQueueSize);
    }

    @Override
    public <T> HystrixRequestVariable<T> getRequestVariable(HystrixRequestVariableLifecycle<T> rv) {
        return this.delegate == null ?
                super.getRequestVariable(rv) :
                this.delegate.getRequestVariable(rv);
    }
}
