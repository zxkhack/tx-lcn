package com.codingapi.txlcn.tc.jta;

import com.codingapi.txlcn.common.exception.TransactionException;
import com.codingapi.txlcn.tc.core.DTXLocalContext;
import com.codingapi.txlcn.tc.core.template.TransactionControlTemplate;
import com.codingapi.txlcn.tc.support.DTXUserControls;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

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

    private final TransactionControlTemplate transactionControlTemplate;

    private PlatformTransactionManager platformTransactionManager;


    @Autowired
    public BranchTransaction(DistributedTransactionManager transactionManager, TransactionControlTemplate transactionControlTemplate, PlatformTransactionManager platformTransactionManager) {
        this.transactionManager = transactionManager;
        this.transactionControlTemplate = transactionControlTemplate;
        this.platformTransactionManager = platformTransactionManager;
    }

    public void begin() throws NotSupportedException, SystemException {
        // todo prepare local transaction
        log.info("branch transaction begin.");
        TransactionStatus status = platformTransactionManager.getTransaction(null);
        transactionManager.associateTransaction(status);
    }

    private void join() throws SystemException {
        try {
            transactionControlTemplate.joinGroup(DTXLocalContext.cur().getGroupId(), DTXLocalContext.cur().getUnitId(), "", null);
        } catch (TransactionException e) {
            throw new SystemException(e.getMessage());
        }
    }

    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        if (transactionManager.isOriginalBranch()) {
            transactionManager.commit();
        } else {
            join();
        }
        // todo local first phase commit
        log.info("commit branch transaction.");
        platformTransactionManager.commit(transactionManager.transactionStatus());
    }


    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        // todo local first phase rollback
        if (transactionManager.isOriginalBranch()) {
            transactionManager.rollback();
        }
        log.info("rollback branch transaction.");
        platformTransactionManager.rollback(transactionManager.transactionStatus());
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        log.info("branch setRollbackOnly");
        DTXUserControls.rollbackCurrentGroup();
    }

    public int getStatus() throws SystemException {
        log.info("branch getStatus");
        return transactionManager.getStatus();
    }

    public void setTransactionTimeout(int i) throws SystemException {
        throw new UnsupportedOperationException("unsupported setTransactionTimeout.");
    }

    @Override
    public Reference getReference() throws NamingException {
        throw new UnsupportedOperationException("unsupported getReference.");
    }
}
