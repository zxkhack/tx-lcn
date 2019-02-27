package com.codingapi.txlcn.tc.jta;

import com.codingapi.txlcn.common.util.Maps;
import com.codingapi.txlcn.tc.core.context.NonSpringRuntimeContext;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeEditor;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Description:
 * Date: 19-2-27 上午9:44
 *
 * @author ujued
 */
public class LcnNameMatchTransactionAttributeSource extends NameMatchTransactionAttributeSource {

    @Override
    public void setProperties(Properties transactionAttributes) {
        TransactionAttributeEditor tae = new TransactionAttributeEditor();
        Enumeration<?> propNames = transactionAttributes.propertyNames();
        while (propNames.hasMoreElements()) {
            String methodName = (String) propNames.nextElement();
            String value = transactionAttributes.getProperty(methodName);
            tae.setAsText(handleLcnTransactionAttributes(methodName, value));
            TransactionAttribute attr = (TransactionAttribute) tae.getValue();
            addTransactionalMethod(methodName, attr);
        }
    }

    private String handleLcnTransactionAttributes(String methodName, String value) {
        String[] tokens = StringUtils.commaDelimitedListToStringArray(value);
        return Arrays.stream(tokens).filter(token -> {
            if (token.startsWith("DTX_")) {
                token = token.substring(4);
                if (token.startsWith("TYPE_")) {
                    // todo validate type
                    NonSpringRuntimeContext.instance().cacheTransactionAttributes(methodName,
                            Maps.newHashMap(NonSpringRuntimeContext.TRANSACTION_TYPE, token.substring(5)));
                }
                return false;
            }
            return true;
        }).collect(Collectors.joining(","));
    }
}
