package com.codingapi.txlcn.tc.core.jta;

import com.codingapi.txlcn.tc.core.context.BranchSession;
import com.codingapi.txlcn.tc.core.mode.local.LocalConnectionProxy;
import lombok.extern.slf4j.Slf4j;

import javax.transaction.*;
import javax.transaction.xa.XAResource;
import java.sql.SQLException;

/**
 * Description:
 * Date: 19-3-9 下午2:40
 *
 * @author ujued
 */
@Slf4j
public class LocalTransaction implements Transaction {

    private boolean rollbackOnly;

    public LocalTransaction() {
        BranchSession.getOrOpen().setProxyConnection(true);
        BranchSession.cur().setTransactionType("local");
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        try {
            if (rollbackOnly) {
                rollback();
                return;
            }
            log.debug("commit local transaction");
            ((LocalConnectionProxy) BranchSession.cur().getResource()).realCommit();
        } catch (SQLException e) {
            throw new SystemException(e.getMessage());
        }
    }

    @Override
    public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException, SystemException {
        throw new UnsupportedOperationException("unsupported delistResource");
    }

    @Override
    public boolean enlistResource(XAResource xaRes) throws RollbackException, IllegalStateException, SystemException {
        throw new UnsupportedOperationException("unsupported enlistResource");
    }

    @Override
    public int getStatus() throws SystemException {
        log.debug("get local transaction status. default prepared");
        return Status.STATUS_PREPARED;
    }

    @Override
    public void registerSynchronization(Synchronization sync) throws RollbackException, IllegalStateException, SystemException {
        throw new UnsupportedOperationException("unsupported registerSynchronization");
    }

    @Override
    public void rollback() throws IllegalStateException, SystemException {
        log.debug("rollback local transaction");
        try {
            ((LocalConnectionProxy) BranchSession.cur().getResource()).realRollback();
        } catch (SQLException e) {
            throw new SystemException(e.getMessage());
        }
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        log.debug("rollback local transaction only");
        this.rollbackOnly = true;
    }
}
