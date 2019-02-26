package com.codingapi.txlcn.tc.jta;

import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
public class JtaAutoConfiguration {

    @Bean
    public JtaTransactionManager jtaTransactionManager(UserTransaction userTransaction, TransactionManager transactionManager,
                                                       TransactionManagerCustomizers transactionManagerCustomizers) {
        JtaTransactionManager jtaTransactionManager = new NewJtaTransactionManager(userTransaction, transactionManager);
        if (Objects.nonNull(transactionManagerCustomizers)) {
            transactionManagerCustomizers.customize(jtaTransactionManager);
        }
        return jtaTransactionManager;
    }
}
