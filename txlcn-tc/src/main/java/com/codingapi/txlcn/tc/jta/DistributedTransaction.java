package com.codingapi.txlcn.tc.jta;

import com.codingapi.txlcn.common.exception.TransactionException;
import com.codingapi.txlcn.tc.core.DTXLocalContext;
import com.codingapi.txlcn.tc.core.context.TCGlobalContext;
import com.codingapi.txlcn.tc.core.template.TransactionControlTemplate;
import com.codingapi.txlcn.tracing.TracingContext;
import lombok.extern.slf4j.Slf4j;

import javax.transaction.*;
import javax.transaction.xa.XAResource;

/**
 * Description:
 * Date: 19-2-26 下午4:25
 *
 * @author ujued
 */
@Slf4j
public class DistributedTransaction implements Transaction {

    private final TransactionControlTemplate transactionControlTemplate;

    private final TCGlobalContext globalContext;

    public DistributedTransaction(TransactionControlTemplate transactionControlTemplate, TCGlobalContext globalContext) {
        this.transactionControlTemplate = transactionControlTemplate;
        this.globalContext = globalContext;
    }

    @Override
    public void commit() throws SecurityException, IllegalStateException {
        DTXLocalContext.cur().setSysTransactionState(Status.STATUS_PREPARED);
        log.info("original branch transaction send tx message to TM");
        String groupId = DTXLocalContext.cur().getGroupId();
        String unitId = DTXLocalContext.cur().getUnitId();
        String transactionType = DTXLocalContext.cur().getTransactionType();
        transactionControlTemplate.notifyGroup(groupId, unitId, transactionType, 1);
        DTXLocalContext.cur().setSysTransactionState(Status.STATUS_COMMITTED);
    }

    @Override
    public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException {
        throw new UnsupportedOperationException("unsupported delistResource");
    }

    @Override
    public boolean enlistResource(XAResource xaRes) throws IllegalStateException {
        throw new UnsupportedOperationException("unsupported enlistResource");
    }

    @Override
    public int getStatus() {
        int ret = Status.STATUS_NO_TRANSACTION;
        if (TracingContext.tracing().hasGroup()) {
            ret = Status.STATUS_PREPARED;
        } else {
            globalContext.startTx();
        }
        log.info("branch getStatus: {}", ret);
        return ret;
    }

    @Override
    public void registerSynchronization(Synchronization sync) throws RollbackException, IllegalStateException, SystemException {
        DTXLocalContext.cur().setSysTransactionState(Status.STATUS_COMMITTING);
        try {
            transactionControlTemplate.joinGroup(DTXLocalContext.cur().getGroupId(), DTXLocalContext.cur().getUnitId(),
                    DTXLocalContext.cur().getTransactionType(), null);
        } catch (TransactionException e) {
            throw new RollbackException(e.getMessage());
        }
    }

    @Override
    public void rollback() throws IllegalStateException {
        transactionControlTemplate.notifyGroup(TracingContext.tracing().groupId(),
                DTXLocalContext.cur().getUnitId(), DTXLocalContext.cur().getTransactionType(), 0);
        DTXLocalContext.cur().setSysTransactionState(Status.STATUS_ROLLEDBACK);
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException {
        throw new UnsupportedOperationException("unsupported Transaction setRollbackOnly");
    }

}
