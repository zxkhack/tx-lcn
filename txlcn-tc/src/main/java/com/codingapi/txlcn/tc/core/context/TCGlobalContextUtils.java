package com.codingapi.txlcn.tc.core.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Description:
 * Date: 19-2-26 下午3:22
 *
 * @author ujued
 */
@Component
public class TCGlobalContextUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        TCGlobalContextUtils.applicationContext = applicationContext;
    }

    public static TCGlobalContext context() {
        return applicationContext.getBean(TCGlobalContext.class);
    }
}
