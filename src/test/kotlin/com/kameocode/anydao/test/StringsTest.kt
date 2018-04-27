package com.kameocode.anydao.test

import com.kameocode.anydao.test.helpers.BaseTest
import com.kameocode.anydao.test.helpers.TaskODB
import com.kameocode.anydao.test.helpers.UserODB
import com.kameocode.anydao.wraps.concat
import com.kameocode.anydao.wraps.contains
import com.kameocode.anydao.wraps.isNullOrContains
import com.kameocode.anydao.wraps.length
import com.kameocode.anydao.wraps.like
import com.kameocode.anydao.wraps.locate
import com.kameocode.anydao.wraps.lower
import com.kameocode.anydao.wraps.substring
import com.kameocode.anydao.wraps.trim
import com.kameocode.anydao.wraps.upper
import org.junit.Assert
import org.junit.Test
import javax.persistence.criteria.CriteriaBuilder


class StringsTest : BaseTest() {


    @Test
    fun `should return lower string`() {
        anyDao.persist(
                UserODB(email = "Email1", task = TaskODB(name = "task1")),
                UserODB(email = "Email2", task = TaskODB(name = "task2")),
                UserODB(email = "Email3", task = TaskODB(name = "task3")))

        val uu1: String = anyDao.one(UserODB::class) {
            it[UserODB::email] like "Email1"
            it.select(it[UserODB::email].lower())
        }

        Assert.assertEquals("email1", uu1)
    }

    @Test
    fun `should find by like expression`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task1"), emailNullable = "email2")
        anyDao.persist(u1, u2)
        val res1 = anyDao.all(UserODB::class) {
            it[UserODB::email] like "%email1"
        }
        Assert.assertEquals(setOf(u1).map { it.id }.toSet(), res1.map { it.id }.toSet())

        val res3 = anyDao.all(UserODB::class) {
            it[UserODB::email] like it[UserODB::emailNullable]
        }
        Assert.assertEquals(setOf(u2).map { it.id }.toSet(), res3.map { it.id }.toSet())
    }

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
        val u1 = UserODB(email = "1234", task = TaskODB(name = "task1"), counter = 2)
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

        val res4 = anyDao.all(UserODB::class) {
            select(it[UserODB::email].substring(it[UserODB::counter]))
        }
        Assert.assertEquals(setOf("234", "efgh"), res4.toSet())

        val res5 = anyDao.all(UserODB::class) {
            select(it[UserODB::email].substring(it[UserODB::counter], it.literal(1)))
        }
        Assert.assertEquals(setOf("2", "e"), res5.toSet())
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