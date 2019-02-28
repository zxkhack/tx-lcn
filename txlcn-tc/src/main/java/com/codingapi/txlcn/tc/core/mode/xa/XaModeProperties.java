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

import com.codingapi.txlcn.common.util.Transactions;
import com.codingapi.txlcn.tc.core.context.TransactionModeProperties;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Description:
 * Date: 19-2-28 下午1:48
 *
 * @author ujued
 */
@Component
public class XaModeProperties implements TransactionModeProperties {

    @Override
    public Properties provide() {
        Properties properties = new Properties();
        properties.setProperty(Transactions.XA + ".connection.proxy", "false");
        return properties;
    }
}
