package com.kameo.anydao.test

import com.kameo.anydao.test.helpers.AddressODB
import com.kameo.anydao.test.helpers.BaseTest
import com.kameo.anydao.test.helpers.TaskODB
import com.kameo.anydao.test.helpers.UserODB
import com.kameo.anydao.test.helpers.UserRole
import com.kameo.anydao.wraps.and
import com.kameo.anydao.wraps.lessThan
import com.kameo.anydao.wraps.like
import com.kameo.anydao.wraps.or
import org.junit.Assert
import org.junit.Test
import java.time.LocalDateTime

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

    @Test
    fun `should execute subquery with custom selection - inlined`() {
        val u1 = UserODB(email = "email2", task = TaskODB(name = "task1"))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"))
        val u3 = UserODB(email = "email3", task = TaskODB(name = "task3"))
        anyDao.persist(u1, u2, u3)

        val res = anyDao.all(UserODB::class) {
            it[UserODB::task, TaskODB::name] isIn subqueryFrom(TaskODB::class) {
                it[TaskODB::name] like "task1"
                it.select(it[TaskODB::name])
            }
        }
        Assert.assertEquals(1, res.size)

        val res2 = anyDao.all(UserODB::class) {
            it[UserODB::task] isIn subqueryFrom(TaskODB::class) {
                it[TaskODB::name] like "task1"
            }
        }
        Assert.assertEquals(1, res2.size)

    }

    @Test
    fun `should execute subquery for other entity`() {
        val u1 = UserODB(email = "email2", task = TaskODB(name = "task1"))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"))
        val u3 = UserODB(email = "email3", task = TaskODB(name = "task3"))
        anyDao.persist(u1, u2, u3)

        val res = anyDao.all(UserODB::class) {
            it[UserODB::task] isIn subqueryFrom(TaskODB::class) {
                it[TaskODB::name] like "task1"
            }
        }
        Assert.assertEquals(setOf(u1).map { it.id }.toSet(), res.map { it.id }.toSet())

    }

    @Test
    fun `should execute subquery for other entity - subquery with compound clauses`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), userRole = UserRole.NORMAL)
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"))
        val u3 = UserODB(email = "email3", task = TaskODB(name = "task2", createDateTime = LocalDateTime.now().minusDays(2)))
        anyDao.persist(u1, u2, u3)

        val res = anyDao.all(UserODB::class) {
            it[UserODB::task] isIn subqueryFrom(TaskODB::class) {
                or {
                    it[TaskODB::name] like "task1"
                    and {
                        it[TaskODB::name] like "task2"
                        it[TaskODB::createDateTime] lessThan LocalDateTime.now().minusDays(1)
                    }
                }
            }
            it[UserODB::userRole] notEq UserRole.ADMIN
        }
        Assert.assertEquals(setOf(u1,u3).map { it.id }.toSet(), res.map { it.id }.toSet())

        val res2 = PlainJpaQueries().shouldExecuteSubqueryForOtherEntityWithCompoundClauses(anyDao.em);
        Assert.assertEquals(setOf(u1,u3).map { it.id }.toSet(), res2.map { it.id }.toSet())

    }
}