package com.codingapi.txlcn.tc.jta;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * Description:
 * Date: 19-2-27 上午11:19
 *
 * @author ujued
 */
@Data
@AllArgsConstructor
public class Invocation {
    private Method method;
    private Object target;
    private Object[] args;
}
