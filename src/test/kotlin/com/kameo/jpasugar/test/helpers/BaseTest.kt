package com.kameo.jpasugar.test.helpers

import com.kameo.jpasugar.AnyDAONew
import org.junit.After
import org.junit.Before
import javax.persistence.EntityManager
import javax.persistence.Persistence

open class BaseTest {
    private val em: EntityManager = Persistence.createEntityManagerFactory("test-pu").createEntityManager()
    protected val anyDao = AnyDAONew(em)

    @Before
    fun before() {
        em.transaction.begin()
    }

    @After
    fun after() {
        em.transaction.rollback()
    }
}