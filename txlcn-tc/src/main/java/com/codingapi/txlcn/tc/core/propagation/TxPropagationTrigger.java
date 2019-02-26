package com.codingapi.txlcn.tc.core.propagation;

import com.codingapi.txlcn.tc.core.context.TCGlobalContext;
import com.codingapi.txlcn.tc.jta.BranchTransaction;
import com.codingapi.txlcn.tc.jta.DistributedTransactionManager;
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
public class TxPropagationTrigger implements TracingTrigger {

    private final TCGlobalContext globalContext;

    private final DistributedTransactionManager transactionManager;

    private final BranchTransaction branchTransaction;

    @Autowired
    public TxPropagationTrigger(TCGlobalContext globalContext, DistributedTransactionManager transactionManager, BranchTransaction branchTransaction) {
        this.globalContext = globalContext;
        this.transactionManager = transactionManager;
        this.branchTransaction = branchTransaction;
    }

    @Override
    public void onTracingApply() {
        globalContext.startTx();
        transactionManager.associateTransaction();
        branchTransaction.associateTransaction();
    }

    @Override
    public void onTracingApplyBack() {
        transactionManager.disassociateTransaction();
        branchTransaction.disassociateTransaction();
        synchronized (globalContext.txContext().getLock()) {
            globalContext.txContext().getLock().notifyAll();
        }
        globalContext.destroyTx();
        TracingContext.tracing().destroy();
    }
}
