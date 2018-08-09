package com.kameocode.anydao.test

import com.kameocode.anydao.test.helpers.BaseTest
import com.kameocode.anydao.test.helpers.TaskODB
import com.kameocode.anydao.test.helpers.UserODB
import com.kameocode.anydao.wraps.JoinWrap
import org.junit.Assert
import org.junit.Test
import javax.persistence.criteria.JoinType


class SingleSelectTest : BaseTest() {

    @Test
    fun `should return plain select with nullable`() {
        anyDao.persist(
                UserODB(email = "email1", task = TaskODB(name = "t1"), taskNullable = TaskODB(name = "t1n")),
                UserODB(email = "email2", task = TaskODB(name = "t1")),
                UserODB(email = "email3", task = TaskODB(name = "t2")))

        val res1: List<TaskODB> = anyDao.all(UserODB::class) {
            it.select(UserODB::task)
        }
        Assert.assertEquals(3, res1.size)



        val res2: List<TaskODB?> = anyDao.all(UserODB::class) {
            val task: JoinWrap<TaskODB?, UserODB> = it.join(UserODB::taskNullable, JoinType.LEFT)
            it.selectNullable(task)
        }
        Assert.assertEquals(3, res2.size)
        Assert.assertNotNull(res2[0])
        Assert.assertNull(res2[1])
        Assert.assertNull(res2[2])


        val res3: List<TaskODB?> = anyDao.all(UserODB::class) {
            val task: JoinWrap<TaskODB?, UserODB> = it.join(UserODB::taskNullable, JoinType.LEFT)
            it.select(task)
        }
        Assert.assertEquals(3, res3.size)
        Assert.assertNotNull(res3[0])
        Assert.assertNull(res2[1])
        Assert.assertNull(res3[2])

        val res4: List<TaskODB> = anyDao.all(UserODB::class) {
            val task: JoinWrap<TaskODB, UserODB> = it.join(UserODB::taskNullable, JoinType.LEFT)
            it.select(task)
        }
        Assert.assertEquals(3, res4.size)
        Assert.assertNotNull(res4[0])
        Assert.assertNull(res4[1])
        Assert.assertNull(res4[2])
    }

}

