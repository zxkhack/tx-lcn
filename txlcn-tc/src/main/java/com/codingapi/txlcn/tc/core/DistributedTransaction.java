package com.codingapi.txlcn.tc.core;

import com.codingapi.txlcn.common.exception.TransactionClearException;
import com.codingapi.txlcn.common.exception.TransactionException;
import com.codingapi.txlcn.tc.aspect.TransactionInfo;
import com.codingapi.txlcn.tc.core.template.TransactionCleanupTemplate;
import com.codingapi.txlcn.tc.core.template.TransactionControlTemplate;
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

    private final TransactionCleanupTemplate transactionCleanupTemplate;

    public DistributedTransaction(TransactionControlTemplate transactionControlTemplate,
                                  TransactionCleanupTemplate transactionCleanupTemplate) {
        this.transactionControlTemplate = transactionControlTemplate;
        this.transactionCleanupTemplate = transactionCleanupTemplate;
    }

    @Override
    public void commit() throws SecurityException, IllegalStateException, SystemException {
        log.info("original branch transaction send tx message to TM");
        DTXLocalContext.cur().setSysTransactionState(Status.STATUS_PREPARED);
        String groupId = DTXLocalContext.cur().getGroupId();
        String unitId = DTXLocalContext.cur().getUnitId();
        String transactionType = DTXLocalContext.cur().getTransactionType();
        transactionControlTemplate.notifyGroup(groupId, unitId, transactionType, 1);
        try {
            transactionCleanupTemplate.secondPhase(groupId, unitId, transactionType, 1);
        } catch (TransactionClearException e) {
            throw new SystemException(e.getMessage());
        }
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
        return Status.STATUS_PREPARED;
    }

    @Override
    public void registerSynchronization(Synchronization sync) throws RollbackException, IllegalStateException, SystemException {
        try {
            DTXLocalContext.cur().setSysTransactionState(Status.STATUS_COMMITTING);
            String groupId = DTXLocalContext.cur().getGroupId();
            String unitId = DTXLocalContext.cur().getUnitId();
            String transactionType = DTXLocalContext.cur().getTransactionType();
            TransactionInfo transactionInfo = DTXLocalContext.cur().getTransactionInfo();
            transactionControlTemplate.joinGroup(groupId, unitId, transactionType, transactionInfo);
        } catch (TransactionException e) {
            throw new RollbackException(e.getMessage());
        }
    }

    @Override
    public void rollback() throws IllegalStateException, SystemException {
        DTXLocalContext.cur().setSysTransactionState(Status.STATUS_ROLLING_BACK);
        String groupId = DTXLocalContext.cur().getGroupId();
        String unitId = DTXLocalContext.cur().getUnitId();
        String transactionType = DTXLocalContext.cur().getTransactionType();
        transactionControlTemplate.notifyGroup(groupId, unitId, transactionType, 0);
        try {
            transactionCleanupTemplate.secondPhase(groupId, unitId, transactionType, 0);
        } catch (TransactionClearException e) {
            throw new SystemException(e.getMessage());
        }
        DTXLocalContext.cur().setSysTransactionState(Status.STATUS_ROLLEDBACK);
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException {
        throw new UnsupportedOperationException("unsupported Transaction setRollbackOnly");
    }

}
