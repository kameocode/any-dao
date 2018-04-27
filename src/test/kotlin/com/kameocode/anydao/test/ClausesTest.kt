package com.kameocode.anydao.test

import com.kameocode.anydao.KClause
import com.kameocode.anydao.KFromClause
import com.kameocode.anydao.KQuery
import com.kameocode.anydao.KRoot
import com.kameocode.anydao.Page
import com.kameocode.anydao.test.helpers.AddressODB
import com.kameocode.anydao.test.helpers.BaseTest
import com.kameocode.anydao.test.helpers.TaskODB
import com.kameocode.anydao.test.helpers.UserODB
import com.kameocode.anydao.wraps.and
import com.kameocode.anydao.wraps.clause
import com.kameocode.anydao.wraps.like
import com.kameocode.anydao.wraps.not
import com.kameocode.anydao.wraps.or
import com.kameocode.anydao.wraps.orr
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
    fun `should process embedded or clause`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"))
        val u3 = UserODB(email = "email3", task = TaskODB(name = "task3", address = AddressODB(city = "Cracow")))
        anyDao.persist(u1, u2, u3)
        val res1 = anyDao.all(UserODB::class) {
            it[UserODB::email] like "email1"
            or
            it[UserODB::email] like "email2"
            or
            it[UserODB::email] like "email3"
        }
        Assert.assertEquals(setOf(u1, u2, u3).map { it.id }.toSet(), res1.map { it.id }.toSet())

        val res2 = anyDao.all(UserODB::class) {
            it[UserODB::email] and {
                it like "email1"
                or
                it like "email2"
                or
                it like "email3"
            }
        }
        Assert.assertEquals(setOf(u1, u2, u3).map { it.id }.toSet(), res2.map { it.id }.toSet())

        val res3 = anyDao.all(UserODB::class) {
            it[UserODB::task, TaskODB::name] like "task1"
            it[UserODB::email] like "email1"
            or
            it[UserODB::email] like "email2"
            or
            it[UserODB::email] like "email3"
        }
        Assert.assertEquals(setOf(u1, u2, u3).map { it.id }.toSet(), res3.map { it.id }.toSet())

        val res4 = anyDao.all(UserODB::class) {
            it[UserODB::task, TaskODB::name] like "taskX"
            it[UserODB::email] like "email1"
            or
            it[UserODB::email] like "email2"
            or
            it[UserODB::email] like "email3"
        }
        Assert.assertEquals(setOf(u2, u3).map { it.id }.toSet(), res4.map { it.id }.toSet())


        val res5 = anyDao.all(UserODB::class) {
            it[UserODB::task, TaskODB::name] like "task1"
            it[UserODB::email] like "email1"
            or
            it[UserODB::task, TaskODB::name] like "task2"
            it[UserODB::email] like "email2"
            or
            it[UserODB::email] like "email3"
        }
        Assert.assertEquals(setOf(u1, u2, u3).map { it.id }.toSet(), res5.map { it.id }.toSet())

        val res6 = anyDao.all(UserODB::class) {
            it[UserODB::task, TaskODB::name] like "task1"
            it[UserODB::email] like "email1"
            or
            it[UserODB::task, TaskODB::name] like "task2"
            it[UserODB::email] like "email2"
            or
            it[UserODB::task, TaskODB::name] like "task3"
            it[UserODB::email] like "email3"
        }
        Assert.assertEquals(setOf(u1, u2, u3).map { it.id }.toSet(), res6.map { it.id }.toSet())

        val res7 = anyDao.all(UserODB::class) {
            it[UserODB::task, TaskODB::name] like "task1"
            it[UserODB::email] like "email1"
            or
            it[UserODB::task, TaskODB::name] like "task2"
            it[UserODB::email] like "email2"
            or
            it[UserODB::task, TaskODB::name] like "taskX"
            it[UserODB::email] like "email3"
        }
        Assert.assertEquals(setOf(u1, u2).map { it.id }.toSet(), res7.map { it.id }.toSet())

        val res8 = anyDao.all(UserODB::class) {
            and {
                it[UserODB::task, TaskODB::name] like "task1"
                it[UserODB::email] like "email1"
                or
                it[UserODB::task, TaskODB::name] like "task2"
                it[UserODB::email] like "email2"
                or
                it[UserODB::task, TaskODB::name] like "task3"
                it[UserODB::email] like "email3"
            }
        }
        Assert.assertEquals(setOf(u1, u2, u3).map { it.id }.toSet(), res8.map { it.id }.toSet())

        val res9 = anyDao.all(UserODB::class) {
            not {
                it[UserODB::task, TaskODB::name] like "task1"
                it[UserODB::email] like "email1"
                or
                it[UserODB::task, TaskODB::name] like "task2"
                it[UserODB::email] like "email2"
                or
                it[UserODB::task, TaskODB::name] like "taskX"
                it[UserODB::email] like "email3"
            }
        }
        Assert.assertEquals(setOf(u3).map { it.id }.toSet(), res9.map { it.id }.toSet())
        val res10 = anyDao.all(UserODB::class) {
            and {
                it[UserODB::task, TaskODB::name] like "task1"
                it[UserODB::email] like "email1"
            }
            or
            and {
                it[UserODB::task, TaskODB::name] like "task2"
                it[UserODB::email] like "email2"
            }

        }
        Assert.assertEquals(setOf(u1, u2).map { it.id }.toSet(), res10.map { it.id }.toSet())

        val res11 = anyDao.all(UserODB::class) {
            and {
                it[UserODB::task, TaskODB::name] like "task1"
                it[UserODB::email] like "email1"
            }
            orr {
                it[UserODB::task, TaskODB::name] like "task2"
                it[UserODB::email] like "email2"
            }

        }
        Assert.assertEquals(setOf(u1, u2).map { it.id }.toSet(), res11.map { it.id }.toSet())
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

    @Test
    fun `should work with empty clauses`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"))
        val u2 = UserODB(email = "email1", task = TaskODB(name = "task2"))
        val u3 = UserODB(email = "email1", task = TaskODB(name = "task2"))
        anyDao.persist(u1, u2, u3)
        val res = anyDao.all(UserODB::class) {
            it.or { }
            it.and { it.or { } }
            it.not { }
            it.orderBy(it[UserODB::email] to true, it[UserODB::task, TaskODB::name] to true)
        }
        Assert.assertEquals(setOf(u1, u2, u3).map { it.id }.toSet(), res.map { it.id }.toSet())

    }
}