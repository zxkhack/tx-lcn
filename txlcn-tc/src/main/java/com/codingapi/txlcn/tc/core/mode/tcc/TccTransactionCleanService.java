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

import com.codingapi.txlcn.common.exception.TransactionClearException;
import com.codingapi.txlcn.common.util.Maps;
import com.codingapi.txlcn.tc.core.context.BranchSession;
import com.codingapi.txlcn.tc.core.TransactionCleanService;
import com.codingapi.txlcn.tc.core.context.BranchContext;
import com.codingapi.txlcn.tc.core.context.TransactionAttribute;
import com.codingapi.txlcn.tc.support.TxLcnBeanHelper;
import com.codingapi.txlcn.tc.txmsg.TMReporter;
import com.codingapi.txlcn.tracing.TracingConstants;
import com.codingapi.txlcn.tracing.TracingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.reflect.Method;

/**
 * Description:
 * Date: 2018/12/13
 *
 * @author 侯存路
 */
@Component
@Slf4j
public class TccTransactionCleanService implements TransactionCleanService {

    private final ApplicationContext applicationContext;

    private final TMReporter tmReporter;

    private final BranchContext globalContext;

    private final TxLcnBeanHelper beanHelper;

    @Autowired
    public TccTransactionCleanService(ApplicationContext applicationContext,
                                      TMReporter tmReporter, BranchContext globalContext, TxLcnBeanHelper beanHelper) {
        this.applicationContext = applicationContext;
        this.tmReporter = tmReporter;
        this.globalContext = globalContext;
        this.beanHelper = beanHelper;
    }

    @Override
    public void secondPhase(String groupId, int state, String unitId, String unitType) throws TransactionClearException {
        Method exeMethod;
        boolean shouldDestroy = !TracingContext.tracing().hasGroup();
        try {
            TransactionAttribute transactionAttribute = globalContext.getTransactionAttribute(unitId);
            if (shouldDestroy) {
                TracingContext.init(Maps.of(TracingConstants.GROUP_ID, groupId, TracingConstants.APP_MAP, "{}"));
            }
            BranchSession.getOrOpen().setGroupId(groupId);
            BranchSession.cur().setUnitId(unitId);

            Object bean = state == 1 ? beanHelper.loadBeanByName(transactionAttribute.getCommitBeanName()) :
                    beanHelper.loadBeanByName(transactionAttribute.getRollbackBeanName());
            Assert.notNull(bean, "second phase action bean must not null.");

            exeMethod = bean.getClass().getMethod(
                    state == 1 ? transactionAttribute.getCommitMethod() : transactionAttribute.getRollbackMethod());
            Assert.notNull(exeMethod, "second phase action method must not null.");
            try {
                exeMethod.invoke(bean);
                log.debug("User confirm/cancel logic over.");
            } catch (Throwable e) {
                log.error("Tcc secondPhase error.", e);
                tmReporter.reportTccCleanException(groupId, unitId, state);
            }
        } catch (Throwable e) {
            throw new TransactionClearException(e.getMessage());
        } finally {
            if (shouldDestroy) {
                TracingContext.tracing().destroy();
            }
        }
    }
}
