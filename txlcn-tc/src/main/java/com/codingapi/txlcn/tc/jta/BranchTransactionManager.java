package com.codingapi.txlcn.tc.jta;

import com.codingapi.txlcn.common.exception.TransactionException;
import com.codingapi.txlcn.common.util.Transactions;
import com.codingapi.txlcn.tc.core.DTXLocalContext;
import com.codingapi.txlcn.tc.core.context.TCGlobalContext;
import com.codingapi.txlcn.tc.core.template.TransactionCleanTemplate;
import com.codingapi.txlcn.tc.core.template.TransactionControlTemplate;
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
import java.util.Objects;

/**
 * Description:
 * Date: 19-2-22 上午11:02
 *
 * @author ujued
 */
@Component
@Slf4j
public class BranchTransactionManager implements TransactionManager, UserTransaction, Referenceable, Serializable {

    private final TCGlobalContext globalContext;

    private final TransactionControlTemplate transactionControlTemplate;

    private final TransactionCleanTemplate transactionCleanTemplate;

    @Autowired
    public BranchTransactionManager(TCGlobalContext globalContext, TransactionControlTemplate transactionControlTemplate, TransactionCleanTemplate transactionCleanTemplate) {
        this.globalContext = globalContext;
        this.transactionControlTemplate = transactionControlTemplate;
        this.transactionCleanTemplate = transactionCleanTemplate;
    }

    public void begin() throws NotSupportedException, SystemException {
        log.info("branch transaction begin.");
        if (isOriginalBranch()) {
            globalContext.startTx();
            DTXLocalContext.getOrNew().setGroupId(TracingContext.tracing().groupId());
            DTXLocalContext.getOrNew().setUnitId("1213");
            DTXLocalContext.getOrNew().setTransactionType(Transactions.LCN);
            try {
                transactionControlTemplate.createGroup(DTXLocalContext.cur().getGroupId(),
                        DTXLocalContext.cur().getUnitId(), null, DTXLocalContext.cur().getTransactionType());
            } catch (TransactionException e) {
                throw new SystemException(e.getMessage());
            }
        } else if (!globalContext.hasTxContext()) {
            globalContext.startTx();
        }
        DTXLocalContext.getOrNew().setSysTransactionState(Status.STATUS_ACTIVE);
    }

    public void commit() throws SecurityException, IllegalStateException, SystemException {
        try {
            // Original Branch
            if (isOriginalBranch()) {
                DTXLocalContext.cur().setSysTransactionState(Status.STATUS_PREPARED);
                log.info("original branch transaction send tx message to TM");
                String groupId = DTXLocalContext.cur().getGroupId();
                String unitId = DTXLocalContext.cur().getUnitId();
                String transactionType = DTXLocalContext.cur().getTransactionType();
                transactionControlTemplate.notifyGroup(groupId, unitId, transactionType, 1);
                DTXLocalContext.cur().setSysTransactionState(Status.STATUS_COMMITTED);
                return;
            }
            // Non Original Branch

            DTXLocalContext.cur().setSysTransactionState(Status.STATUS_COMMITTING);
            transactionControlTemplate.joinGroup(DTXLocalContext.cur().getGroupId(), DTXLocalContext.cur().getUnitId(),
                    DTXLocalContext.cur().getTransactionType(), null);
            // todo first phase commit
            log.info("commit branch transaction.");
        } catch (TransactionException e) {
            throw new SystemException(e.getMessage());
        } finally {
            disassociateTransaction();
        }
    }

    private void disassociateTransaction() {
        globalContext.destroyTx();
        DTXLocalContext.makeNeverAppeared();
        TracingContext.tracing().destroy();
    }

    private boolean isOriginalBranch() {
        return !TracingContext.tracing().hasGroup() || globalContext.txContext().isDtxStart();
    }


    public void rollback() throws IllegalStateException, SecurityException {
        try {
            DTXLocalContext.cur().setSysTransactionState(Status.STATUS_ROLLING_BACK);
            if (isOriginalBranch()) {
                transactionControlTemplate.notifyGroup(TracingContext.tracing().groupId(),
                        DTXLocalContext.cur().getUnitId(), "", 0);
                DTXLocalContext.cur().setSysTransactionState(Status.STATUS_ROLLEDBACK);
                return;
            }
            // todo local first phase rollback
            log.info("rollback branch transaction.");
        } finally {
            disassociateTransaction();
        }
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        log.info("branch setRollbackOnly");
        DTXUserControls.rollbackCurrentGroup();
    }

    public int getStatus() throws SystemException {
        log.info("branch getStatus");
        if (Objects.isNull(DTXLocalContext.cur())) {
            DTXLocalContext.getOrNew().setSysTransactionState(Status.STATUS_NO_TRANSACTION);
            return Status.STATUS_NO_TRANSACTION;
        }
        return DTXLocalContext.cur().getSysTransactionState();
    }

    @Override
    public Transaction getTransaction() {
        return null;
    }

    @Override
    public void resume(Transaction tobj) throws InvalidTransactionException, IllegalStateException, SystemException {
        throw new UnsupportedOperationException("unsupported resume.");
    }

    public void setTransactionTimeout(int i) throws SystemException {
        throw new UnsupportedOperationException("unsupported setTransactionTimeout.");
    }

    @Override
    public Transaction suspend() throws SystemException {
        throw new UnsupportedOperationException("unsupported suspend.");
    }

    @Override
    public Reference getReference() throws NamingException {
        throw new UnsupportedOperationException("unsupported getReference.");
    }
}
