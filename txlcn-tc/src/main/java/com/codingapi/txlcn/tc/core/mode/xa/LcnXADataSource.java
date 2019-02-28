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
package com.codingapi.txlcn.tc.core.mode.xa;

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
