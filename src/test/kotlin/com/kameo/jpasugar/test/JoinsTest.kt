package com.kameo.jpasugar.test

import com.kameo.jpasugar.test.helpers.AddressODB
import com.kameo.jpasugar.test.helpers.BaseTest
import com.kameo.jpasugar.test.helpers.TaskODB
import com.kameo.jpasugar.test.helpers.UserODB
import com.kameo.jpasugar.wraps.and
import com.kameo.jpasugar.wraps.or
import org.junit.Assert
import org.junit.Test
import javax.persistence.criteria.JoinType


class JoinsTest : BaseTest() {

    @Test
    fun `should execute query with left join`() {

        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), address = AddressODB(city = "Cracow"))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"), address = AddressODB(city = "Warsaw"))
        val u3 = UserODB(email = "email3", task = TaskODB(name = "task3"))
        anyDao.persist(u1, u2, u3)


        val res1 = anyDao.all(UserODB::class) {
            it.join(UserODB::address, JoinType.LEFT) or {
                it isNull {}
                it[AddressODB::city] eq "Cracow"
            }
        }
        Assert.assertEquals(2, res1.size)


        val res1a = anyDao.all(UserODB::class) {
            it.join(UserODB::address, JoinType.LEFT) {
                it[AddressODB::city] eq "Cracow"
            }
        }
        Assert.assertEquals(1, res1a.size)


        val res1b = anyDao.all(UserODB::class) {
            val addressJoin = it.join(UserODB::address, JoinType.LEFT)
            or {
                addressJoin isNull {}
                addressJoin[AddressODB::city] eq "Cracow"
            }
        }
        Assert.assertEquals(2, res1b.size)


        val res1c = anyDao.all(UserODB::class) {
            val addressJoin = it.join(UserODB::address)
            or {
                addressJoin isNull {}
                addressJoin[AddressODB::city] eq "Cracow"
            }
        }
        Assert.assertEquals("IsNull should be ingored because of default inner join", 1, res1c.size)


        val res1d = anyDao.all(UserODB::class) {
            it.join(UserODB::address, JoinType.LEFT) isNull {}
        }
        Assert.assertEquals(1, res1d.size)

    }

    @Test
    fun `should execute query with left join to @OneToMany`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), allTasks = listOf(TaskODB(name = "task1a"), TaskODB(name = "task1b")))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"), address = AddressODB(city = "Warsaw"),
                allTasks = listOf(TaskODB(name = "task2a"), TaskODB(name = "task2b")))
        val u3 = UserODB(email = "email3", task = TaskODB(name = "task3"))
        anyDao.persist(u1, u2, u3)

        val res = anyDao.all(UserODB::class) {
            it.join(UserODB::allTasks) or {
                it[TaskODB::name] like "task1a"
                it[TaskODB::name] like "task2a"
            }
        }

        Assert.assertEquals(2, res.size)

        val res2 = anyDao.all(UserODB::class) {
            it.join(UserODB::allTasks) and {
                it[TaskODB::name] like "task1a"
                it[TaskODB::name] like "task2a"
            }
        }
        Assert.assertEquals(0, res2.size)


    }
}