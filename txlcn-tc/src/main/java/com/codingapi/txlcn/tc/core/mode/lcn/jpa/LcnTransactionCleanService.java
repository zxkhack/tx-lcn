package com.codingapi.txlcn.tc.core.mode.lcn.jpa;

import com.codingapi.txlcn.tc.core.TransactionCleanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Objects;

/**
 * Description:
 * Date: 19-3-16 下午2:12
 *
 * @author ujued
 */
@Component
@ConditionalOnClass({EntityManagerHolder.class, EntityManagerFactory.class})
@Slf4j
public class LcnTransactionCleanService implements TransactionCleanService {

    private final List<EntityManagerFactory> entityManagerFactories;

    @Autowired
    public LcnTransactionCleanService(List<EntityManagerFactory> entityManagerFactories) {
        this.entityManagerFactories = entityManagerFactories;
        if (entityManagerFactories.size() != 1) {
            throw new IllegalStateException("required a single javax.persistence.EntityManagerFactory bean.");
        }
    }

    @Override
    public void secondPhase(String groupId, int state, String unitId, String unitType) {
        Object emHolder = TransactionSynchronizationManager.getResource(entityManagerFactories.get(0));
        if (Objects.isNull(emHolder)) {
            log.warn("non lcn jpa emHolder!");
            return;
        }
        EntityManagerHolder emh = (EntityManagerHolder) emHolder;
        if (state == 1) {
            emh.getEntityManager().getTransaction().commit();
        } else {
            emh.getEntityManager().getTransaction().rollback();
        }
    }
}
