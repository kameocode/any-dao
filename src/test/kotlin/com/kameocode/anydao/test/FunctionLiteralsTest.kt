package com.kameocode.anydao.test

import com.kameocode.anydao.KRoot
import com.kameocode.anydao.KSelect
import com.kameocode.anydao.test.helpers.BaseTest
import com.kameocode.anydao.test.helpers.TaskODB
import com.kameocode.anydao.test.helpers.UserODB
import com.kameocode.anydao.wraps.ge
import com.kameocode.anydao.wraps.like
import org.junit.Assert
import org.junit.Test

class FunctionLiteralsTest : BaseTest() {

    private fun KRoot<UserODB>.hasHighCounter(): KSelect<UserODB> {
        return this[UserODB::counter] ge 15
    }

    private fun KRoot<UserODB>.hasEmail1InName(): KSelect<UserODB> {
        return this[UserODB::email] like "%email1%"
    }

    @Test
    fun `should work with function literals with receiver`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "email1"), counter = 0)
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"), counter = 10)
        val u3 = UserODB(email = "email3", task = TaskODB(name = "task3"), counter = 20)
        anyDao.persist(u1, u2, u3)
        val res = anyDao.all(UserODB::class) {
            hasHighCounter()
            or
            hasEmail1InName()

            it
        }
        Assert.assertEquals(listOf(u1.id, u3.id).toSet(), res.map { it.id }.toSet())

        val res1 = anyDao.all(UserODB::class) {
            hasHighCounter()
            hasEmail1InName()

            it
        }
        Assert.assertTrue(res1.isEmpty())
    }
}


