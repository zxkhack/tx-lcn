package com.codingapi.txlcn.tc.annotation;

import java.lang.annotation.*;

/**
 * Description:
 * Describe transaction attributes.
 * Usually used in combination with {@link org.springframework.transaction.annotation.Transactional}
 * Date: 19-2-27 下午5:16
 *
 * @author ujued
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TransactionAttribute {

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
     * as: beanName#methodName(non arg).
     * only can used in transaction type {@code tcc}
     *
     * @return action
     */
    String commit() default DO_BY_TYPE;

    /**
     * second phase rollback action.
     * as: beanName#methodName(non arg).
     * only can used in transaction type {@code tcc}
     *
     * @return action
     */
    String rollback() default DO_BY_TYPE;
}
