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
