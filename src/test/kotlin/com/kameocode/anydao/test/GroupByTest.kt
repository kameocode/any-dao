package com.kameocode.anydao.test

import com.kameocode.anydao.test.helpers.BaseTest
import com.kameocode.anydao.test.helpers.TaskODB
import com.kameocode.anydao.test.helpers.UserODB
import com.kameocode.anydao.wraps.greaterThan
import com.kameocode.anydao.wraps.lessThan
import com.kameocode.anydao.wraps.max
import org.junit.Assert
import org.junit.Test


class GroupByTest : BaseTest() {

    @Test
    fun `should return grouped by result`() {
        val u1= UserODB(email = "email1", task = TaskODB(name = "t1"), emailNullable="A")
        val u2= UserODB(email = "email2", task = TaskODB(name = "t1"))
        val u3= UserODB(email = "email3", task = TaskODB(name = "t2"))
        anyDao.persist(u1, u2, u3)

        val res: List<Pair<UserODB, Long>> = anyDao.all(UserODB::class) {
            val taskName = it[UserODB::task, TaskODB::name]
            it.groupBy(taskName, it[UserODB::id])
            it.select(it, it[UserODB::id].max())
        }
        Assert.assertEquals(3, res.size)
        res.forEach { (user, maxUserId) ->
            Assert.assertEquals(user.id, maxUserId)
        }


        val res2: List<Long> = anyDao.all(UserODB::class) {
            it.groupBy(UserODB::counter, UserODB::emailNullable)
            it.select(it[UserODB::id].max())
        }
        Assert.assertEquals(setOf(u1.id, u3.id).toSortedSet(), res2.toSortedSet())
    }

    @Test
    fun `should handle with having clause`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "t1"))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "t1"))
        val u3 = UserODB(email = "email3", task = TaskODB(name = "t1"))
        val u4 = UserODB(email = "email4", task = TaskODB(name = "t2"))
        anyDao.persist(u1, u2, u3, u4)


        val res0 = anyDao.all(UserODB::class) {
            it.groupBy(it[UserODB::task, TaskODB::name])
            it.select(
                    it[UserODB::task, TaskODB::name].count()
            )
        }
        Assert.assertEquals(setOf(1L, 3L).toSortedSet(), res0.toSortedSet())


        val res1 = anyDao.all(UserODB::class) {
            it.groupBy(it[UserODB::task, TaskODB::name])
            it.having {
                it[UserODB::task, TaskODB::name].count() greaterThan 1L
            }
            it.select(
                    it[UserODB::task, TaskODB::name].count()
            )
        }
        Assert.assertEquals(setOf(3L).toSortedSet(), res1.toSortedSet())

        val res2 = anyDao.all(UserODB::class) {
            it.groupBy(it[UserODB::task, TaskODB::name])
            it.having {
                it[UserODB::task, TaskODB::name].count() greaterThan 1L
            }
            select(it[UserODB::task, TaskODB::name])
        }
        Assert.assertEquals(setOf("t1").toSortedSet(), res2.toSortedSet())
    }
}

