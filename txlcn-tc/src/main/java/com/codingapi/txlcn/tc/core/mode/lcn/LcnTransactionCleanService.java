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
package com.codingapi.txlcn.tc.core.mode.lcn;

import com.codingapi.txlcn.common.exception.BranchContextException;
import com.codingapi.txlcn.common.exception.TransactionClearException;
import com.codingapi.txlcn.tc.core.TransactionCleanService;
import com.codingapi.txlcn.tc.core.context.BranchContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Description:
 * Date: 2018/12/13
 *
 * @author ujued
 */
@Component
@ConditionalOnMissingClass("javax.persistence.EntityManagerFactory")
@Slf4j
public class LcnTransactionCleanService implements TransactionCleanService {

    private final BranchContext globalContext;

    @Autowired
    public LcnTransactionCleanService(BranchContext globalContext) {
        this.globalContext = globalContext;
    }

    @Override
    public void secondPhase(String groupId, int state, String unitId, String unitType) throws TransactionClearException {
        try {
            Collection<Object> connections = globalContext.lcnConnections(groupId);
            for (Object conn : connections) {
                if (state == 1) {
                    log.debug("real commit connection: {}", conn);
                    ((LcnConnectionProxy) conn).realCommit();
                    continue;
                }
                log.debug("real rollback connection: {}", conn);
                ((LcnConnectionProxy) conn).realRollback();
            }
        } catch (BranchContextException | SQLException e) {
            // ignore BranchContextException, todo notify exception
        }
    }
}
