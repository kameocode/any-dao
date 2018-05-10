package com.kameocode.anydao.test.helpers

import org.junit.After
import org.junit.Before
import javax.persistence.EntityManager
import javax.persistence.Persistence

open class BaseTest {
    private val em: EntityManager = Persistence.createEntityManagerFactory("test-pu").createEntityManager()
    protected val anyDao = com.kameocode.anydao.AnyDAO(em)

    @Before
    fun before() {
        em.transaction.begin()
    }

    @After
    fun after() {
        em.transaction.rollback()
    }
}