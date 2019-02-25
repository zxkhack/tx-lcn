package com.codingapi.txlcn.tc.jta;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Description:
 * Date: 19-2-25 上午10:27
 *
 * @author ujued
 */
public class LcnXADataSource implements XADataSource {

    private DataSource dataSource;

    private XAResourceCreator xaResourceCreator;

    public LcnXADataSource(DataSource dataSource, XAResourceCreator xaResourceCreator) {
        this.dataSource = dataSource;
        this.xaResourceCreator = xaResourceCreator;
    }

    @Override
    public XAConnection getXAConnection() throws SQLException {
        Connection connection = dataSource.getConnection().unwrap(Connection.class);
        return new PooledXAConnection(connection, xaResourceCreator.create(connection));

    }

    @Override
    public XAConnection getXAConnection(String user, String password) throws SQLException {
        throw new UnsupportedOperationException("unsupported.");
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        dataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        dataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return dataSource.getParentLogger();
    }
}
