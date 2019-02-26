package com.codingapi.txlcn.tc.jta;

import com.codingapi.txlcn.tc.core.DTXLocalContext;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.jta.JtaTransactionObject;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 * Description:
 * Date: 19-2-26 下午2:19
 *
 * @author ujued
 */
public class NewJtaTransactionManager extends JtaTransactionManager {

    public NewJtaTransactionManager(UserTransaction userTransaction, TransactionManager transactionManager) {
        super(userTransaction, transactionManager);
    }

    @Override
    protected void doJtaBegin(JtaTransactionObject txObject, TransactionDefinition definition) throws NotSupportedException, SystemException {
        DTXLocalContext.getOrNew().setAttachment(definition);
        super.doJtaBegin(txObject, definition);
    }
}
