package com.kameo.jpasugar.test

import com.kameo.jpasugar.wraps.or
import com.kameo.jpasugar.AnyDAONew
import com.kameo.jpasugar.test.data.AddressODB
import com.kameo.jpasugar.test.data.TaskODB
import com.kameo.jpasugar.test.data.UserODB
import org.junit.Assert
import org.junit.Test
import javax.persistence.EntityManager
import javax.persistence.Persistence

class SubqueriesTest {
    private val em: EntityManager = Persistence.createEntityManagerFactory("test-pu").createEntityManager()
    private val anyDao = AnyDAONew(em)

    @Test
    fun testSubquery() {
        em.transaction.begin()
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), address = AddressODB(city = "Cracow"))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"), address = AddressODB(city = "Warsaw"))
        val u3 = UserODB(email = "email3", task = TaskODB(name = "task3"))
        anyDao.persist(u1, u2, u3)

        val res = anyDao.all(UserODB::class) {
            it.isIn(UserODB::class) { it[UserODB::address, AddressODB::city] like "Cracow" }
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
                exists(UserODB::class) { it[UserODB::address, AddressODB::city] like "Cracow" }
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
            notExists(UserODB::class) { it[UserODB::task, TaskODB::name] like "task3" }
        }
        Assert.assertEquals(0, res5.size)
        em.transaction.rollback()
    }
}