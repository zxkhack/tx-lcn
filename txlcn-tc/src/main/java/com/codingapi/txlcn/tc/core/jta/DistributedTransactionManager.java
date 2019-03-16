/*
 * Copyright 2017-2019 CodingApi .
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codingapi.txlcn.tc.core.jta;

import com.codingapi.txlcn.common.exception.TransactionException;
import com.codingapi.txlcn.tc.aspect.AspectInfo;
import com.codingapi.txlcn.tc.config.TxClientConfig;
import com.codingapi.txlcn.tc.core.TransactionUtils;
import com.codingapi.txlcn.tc.core.context.BranchSession;
import com.codingapi.txlcn.tc.core.template.TransactionCleanupTemplate;
import com.codingapi.txlcn.tc.core.template.TransactionControlTemplate;
import com.codingapi.txlcn.tc.support.DTXUserControls;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;

import javax.transaction.*;

/**
 * Description: 不支持本地事务嵌套
 * Date: 19-2-26 下午3:58
 *
 * @author ujued
 */
@Component
@Slf4j
public class DistributedTransactionManager implements TransactionManager {

    private final TransactionControlTemplate transactionControlTemplate;

    private final TransactionCleanupTemplate transactionCleanupTemplate;

    private final TxClientConfig clientConfig;

    private final ThreadLocal<Transaction> transactionLocal = new NamedThreadLocal<>("Transaction Map");

    @Autowired
    public DistributedTransactionManager(TransactionControlTemplate transactionControlTemplate,
                                         TransactionCleanupTemplate transactionCleanupTemplate, TxClientConfig clientConfig) {
        this.transactionControlTemplate = transactionControlTemplate;
        this.transactionCleanupTemplate = transactionCleanupTemplate;
        this.clientConfig = clientConfig;
    }

    private void associateTransaction(Transaction transaction) {
        this.transactionLocal.set(transaction);
    }

    public DistributedTransaction associateTransaction() {
        DistributedTransaction transaction = new DistributedTransaction(transactionControlTemplate, transactionCleanupTemplate);
        associateTransaction(transaction);
        return transaction;
    }

    public Transaction disassociateTransaction() {
        Transaction transaction = transactionLocal.get();
        transactionLocal.remove();
        return transaction;
    }

    @Override
    public void begin() throws SystemException {
        try {
            if (TransactionUtils.isLocalNestingTransaction()) {
                log.debug("begin a local transaction.");
                associateTransaction(new EmptyLocalTransaction());
                return;
            }
            log.debug("begin a distributed transaction.");
            DistributedTransaction transaction = associateTransaction();
            transaction.setBranchSession(BranchSession.cur());
            String groupId = BranchSession.cur().getGroupId();
            String unitId = BranchSession.cur().getUnitId();
            AspectInfo aspectInfo = BranchSession.cur().getAspectInfo();
            String transactionType = BranchSession.cur().getTransactionType();
            transactionControlTemplate.createGroup(groupId, unitId, aspectInfo, transactionType);
            BranchSession.cur().setSysTransactionState(Status.STATUS_PREPARED);
        } catch (TransactionException e) {
            disassociateTransaction();
            throw new SystemException(e.getMessage());
        }
    }

    @Override
    public void commit() throws SecurityException, IllegalStateException, HeuristicRollbackException,
            RollbackException, HeuristicMixedException, SystemException {
        try {
            getTransaction().commit();
        } finally {
            disassociateTransaction();
        }
    }

    @Override
    public int getStatus() {
        if (getTransaction() == null) {
            return Status.STATUS_NO_TRANSACTION;
        }
        return Status.STATUS_PREPARED;
    }

    @Override
    public Transaction getTransaction() {
        return transactionLocal.get();
    }

    @Override
    public void resume(Transaction tobj) throws IllegalStateException {
        log.debug("resume transaction: {}", tobj);
        associateTransaction(tobj);
        BranchSession.openAs(((DistributedTransaction) tobj).getBranchSession());

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
    public void setRollbackOnly() throws IllegalStateException {
        DTXUserControls.rollbackCurrentGroup();
    }

    @Override
    public void setTransactionTimeout(int seconds) {
        long time = seconds * 1000;
        clientConfig.applyDtxTime(time);
        log.debug("Apply distributed transaction timeout: {}ms.", time);
    }

    @Override
    public Transaction suspend() {
        Transaction transaction = disassociateTransaction();
        log.debug("suspend transaction: {}", transaction);
        if (transaction instanceof DistributedTransaction) {
            ((DistributedTransaction) transaction).setBranchSession(BranchSession.cur());
            BranchSession.closeSession();
        }
        return transaction;
    }
}
