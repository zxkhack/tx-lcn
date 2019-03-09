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
package com.codingapi.txlcn.tc.core;

import com.codingapi.txlcn.tc.core.context.BranchContext;
import com.codingapi.txlcn.tc.core.jta.DistributedTransactionManager;
import com.codingapi.txlcn.tracing.TracingContext;
import com.codingapi.txlcn.tracing.TracingTrigger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description:
 * Date: 19-2-26 下午3:39
 *
 * @author ujued
 */
@Component
@Slf4j
public class TransactionPropagationTrigger implements TracingTrigger {

    private final BranchContext globalContext;

    private final DistributedTransactionManager transactionManager;


    @Autowired
    public TransactionPropagationTrigger(BranchContext globalContext, DistributedTransactionManager transactionManager) {
        this.globalContext = globalContext;
        this.transactionManager = transactionManager;
    }

    @Override
    public void onTracingApply() {
        // Start TxContext
        globalContext.startTx(false, TracingContext.tracing().groupId());

        // Start Propagation DTX
        transactionManager.associateTransaction();
    }

    @Override
    public void onTracingApplyBack() {
        // Stop Propagation DTX
        transactionManager.disassociateTransaction();

        // Stop TxContext
        synchronized (globalContext.txContext().getLock()) {
            globalContext.txContext().getLock().notifyAll();
        }
        globalContext.destroyTx();

        TracingContext.tracing().destroy();
    }
}
