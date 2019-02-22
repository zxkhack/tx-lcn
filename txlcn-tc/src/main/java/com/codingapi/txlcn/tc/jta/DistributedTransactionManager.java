package com.codingapi.txlcn.tc.jta;

import com.codingapi.txlcn.common.exception.TransactionException;
import com.codingapi.txlcn.tc.core.DTXLocalContext;
import com.codingapi.txlcn.tc.core.context.TCGlobalContext;
import com.codingapi.txlcn.tc.core.template.TransactionControlTemplate;
import com.codingapi.txlcn.tracing.TracingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.*;

/**
 * Description:
 * Date: 19-2-22 上午11:05
 *
 * @author ujued
 */
@Component
public class DistributedTransactionManager implements TransactionManager {

    private final TCGlobalContext globalContext;

    private final TransactionControlTemplate transactionControlTemplate;

    @Autowired
    public DistributedTransactionManager(TCGlobalContext globalContext, TransactionControlTemplate transactionControlTemplate) {
        this.globalContext = globalContext;
        this.transactionControlTemplate = transactionControlTemplate;
    }

    public void begin() throws NotSupportedException, SystemException {
        // original branch transaction send tx message to TM
        try {
            transactionControlTemplate.createGroup(TracingContext.tracing().groupId(), DTXLocalContext.cur().getUnitId(), null, "");
        } catch (TransactionException e) {
            throw new SystemException(e.getMessage());
        }
        System.out.println("TM begin");
    }

    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        // original branch transaction send tx message to TM
        transactionControlTemplate.notifyGroup(TracingContext.tracing().groupId(), DTXLocalContext.cur().getUnitId(), "", 1);
        System.out.println("TM commit");
    }

    public int getStatus() throws SystemException {
        // DTX status
        System.out.println("TM getStatus");
        return DTXLocalContext.transactionState(globalContext.dtxState(TracingContext.tracing().groupId()));
    }

    public Transaction getTransaction() throws SystemException {
        // todo get DistributedTransaction
        System.out.println("TM getTransaction");
        return null;
    }

    public void resume(Transaction transaction) throws InvalidTransactionException, IllegalStateException, SystemException {
        System.out.println("TM resume");
        // original branch transaction send tx message to TM, resume DistributedTransaction
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        System.out.println("TM rollback");
        // original branch transaction send tx message to TM, rollback DistributedTransaction
        transactionControlTemplate.notifyGroup(TracingContext.tracing().groupId(), DTXLocalContext.cur().getUnitId(), "", 0);
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        System.out.println("TM setRollbackOnly");
        // original branch transaction send tx message to TM , dtx rollback only
    }

    public void setTransactionTimeout(int i) throws SystemException {
        System.out.println("TM setTransactionTimeout");

        // original branch transaction send tx message to TM, set dtx timeout
    }

    public Transaction suspend() throws SystemException {
        System.out.println("TM suspend");
        //
        return null;
    }

    public void associateTransaction() throws SystemException, NotSupportedException {
        if (!TracingContext.tracing().hasGroup()) {
            globalContext.startTx();
            this.begin();
            return;
        }
        if (!globalContext.hasTxContext()) {
            globalContext.startTx();
        }
    }
}
