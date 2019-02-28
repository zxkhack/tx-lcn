package com.codingapi.txlcn.tc.core;

import com.codingapi.txlcn.common.exception.TransactionClearException;
import com.codingapi.txlcn.tc.core.template.TransactionCleanupTemplate;
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

    private final TransactionCleanupTemplate transactionCleanupTemplate;

    private boolean isOriginalBranch() {
        return DTXLocalContext.cur().isOriginalBranch();
    }

    private void firstPhaseCleanup(int state) throws SystemException {
        String groupId = DTXLocalContext.cur().getGroupId();
        String unitId = DTXLocalContext.cur().getUnitId();
        String transactionType = DTXLocalContext.cur().getTransactionType();
        try {
            transactionCleanupTemplate.firstPhase(groupId, unitId, transactionType, state);
        } catch (TransactionClearException e) {
            throw new SystemException(e.getMessage());
        }
    }

    @Autowired
    public BranchTransaction(DistributedTransactionManager transactionManager, TransactionCleanupTemplate transactionCleanupTemplate) {
        this.transactionManager = transactionManager;
        this.transactionCleanupTemplate = transactionCleanupTemplate;
    }

    public void begin() throws SystemException {
        log.debug("Branch transaction begin.");
        if (isOriginalBranch()) {
            transactionManager.begin();
        }
        DTXLocalContext.cur().setSysTransactionState(Status.STATUS_ACTIVE);
    }

    public void commit() throws SecurityException, IllegalStateException, HeuristicRollbackException,
            RollbackException, HeuristicMixedException, SystemException {
        DTXLocalContext.cur().setSysTransactionState(Status.STATUS_COMMITTING);
        firstPhaseCleanup(1);
        if (isOriginalBranch()) {
            transactionManager.commit();
        }
    }


    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        DTXLocalContext.cur().setSysTransactionState(Status.STATUS_ROLLING_BACK);
        firstPhaseCleanup(0);
        if (isOriginalBranch()) {
            transactionManager.rollback();
        }
    }

    public void setRollbackOnly() throws IllegalStateException {
        log.debug("Global transaction rollback only.");
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
        log.debug("Branch status: {}", status);
        return status;
    }

    public void setTransactionTimeout(int i) throws SystemException {
        throw new UnsupportedOperationException("unsupported transaction timeout.");
    }

    @Override
    public Reference getReference() throws NamingException {
        throw new UnsupportedOperationException("unsupported JNDI.");
    }
}
