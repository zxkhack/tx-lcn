/*
 * Copyright 2017-2019 CodingApi .
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codingapi.txlcn.tc.aspect.info;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Description:
 * Date: 19-2-27 上午11:19
 *
 * @author ujued
 */
@Data
@NoArgsConstructor
public class InvocationInfo {
    private Method method;
    private Object target;
    private Object[] args;
    private Class<?> targetClass;
    private volatile String methodId;

    public String methodId() {
        if (Objects.nonNull(methodId)) {
            return methodId;
        }
        this.methodId = targetClass == null ? "class" : targetClass.getName() + '#' + method.getName() + args.length;
        return methodId;
    }
}
