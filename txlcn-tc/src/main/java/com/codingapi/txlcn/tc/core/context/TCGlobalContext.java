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
package com.codingapi.txlcn.tc.core.context;

import com.codingapi.txlcn.common.exception.TCGlobalContextException;
import com.codingapi.txlcn.common.util.function.Supplier;
import com.codingapi.txlcn.tc.core.mode.lcn.LcnConnectionProxy;
import com.codingapi.txlcn.tc.core.mode.txc.analy.def.bean.TableStruct;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

/**
 * Description:
 * Date: 19-1-22 下午6:13
 *
 * @author ujued
 */
public interface TCGlobalContext extends NonSpringRuntimeContext {

    /**
     * set lcn connection
     *
     * @param groupId         groupId
     * @param connectionProxy connectionProxy
     */
    void cacheLcnConnection(String groupId, DataSource dataSource, LcnConnectionProxy connectionProxy);

    /**
     * get lcn proxy
     *
     * @param groupId groupId
     * @return connection proxy
     * @throws TCGlobalContextException TCGlobalContextException
     */
    LcnConnectionProxy getLcnConnection(String groupId, DataSource dataSource) throws TCGlobalContextException;

    /**
     * all connections about lcn.
     *
     * @param groupId groupId
     * @return collection of connections
     * @throws TCGlobalContextException TCGlobalContextException
     */
    Collection<Object> findLcnConnections(String groupId) throws TCGlobalContextException;

    /**
     * txc type lock
     *
     * @param groupId   groupId
     * @param unitId    unitId
     * @param lockIdSet lockIdSet
     */
    void addTxcLockId(String groupId, String unitId, Set<String> lockIdSet);

    /**
     * find txc lock set
     *
     * @param groupId groupId
     * @param unitId  unitId
     * @return set
     * @throws TCGlobalContextException TCGlobalContextException
     */
    Set<String> findTxcLockSet(String groupId, String unitId) throws TCGlobalContextException;

    /**
     * table struct info
     *
     * @param table          table
     * @param structSupplier structSupplier
     * @return table info
     * @throws SQLException SQLException
     */
    TableStruct tableStruct(String table, Supplier<TableStruct, SQLException> structSupplier) throws SQLException;

    /**
     * secondPhase group
     *
     * @param groupId groupId
     */
    void clearGroup(String groupId);

    /**
     * start tx
     *
     * @return tx context info
     */
    TxContext startTx(boolean isOriginalBranch, String groupId);

    /**
     * get tx context info by groupId
     *
     * @param groupId groupId
     * @return tx context info
     */
    TxContext txContext(String groupId);

    /**
     * get context info
     *
     * @return info
     */
    TxContext txContext();

    /**
     * del tx info
     */
    void destroyTx();

    /**
     * del tx info
     *
     * @param groupId groupId
     */
    void destroyTx(String groupId);

    /**
     * has tx context
     *
     * @return bool
     */
    boolean hasTxContext();

    /**
     * is time out
     *
     * @return bool
     */
    boolean isDTXTimeout();

    /**
     * 判断某个事务是否不允许提交
     *
     * @param groupId groupId
     * @return result
     */
    int dtxState(String groupId);

    /**
     * 设置某个事务组不允许提交
     *
     * @param groupId groupId
     */
    void setRollbackOnly(String groupId);

    /**
     * 是否代理java.sql.Connection
     *
     * @param transactionType transactionType
     * @return result
     */
    boolean isProxyConnection(String transactionType);

    /**
     * 缓存属性（如果不存在）
     *
     * @param key   key
     * @param value value
     */
    void cacheIfAbsentProperty(Object key, Object value);
}
