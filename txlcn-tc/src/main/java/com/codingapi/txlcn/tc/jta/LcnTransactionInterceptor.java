package com.codingapi.txlcn.tc.jta;

import com.codingapi.txlcn.tc.core.DTXLocalContext;
import com.codingapi.txlcn.tc.core.context.NonSpringRuntimeContext;
import com.codingapi.txlcn.tc.core.context.TCGlobalContext;
import com.codingapi.txlcn.tc.core.context.TransactionAttributes;
import com.codingapi.txlcn.tracing.TracingContext;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.lang.NonNull;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.util.Objects;
import java.util.Properties;

/**
 * Description:
 * Date: 19-2-27 上午9:42
 *
 * @author ujued
 */
public class LcnTransactionInterceptor extends TransactionInterceptor {

    private final TCGlobalContext globalContext;

    public LcnTransactionInterceptor(TCGlobalContext globalContext) {
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
        boolean[] ret = associateTransactionAttributes(invocation);
        try {
            return super.invoke(invocation);
        } finally {
            disassociateTransactionAttributes(ret[0], ret[1]);
        }

    }

    private boolean[] associateTransactionAttributes(MethodInvocation invocation) {
        boolean[] ret = new boolean[2];
        // 最外层方法时
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
            TransactionAttributes transactionAttributes = NonSpringRuntimeContext.instance().getTransactionAttributes(outerInvocation);
            DTXLocalContext.cur().setTransactionInfo(transactionInfo);
            DTXLocalContext.cur().setTransactionType(transactionAttributes.getTransactionType());
            DTXLocalContext.cur().setUnitId(transactionAttributes.getUnitId());
            DTXLocalContext.makeProxy();
            ret[0] = true;
        }

        // OriginalBranch 开启TxContext
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

        // 发起方关闭TxContext
        if (isOriginalBranch) {
            globalContext.destroyTx();
        }

        // 最外层方法时
        if (isOuter) {
            logger.debug("outer method stop.");
            DTXLocalContext.makeNeverAppeared();
        }
    }
}
