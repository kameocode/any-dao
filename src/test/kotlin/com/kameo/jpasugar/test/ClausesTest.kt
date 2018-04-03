package com.kameo.jpasugar.test

import com.kameo.jpasugar.Root
import com.kameo.jpasugar.test.helpers.BaseTest
import com.kameo.jpasugar.test.helpers.TaskODB
import com.kameo.jpasugar.test.helpers.UserODB
import com.kameo.jpasugar.wraps.and
import com.kameo.jpasugar.wraps.clause
import com.kameo.jpasugar.wraps.or
import org.junit.Assert
import org.junit.Test

class ClausesTest : BaseTest() {

    @Test
    fun `should process nested or clause`() {
        val u4 =  UserODB(email = "email4", task = TaskODB(name = "task2"))
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
        Assert.assertEquals("Should apply restrinctions from or", 2, res1.size)

        val res2 = anyDao.all(UserODB::class) {
            @Suppress("UNUSED_VARIABLE")
            val p = clause {
                it[UserODB::email] like "email1"
                it[UserODB::email] like "email2"
            }
            it
        }
        Assert.assertEquals("Should not apply restrictions from clause", 5, res2.size)

        val res3 = anyDao.all(UserODB::class) {
            val p = clause {
                it[UserODB::email] like "email1"
                it[UserODB::email] like "email2"
            }
            it or p
            it
        }
        Assert.assertEquals("Should apply restrictions from clause as or", 2, res3.size)

        val res4 = anyDao.all(UserODB::class) {
            val p = clause {
                it[UserODB::email] like "email1"
                it[UserODB::email] like "email2"
            }
            it and p
            it
        }
        Assert.assertEquals("Should apply restrictions from clause as and", 0, res4.size)

    }

    @Test
    fun `should apply clauses conditionally`() {
        anyDao.persist(
                UserODB(email = "email1", task = TaskODB(name = "task1")),
                UserODB(email = "email2", task = TaskODB(name = "task1")))

        var restrictAlsoToEmail2 = false
        val query: Root<UserODB>.(Root<UserODB>) -> Root<UserODB> = {
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
        Assert.assertEquals("Should not apply clause because if condition is not met", 2, res1.size)

        restrictAlsoToEmail2 = true
        val res2 = anyDao.all(UserODB::class, query)
        Assert.assertEquals("Should apply clause because if condition is met", 0, res2.size)

    }

    @Test
    fun `should apply and conditionally`() {
        anyDao.persist(
                UserODB(email = "email1", task = TaskODB(name = "task1")),
                UserODB(email = "email2", task = TaskODB(name = "task1")))

        var restrictAlsoToEmail2 = false
        val query: Root<UserODB>.(Root<UserODB>) -> Root<UserODB> = {
            if (restrictAlsoToEmail2)
                and {
                    it[UserODB::email] like "email1"
                    it[UserODB::email] like "email2"
                }
            it
        }

        val res1 = anyDao.all(UserODB::class, query)
        Assert.assertEquals("Should not apply clause because if condition is not met", 2, res1.size)

        restrictAlsoToEmail2 = true
        val res2 = anyDao.all(UserODB::class, query)
        Assert.assertEquals("Should apply clause because if condition is met", 0, res2.size)

    }

}