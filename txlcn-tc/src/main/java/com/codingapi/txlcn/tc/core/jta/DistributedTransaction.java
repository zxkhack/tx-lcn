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

import com.codingapi.txlcn.common.exception.TransactionClearException;
import com.codingapi.txlcn.common.exception.TransactionException;
import com.codingapi.txlcn.tc.aspect.info.AspectInfo;
import com.codingapi.txlcn.tc.core.context.BranchSession;
import com.codingapi.txlcn.tc.core.template.TransactionCleanupTemplate;
import com.codingapi.txlcn.tc.core.template.TransactionControlTemplate;
import com.codingapi.txlcn.tc.support.DTXUserControls;
import lombok.extern.slf4j.Slf4j;

import javax.transaction.*;
import javax.transaction.xa.XAResource;

/**
 * Description:
 * Date: 19-2-26 下午4:25
 *
 * @author ujued
 */
@Slf4j
public class DistributedTransaction implements Transaction {

    private final TransactionControlTemplate transactionControlTemplate;

    private final TransactionCleanupTemplate transactionCleanupTemplate;

    private BranchSession branchSession;

    DistributedTransaction(TransactionControlTemplate transactionControlTemplate,
                           TransactionCleanupTemplate transactionCleanupTemplate) {
        this.transactionControlTemplate = transactionControlTemplate;
        this.transactionCleanupTemplate = transactionCleanupTemplate;
    }

    public void setBranchSession(BranchSession branchSession) {
        this.branchSession = branchSession;
    }

    public BranchSession getBranchSession() {
        return this.branchSession;
    }

    private void allBranchesCleanup(int s) throws SystemException {
        String groupId = BranchSession.cur().getGroupId();
        String unitId = BranchSession.cur().getUnitId();
        String transactionType = BranchSession.cur().getTransactionType();
        int state = transactionControlTemplate.notifyGroup(groupId, unitId, transactionType, s);
        try {
            transactionCleanupTemplate.secondPhase(groupId, unitId, transactionType, state);
        } catch (TransactionClearException e) {
            throw new SystemException(e.getMessage());
        }
    }

    @Override
    public void commit() throws SecurityException, IllegalStateException, SystemException {
        log.debug("original branch commit transaction group.");
        BranchSession.cur().setSysTransactionState(Status.STATUS_PREPARED);
        allBranchesCleanup(1);
        BranchSession.cur().setSysTransactionState(Status.STATUS_COMMITTED);
    }

    @Override
    public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException {
        throw new UnsupportedOperationException("unsupported delistResource");
    }

    @Override
    public boolean enlistResource(XAResource xaRes) throws IllegalStateException {
        throw new UnsupportedOperationException("unsupported enlistResource");
    }

    @Override
    public int getStatus() {
        return Status.STATUS_PREPARED;
    }

    @Override
    public void registerSynchronization(Synchronization sync) throws IllegalStateException, SystemException {
        if (BranchTransaction.isOriginalBranch()) {
            return;
        }
        try {
            BranchSession.cur().setSysTransactionState(Status.STATUS_COMMITTING);
            String groupId = BranchSession.cur().getGroupId();
            String unitId = BranchSession.cur().getUnitId();
            String transactionType = BranchSession.cur().getTransactionType();
            AspectInfo aspectInfo = BranchSession.cur().getAspectInfo();
            transactionControlTemplate.joinGroup(groupId, unitId, transactionType, aspectInfo);
        } catch (TransactionException e) {
            throw new SystemException(e.getMessage());
        }
    }

    @Override
    public void rollback() throws IllegalStateException, SystemException {
        log.debug("original branch rollback transaction group.");
        BranchSession.cur().setSysTransactionState(Status.STATUS_ROLLING_BACK);
        allBranchesCleanup(0);
        BranchSession.cur().setSysTransactionState(Status.STATUS_ROLLEDBACK);
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException {
        DTXUserControls.rollbackCurrentGroup();
    }

}
