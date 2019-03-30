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
package com.codingapi.txlcn.tc;

import com.codingapi.txlcn.common.runner.TxLcnApplicationRunner;
import com.codingapi.txlcn.common.util.ApplicationInformation;
import com.codingapi.txlcn.common.util.id.ModIdProvider;
import com.codingapi.txlcn.logger.TransactionLoggerConfiguration;
import com.codingapi.txlcn.tc.aspect.BranchAnnotationTransactionAttributeSource;
import com.codingapi.txlcn.tc.aspect.BranchTransactionInterceptor;
import com.codingapi.txlcn.tc.config.EnableDistributedTransaction;
import com.codingapi.txlcn.tc.core.context.BranchContext;
import com.codingapi.txlcn.tracing.TransactionTracingConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.transaction.annotation.AbstractTransactionManagementConfiguration;
import org.springframework.transaction.config.TransactionManagementConfigUtils;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.util.Objects;

/**
 * Description:
 * Date: 1/19/19
 *
 * @author ujued
 * @see EnableDistributedTransaction
 */
@Configuration
@ComponentScan(
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASPECTJ, pattern = "com.codingapi.txlcn.tc.core.mode.txc..*"
        )
)
@Import({
        TransactionLoggerConfiguration.class,
        TransactionTracingConfiguration.class,
        BranchTransactionConfiguration.JtaManagementConfiguration.class
})
public class BranchTransactionConfiguration {

    /**
     * All initialization about TX-LCN
     *
     * @param applicationContext Spring ApplicationContext
     * @return TX-LCN custom runner
     */
    @Bean
    public ApplicationRunner txLcnApplicationRunner(ApplicationContext applicationContext) {
        return new TxLcnApplicationRunner(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public ModIdProvider modIdProvider(ConfigurableEnvironment environment,
                                       @Autowired(required = false) ServerProperties serverProperties) {
        return () -> ApplicationInformation.modId(environment, serverProperties);
    }

    /**
     * Jta Config
     */
    public class JtaManagementConfiguration extends AbstractTransactionManagementConfiguration {

        @Bean(name = TransactionManagementConfigUtils.TRANSACTION_ADVISOR_BEAN_NAME)
        @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
        public BeanFactoryTransactionAttributeSourceAdvisor transactionAdvisor(TransactionInterceptor transactionInterceptor) {
            BeanFactoryTransactionAttributeSourceAdvisor advisor = new BeanFactoryTransactionAttributeSourceAdvisor();
            advisor.setTransactionAttributeSource(transactionAttributeSource());
            advisor.setAdvice(transactionInterceptor);
            if (this.enableTx != null) {
                advisor.setOrder(this.enableTx.getNumber("order"));
            }
            return advisor;
        }

        @Bean
        public JtaTransactionManager transactionManager(UserTransaction userTransaction, TransactionManager transactionManager,
                                                        TransactionManagerCustomizers transactionManagerCustomizers) {
            JtaTransactionManager jtaTransactionManager = new JtaTransactionManager(userTransaction, transactionManager);
            if (Objects.nonNull(transactionManagerCustomizers)) {
                transactionManagerCustomizers.customize(jtaTransactionManager);
            }
            return jtaTransactionManager;
        }

        @Bean
        @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
        public TransactionInterceptor transactionInterceptor(JtaTransactionManager transactionManager, BranchContext branchContext) {
            BranchTransactionInterceptor transactionInterceptor = new BranchTransactionInterceptor(branchContext);
            transactionInterceptor.setTransactionManager(transactionManager);
            transactionInterceptor.setTransactionAttributeSource(transactionAttributeSource());
            return transactionInterceptor;
        }

        @Bean
        @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
        public TransactionAttributeSource transactionAttributeSource() {
            return new BranchAnnotationTransactionAttributeSource();
        }
    }
}
