package com.kameo.jpasugar.test

import com.kameo.jpasugar.KClause
import com.kameo.jpasugar.KFromClause
import com.kameo.jpasugar.KQuery
import com.kameo.jpasugar.KRoot
import com.kameo.jpasugar.Page
import com.kameo.jpasugar.test.helpers.AddressODB
import com.kameo.jpasugar.test.helpers.BaseTest
import com.kameo.jpasugar.test.helpers.TaskODB
import com.kameo.jpasugar.test.helpers.UserODB
import com.kameo.jpasugar.wraps.and
import com.kameo.jpasugar.wraps.clause
import com.kameo.jpasugar.wraps.like
import com.kameo.jpasugar.wraps.not
import com.kameo.jpasugar.wraps.or
import org.junit.Assert
import org.junit.Test
import javax.persistence.NonUniqueResultException

class ClausesTest : BaseTest() {

    @Test
    fun `should process nested or clause`() {
        val u4 = UserODB(email = "email4", task = TaskODB(name = "task2"))
        anyDao.persist(
                UserODB(email = "email1", task = TaskODB(name = "task1")),
                UserODB(email = "email2", task = TaskODB(name = "task1")),
                UserODB(email = "email3", task = TaskODB(name = "task1")),
                u4,
                UserODB(email = "email5", task = TaskODB(name = "task3")))

        val res = anyDao.all(UserODB::class) {
            and {
                it[UserODB::task, TaskODB::name] like "task1"
                or {
                    it[UserODB::email] like "email1"
                    it[UserODB::email] like "email2"
                }
            }
        }
        Assert.assertEquals(2, res.size)

        val res2 = anyDao.all(UserODB::class) {
            it[UserODB::email] or {
                it like "email4"
                it like "email5"
            }
            it[UserODB::task, TaskODB::name] or {
                it like "task1"
                it like "task2"
            }
        }
        Assert.assertEquals("Only user with email4 should be matched", u4.id, res2[0].id)
        Assert.assertEquals(1, res2.size)

    }

    @Test
    fun `should process properly clauses for later use`() {
        anyDao.persist(
                UserODB(email = "email1", task = TaskODB(name = "task1")),
                UserODB(email = "email2", task = TaskODB(name = "task1")),
                UserODB(email = "email3", task = TaskODB(name = "task1")),
                UserODB(email = "email4", task = TaskODB(name = "task2")),
                UserODB(email = "email5", task = TaskODB(name = "task3")))

        val res1 = anyDao.all(UserODB::class) {
            @Suppress("UNUSED_VARIABLE")
            val p = or {
                it[UserODB::email] like "email1"
                it[UserODB::email] like "email2"
            }
            it
        }
        Assert.assertEquals("Should applyClause restrinctions from or", 2, res1.size)

        val res2 = anyDao.all(UserODB::class) {
            @Suppress("UNUSED_VARIABLE")
            val p = clause {
                it[UserODB::email] like "email1"
                it[UserODB::email] like "email2"
            }
            it
        }
        Assert.assertEquals("Should not applyClause restrictions from clause", 5, res2.size)

        val res3 = anyDao.all(UserODB::class) {
            val p = clause {
                it[UserODB::email] like "email1"
                it[UserODB::email] like "email2"
            }
            it or p
            it
        }
        Assert.assertEquals("Should applyClause restrictions from clause as or", 2, res3.size)

        val res4 = anyDao.all(UserODB::class) {
            val p = clause {
                it[UserODB::email] like "email1"
                it[UserODB::email] like "email2"
            }
            it and p
            it
        }
        Assert.assertEquals("Should applyClause restrictions from clause as and", 0, res4.size)

    }

    @Test
    fun `should apply clauses conditionally`() {
        anyDao.persist(
                UserODB(email = "email1", task = TaskODB(name = "task1")),
                UserODB(email = "email2", task = TaskODB(name = "task1")))

        var restrictAlsoToEmail2 = false
        val query: KRoot<UserODB>.(KRoot<UserODB>) -> KRoot<UserODB> = {
            val p = clause {
                it[UserODB::email] like "email1"
                it[UserODB::email] like "email2"
            }
            if (restrictAlsoToEmail2) {
                it and p
            }
            it
        }
        val res1 = anyDao.all(UserODB::class, query)
        Assert.assertEquals("Should not applyClause clause because if condition is not met", 2, res1.size)

        restrictAlsoToEmail2 = true
        val res2 = anyDao.all(UserODB::class, query)
        Assert.assertEquals("Should applyClause clause because if condition is met", 0, res2.size)

    }

    @Test
    fun `should apply and conditionally`() {
        anyDao.persist(
                UserODB(email = "email1", task = TaskODB(name = "task1")),
                UserODB(email = "email2", task = TaskODB(name = "task1")))

        var restrictAlsoToEmail2 = false
        val query: KRoot<UserODB>.(KRoot<UserODB>) -> KRoot<UserODB> = {
            if (restrictAlsoToEmail2)
                and {
                    it[UserODB::email] like "email1"
                    it[UserODB::email] like "email2"
                }
            it
        }

        val res1 = anyDao.all(UserODB::class, query)
        Assert.assertEquals("Should not applyClause clause because if condition is not met", 2, res1.size)

        restrictAlsoToEmail2 = true
        val res2 = anyDao.all(UserODB::class, query)
        Assert.assertEquals("Should applyClause clause because if condition is met", 0, res2.size)

    }


    @Test
    fun `should allow using same closure for all, count, one etc`() {

        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"))
        val u3 = UserODB(email = "email3", task = TaskODB(name = "task3"))
        anyDao.persist(u1, u2, u3)


        val query: KQuery<UserODB, TaskODB> = {
            not { it[UserODB::email].eq("email1") }
            it select it[UserODB::task]
        }
        val res1 = anyDao.all(UserODB::class, query)
        val count = anyDao.count(UserODB::class, query)
        val exists = anyDao.exists(UserODB::class, query)
        try {
            anyDao.one(UserODB::class, query)
            Assert.fail("There should be two results")
        } catch (ex: NonUniqueResultException) {
            // expected
        }
        val first = anyDao.first(UserODB::class, query)
        val page = anyDao.page(UserODB::class, Page(), query)
        val pages = anyDao.pages(UserODB::class, Page(), query)
        Assert.assertEquals(setOf(u2.task, u3.task).map { it.id }.toSet(), res1.map { it.id }.toSet())

        Assert.assertEquals(2, count)
        Assert.assertEquals(true, exists)
        Assert.assertNotNull(first)
        Assert.assertNotNull(page)
        Assert.assertNotNull(pages)
    }


    @Test
    fun `should allow to define predicates outside query`() {

        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"))
        val u3 = UserODB(email = "email3", task = TaskODB(name = "task3", address = AddressODB(city = "Cracow")))
        anyDao.persist(u1, u2, u3)

        val clause: KClause<UserODB> = {
            it[UserODB::email] like "email1"
        }
        val res1 = anyDao.all(UserODB::class) {
            or {
                it applyClause clause
                it[UserODB::email] like "email2"
            }
        }
        val res2 = anyDao.all(UserODB::class) {
            or {
                it applyClause clause
                it[UserODB::email] like "email3"
            }
        }
        Assert.assertEquals(setOf(u1, u2).map { it.id }.toSet(), res1.map { it.id }.toSet())
        Assert.assertEquals(setOf(u1, u3).map { it.id }.toSet(), res2.map { it.id }.toSet())


        val clauseTask: KClause<UserODB> = {
            it[UserODB::task, TaskODB::name] like "task1"
        }
        val res3 = anyDao.all(UserODB::class) {
            it applyClause clauseTask
        }
        Assert.assertEquals(setOf(u1).map { it.id }.toSet(), res3.map { it.id }.toSet())


        val rootClauseTask: KFromClause<UserODB> = {
            it.join(UserODB::task)[TaskODB::name] like "task1"
        }
        val res4 = anyDao.all(UserODB::class) {
            it applyClause rootClauseTask
        }
        Assert.assertEquals(setOf(u1).map { it.id }.toSet(), res4.map { it.id }.toSet())


        val joinClauseTask: KFromClause<TaskODB> = {
            it.join(TaskODB::address)[AddressODB::city] like "Cracow"
        }
        val res5 = anyDao.all(UserODB::class) {
            it.join(UserODB::task) {
                it applyClause joinClauseTask
            }
        }
        Assert.assertEquals(setOf(u3).map { it.id }.toSet(), res5.map { it.id }.toSet())
    }

}