package com.codingapi.txlcn.tc.jta;

import javax.sql.ConnectionEventListener;
import javax.sql.StatementEventListener;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Description:
 * Date: 19-2-25 上午10:29
 *
 * @author ujued
 */
public class PooledXAConnection implements XAConnection {

    private XAResource xaResource;

    private Connection connection;

    public PooledXAConnection(Connection connection, XAResource xaResource) {
        this.xaResource = xaResource;
        this.connection = connection;
    }

    @Override
    public XAResource getXAResource() throws SQLException {
        return xaResource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return connection;
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {

    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {

    }

    @Override
    public void addStatementEventListener(StatementEventListener listener) {

    }

    @Override
    public void removeStatementEventListener(StatementEventListener listener) {

    }
}
