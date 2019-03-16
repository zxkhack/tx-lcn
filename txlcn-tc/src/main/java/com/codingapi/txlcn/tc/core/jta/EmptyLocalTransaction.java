package com.codingapi.txlcn.tc.core.jta;

import lombok.extern.slf4j.Slf4j;

import javax.transaction.*;
import javax.transaction.xa.XAResource;

/**
 * Description: for local nesting transaction
 * Date: 19-3-9 下午2:40
 *
 * @author ujued
 */
@Slf4j
public class EmptyLocalTransaction implements Transaction {

    EmptyLocalTransaction() {
    }

    @Override
    public void commit() {
    }

    @Override
    public boolean delistResource(XAResource xaRes, int flag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean enlistResource(XAResource xaRes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStatus() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerSynchronization(Synchronization sync) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rollback() throws IllegalStateException {
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }
}
