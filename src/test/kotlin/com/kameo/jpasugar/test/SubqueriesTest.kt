package com.kameo.jpasugar.test

import com.kameo.jpasugar.test.helpers.AddressODB
import com.kameo.jpasugar.test.helpers.BaseTest
import com.kameo.jpasugar.test.helpers.TaskODB
import com.kameo.jpasugar.test.helpers.UserODB
import com.kameo.jpasugar.wraps.like
import com.kameo.jpasugar.wraps.or
import org.junit.Assert
import org.junit.Test

class SubqueriesTest : BaseTest() {

    @Test
    fun `should execute query with subquery`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), address = AddressODB(city = "Cracow"))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"), address = AddressODB(city = "Warsaw"))
        val u3 = UserODB(email = "email3", task = TaskODB(name = "task3"))
        anyDao.persist(u1, u2, u3)

        val res = anyDao.all(UserODB::class) {
            it.isIn(UserODB::class) {
                it[UserODB::address, AddressODB::city] like "Cracow"
            }
        }
        Assert.assertEquals(1, res.size)


        val res2 = anyDao.all(UserODB::class) {
            or {
                isIn(UserODB::class) {
                    it[UserODB::address, AddressODB::city] like "Cracow"
                }
                it[UserODB::task, TaskODB::name] like "task3"
            }
        }
        Assert.assertEquals(2, res2.size)


        val res3 = anyDao.all(UserODB::class) {
            or {
                exists(UserODB::class) {
                    it[UserODB::address, AddressODB::city] like "Cracow"
                }
                it[UserODB::task, TaskODB::name] like "task3"
            }
        }
        Assert.assertEquals(3, res3.size)


        val res4 = anyDao.all(UserODB::class) {
            val task = it[UserODB::task]
            exists(UserODB::class) { it[UserODB::task] eq task }
            task[TaskODB::name] like "task3"
        }
        Assert.assertEquals(1, res4.size)

        val res5 = anyDao.all(UserODB::class) {
            notExists(UserODB::class) {
                it[UserODB::task, TaskODB::name] like "task3"
            }
        }
        Assert.assertEquals(0, res5.size)
    }

    @Test
    fun `should execute subquery with custom selection`() {
        val u1 = UserODB(email = "email2", task = TaskODB(name = "task1"))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"))
        val u3 = UserODB(email = "email3", task = TaskODB(name = "task3"))
        anyDao.persist(u1, u2, u3)

        val res = anyDao.all(UserODB::class) {
            val subquery = it.subqueryFrom(TaskODB::class) {
                it[TaskODB::name] like "task1"
                it.select(it[TaskODB::name])
            }
            it[UserODB::task, TaskODB::name] isIn subquery
        }
        Assert.assertEquals(1, res.size)

    }
}