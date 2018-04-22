package com.kameo.anydao.test

import com.kameo.anydao.test.helpers.BaseTest
import com.kameo.anydao.test.helpers.TaskODB
import com.kameo.anydao.test.helpers.UserODB
import com.kameo.anydao.wraps.like
import org.junit.Assert
import org.junit.Test


class DeleteTest : BaseTest() {

    @Test
    fun `should delete all entities`() {
        anyDao.persist(
                UserODB(email = "email1", task = TaskODB(name = "task1")),
                UserODB(email = "email2", task = TaskODB(name = "task2")),
                UserODB(email = "email3", task = TaskODB(name = "task3")))

        val count1 = anyDao.count(UserODB::class)
        Assert.assertEquals(3, count1)

        anyDao.delete(UserODB::class) {}

        val count2 = anyDao.count(UserODB::class)
        Assert.assertEquals(0, count2)
    }

    @Test
    fun `should delete entity`() {

        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"))
        anyDao.persist(
                u1,
                UserODB(email = "email2", task = TaskODB(name = "task2")),
                UserODB(email = "email3", task = TaskODB(name = "task3")))

        Assert.assertEquals(3, anyDao.count(UserODB::class))


        anyDao.delete(u1)
        Assert.assertEquals(2, anyDao.count(UserODB::class))

        val removedCount = anyDao.delete(UserODB::class) { it[UserODB::email] like "email1" }
        Assert.assertEquals(0, removedCount)


        val removedCount2 = anyDao.delete(UserODB::class) { it[UserODB::email] like "email2" }
        Assert.assertEquals(1, removedCount2)
        Assert.assertEquals(1, anyDao.count(UserODB::class))

    }
}

