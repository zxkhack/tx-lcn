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

    String TRANSACTION_COMMIT_BEAN = "tx.bean.commit";

    String TRANSACTION_ROLLBACK_BEAN = "tx.bean.rollback";

    String TRANSACTION_COMMIT_METHOD = "tx.method.commit";

    String TRANSACTION_ROLLBACK_METHOD = "tx.method.rollback";

    static NonSpringRuntimeContext instance() {
        return new DefaultNonSpringRuntimeContext();
    }

    void cacheTransactionAttributes(String mappedMethodName, Map<Object, Object> attributes);

    TransactionAttributes getTransactionAttributes(String unitId);

    TransactionAttributes getTransactionAttributes(Invocation invocation);
}
