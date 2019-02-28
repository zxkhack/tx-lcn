package com.codingapi.txlcn.tc.core.context;

import com.codingapi.txlcn.tc.core.Invocation;

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

    void cacheTransactionAttribute(String mappedMethodName, Map<Object, Object> attributes);

    TransactionAttribute getTransactionAttribute(String unitId);

    TransactionAttribute getTransactionAttribute(Invocation invocation);
}
