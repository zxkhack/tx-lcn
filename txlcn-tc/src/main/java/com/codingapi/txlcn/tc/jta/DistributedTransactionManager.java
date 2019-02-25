package com.codingapi.txlcn.tc.jta;

import com.codingapi.txlcn.common.exception.TransactionException;
import com.codingapi.txlcn.tc.core.DTXLocalContext;
import com.codingapi.txlcn.tc.core.context.TCGlobalContext;
import com.codingapi.txlcn.tc.core.template.TransactionControlTemplate;
import com.codingapi.txlcn.tracing.TracingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import javax.transaction.*;

/**
 * Description:
 * Date: 19-2-22 上午11:05
 *
 * @author ujued
 */
@Component
@Slf4j
public class DistributedTransactionManager implements TransactionManager {

    private final TCGlobalContext globalContext;

    private final TransactionControlTemplate transactionControlTemplate;

    @Autowired
    public DistributedTransactionManager(TCGlobalContext globalContext, TransactionControlTemplate transactionControlTemplate) {
        this.globalContext = globalContext;
        this.transactionControlTemplate = transactionControlTemplate;
    }

    public void begin() throws NotSupportedException, SystemException {
        log.info("original branch transaction send tx message to TM");
        try {
            transactionControlTemplate.createGroup(TracingContext.tracing().groupId(),
                    DTXLocalContext.cur().getUnitId(), null, DTXLocalContext.cur().getTransactionType());
        } catch (TransactionException e) {
            throw new SystemException(e.getMessage());
        }
    }

    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        log.info("original branch transaction send tx message to TM");
        transactionControlTemplate.notifyGroup(TracingContext.tracing().groupId(), DTXLocalContext.cur().getUnitId(),
                DTXLocalContext.cur().getTransactionType(), 1);
    }

    public int getStatus() throws SystemException {
        log.info("TransactionManager getStatus.");
        return DTXLocalContext.transactionState(globalContext.dtxState(TracingContext.tracing().groupId()));
    }

    public Transaction getTransaction() throws SystemException {
        throw new UnsupportedOperationException("unsupported getTransaction.");
    }

    public void resume(Transaction transaction) throws InvalidTransactionException, IllegalStateException, SystemException {
        throw new UnsupportedOperationException("unsupported resume.");
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        System.out.println("TM rollback");
        transactionControlTemplate.notifyGroup(TracingContext.tracing().groupId(),
                DTXLocalContext.cur().getUnitId(), "", 0);
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        throw new UnsupportedOperationException("unsupported setRollbackOnly.");
    }

    public void setTransactionTimeout(int i) throws SystemException {
        throw new UnsupportedOperationException("unsupported setTransactionTimeout.");
    }

    public Transaction suspend() throws SystemException {
        throw new UnsupportedOperationException("unsupported suspend.");
    }

    public TransactionStatus transactionStatus() {
        return (TransactionStatus) DTXLocalContext.cur().getAttachment();
    }

    public void associateTransaction(TransactionStatus transactionStatus) throws SystemException, NotSupportedException {
        DTXLocalContext.getOrNew().setAttachment(transactionStatus);
        if (!TracingContext.tracing().hasGroup()) {
            globalContext.startTx();
            this.begin();
            return;
        }
        if (!globalContext.hasTxContext()) {
            globalContext.startTx();
        }
    }

    public boolean isOriginalBranch() {
        return globalContext.txContext().isDtxStart();
    }
}
