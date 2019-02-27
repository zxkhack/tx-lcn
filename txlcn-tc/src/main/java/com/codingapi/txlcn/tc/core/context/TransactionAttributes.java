package com.codingapi.txlcn.tc.core.context;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Description:
 * Date: 19-2-27 上午11:22
 *
 * @author ujued
 */
@AllArgsConstructor
@Data
public class TransactionAttributes {
    private String transactionType;
    private String unitId;
}
