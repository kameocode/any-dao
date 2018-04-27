package com.kameocode.anydao.test

import com.kameocode.anydao.test.helpers.BaseTest
import com.kameocode.anydao.test.helpers.TaskODB
import com.kameocode.anydao.test.helpers.UserODB
import com.kameocode.anydao.wraps.like
import org.junit.Assert
import org.junit.Test


class UpdateTest : BaseTest() {

    @Test
    fun `should update all entities`() {
        anyDao.persist(
                UserODB(email = "email1", task = TaskODB(name = "task1")),
                UserODB(email = "email2", task = TaskODB(name = "task2")),
                UserODB(email = "email3", task = TaskODB(name = "task3")))


        val res = anyDao.update(UserODB::class) {
            it.set(UserODB::email, "email0")
        }

        anyDao.clear()

        Assert.assertEquals(3, res)
        anyDao.all(UserODB::class).forEach {
            Assert.assertEquals("email0", it.email)
        }
    }

    @Test
    fun `should update nullable fields`() {
        anyDao.persist(
                UserODB(email = "email1", task = TaskODB(name = "task1")),
                UserODB(email = "email2", task = TaskODB(name = "task2")),
                UserODB(email = "email3", task = TaskODB(name = "task3")))


        val res = anyDao.update(UserODB::class) {
            it.set(UserODB::emailNullable, "email0")
        }

        anyDao.clear()

        Assert.assertEquals(3, res)
        anyDao.all(UserODB::class).forEach {
            Assert.assertEquals("email0", it.emailNullable)
        }
    }

    @Test
    fun `should update custom entity`() {
        anyDao.persist(
                UserODB(email = "email1", task = TaskODB(name = "task1")),
                UserODB(email = "email2", task = TaskODB(name = "task2")),
                UserODB(email = "email3", task = TaskODB(name = "task3")))

        val res = anyDao.update(UserODB::class) {
            it.set(UserODB::email, "email0")
            it[UserODB::email] like "email2"
        }

        val res1 = anyDao.update(UserODB::class) {
            it[UserODB::email] = "email0"
            it[UserODB::email] like "email2"
        }

        anyDao.clear()

        Assert.assertEquals(1, res)
        Assert.assertEquals(0, res1)
        val email0 = anyDao.all(UserODB::class).count { it.email == "email0" }
        val email1 = anyDao.all(UserODB::class).count { it.email == "email1" }
        val email2 = anyDao.all(UserODB::class).count { it.email == "email2" }
        val email3 = anyDao.all(UserODB::class).count { it.email == "email3" }

        Assert.assertEquals(1, email0)
        Assert.assertEquals(1, email1)
        Assert.assertEquals(0, email2)
        Assert.assertEquals(1, email3)

    }


}

