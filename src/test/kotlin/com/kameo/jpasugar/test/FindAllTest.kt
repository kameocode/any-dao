package com.kameo.jpasugar.test

import com.kameo.jpasugar.test.helpers.AddressODB
import com.kameo.jpasugar.test.helpers.BaseTest
import com.kameo.jpasugar.test.helpers.TaskODB
import com.kameo.jpasugar.test.helpers.UserODB
import com.kameo.jpasugar.wraps.and
import com.kameo.jpasugar.wraps.or
import org.junit.Assert
import org.junit.Test


class FindAllTest : BaseTest() {

    @Test
    fun `should execute simple all queries`() {

        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"))
        val u3 = UserODB(email = "email3", task = TaskODB(name = "task3"))
        anyDao.persist(u1, u2, u3)


        val resAll = anyDao.all(UserODB::class)
        val res0 = anyDao.all(UserODB::class, { it[UserODB::email].eq("email0") })
        Assert.assertEquals(3, resAll.size)
        Assert.assertEquals(0, res0.size)


        val res1 = anyDao.all(UserODB::class, { it[UserODB::email].eq("email1") })
        val res1a = anyDao.all(UserODB::class) { it[UserODB::task, TaskODB::name] eq "task1" }
        val res1b = anyDao.all(UserODB::class) { it[UserODB::task, TaskODB::name] eq "task5" }
        Assert.assertEquals(1, res1.size)
        Assert.assertEquals(1, res1a.size)
        Assert.assertEquals(0, res1b.size)
        Assert.assertEquals(u1, res1[0])


        val res2 = anyDao.all(UserODB::class) {
            or {
                it[UserODB::task, TaskODB::name] eq "task1"
                it[UserODB::task, TaskODB::name] eq "task2"
            }
        }
        val res2a = anyDao.all(UserODB::class) {


            it get UserODB::task get TaskODB::name eq "task1"
            it get UserODB::address get AddressODB::city eq "task1"

            it[UserODB::address] and {
                it[AddressODB::city] eq "cracow"
            }

            it[UserODB::task] and {
                it[TaskODB::name] eq "task1"
                it get TaskODB::name eq "task1"
            }
            and {
                it[UserODB::login] eq "email2"
                or {
                    it[UserODB::task, TaskODB::name] like "task1"
                    it[UserODB::task, TaskODB::name] like "task2"
                }
            }
        }

        Assert.assertEquals(2, res2.size)
        Assert.assertEquals(0, res2a.size)

    }


    @Test
    fun `should return different type than query root`() {

        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"))
        val u3 = UserODB(email = "email3", task = TaskODB(name = "task3"))
        anyDao.persist(u1, u2, u3)

        val res: List<TaskODB> = anyDao.all(UserODB::class) {
            it get UserODB::task get TaskODB::name eq "task1"
            it select UserODB::task
        }
        Assert.assertEquals("Should return list of tasks with one entry", 1, res.size)


        val res2: List<String> = anyDao.all(UserODB::class) {
            it get UserODB::task get TaskODB::name eq "task1"
            it select it[UserODB::task, TaskODB::name]
        }
        Assert.assertEquals("Should return list of strings with one entry", "task1", res2[0])


    }

    @Test
    fun `should return mutable list`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"))
        val u3 = UserODB(email = "email3", task = TaskODB(name = "task3"))
        anyDao.persist(u1, u2, u3)

        val res: MutableList<TaskODB> = anyDao.allMutable(UserODB::class) {
            it get UserODB::task get TaskODB::name eq "task1"
            it select UserODB::task
        }
        res.add(TaskODB(name = "New task"))
        Assert.assertEquals(2, res.size)


        val res2: MutableList<TaskODB> = anyDao.allMutable(UserODB::class) {
            it get UserODB::task get TaskODB::name eq "task-na"
            it select UserODB::task
        }
        res2.add(TaskODB(name = "New task"))
        Assert.assertEquals(1, res2.size)

    }


    @Test
    fun `should return count`() {

        anyDao.persist(
                UserODB(email = "email1", task = TaskODB(name = "task1")),
                UserODB(email = "email2", task = TaskODB(name = "task2")),
                UserODB(email = "email3", task = TaskODB(name = "task3")))

        val count = anyDao.one(UserODB::class) {
            it[UserODB::task, TaskODB::name] or {
                it like "task1"
                it like "task2"
            }
            it.select(it.count())
        }
        Assert.assertEquals(2, count)

        val count2 = anyDao.count(UserODB::class) {
            it[UserODB::task, TaskODB::name] or {
                it like "task1"
                it like "task2"
            }
        }
        Assert.assertEquals(2, count2)
    }

    @Test
    fun `should return max id`() {
        val u3 = UserODB(email = "email3", task = TaskODB(name = "t2"))
        anyDao.persist(
                UserODB(email = "email1", task = TaskODB(name = "t1")),
                UserODB(email = "email2", task = TaskODB(name = "t1")),
                u3)

        val max = anyDao.one(UserODB::class) {
            it.select(it.max(UserODB::id))
        }
        Assert.assertEquals(u3.id, max)
    }

    @Test
    fun `should limit results`() {
        anyDao.persist(
                UserODB(email = "email1", task = TaskODB(name = "t1")),
                UserODB(email = "email2", task = TaskODB(name = "t1")),
                UserODB(email = "email3", task = TaskODB(name = "t2")))

        val res = anyDao.all(UserODB::class) {
            it limit 2
        }
        Assert.assertEquals(2, res.size)
    }


}

