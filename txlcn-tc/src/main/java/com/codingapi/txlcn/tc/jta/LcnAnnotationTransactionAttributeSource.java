package com.codingapi.txlcn.tc.jta;

import com.codingapi.txlcn.tc.annotation.TransactionAttributes;
import com.codingapi.txlcn.tc.core.context.NonSpringRuntimeContext;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttribute;
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
 * Description:
 * Date: 19-2-27 下午4:55
 *
 * @author ujued
 */
public class LcnAnnotationTransactionAttributeSource extends AnnotationTransactionAttributeSource {

    @Override
    protected TransactionAttribute determineTransactionAttribute(@NonNull AnnotatedElement element) {
        TransactionAttribute attribute = super.determineTransactionAttribute(element);
        if (Objects.nonNull(attribute)) {
            String mappedMethodName = "*";
            String transactionType;
            String commitBeanName = null;
            String cancelBeanName = null;
            String commitMethod = null;
            String cancelMethod = null;

            if (element instanceof Method) {
                mappedMethodName = ((Method) element).getName();
            }

            List<Annotation> attributesList = Stream.of(element.getAnnotations()).filter(annotation -> annotation instanceof TransactionAttributes).collect(Collectors.toList());
            Assert.notEmpty(attributesList, "associate @TransactionAttributes to @Transactional please.");
            TransactionAttributes transactionAttributes = (TransactionAttributes) attributesList.get(0);
            transactionType = transactionAttributes.type();
            if (!transactionAttributes.commit().equals(TransactionAttributes.DO_BY_TYPE)) {
                String[] beanAndMethod = transactionAttributes.commit().split("#");
                Assert.isTrue(beanAndMethod.length == 2, "commit action format error. beanName#methodName");
                commitBeanName = beanAndMethod[0];
                commitMethod = beanAndMethod[1];
            }
            if (!transactionAttributes.rollback().equals(TransactionAttributes.DO_BY_TYPE)) {
                String[] beanAndMethod = transactionAttributes.rollback().split("#");
                Assert.isTrue(beanAndMethod.length == 2, "rollback action format error. beanName#methodName");
                cancelBeanName = beanAndMethod[0];
                cancelMethod = beanAndMethod[1];
            }
            Map<Object, Object> attributes = new HashMap<>();
            attributes.put(NonSpringRuntimeContext.TRANSACTION_TYPE, transactionType);
            attributes.put(NonSpringRuntimeContext.TRANSACTION_COMMIT_BEAN, commitBeanName);
            attributes.put(NonSpringRuntimeContext.TRANSACTION_ROLLBACK_BEAN, cancelBeanName);
            attributes.put(NonSpringRuntimeContext.TRANSACTION_COMMIT_METHOD, commitMethod);
            attributes.put(NonSpringRuntimeContext.TRANSACTION_ROLLBACK_METHOD, cancelMethod);
            NonSpringRuntimeContext.instance().cacheTransactionAttributes(mappedMethodName, attributes);
        }
        return attribute;
    }
}
