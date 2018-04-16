package com.kameo.jpasugar.test

import com.kameo.jpasugar.test.helpers.BaseTest
import com.kameo.jpasugar.test.helpers.TaskODB
import com.kameo.jpasugar.test.helpers.UserODB
import com.kameo.jpasugar.wraps.max
import org.junit.Assert
import org.junit.Test


class GroupByTest : BaseTest() {

    @Test
    fun `should return grouped by result`() {
        anyDao.persist(
                UserODB(email = "email1", task = TaskODB(name = "t1")),
                UserODB(email = "email2", task = TaskODB(name = "t1")),
                UserODB(email = "email3", task = TaskODB(name = "t2")))

        val res: List<Pair<UserODB, Long>> = anyDao.all(UserODB::class) {
            val taskName = it[UserODB::task, TaskODB::name]
            it.groupBy(taskName, it[UserODB::id])
            it.select(it, it[UserODB::id].max())
        }
        Assert.assertEquals(3, res.size)
        res.forEach { (user, maxUserId) ->
            Assert.assertEquals(user.id, maxUserId)
        }
    }


}

