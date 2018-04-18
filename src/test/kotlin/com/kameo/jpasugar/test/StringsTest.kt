package com.kameo.jpasugar.test

import com.kameo.jpasugar.test.helpers.BaseTest
import com.kameo.jpasugar.test.helpers.TaskODB
import com.kameo.jpasugar.test.helpers.UserODB
import com.kameo.jpasugar.wraps.concat
import com.kameo.jpasugar.wraps.contains
import com.kameo.jpasugar.wraps.isNullOrContains
import com.kameo.jpasugar.wraps.length
import com.kameo.jpasugar.wraps.locate
import com.kameo.jpasugar.wraps.lower
import com.kameo.jpasugar.wraps.substring
import com.kameo.jpasugar.wraps.trim
import com.kameo.jpasugar.wraps.upper
import org.junit.Assert
import org.junit.Test
import javax.persistence.criteria.CriteriaBuilder


class StringsTest : BaseTest() {


    @Test
    fun `should find by literal - lower`() {
        val u1 = UserODB(email = "ema", task = TaskODB(name = "task1"))
        val u2 = UserODB(email = "Ema", task = TaskODB(name = "task1"))
        val u3 = UserODB(email = "emy", task = TaskODB(name = "task1"))
        val u4 = UserODB(email = "EMA", task = TaskODB(name = "task1"))
        anyDao.persist(u1, u2, u3, u4)

        val res1 = anyDao.all(UserODB::class) {
            it[UserODB::email] eq literal("EMA").lower()
        }
        Assert.assertEquals(setOf(u1).map { it.id }.toSet(), res1.map { it.id }.toSet())

        val res2 = anyDao.all(UserODB::class) {
            it[UserODB::email] eq literal("ema").upper()
        }
        Assert.assertEquals(setOf(u4).map { it.id }.toSet(), res2.map { it.id }.toSet())
    }

    @Test
    fun `should find by string length`() {
        val u1 = UserODB(email = "ee", task = TaskODB(name = "task1"))
        val u2 = UserODB(email = "eeee", task = TaskODB(name = "task1"))
        anyDao.persist(u1, u2)
        val res1 = anyDao.all(UserODB::class) {
            it[UserODB::email].length() eq 2
        }
        Assert.assertEquals(setOf(u1).map { it.id }.toSet(), res1.map { it.id }.toSet())
    }

    @Test
    fun `should find by matching substring`() {
        val u1 = UserODB(email = "1234", task = TaskODB(name = "task1"))
        val u2 = UserODB(email = "efgh", task = TaskODB(name = "task1"))
        anyDao.persist(u1, u2)
        val res1 = anyDao.all(UserODB::class) {
            it[UserODB::email].substring(0, 2) eq "12"
        }
        Assert.assertEquals(setOf(u1).map { it.id }.toSet(), res1.map { it.id }.toSet())

        val res2 = anyDao.all(UserODB::class) {
            it[UserODB::email].substring(2) eq "fgh"
        }
        Assert.assertEquals(setOf(u2).map { it.id }.toSet(), res2.map { it.id }.toSet())

        val res3 = anyDao.all(UserODB::class) {
            select(it[UserODB::email].substring(2))
        }
        Assert.assertEquals(setOf("234", "fgh"), res3.toSet())
    }


    @Test
    fun `should support trim function`() {
        val u1 = UserODB(email = "abba", task = TaskODB(name = "task1"))
        val u2 = UserODB(email = " 1234 ", task = TaskODB(name = "task1"))
        anyDao.persist(u1, u2)
        val res1 = anyDao.all(UserODB::class) {
            it[UserODB::email].trim('a') eq "bb"
        }
        Assert.assertEquals(setOf(u1).map { it.id }.toSet(), res1.map { it.id }.toSet())

        val res2 = anyDao.all(UserODB::class) {
            it[UserODB::email].trim(CriteriaBuilder.Trimspec.LEADING, 'a') eq "bba"
        }
        Assert.assertEquals(setOf(u1).map { it.id }.toSet(), res2.map { it.id }.toSet())

        val res3 = anyDao.all(UserODB::class) {
            it[UserODB::email].trim() eq "1234"
        }
        Assert.assertEquals(setOf(u2).map { it.id }.toSet(), res3.map { it.id }.toSet())


    }

    @Test
    fun `should find by matching concat`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task1"))
        anyDao.persist(u1, u2)
        val res1 = anyDao.all(UserODB::class) {
            it[UserODB::email].concat(it[UserODB::task, TaskODB::name]) eq "email1task1"
        }
        Assert.assertEquals(setOf(u1).map { it.id }.toSet(), res1.map { it.id }.toSet())

        val res2 = anyDao.all(UserODB::class) {
            it.literal("A").concat(it[UserODB::email]) eq "Aemail1"
        }
        Assert.assertEquals(setOf(u1).map { it.id }.toSet(), res2.map { it.id }.toSet())
    }

    @Test
    fun `should support locate function`() {
        val u1 = UserODB(email = "abba", task = TaskODB(name = "task1"))
        val u2 = UserODB(email = "bbebb", task = TaskODB(name = "task1"))
        anyDao.persist(u1, u2)

        val res1 = anyDao.all(UserODB::class) {
            it[UserODB::email].locate("bb") eq 2
        }
        Assert.assertEquals(setOf(u1).map { it.id }.toSet(), res1.map { it.id }.toSet())

        val res2 = anyDao.all(UserODB::class) {
            select(it[UserODB::email].locate("bb"))
        }
        Assert.assertEquals(setOf(2, 1), res2.toSet())
    }

    @Test
    fun `should search by contains function`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), emailNullable = null)
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task1"), emailNullable = "aabbaa")
        val u3 = UserODB(email = "email3", task = TaskODB(name = "task1"), emailNullable = "aaaaaa")
        anyDao.persist(u1, u2, u3)

        val res1 = anyDao.all(UserODB::class) {
            it[UserODB::emailNullable] isNullOrContains "bb"
        }
        Assert.assertEquals(setOf(u1, u2).map { it.id }.toSet(), res1.map { it.id }.toSet())

        val res2 = anyDao.all(UserODB::class) {
            it[UserODB::emailNullable] contains  "bb"
        }
        Assert.assertEquals(setOf(u2).map { it.id }.toSet(), res2.map { it.id }.toSet())
    }

}