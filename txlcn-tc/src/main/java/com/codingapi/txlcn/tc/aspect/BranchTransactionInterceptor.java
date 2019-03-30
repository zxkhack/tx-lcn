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

import com.codingapi.txlcn.tc.aspect.info.AspectInfo;
import com.codingapi.txlcn.tc.aspect.info.InvocationInfo;
import com.codingapi.txlcn.tc.core.context.BranchSession;
import com.codingapi.txlcn.tc.core.context.NonSpringRuntimeContext;
import com.codingapi.txlcn.tc.core.context.BranchContext;
import com.codingapi.txlcn.tc.core.context.TransactionAttribute;
import com.codingapi.txlcn.tracing.TracingContext;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Description: LCN based {@link TransactionInterceptor}
 * Date: 19-2-27 上午9:42
 *
 * @author ujued
 */
public class BranchTransactionInterceptor extends TransactionInterceptor {

    private final BranchContext branchContext;

    public BranchTransactionInterceptor(BranchContext branchContext) {
        this.branchContext = branchContext;
    }

    @Override
    public void setTransactionAttributes(@NonNull Properties transactionAttributes) {
        BranchNameMatchTransactionAttributeSource tas = new BranchNameMatchTransactionAttributeSource();
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
        boolean[] ret = associateTransactionAttributes(invocation, targetClass);
        try {
            return super.invoke(invocation);
        } finally {
            disassociateTransactionAttributes(ret[0], ret[1]);
        }

    }

    private boolean isProxyConnection(String transactionType) {
        return branchContext.isProxyConnection(transactionType);
    }

    private boolean[] associateTransactionAttributes(MethodInvocation invocation, @Nullable Class<?> targetClass) {
        boolean[] ret = new boolean[2];

        // Outer Method Logic
        if (Objects.isNull(BranchSession.cur())) {
            logger.debug("outer method start.");

            // aspect info for log.
            AspectInfo aspectInfo = new AspectInfo();
            aspectInfo.setTargetClazz(invocation.getThis().getClass());
            aspectInfo.setArgumentValues(invocation.getArguments());
            aspectInfo.setTarget(invocation.getThis());
            aspectInfo.setMethodStr(invocation.getMethod().toString());
            aspectInfo.setMethod(invocation.getMethod().getName());

            // distributed transaction attribute.
            InvocationInfo outerInvocation = new InvocationInfo();
            outerInvocation.setMethod(invocation.getMethod());
            outerInvocation.setArgs(invocation.getArguments());
            outerInvocation.setTarget(invocation.getThis());
            outerInvocation.setTargetClass(targetClass);

            TransactionAttribute transactionAttribute = NonSpringRuntimeContext.instance().getTransactionAttribute(outerInvocation);
            BranchSession.getOrOpen().setAspectInfo(aspectInfo);
            BranchSession.cur().setTransactionType(transactionAttribute.getTransactionType());
            BranchSession.cur().setUnitId(transactionAttribute.getUnitId());

            // make proxy javax.sql.Connection flag if should
            if (isProxyConnection(transactionAttribute.getTransactionType())) {
                BranchSession.makeProxyConnection();
            }

            // associate action for tcc's commit or rollback
            String thisBeanName = targetClass == null ? null : StringUtils.uncapitalize(targetClass.getSimpleName());
            String thisMethodName = invocation.getMethod().getName();
            Map<Object, Object> attrs = new HashMap<>(5);
            if (BranchAnnotationTransactionAttributeSource.THIS_BEAN_NAME.equals(transactionAttribute.getCommitBeanName())) {
                attrs.put(NonSpringRuntimeContext.TRANSACTION_COMMIT_BEAN, thisBeanName);
            }
            if (BranchAnnotationTransactionAttributeSource.THIS_BEAN_NAME.equals(transactionAttribute.getRollbackBeanName())) {
                attrs.put(NonSpringRuntimeContext.TRANSACTION_ROLLBACK_BEAN, thisBeanName);
            }
            if (BranchAnnotationTransactionAttributeSource.ASSOCIATE_METHOD_NAME.equals(transactionAttribute.getCommitMethod())) {
                attrs.put(NonSpringRuntimeContext.TRANSACTION_COMMIT_METHOD, "commit" + StringUtils.capitalize(thisMethodName));
            }
            if (BranchAnnotationTransactionAttributeSource.ASSOCIATE_METHOD_NAME.equals(transactionAttribute.getRollbackMethod())) {
                attrs.put(NonSpringRuntimeContext.TRANSACTION_ROLLBACK_METHOD, "rollback" + StringUtils.capitalize(thisMethodName));
            }
            if (!attrs.isEmpty()) {
                branchContext.updateTransactionAttribute(thisMethodName, attrs);
            }

            // set is outer method flag
            ret[0] = true;
        }

        // OriginalBranch Logic
        if (!TracingContext.tracing().hasGroup()) {
            TracingContext.tracing().beginTransactionGroup();
            branchContext.startTx(true, TracingContext.tracing().groupId());
            BranchSession.cur().setOriginalBranch(true);
            ret[1] = true;
        }

        BranchSession.cur().setGroupId(TracingContext.tracing().groupId());

        return ret;
    }

    private void disassociateTransactionAttributes(boolean isOuter, boolean isOriginalBranch) {

        // OriginalBranch Logic
        if (isOriginalBranch) {
            branchContext.destroyTx();
        }

        // Outer Method Logic
        if (isOuter) {
            logger.debug("outer method stop.");
            BranchSession.closeSession();
        }
    }
}
