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
package com.codingapi.txlcn.tc.core.mode.tcc;

import com.codingapi.txlcn.tc.core.TransactionResourceProxy;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @author 侯存路
 */
@Service(value = "transaction_tcc")
public class TccTransactionResourceProxy implements TransactionResourceProxy {

    @Override
    public Connection proxyConnection(DataSource dataSource) throws Throwable {
        return dataSource.getConnection();
    }
}
