package com.kameo.jpasugar.test

import com.kameo.jpasugar.test.helpers.AddressODB
import com.kameo.jpasugar.test.helpers.BaseTest
import com.kameo.jpasugar.test.helpers.TaskODB
import com.kameo.jpasugar.test.helpers.UserODB
import com.kameo.jpasugar.test.helpers.UserRole
import com.kameo.jpasugar.wraps.and
import com.kameo.jpasugar.wraps.greaterThan
import com.kameo.jpasugar.wraps.like
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
            it.joinList(UserODB::allTasks) or {
                it[TaskODB::name] like "task1a"
                it[TaskODB::name] like "task2a"
            }
        }

        Assert.assertEquals(2, res.size)

        val res2 = anyDao.all(UserODB::class) {
            it.joinList(UserODB::allTasks) and {
                it[TaskODB::name] like "task1a"
                it[TaskODB::name] like "task2a"
            }
        }
        Assert.assertEquals(0, res2.size)


    }

    @Test
    fun `should join to element collection - set`() {

        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), userRoles = setOf(UserRole.ADMIN))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"), userRoles = setOf(UserRole.NORMAL, UserRole.GUEST))
        val u3 = UserODB(email = "email3", task = TaskODB(name = "task3"), userRoles = setOf(UserRole.GUEST))
        anyDao.persist(u1, u2, u3)

        val res1 = anyDao.all(UserODB::class) {
            it.joinSet(UserODB::userRoles) {
                or {
                    it eq UserRole.ADMIN
                    it eq UserRole.NORMAL
                }
            }

        }
        Assert.assertEquals(setOf(u1, u2).map { it.id }.toSet(), res1.map { it.id }.toSet())

        val res2 = anyDao.all(UserODB::class) {
            it.joinSet(UserODB::userRoles) {
                or {
                    it eq UserRole.ADMIN
                    it eq UserRole.GUEST
                }
            }
        }
        Assert.assertEquals(setOf(u1, u2, u3).map { it.id }.toSet(), res2.map { it.id }.toSet())
    }

    @Test
    fun `should join to element collection - map with boolean values`() {

        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), userRoles2 = mapOf(UserRole.ADMIN to true))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"), userRoles2 = mapOf(UserRole.NORMAL to true, UserRole.GUEST to false))
        val u3 = UserODB(email = "email3", task = TaskODB(name = "task3"), userRoles2 = mapOf(UserRole.GUEST to false))
        anyDao.persist(u1, u2, u3)

        val res1 = anyDao.all(UserODB::class) {
            it.joinMap(UserODB::userRoles2) {
                it eq true
            }
        }
        Assert.assertEquals(setOf(u1, u2).map { it.id }.toSet(), res1.map { it.id }.toSet())

        val res2 = anyDao.all(UserODB::class) {
            it.joinMap(UserODB::userRoles2) {
                it.key() eq UserRole.NORMAL
                it.value() eq true
            }
        }
        Assert.assertEquals(setOf(u2).map { it.id }.toSet(), res2.map { it.id }.toSet())

        val res3 = anyDao.all(UserODB::class) {
            it.joinMap(UserODB::userRoles2) {
                it.key() eq UserRole.NORMAL
                it.value() eq false
            }
        }
        Assert.assertEquals(0, res3.size)
    }

    @Test
    fun `should join to element collection - map with string values`() {

        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), userRoles3 = mapOf("ADMIN" to "true"))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"), userRoles3 = mapOf("NORMAL" to "true", "GUEST" to "false"))
        val u3 = UserODB(email = "email3", task = TaskODB(name = "task3"), userRoles3 = mapOf("GUEST" to "false"))
        anyDao.persist(u1, u2, u3)

        val res1 = anyDao.all(UserODB::class) {
            it.joinMap(UserODB::userRoles3) {
                it.value() like "true"
            }
        }
        Assert.assertEquals(setOf(u1, u2).map { it.id }.toSet(), res1.map { it.id }.toSet())

        val res1b = anyDao.all(UserODB::class) {
            it.joinMap(UserODB::userRoles3) {
                it.value() eq "true"
            }
        }
        Assert.assertEquals(setOf(u1, u2).map { it.id }.toSet(), res1b.map { it.id }.toSet())

        val res2 = anyDao.all(UserODB::class) {
            it.joinMap(UserODB::userRoles3) {
                it.key() like "GUEST"
                it.value() like "false"
                it like "false"
            }
        }
        Assert.assertEquals(setOf(u2, u3).map { it.id }.toSet(), res2.map { it.id }.toSet())

        val res3 = anyDao.all(UserODB::class) {
            it.joinMap(UserODB::userRoles3) {
                it.key() like "GUEST"
                it.value() like "true"
            }
        }
        Assert.assertEquals(0, res3.size)

    }


    @Test
    fun `should join to element collection - map with int values`() {

        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), userRoles4 = mapOf("ADMIN" to 1))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"), userRoles4 = mapOf("NORMAL" to 1, "GUEST" to 2))
        val u3 = UserODB(email = "email3", task = TaskODB(name = "task3"), userRoles4 = mapOf("GUEST" to 3))
        anyDao.persist(u1, u2, u3)

        val res1 = anyDao.all(UserODB::class) {
            it.joinMap(UserODB::userRoles4) {
                it.value() greaterThan 2
            }
        }
        Assert.assertEquals(setOf(u3).map { it.id }.toSet(), res1.map { it.id }.toSet())

    }

    @Test
    fun `should join to element collection - list with string values`() {

        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), userRoles5 = listOf("ADMIN"))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"), userRoles5 = listOf("NORMAL", "GUEST"))
        val u3 = UserODB(email = "email3", task = TaskODB(name = "task3"), userRoles5 = listOf("GUEST"))
        anyDao.persist(u1, u2, u3)

        val res1 = anyDao.all(UserODB::class) {
            it.joinList(UserODB::userRoles5) {
                it like "GUEST"
            }
        }
        Assert.assertEquals(setOf(u2, u3).map { it.id }.toSet(), res1.map { it.id }.toSet())

    }

    @Test
    fun `should join to element collection - nullable list with string values`() {

        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), userRoles6 = listOf("ADMIN"))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"), userRoles6 = listOf("NORMAL", "GUEST"))
        val u3 = UserODB(email = "email3", task = TaskODB(name = "task3"), userRoles6 = null)
        anyDao.persist(u1, u2, u3)

        val res1 = anyDao.all(UserODB::class) {
            it.joinList(UserODB::userRoles6, JoinType.LEFT) {
                it like "ADMIN"
            }
        }
        Assert.assertEquals(1, res1.size)

        val res2 = anyDao.all(UserODB::class) {
            it.joinList(UserODB::userRoles6, JoinType.LEFT) {
                it isNull {}
            }
        }
        Assert.assertEquals(1, res2.size)
        Assert.assertEquals(setOf(u3).map { it.id }.toSet(), res2.map { it.id }.toSet())

    }
}