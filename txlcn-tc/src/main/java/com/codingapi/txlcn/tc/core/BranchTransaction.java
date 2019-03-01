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

import com.codingapi.txlcn.common.exception.TransactionClearException;
import com.codingapi.txlcn.tc.core.context.BranchSession;
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
        return BranchSession.cur().isOriginalBranch();
    }

    private void firstPhaseCleanup(int state) throws SystemException {
        String groupId = BranchSession.cur().getGroupId();
        String unitId = BranchSession.cur().getUnitId();
        String transactionType = BranchSession.cur().getTransactionType();
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
        BranchSession.cur().setSysTransactionState(Status.STATUS_ACTIVE);
    }

    public void commit() throws SecurityException, IllegalStateException, HeuristicRollbackException,
            RollbackException, HeuristicMixedException, SystemException {
        BranchSession.cur().setSysTransactionState(Status.STATUS_COMMITTING);
        firstPhaseCleanup(1);
        if (isOriginalBranch()) {
            transactionManager.commit();
        }
    }


    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        BranchSession.cur().setSysTransactionState(Status.STATUS_ROLLING_BACK);
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
        log.debug("Get status from thread.");
        int status = BranchSession.cur().getSysTransactionState();
        if (status == Status.STATUS_UNKNOWN) {
            log.debug("Get status from TM");
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
