package com.kameo.jpasugar.test

import com.kameo.jpasugar.Page
import com.kameo.jpasugar.KQuery
import com.kameo.jpasugar.test.helpers.AddressODB
import com.kameo.jpasugar.test.helpers.BaseTest
import com.kameo.jpasugar.test.helpers.TaskODB
import com.kameo.jpasugar.test.helpers.UserODB
import com.kameo.jpasugar.wraps.and
import com.kameo.jpasugar.wraps.like
import com.kameo.jpasugar.wraps.lower
import com.kameo.jpasugar.wraps.max
import com.kameo.jpasugar.wraps.not
import com.kameo.jpasugar.wraps.or
import org.junit.Assert
import org.junit.Test
import javax.persistence.NoResultException
import javax.persistence.NonUniqueResultException


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
    fun `should return one and first result`() {

        anyDao.persist(
                UserODB(email = "email1", task = TaskODB(name = "task1")),
                UserODB(email = "email2", task = TaskODB(name = "task2")),
                UserODB(email = "email3", task = TaskODB(name = "task3")))

        val u1: UserODB = anyDao.one(UserODB::class) { it[UserODB::email] like "email1" }
        val u2: UserODB? = anyDao.first(UserODB::class) { it[UserODB::email] like "email1" }

        try {
            anyDao.one(UserODB::class) { it[UserODB::email] like "email4" }
            Assert.fail()
        } catch (th: NoResultException) {
            // ignore
        }
        val uu2: UserODB? = anyDao.first(UserODB::class) { it[UserODB::email] like "email4" }

        Assert.assertNotNull(u1)
        Assert.assertNotNull(u2)
        Assert.assertNull(uu2)
    }

    @Test
    fun `should return lower string`() {

        anyDao.persist(
                UserODB(email = "Email1", task = TaskODB(name = "task1")),
                UserODB(email = "Email2", task = TaskODB(name = "task2")),
                UserODB(email = "Email3", task = TaskODB(name = "task3")))

        val uu1: String = anyDao.one(UserODB::class) {
            it[UserODB::email] like "Email1"
            it.select(it[UserODB::email].lower())
        }

        Assert.assertEquals("email1", uu1)
    }

    @Test
    fun `should return max id`() {
        val u3 = UserODB(email = "email3", task = TaskODB(name = "t2"))
        anyDao.persist(
                UserODB(email = "email1", task = TaskODB(name = "t1")),
                UserODB(email = "email2", task = TaskODB(name = "t1")),
                u3)

        val max = anyDao.one(UserODB::class) {
            it.select(it[UserODB::id].max())
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

    @Test
    fun `should handle not predicates`() {

        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"))
        val u3 = UserODB(email = "email3", task = TaskODB(name = "task3"))
        anyDao.persist(u1, u2, u3)

        val res1 = anyDao.all(UserODB::class, {
            not { it[UserODB::email].eq("email1") }
        })
        Assert.assertEquals(setOf(u2, u3).map { it.id }.toSet(), res1.map { it.id }.toSet())
    }

    @Test
    fun `should allow get three paths at once`() {

        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1", address = AddressODB(city = "Cracow")),
                taskNullable =  TaskODB(name = "task1", address = AddressODB(city = "Cracow")))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2", addressNullable = AddressODB(city = "Cracow")),
                taskNullable = TaskODB(name = "task2", addressNullable = AddressODB(city = "Cracow", cityNullable = "Cracow")))
        val u3 = UserODB(email = "email3", task = TaskODB(name = "task3"))
        anyDao.persist(u1, u2, u3)

        val res1 = anyDao.all(UserODB::class, {
           it[UserODB::task, TaskODB::address, AddressODB::city] like "Cracow"
        })
        Assert.assertEquals(setOf(u1).map { it.id }.toSet(), res1.map { it.id }.toSet())

        val res2 = anyDao.all(UserODB::class, {
            it[UserODB::task, TaskODB::addressNullable, AddressODB::city] like "Cracow"
        })
        Assert.assertEquals(setOf(u2).map { it.id }.toSet(), res2.map { it.id }.toSet())

        val res3 = anyDao.all(UserODB::class, {
            it[UserODB::taskNullable, TaskODB::address, AddressODB::city] like "Cracow"
        })
        Assert.assertEquals(setOf(u1).map { it.id }.toSet(), res3.map { it.id }.toSet())

        val res4 = anyDao.all(UserODB::class, {
            it[UserODB::taskNullable, TaskODB::addressNullable, AddressODB::city] like "Cracow"
        })
        Assert.assertEquals(setOf(u2).map { it.id }.toSet(), res4.map { it.id }.toSet())

        val res5 = anyDao.all(UserODB::class, {
            it[UserODB::taskNullable, TaskODB::addressNullable, AddressODB::cityNullable] like "Cracow"
        })
        Assert.assertEquals(setOf(u2).map { it.id }.toSet(), res5.map { it.id }.toSet())
    }

}

