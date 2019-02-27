package com.codingapi.txlcn.tc.jta;

import com.codingapi.txlcn.tc.core.DTXLocalContext;
import com.codingapi.txlcn.tc.support.DTXUserControls;
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

    private final DistributedTransactionManager transactionManager;

    private boolean isOriginalBranch() {
        return DTXLocalContext.cur().isOriginalBranch();
    }

    @Autowired
    public BranchTransaction(DistributedTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void begin() throws SystemException {
        log.info("branch transaction begin.");
        if (isOriginalBranch()) {
            transactionManager.begin();
        }
        DTXLocalContext.cur().setSysTransactionState(Status.STATUS_ACTIVE);
    }

    public void commit() throws SecurityException, IllegalStateException, HeuristicRollbackException, RollbackException, HeuristicMixedException, SystemException {
        DTXLocalContext.cur().setSysTransactionState(Status.STATUS_COMMITTING);
        log.info("commit branch transaction.");
        // todo first phase commit
        if (isOriginalBranch()) {
            transactionManager.commit();
        }
    }


    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        DTXLocalContext.cur().setSysTransactionState(Status.STATUS_ROLLING_BACK);
        // todo local first phase rollback
        log.info("rollback branch transaction.");
        if (isOriginalBranch()) {
            transactionManager.rollback();
        }
    }

    public void setRollbackOnly() throws IllegalStateException {
        log.info("branch setRollbackOnly");
        DTXUserControls.rollbackCurrentGroup();
    }

    public int getStatus() throws SystemException {
        int status = DTXLocalContext.cur().getSysTransactionState();
        if (status == Status.STATUS_UNKNOWN) {
            status = transactionManager.getStatus();
            if (status == Status.STATUS_UNKNOWN) {
                status = Status.STATUS_NO_TRANSACTION;
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
