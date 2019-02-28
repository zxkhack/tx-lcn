package com.codingapi.txlcn.tc.core.context;

import com.codingapi.txlcn.common.util.Transactions;
import com.codingapi.txlcn.tc.aspect.Invocation;
import org.springframework.util.Assert;
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
    public void cacheTransactionAttribute(String mappedMethodName, Map<Object, Object> attributes) {
        MAP.put(mappedMethodName, attributes);
    }

    @Override
    public TransactionAttribute getTransactionAttribute(String unitId) {
        return fromMap(MAP.get(unitId), unitId);
    }

    @Override
    public TransactionAttribute getTransactionAttribute(Invocation invocation) {
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

    @Override
    public synchronized void updateTransactionAttribute(String mappedMethodName, Map<Object, Object> attributes) {
        Assert.notNull(attributes, "attributes must not null");
        Map<Object, Object> attrs = MAP.get(mappedMethodName);
        Assert.notNull(attrs, "must not null when update");

        for (Map.Entry<Object, Object> entry : attrs.entrySet()) {
            if (attributes.containsKey(entry.getKey())) {
                entry.setValue(attributes.get(entry.getKey()));
            }
        }
    }

    private boolean isMatch(String methodName, String mappedName) {
        return PatternMatchUtils.simpleMatch(mappedName, methodName);
    }

    private TransactionAttribute fromMap(Map<Object, Object> attrs, String unitId) {
        TransactionAttribute transactionAttribute = new TransactionAttribute();
        transactionAttribute.setUnitId(unitId);
        transactionAttribute.setTransactionType((String) attrs.get(TRANSACTION_TYPE));
        transactionAttribute.setCommitBeanName((String) attrs.get(TRANSACTION_COMMIT_BEAN));
        transactionAttribute.setCommitMethod((String) attrs.get(TRANSACTION_COMMIT_METHOD));
        transactionAttribute.setRollbackBeanName((String) attrs.get(TRANSACTION_ROLLBACK_BEAN));
        transactionAttribute.setRollbackMethod((String) attrs.get(TRANSACTION_ROLLBACK_METHOD));
        return transactionAttribute;
    }
}
