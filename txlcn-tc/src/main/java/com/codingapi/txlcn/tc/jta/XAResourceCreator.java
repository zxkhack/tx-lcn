package com.codingapi.txlcn.tc.jta;

import javax.transaction.xa.XAResource;
import java.sql.Connection;

/**
 * Description:
 * Date: 19-2-25 上午10:43
 *
 * @author ujued
 */
public interface XAResourceCreator {

    XAResource create(Connection connection);
}
