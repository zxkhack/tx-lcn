package com.codingapi.txlcn.tc.core.context;

import com.codingapi.txlcn.common.util.Transactions;
import com.codingapi.txlcn.tc.jta.Invocation;
import org.springframework.util.ClassUtils;
import org.springframework.util.PatternMatchUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Description:
 * Date: 19-2-27 上午10:46
 *
 * @author ujued
 */
public class DefaultNonSpringRuntimeContext implements NonSpringRuntimeContext {

    private static final Map<String, Map<Object, Object>> MAP = new HashMap<>();

    DefaultNonSpringRuntimeContext() {
    }

    @Override
    public void cacheTransactionAttributes(String mappedMethodName, Map<Object, Object> attributes) {
        MAP.put(mappedMethodName, attributes);
    }

    @Override
    public TransactionAttributes getTransactionAttributes(String unitId) {
        return fromMap(MAP.get(unitId), unitId);
    }

    @Override
    public TransactionAttributes getTransactionAttributes(Invocation invocation) {
        String unitId = Transactions.unitId(invocation.getMethod().toString());
        if (!ClassUtils.isUserLevelMethod(invocation.getMethod())) {
            return null;
        }

        // Look for direct name match.
        String methodName = invocation.getMethod().getName();
        Map<Object, Object> attrs = MAP.get(methodName);

        if (attrs == null) {
            // Look for most specific name match.
            String bestNameMatch = null;
            for (String mappedName : MAP.keySet()) {
                if (isMatch(methodName, mappedName) &&
                        (bestNameMatch == null || bestNameMatch.length() <= mappedName.length())) {
                    attrs = MAP.get(mappedName);
                    bestNameMatch = mappedName;
                    MAP.put(unitId, attrs);
                    MAP.put(methodName, attrs);
                }
            }
        } else if (!MAP.containsKey(unitId)) {
            MAP.put(unitId, attrs);
        }

        if (Objects.nonNull(attrs)) {
            return fromMap(attrs, unitId);
        }
        throw new IllegalStateException("");
    }

    private boolean isMatch(String methodName, String mappedName) {
        return PatternMatchUtils.simpleMatch(mappedName, methodName);
    }

    private TransactionAttributes fromMap(Map<Object, Object> attrs, String unitId) {
        TransactionAttributes transactionAttributes = new TransactionAttributes();
        transactionAttributes.setUnitId(unitId);
        transactionAttributes.setTransactionType((String) attrs.get(TRANSACTION_TYPE));
        transactionAttributes.setCommitBeanName((String) attrs.get(TRANSACTION_COMMIT_BEAN));
        transactionAttributes.setCommitMethod((String) attrs.get(TRANSACTION_COMMIT_METHOD));
        transactionAttributes.setRollbackBeanName((String) attrs.get(TRANSACTION_ROLLBACK_BEAN));
        transactionAttributes.setRollbackBeanName((String) attrs.get(TRANSACTION_ROLLBACK_METHOD));
        return transactionAttributes;
    }
}
