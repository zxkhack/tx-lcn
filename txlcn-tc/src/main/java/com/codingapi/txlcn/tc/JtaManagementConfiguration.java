package com.codingapi.txlcn.tc;

import com.codingapi.txlcn.tc.core.context.TCGlobalContext;
import com.codingapi.txlcn.tc.aspect.LcnAnnotationTransactionAttributeSource;
import com.codingapi.txlcn.tc.aspect.LcnTransactionInterceptor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
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
 * Date: 19-2-25 下午3:54
 *
 * @author ujued
 */
@Configuration
public class JtaManagementConfiguration extends AbstractTransactionManagementConfiguration {

    @Bean(name = TransactionManagementConfigUtils.TRANSACTION_ADVISOR_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public BeanFactoryTransactionAttributeSourceAdvisor transactionAdvisor(TransactionInterceptor transactionInterceptor) {
        BeanFactoryTransactionAttributeSourceAdvisor advisor = new BeanFactoryTransactionAttributeSourceAdvisor();
        advisor.setTransactionAttributeSource(transactionAttributeSource());
        advisor.setAdvice(transactionInterceptor);
        if (this.enableTx != null) {
            advisor.setOrder(this.enableTx.<Integer>getNumber("order"));
        }
        return advisor;
    }

    @Bean
    public JtaTransactionManager jtaTransactionManager(UserTransaction userTransaction, TransactionManager transactionManager,
                                                       TransactionManagerCustomizers transactionManagerCustomizers) {
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager(userTransaction, transactionManager);
        if (Objects.nonNull(transactionManagerCustomizers)) {
            transactionManagerCustomizers.customize(jtaTransactionManager);
        }
        return jtaTransactionManager;
    }

    @Bean
    public TransactionInterceptor transactionInterceptor(JtaTransactionManager transactionManager, TCGlobalContext globalContext) {
        LcnTransactionInterceptor transactionInterceptor = new LcnTransactionInterceptor(globalContext);
        transactionInterceptor.setTransactionManager(transactionManager);
        transactionInterceptor.setTransactionAttributeSource(transactionAttributeSource());
        return transactionInterceptor;
    }

    @Bean
    public TransactionAttributeSource transactionAttributeSource() {
        return new LcnAnnotationTransactionAttributeSource();
    }
}
