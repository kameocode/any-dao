package com.kameo.jpasugar.test

import com.kameo.jpasugar.test.helpers.BaseTest
import com.kameo.jpasugar.test.helpers.TaskODB
import com.kameo.jpasugar.test.helpers.UserODB
import org.junit.Assert
import org.junit.Test


class DeleteTest : BaseTest() {

    @Test
    fun `should remove all entities`() {
        anyDao.persist(
                UserODB(email = "email1", task = TaskODB(name = "task1")),
                UserODB(email = "email2", task = TaskODB(name = "task2")),
                UserODB(email = "email3", task = TaskODB(name = "task3")))

        val count1 = anyDao.count(UserODB::class)
        Assert.assertEquals(3, count1)

        anyDao.remove(UserODB::class) {}

        val count2 = anyDao.count(UserODB::class)
        Assert.assertEquals(0, count2)
    }

    @Test
    fun `should remove entity`() {

        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"))
        anyDao.persist(
                u1,
                UserODB(email = "email2", task = TaskODB(name = "task2")),
                UserODB(email = "email3", task = TaskODB(name = "task3")))

        Assert.assertEquals(3, anyDao.count(UserODB::class))


        anyDao.remove(u1)
        Assert.assertEquals(2, anyDao.count(UserODB::class))

        val removedCount = anyDao.remove(UserODB::class) { it[UserODB::email] like "email1" }
        Assert.assertEquals(0, removedCount)


        val removedCount2 = anyDao.remove(UserODB::class) { it[UserODB::email] like "email2" }
        Assert.assertEquals(1, removedCount2)
        Assert.assertEquals(1, anyDao.count(UserODB::class))

    }
}

