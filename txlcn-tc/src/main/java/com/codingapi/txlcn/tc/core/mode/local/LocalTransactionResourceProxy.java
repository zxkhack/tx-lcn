package com.codingapi.txlcn.tc.core.mode.local;

import com.codingapi.txlcn.tc.core.TransactionResourceProxy;
import com.codingapi.txlcn.tc.core.context.BranchSession;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Description:
 * Date: 19-3-9 下午3:03
 *
 * @author ujued
 */
@Service(value = "transaction_local")
public class LocalTransactionResourceProxy implements TransactionResourceProxy {

    @Override
    public Connection proxyConnection(DataSource dataSource) throws Throwable {
        Connection connection = dataSource.getConnection();
        LocalConnectionProxy connectionProxy = new LocalConnectionProxy(connection);
        connectionProxy.setAutoCommit(false);
        BranchSession.cur().setResource(connectionProxy);
        return connectionProxy;
    }
}
