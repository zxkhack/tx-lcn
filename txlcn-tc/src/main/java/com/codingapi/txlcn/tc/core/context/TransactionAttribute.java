package com.codingapi.txlcn.tc.core.context;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 * Date: 19-2-27 上午11:22
 *
 * @author ujued
 */
@NoArgsConstructor
@Data
public class TransactionAttribute {
    private String transactionType;
    private String commitMethod;
    private String commitBeanName;
    private String rollbackMethod;
    private String rollbackBeanName;
    private String unitId;
}
