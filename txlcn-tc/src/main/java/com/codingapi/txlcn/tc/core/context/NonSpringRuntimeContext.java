package com.codingapi.txlcn.tc.core.context;

import com.codingapi.txlcn.tc.jta.Invocation;

import java.util.Map;

/**
 * Description:
 * Date: 19-2-27 上午10:43
 *
 * @author ujued
 */
public interface NonSpringRuntimeContext {

    String TRANSACTION_TYPE = "tx.type";

    static NonSpringRuntimeContext instance() {
        return new DefaultNonSpringRuntimeContext();
    }

    void cacheTransactionAttributes(String mappedMethodName, Map<Object, Object> attributes);

    Map<Object, Object> getTransactionAttributes(String mappedMethodName);

    TransactionAttributes getTransactionAttributes(Invocation invocation);
}
