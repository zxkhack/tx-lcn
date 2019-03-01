package com.codingapi.txlcn.tc.coprelog;

import com.codingapi.txlcn.common.util.id.RandomUtils;
import com.codingapi.txlcn.tc.MiniConfiguration;
import com.codingapi.txlcn.tc.aspect.AspectInfo;
import com.codingapi.txlcn.tc.corelog.aspect.AspectLogger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Description:
 * Date: 19-2-12 上午10:06
 *
 * @author ujued
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MiniConfiguration.class)
public class AspectLogTest {

    @Autowired
    private AspectLogger aspectLogger;

    @Test
    public void testTrace() throws InterruptedException {
        AspectInfo aspectInfo = new AspectInfo();
        aspectInfo.setMethod("method");
        aspectInfo.setMethodStr("method str");
        aspectInfo.setParameterTypes(new Class[]{String.class, Integer.class});
        aspectInfo.setTargetClazz(String.class);
        aspectInfo.setArgumentValues(new Object[]{"1", 2});
        aspectLogger.trace(RandomUtils.randomKey(), RandomUtils.randomKey(), aspectInfo);
        Thread.sleep(1000);
    }
}
