package com.codingapi.txlcn.tc.jta;

import com.codingapi.txlcn.common.exception.TransactionException;
import com.codingapi.txlcn.tc.core.DTXLocalContext;
import com.codingapi.txlcn.tc.core.template.TransactionControlTemplate;
import com.codingapi.txlcn.tc.support.DTXUserControls;
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
public class BranchTransaction implements UserTransaction, Referenceable, Serializable {

    private final DistributedTransactionManager transactionManager;

    private final TransactionControlTemplate transactionControlTemplate;


    @Autowired
    public BranchTransaction(DistributedTransactionManager transactionManager, TransactionControlTemplate transactionControlTemplate) {
        this.transactionManager = transactionManager;
        this.transactionControlTemplate = transactionControlTemplate;
    }

    public void begin() throws NotSupportedException, SystemException {
        // todo prepare local transaction
        transactionManager.associateTransaction();
        System.out.println("user begin");
    }

    private void join() throws SystemException {
        try {
            transactionControlTemplate.joinGroup(DTXLocalContext.cur().getGroupId(), DTXLocalContext.cur().getUnitId(), "", null);
        } catch (TransactionException e) {
            throw new SystemException(e.getMessage());
        }
    }


    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        join();
        // todo local first phase commit
        System.out.println("user begin");
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        System.out.println("user rollback");
        // todo local first phase rollback
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        DTXUserControls.rollbackCurrentGroup();
        System.out.println("user setRollbackOnly");
    }

    public int getStatus() throws SystemException {
        System.out.println("user getStatus");
        return transactionManager.getStatus();
    }

    public void setTransactionTimeout(int i) throws SystemException {
        System.out.println("user setTransactionTimeout");
        // todo local transaction timeout.
    }

    @Override
    public Reference getReference() throws NamingException {
        return null;
    }
}
