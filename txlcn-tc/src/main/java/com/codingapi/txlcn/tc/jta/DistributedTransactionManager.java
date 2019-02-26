package com.codingapi.txlcn.tc.jta;

import com.codingapi.txlcn.common.exception.TransactionException;
import com.codingapi.txlcn.tc.core.DTXLocalContext;
import com.codingapi.txlcn.tc.core.context.TCGlobalContext;
import com.codingapi.txlcn.tc.core.template.TransactionControlTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;

import javax.transaction.*;

/**
 * Description:
 * Date: 19-2-26 下午3:58
 *
 * @author ujued
 */
@Component
@Slf4j
public class DistributedTransactionManager implements TransactionManager {

    private final TransactionControlTemplate transactionControlTemplate;

    private final TCGlobalContext globalContext;

    private final ThreadLocal<Transaction> transactionThreadLocal = new NamedThreadLocal<>("Transaction Map");

    @Autowired
    public DistributedTransactionManager(TransactionControlTemplate transactionControlTemplate, TCGlobalContext globalContext) {
        this.transactionControlTemplate = transactionControlTemplate;
        this.globalContext = globalContext;
    }

    public void associateTransaction() {
        transactionThreadLocal.set(new DistributedTransaction(transactionControlTemplate, globalContext));
    }

    public void disassociateTransaction() {
        transactionThreadLocal.remove();
    }

    @Override
    public void begin() throws SystemException {
        try {
            associateTransaction();
            transactionControlTemplate.createGroup(DTXLocalContext.cur().getGroupId(),
                    DTXLocalContext.cur().getUnitId(), null, DTXLocalContext.cur().getTransactionType());
        } catch (TransactionException e) {
            throw new SystemException(e.getMessage());
        }
    }

    @Override
    public void commit() throws SecurityException, IllegalStateException, HeuristicRollbackException, RollbackException, HeuristicMixedException, SystemException {
        try {
            getTransaction().commit();
        } finally {
            disassociateTransaction();
        }

    }

    @Override
    public int getStatus() throws SystemException {
        if (getTransaction() == null) {
            return Status.STATUS_UNKNOWN;
        }
        return getTransaction().getStatus();
    }

    @Override
    public Transaction getTransaction() {
        return transactionThreadLocal.get();
    }

    @Override
    public void resume(Transaction tobj) throws InvalidTransactionException, IllegalStateException, SystemException {
        throw new UnsupportedOperationException("unsupported tm resume.");
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        try {
            getTransaction().rollback();
        } finally {
            disassociateTransaction();
        }
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        throw new UnsupportedOperationException("unsupported tm setRollbackOnly.");
    }

    @Override
    public void setTransactionTimeout(int seconds) throws SystemException {
        throw new UnsupportedOperationException("unsupported tm setTransactionTimeout.");
    }

    @Override
    public Transaction suspend() throws SystemException {
        throw new UnsupportedOperationException("unsupported tm suspend.");
    }
}
