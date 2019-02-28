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
package com.codingapi.txlcn.tc.core;

import com.codingapi.txlcn.common.exception.TransactionException;
import com.codingapi.txlcn.tc.aspect.TransactionInfo;
import com.codingapi.txlcn.tc.core.template.TransactionCleanupTemplate;
import com.codingapi.txlcn.tc.core.template.TransactionControlTemplate;
import com.codingapi.txlcn.tc.support.DTXUserControls;
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

    private final TransactionCleanupTemplate transactionCleanupTemplate;

    private final ThreadLocal<Transaction> transactionLocal = new NamedThreadLocal<>("Transaction Map");

    @Autowired
    public DistributedTransactionManager(TransactionControlTemplate transactionControlTemplate,
                                         TransactionCleanupTemplate transactionCleanupTemplate) {
        this.transactionControlTemplate = transactionControlTemplate;
        this.transactionCleanupTemplate = transactionCleanupTemplate;
    }

    void associateTransaction() {
        transactionLocal.set(new DistributedTransaction(transactionControlTemplate, transactionCleanupTemplate));
    }

    void disassociateTransaction() {
        transactionLocal.remove();
    }

    @Override
    public void begin() throws SystemException {
        try {
            associateTransaction();
            String groupId = DTXLocalContext.cur().getGroupId();
            String unitId = DTXLocalContext.cur().getUnitId();
            TransactionInfo transactionInfo = DTXLocalContext.cur().getTransactionInfo();
            String transactionType = DTXLocalContext.cur().getTransactionType();
            transactionControlTemplate.createGroup(groupId, unitId, transactionInfo, transactionType);
        } catch (TransactionException e) {
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
            return Status.STATUS_UNKNOWN;
        }
        return Status.STATUS_PREPARED;
    }

    @Override
    public Transaction getTransaction() {
        return transactionLocal.get();
    }

    @Override
    public void resume(Transaction tobj) throws IllegalStateException {
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
    public void setRollbackOnly() throws IllegalStateException {
        DTXUserControls.rollbackCurrentGroup();
    }

    @Override
    public void setTransactionTimeout(int seconds) {
        throw new UnsupportedOperationException("unsupported tm setTransactionTimeout.");
    }

    @Override
    public Transaction suspend() {
        throw new UnsupportedOperationException("unsupported tm suspend.");
    }
}
