/*
 * Copyright 2017-2019 CodingApi .
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codingapi.txlcn.tc.aspect;

import com.codingapi.txlcn.tc.core.DTXLocalContext;
import com.codingapi.txlcn.tc.core.context.NonSpringRuntimeContext;
import com.codingapi.txlcn.tc.core.context.BranchContext;
import com.codingapi.txlcn.tc.core.context.TransactionAttribute;
import com.codingapi.txlcn.tracing.TracingContext;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.lang.NonNull;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.jta.JtaTransactionManager;

import java.util.Objects;
import java.util.Properties;

/**
 * Description: LCN based {@link TransactionInterceptor}
 * Date: 19-2-27 上午9:42
 *
 * @author ujued
 */
public class LcnTransactionInterceptor extends TransactionInterceptor {

    private final BranchContext globalContext;

    public LcnTransactionInterceptor(BranchContext globalContext) {
        this.globalContext = globalContext;
    }

    @Override
    public void setTransactionAttributes(@NonNull Properties transactionAttributes) {
        LcnNameMatchTransactionAttributeSource tas = new LcnNameMatchTransactionAttributeSource();
        tas.setProperties(transactionAttributes);
        super.setTransactionAttributeSource(tas);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        // do non associate logic if not JtaTransactionManager
        TransactionAttributeSource tas = getTransactionAttributeSource();
        Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);
        PlatformTransactionManager transactionManager =
                determineTransactionManager((tas != null ? tas.getTransactionAttribute(invocation.getMethod(), targetClass) : null));
        if (!(transactionManager instanceof JtaTransactionManager)) {
            return super.invoke(invocation);
        }

        // do associate logic when JtaTransactionManager
        boolean[] ret = associateTransactionAttributes(invocation);
        try {
            return super.invoke(invocation);
        } finally {
            disassociateTransactionAttributes(ret[0], ret[1]);
        }

    }

    private boolean isProxyConnection(String transactionType) {
        return globalContext.isProxyConnection(transactionType);
    }

    private boolean[] associateTransactionAttributes(MethodInvocation invocation) {
        boolean[] ret = new boolean[2];

        // Outer Method Logic
        if (Objects.isNull(DTXLocalContext.cur())) {
            logger.debug("outer method start.");
            com.codingapi.txlcn.tc.aspect.TransactionInfo transactionInfo = new com.codingapi.txlcn.tc.aspect.TransactionInfo();
            transactionInfo.setTargetClazz(invocation.getThis().getClass());
            transactionInfo.setArgumentValues(invocation.getArguments());
            transactionInfo.setTarget(invocation.getThis());
            transactionInfo.setMethodStr(invocation.getMethod().toString());
            transactionInfo.setMethod(invocation.getMethod().getName());

            Invocation outerInvocation = new Invocation(invocation.getMethod(), invocation.getThis(), invocation.getArguments());
            DTXLocalContext.getOrNew()
                    .getInvocations()
                    .add(outerInvocation);
            TransactionAttribute transactionAttribute = NonSpringRuntimeContext.instance().getTransactionAttribute(outerInvocation);
            DTXLocalContext.cur().setTransactionInfo(transactionInfo);
            DTXLocalContext.cur().setTransactionType(transactionAttribute.getTransactionType());
            DTXLocalContext.cur().setUnitId(transactionAttribute.getUnitId());
            if (isProxyConnection(transactionAttribute.getTransactionType())) {
                DTXLocalContext.makeProxyConnection();
            }
            ret[0] = true;
        }

        // OriginalBranch Logic
        if (!TracingContext.tracing().hasGroup()) {
            TracingContext.tracing().beginTransactionGroup();
            globalContext.startTx(true, TracingContext.tracing().groupId());
            DTXLocalContext.cur().setOriginalBranch(true);
            ret[1] = true;
        }

        DTXLocalContext.cur().setGroupId(TracingContext.tracing().groupId());

        return ret;
    }

    private void disassociateTransactionAttributes(boolean isOuter, boolean isOriginalBranch) {

        // OriginalBranch Logic
        if (isOriginalBranch) {
            globalContext.destroyTx();
        }

        // Outer Method Logic
        if (isOuter) {
            logger.debug("outer method stop.");
            DTXLocalContext.makeNeverAppeared();
        }
    }
}
