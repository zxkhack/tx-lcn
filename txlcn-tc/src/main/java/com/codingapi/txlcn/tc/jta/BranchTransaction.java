package com.codingapi.txlcn.tc.jta;

import com.codingapi.txlcn.common.util.Transactions;
import com.codingapi.txlcn.tc.core.DTXLocalContext;
import com.codingapi.txlcn.tc.core.context.TCGlobalContext;
import com.codingapi.txlcn.tc.support.DTXUserControls;
import com.codingapi.txlcn.tracing.TracingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.transaction.*;
import java.io.Serializable;

/**
 * Description:
 * Date: 19-2-22 上午11:02
 *
 * @author ujued
 */
@Component
@Slf4j
public class BranchTransaction implements UserTransaction, Referenceable, Serializable {

    private final TCGlobalContext globalContext;

    private final DistributedTransactionManager transactionManager;


    @Autowired
    public BranchTransaction(TCGlobalContext globalContext, DistributedTransactionManager transactionManager) {
        this.globalContext = globalContext;
        this.transactionManager = transactionManager;
    }

    public void associateTransaction() {
        DTXLocalContext.getOrNew().setGroupId(TracingContext.tracing().groupId());
        DTXLocalContext.cur().setTransactionType(Transactions.LCN);
        DTXLocalContext.cur().setUnitId(Transactions.getApplicationId());
        DTXLocalContext.makeProxy();
    }

    private void associateTransactionDefinition() {
        associateTransaction();
    }

    public void disassociateTransaction() {
        DTXLocalContext.makeNeverAppeared();
    }

    public void begin() throws SystemException, NotSupportedException {
        log.info("branch transaction begin.");
        if (isOriginalBranch()) {
            associateTransactionDefinition();
            transactionManager.begin();
        }
        DTXLocalContext.getOrNew().setSysTransactionState(Status.STATUS_ACTIVE);
    }

    public void commit() throws SecurityException, IllegalStateException, HeuristicRollbackException, RollbackException, HeuristicMixedException, SystemException {
        try {
            // Original Branch
            if (isOriginalBranch()) {
                transactionManager.commit();
                return;
            }
            // todo first phase commit
            log.info("commit branch transaction.");
        } finally {
            disassociateTransaction();
        }
    }


    private boolean isOriginalBranch() {
        return !TracingContext.tracing().hasGroup() || globalContext.txContext().isDtxStart();
    }


    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        try {
            DTXLocalContext.cur().setSysTransactionState(Status.STATUS_ROLLING_BACK);
            if (isOriginalBranch()) {
                transactionManager.rollback();
                return;
            }
            // todo local first phase rollback
            log.info("rollback branch transaction.");
        } finally {
            disassociateTransaction();
        }
    }

    public void setRollbackOnly() throws IllegalStateException {
        log.info("branch setRollbackOnly");
        DTXUserControls.rollbackCurrentGroup();
    }

    public int getStatus() throws SystemException {
        int status = transactionManager.getStatus();
        if (status == Status.STATUS_UNKNOWN) {
            status = Status.STATUS_NO_TRANSACTION;
            if (TracingContext.tracing().hasGroup()) {
                status = Status.STATUS_PREPARED;
            } else {
                globalContext.startTx();
            }
        }
        log.info("branch getStatus: {}", status);
        return status;
    }

    public void setTransactionTimeout(int i) throws SystemException {
        throw new UnsupportedOperationException("unsupported setTransactionTimeout.");
    }

    @Override
    public Reference getReference() throws NamingException {
        throw new UnsupportedOperationException("unsupported getReference.");
    }
}
