package com.kameo.jpasugar.test;

import com.kameo.jpasugar.test.helpers.TaskODB;
import com.kameo.jpasugar.test.helpers.UserODB;
import com.kameo.jpasugar.test.helpers.UserRole;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.util.List;

public class PlainJpaQueries {

    List<UserODB> getAllUsersWithEmail1(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<UserODB> criteriaQuery = cb.createQuery(UserODB.class);
        Root<UserODB> root = criteriaQuery.from(UserODB.class);
        criteriaQuery.select(root);
        criteriaQuery.where(cb.like(root.get("email"),"email1"));
        TypedQuery<UserODB> query = em.createQuery(criteriaQuery);
        List<UserODB> result = query.getResultList();
        return result;
    }

    List<UserODB> getAllUsersWithAddressCityCracow(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<UserODB> criteriaQuery = cb.createQuery(UserODB.class);
        Root<UserODB> root = criteriaQuery.from(UserODB.class);
        criteriaQuery.select(root);
        criteriaQuery.where(cb.like(root.get("address").get("city"),"Cracow"));
        TypedQuery<UserODB> query = em.createQuery(criteriaQuery);
        List<UserODB> result = query.getResultList();
        return result;
    }


    List<UserODB> shouldExecuteSubqueryForOtherEntityWithCompoundClauses(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<UserODB> criteriaQuery = cb.createQuery(UserODB.class);
        Root<UserODB> root = criteriaQuery.from(UserODB.class);
        criteriaQuery.select(root);

        Subquery<TaskODB> subquery = criteriaQuery.subquery(TaskODB.class);
        Root<TaskODB> taskRoot = subquery.from(TaskODB.class);
        subquery.select(taskRoot);
        subquery.where(
                cb.or(
                    cb.like(taskRoot.get("name"), "task1"),
                    cb.and(
                            cb.like(taskRoot.get("name"), "task2"),
                            cb.lessThan(taskRoot.get("createDateTime"), LocalDateTime.now().minusDays(1))
                    )
                )
        );
        criteriaQuery.where(
                root.get("task").in(subquery),
                cb.not(cb.equal(root.get("userRole"), UserRole.ADMIN))
        );
        TypedQuery<UserODB> query = em.createQuery(criteriaQuery);
        List<UserODB> result = query.getResultList();
        return result;
    }


}
