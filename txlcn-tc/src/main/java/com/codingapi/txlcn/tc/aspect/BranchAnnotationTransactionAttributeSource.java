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
package com.codingapi.txlcn.tc.aspect;

import com.codingapi.txlcn.common.util.Transactions;
import com.codingapi.txlcn.tc.annotation.TransactionAttribute;
import com.codingapi.txlcn.tc.core.context.NonSpringRuntimeContext;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Description: LCN based {@link Transactional} annotation type transaction metadata.
 * Date: 19-2-27 下午4:55
 *
 * @author ujued
 */
public class BranchAnnotationTransactionAttributeSource extends AnnotationTransactionAttributeSource {

    static final String THIS_BEAN_NAME = "this";

    static final String ASSOCIATE_METHOD_NAME = "associate_method";

    @Override
    protected org.springframework.transaction.interceptor.TransactionAttribute determineTransactionAttribute(
            @NonNull AnnotatedElement element) {
        org.springframework.transaction.interceptor.TransactionAttribute attribute = super.determineTransactionAttribute(element);
        if (Objects.nonNull(attribute)) {

            // TransactionAttribute annotation
            List<Annotation> attributesList = Stream.of(element.getAnnotations()).filter(annotation -> annotation instanceof TransactionAttribute).collect(Collectors.toList());
            TransactionAttribute transactionAttribute = attributesList.isEmpty() ? null : (TransactionAttribute) attributesList.get(0);

            String mappedMethodName = "*";
            if (element instanceof Method) {
                mappedMethodName = ((Method) element).getName();
            }

            String transactionType = transactionAttribute == null ? Transactions.LCN : transactionAttribute.type();

            // nullable
            String commitBeanName = null;
            String cancelBeanName = null;
            String commitMethod = null;
            String cancelMethod = null;


            // check transaction type
            Assert.isTrue(Transactions.VALID_TRANSACTION_TYPES.contains(transactionType),
                    "non exists transaction type: " + transactionType);

            // TCC Type must has commit and rollback action
            if (Transactions.TCC.equals(transactionType)) {
                commitBeanName = cancelBeanName = THIS_BEAN_NAME;
                commitMethod = cancelMethod = ASSOCIATE_METHOD_NAME;
            }

            // resolve commit or rollback execute context
            if (transactionAttribute != null) {
                if (!transactionAttribute.commit().equals(TransactionAttribute.DO_BY_TYPE)) {
                    String[] beanAndMethod = transactionAttribute.commit().split("#");
                    Assert.isTrue(beanAndMethod.length == 2,
                            "@TransactionAttribute. commit action format error. usage: beanName#methodName");
                    commitBeanName = beanAndMethod[0];
                    commitMethod = beanAndMethod[1];
                }
                if (!transactionAttribute.rollback().equals(TransactionAttribute.DO_BY_TYPE)) {
                    String[] beanAndMethod = transactionAttribute.rollback().split("#");
                    Assert.isTrue(beanAndMethod.length == 2,
                            "@TransactionAttribute. rollback action format error. usage: beanName#methodName");
                    cancelBeanName = beanAndMethod[0];
                    cancelMethod = beanAndMethod[1];
                }
            }

            Map<Object, Object> attributes = new HashMap<>();
            attributes.put(NonSpringRuntimeContext.TRANSACTION_TYPE, transactionType);
            attributes.put(NonSpringRuntimeContext.TRANSACTION_COMMIT_BEAN, commitBeanName);
            attributes.put(NonSpringRuntimeContext.TRANSACTION_ROLLBACK_BEAN, cancelBeanName);
            attributes.put(NonSpringRuntimeContext.TRANSACTION_COMMIT_METHOD, commitMethod);
            attributes.put(NonSpringRuntimeContext.TRANSACTION_ROLLBACK_METHOD, cancelMethod);
            NonSpringRuntimeContext.instance().cacheTransactionAttribute(mappedMethodName, attributes);
        }
        return attribute;
    }
}
