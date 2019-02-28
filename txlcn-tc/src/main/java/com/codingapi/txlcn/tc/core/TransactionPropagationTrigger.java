package com.codingapi.txlcn.tc.core;

import com.codingapi.txlcn.tc.core.context.TCGlobalContext;
import com.codingapi.txlcn.tracing.TracingContext;
import com.codingapi.txlcn.tracing.TracingTrigger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description:
 * Date: 19-2-26 下午3:39
 *
 * @author ujued
 */
@Component
@Slf4j
public class TransactionPropagationTrigger implements TracingTrigger {

    private final TCGlobalContext globalContext;

    private final DistributedTransactionManager transactionManager;


    @Autowired
    public TransactionPropagationTrigger(TCGlobalContext globalContext, DistributedTransactionManager transactionManager) {
        this.globalContext = globalContext;
        this.transactionManager = transactionManager;
    }

    @Override
    public void onTracingApply() {
        // Start TxContext
        globalContext.startTx(false, TracingContext.tracing().groupId());

        // Start Propagation DTX
        transactionManager.associateTransaction();
    }

    @Override
    public void onTracingApplyBack() {
        // Stop Propagation DTX
        transactionManager.disassociateTransaction();

        // Stop TxContext
        synchronized (globalContext.txContext().getLock()) {
            globalContext.txContext().getLock().notifyAll();
        }
        globalContext.destroyTx();

        TracingContext.tracing().destroy();
    }
}
