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
    public Map<Object, Object> getTransactionAttributes(String mappedMethodName) {
        return MAP.get(mappedMethodName);
    }

    @Override
    public TransactionAttributes getTransactionAttributes(Invocation invocation) {
        if (!ClassUtils.isUserLevelMethod(invocation.getMethod())) {
            return null;
        }

        // Look for direct name match.
        String methodName = invocation.getMethod().getName();
        Map<Object, Object> attrs = getTransactionAttributes(methodName);

        if (attrs == null) {
            // Look for most specific name match.
            String bestNameMatch = null;
            for (String mappedName : MAP.keySet()) {
                if (isMatch(methodName, mappedName) &&
                        (bestNameMatch == null || bestNameMatch.length() <= mappedName.length())) {
                    attrs = getTransactionAttributes(mappedName);
                    bestNameMatch = mappedName;
                }
            }
        }
        // todo cache
        if (Objects.nonNull(attrs)) {
            return new TransactionAttributes((String) attrs.get(TRANSACTION_TYPE), Transactions.unitId(invocation.getMethod().toString()));
        }
        throw new IllegalStateException("");
    }

    private boolean isMatch(String methodName, String mappedName) {
        return PatternMatchUtils.simpleMatch(mappedName, methodName);
    }
}
