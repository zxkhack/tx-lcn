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
package com.codingapi.txlcn.tc.support;

import com.codingapi.txlcn.tc.core.TransactionCleanService;
import com.codingapi.txlcn.tc.core.TransactionResourceProxy;
import com.codingapi.txlcn.tc.txmsg.RpcExecuteService;
import com.codingapi.txlcn.txmsg.LCNCmdType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Description: BeanName 获取工具类
 * Company: CodingApi
 * Date: 2018/12/10
 *
 * @author lorne
 */
@Component
@Slf4j
public class TxLcnBeanHelper {

    /**
     * message bean 名称格式
     * rpc_%s_%s
     * message:前缀 %s:事务类型（lcn,tcc,txc） %s:事务业务(commit,rollback)
     */
    private static final String RPC_BEAN_NAME_FORMAT = "rpc_%s_%s";


    /**
     * transaction bean 名称格式
     * transaction_%s_%s
     * transaction:前缀 %s:事务类型（lcn,tcc,txc)
     */
    private static final String TRANSACTION_BEAN_NAME_FORMAT = "transaction_%s";

    /**
     * Transaction Clean Service
     * %s: transaction type
     */
    private static final String TRANSACTION_CLEAN_SERVICE_NAME_FORMAT = "%sTransactionCleanService";


    private final ApplicationContext spring;

    @Autowired
    public TxLcnBeanHelper(ApplicationContext spring) {
        this.spring = spring;
    }

    public TransactionResourceProxy loadTransactionResourceProxy(String beanName) {
        String name = String.format(TRANSACTION_BEAN_NAME_FORMAT, beanName.toLowerCase());
        return spring.getBean(name, TransactionResourceProxy.class);
    }

    public RpcExecuteService loadRpcExecuteService(LCNCmdType cmdType) {
        String name = String.format(RPC_BEAN_NAME_FORMAT.replaceFirst("_%s", ""), cmdType.getCode());
        log.debug("getRpcBeanName->{}", name);
        return spring.getBean(name, RpcExecuteService.class);
    }

    public TransactionCleanService loadTransactionCleanService(String transactionType) {
        return spring.getBean(String.format(TRANSACTION_CLEAN_SERVICE_NAME_FORMAT, transactionType.toLowerCase()), TransactionCleanService.class);
    }

    @SuppressWarnings("unchecked")
    public <T> T loadBeanByName(String beanName) {
        try {
            return (T) spring.getBean(beanName);
        } catch (BeansException e) {
            return null;
        }
    }
}
