package com.kameocode.anydao.test

import com.kameocode.anydao.KPage
import com.kameocode.anydao.test.helpers.BaseTest
import com.kameocode.anydao.test.helpers.TaskODB
import com.kameocode.anydao.test.helpers.UserODB
import com.kameocode.anydao.wraps.like
import com.kameocode.anydao.wraps.mod
import com.kameocode.anydao.wraps.not
import org.junit.Assert
import org.junit.Test

class PagesTest : BaseTest() {

    @Test
    fun `should return paged results`() {

        for (i in 1..19) {
            anyDao.persist(UserODB(email = "email$i", task = TaskODB(name = "task$i"), counter = i))
        }

        val res1 = anyDao.page(UserODB::class, KPage()) { it[UserODB::email] like "email1" }
        val res1a = anyDao.page(UserODB::class, KPage()) { it }
        val res1b = anyDao.page(UserODB::class, KPage().next()) { it }

        Assert.assertEquals(1, res1.size)
        Assert.assertEquals(10, res1a.size)
        Assert.assertEquals(9, res1b.size)

        val res2 = anyDao.page(UserODB::class, KPage()) { it[UserODB::counter].mod(3) eq 1 }
        Assert.assertEquals(7, res2.size)


        val sup = anyDao.pages(UserODB::class, KPage(3)) { it[UserODB::counter].mod(3) eq 1 }
        Assert.assertEquals(3, sup.invoke().size)
        Assert.assertEquals(3, sup.invoke().size)
        Assert.assertEquals(1, sup.invoke().size)
        Assert.assertEquals(0, sup.invoke().size)


        val sup2 = anyDao.pages(UserODB::class, KPage(3)) { it[UserODB::counter].mod(3) eq 1 }
        val chunkSizes2 = mutableListOf<Int>()
        sup2.forEach { chunkSizes2.add(it.size) }
        Assert.assertEquals(listOf(3, 3, 1), chunkSizes2)


        val chunkSizes2b = mutableListOf<Int>()
        sup2.forEach { chunkSizes2b.add(it.size) }
        Assert.assertEquals("Should return same results second time", listOf(3, 3, 1), chunkSizes2b)


        val sup3 = anyDao.pages(UserODB::class, KPage(3)) { it[UserODB::counter].mod(3) eq 1 }
        val chunkSizes3 = mutableListOf<Int>()
        sup3.forEachFlat { chunkSizes3.add(1) }
        Assert.assertEquals(listOf(1, 1, 1, 1, 1, 1, 1), chunkSizes3)


        val sup4 = anyDao.pages(UserODB::class, KPage(3)) { it[UserODB::counter].mod(3) eq 1 }
        val chunkSizes4 = mutableListOf<Int>()
        sup4.forEachUntil { chunkSizes4.add(it.size); false }
        Assert.assertEquals(listOf(3), chunkSizes4)


        val sup5 = anyDao.pages(UserODB::class, KPage(3)) { it[UserODB::counter].mod(3) eq 1 }
        val chunkSizes5 = mutableListOf<Int>()
        sup5.forEachFlatUntil { chunkSizes5.add(1); chunkSizes5.size < 2 }
        Assert.assertEquals(listOf(1, 1), chunkSizes5)

    }

    @Test
    fun `should return sorted paged results`() {

        for (i in 1..10) {
            anyDao.persist(UserODB(email = "email$i", task = TaskODB(name = "task$i")))
        }

        val res1 = anyDao.pagesSorted(UserODB::class, UserODB::id, KPage(3)) { not { it[UserODB::email] like "n" } }
        val chunkSizes1 = mutableListOf<Int>()
        res1.forEach { chunkSizes1.add(it.size) }
        Assert.assertEquals(listOf(3, 3, 3, 1), chunkSizes1)

        val res2 = anyDao.pagesSorted(UserODB::class, UserODB::id, KPage(10)) { not { it[UserODB::email] like "n" } }
        val chunkSizes2 = mutableListOf<Int>()
        res2.forEach { chunkSizes2.add(it.size) }
        Assert.assertEquals(listOf(10), chunkSizes2)

    }
}