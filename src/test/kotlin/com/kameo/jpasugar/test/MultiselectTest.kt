package com.kameo.jpasugar.test

import com.kameo.jpasugar.AnyDAONew
import com.kameo.jpasugar.Quadruple
import com.kameo.jpasugar.test.helpers.BaseTest
import com.kameo.jpasugar.test.helpers.TaskODB
import com.kameo.jpasugar.test.helpers.UserODB
import org.junit.Assert
import org.junit.Test


class MultiselectTest : BaseTest() {

    @Test
    fun `should return multiselect (pair)`() {
        anyDao.persist(
                UserODB(email = "email1", task = TaskODB(name = "t1")),
                UserODB(email = "email2", task = TaskODB(name = "t1")),
                UserODB(email = "email3", task = TaskODB(name = "t2")))

        val res: List<Pair<String, Long>> = anyDao.all(UserODB::class) {
            val taskName = it[UserODB::task, TaskODB::name]
            it.select(taskName, it[UserODB::id])
        }
        Assert.assertEquals(2, res.size)
    }

    @Test
    fun `should return multiselect (triple)`() {
        anyDao.persist(
                UserODB(email = "email1", task = TaskODB(name = "t1")),
                UserODB(email = "email2", task = TaskODB(name = "t1")),
                UserODB(email = "email3", task = TaskODB(name = "t2")))

        val res: List<Triple<String, Long, Long>> = anyDao.all(UserODB::class) {
            val taskName = it[UserODB::task, TaskODB::name]
            it.groupBy(taskName)
            it.select(taskName, it.max(UserODB::id), it[UserODB::id].count())
        }
        Assert.assertEquals(2, res.size)
    }

    @Test
    fun `should return multiselect (quadruple)`() {
        anyDao.persist(
                UserODB(email = "email1", task = TaskODB(name = "t1")),
                UserODB(email = "email2", task = TaskODB(name = "t1")),
                UserODB(email = "email3", task = TaskODB(name = "t2")))

        val res: List<Quadruple<String, Long, Long, Long>> = anyDao.all(UserODB::class) {
            val taskName = it[UserODB::task, TaskODB::name]
            it.groupBy(taskName)
            it.select(taskName, it.max(UserODB::id), it[UserODB::id].count(), it[UserODB::id].count())
        }
        Assert.assertEquals(2, res.size)
    }

    @Test
    fun `should return multiselect (array)`() {
        anyDao.persist(
                UserODB(email = "email1", task = TaskODB(name = "t1")),
                UserODB(email = "email2", task = TaskODB(name = "t2")),
                UserODB(email = "email3", task = TaskODB(name = "t3")))

        val res: List<Array<Any>> = anyDao.all(UserODB::class) {
            val taskName = it[UserODB::task, TaskODB::name]
            it.orderBy(UserODB::id)
            it.selectArray(taskName, it[UserODB::id], it[UserODB::id], it[UserODB::id])
        }


        Assert.assertEquals(3, res.size)

        Assert.assertEquals("t1", res[0][0])
        Assert.assertEquals(1L, res[0][1])

        Assert.assertEquals("t2", res[1][0])
        Assert.assertEquals(3L, res[1][1])
    }


    class UserTaskDTO(val userEmail: String, val userId: Long, val taskName: String)

    @Test
    fun `should return multiselect (object)`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "t1"))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "t2"))
        val u3 = UserODB(email = "email3", task = TaskODB(name = "t3"))
        anyDao.persist(u1, u2, u3)

        val res: List<UserTaskDTO> = anyDao.all(UserODB::class) {

            val taskName = it[UserODB::task, TaskODB::name]
            it.orderBy(UserODB::id)
            it.select(UserTaskDTO::class,
                    it[UserODB::email].concat("_").concat(taskName),
                    it[UserODB::id],
                    taskName)
        }

        Assert.assertEquals(3, res.size)
        Assert.assertEquals("email1_t1", res[0].userEmail)
        Assert.assertEquals(u1.id, res[0].userId)
        Assert.assertEquals("t1", res[0].taskName)
        Assert.assertEquals("email2_t2", res[1].userEmail)
        Assert.assertEquals(u2.id, res[1].userId)
        Assert.assertEquals("t2", res[1].taskName)
        Assert.assertEquals("email3_t3", res[2].userEmail)
        Assert.assertEquals(u3.id, res[2].userId)
        Assert.assertEquals("t3", res[2].taskName)

    }

}
