package com.codingapi.txlcn.tc.annotation;

import java.lang.annotation.*;

/**
 * Description:
 * Date: 19-2-27 下午5:16
 *
 * @author ujued
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TransactionAttributes {

    /**
     * second phase action by type.
     */
    String DO_BY_TYPE = "bt.do";

    /**
     * distributed transaction type.
     *
     * @return type
     * @see com.codingapi.txlcn.common.util.Transactions
     */
    String type();

    /**
     * second phase commit action.
     *
     * @return action
     */
    String commit() default DO_BY_TYPE;

    /**
     * second phase rollback action.
     *
     * @return action
     */
    String rollback() default DO_BY_TYPE;
}
