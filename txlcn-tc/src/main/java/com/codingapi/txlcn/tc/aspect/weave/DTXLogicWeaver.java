package com.codingapi.txlcn.tc.aspect.weave;

import com.codingapi.txlcn.tc.core.context.BranchContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description: 下个版本删除此类
 * Date: 19-3-1 下午5:00
 *
 * @author ujued
 * @see com.codingapi.txlcn.tc.aspect.BranchTransactionInterceptor
 */
@Deprecated
@Component
public class DTXLogicWeaver {

    private final BranchContext branchContext;

    @Autowired
    public DTXLogicWeaver(BranchContext branchContext) {
        this.branchContext = branchContext;
    }

    public BranchContext branchContext() {
        return branchContext;
    }
}
