package com.codingapi.txlcn.tc.core.mode.xa;

import com.codingapi.txlcn.common.exception.TransactionClearException;
import com.codingapi.txlcn.tc.core.TransactionCleanService;
import org.springframework.stereotype.Service;

/**
 * Description:
 * Date: 19-2-28 下午1:18
 *
 * @author ujued
 */
@Service
public class XaTransactionCleanService implements TransactionCleanService {

    @Override
    public void firstPhase(String groupId, int state, String unitId, String unitType) throws TransactionClearException {

    }

    @Override
    public void secondPhase(String groupId, int state, String unitId, String unitType) throws TransactionClearException {

    }
}
