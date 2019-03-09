package com.codingapi.txlcn.tc.core;

import com.codingapi.txlcn.tc.core.context.BranchSession;

/**
 * Description:
 * Date: 19-3-9 下午2:04
 *
 * @author ujued
 */
public abstract class TransactionUtils {

    /**
     * 是否是本地嵌套事务
     *
     * @return
     */
    public static boolean isLocalNestingTransaction() {
        return !BranchSession.isOpen();
    }
}
