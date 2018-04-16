package com.kameo.jpasugar.test

import com.kameo.jpasugar.Page
import com.kameo.jpasugar.test.helpers.BaseTest
import com.kameo.jpasugar.test.helpers.TaskODB
import com.kameo.jpasugar.test.helpers.UserODB
import com.kameo.jpasugar.wraps.like
import com.kameo.jpasugar.wraps.mod
import org.junit.Assert
import org.junit.Test

class PagesTest : BaseTest() {

    @Test
    fun `should return paged results`() {

        for (i in 1..19) {
            anyDao.persist(UserODB(email = "email$i", task = TaskODB(name = "task$i")))
        }

        val res1 = anyDao.page(UserODB::class, Page()) { it[UserODB::email] like "email1" }
        val res1a = anyDao.page(UserODB::class, Page()) { it }
        val res1b = anyDao.page(UserODB::class, Page().next()) { it }

        Assert.assertEquals(1, res1.size)
        Assert.assertEquals(10, res1a.size)
        Assert.assertEquals(9, res1b.size)

        val res2 = anyDao.page(UserODB::class, Page()) { it[UserODB::id].mod(3) eq 1 }
        /*val res2b = anyDao.page(UserODB::class, Page()) {
            //it[UserODB::id].mod(4) eq 1
            it.select(it[UserODB::id].mod(2))};

        res2.forEach { println("id "+it.id+" "+(it.id % 4)) }
        res2b.forEach { println("... "+it) }*/
        Assert.assertEquals(7, res2.size)


        val sup = anyDao.pages(UserODB::class, Page(3)) { it[UserODB::id].mod(3) eq 1 }
        Assert.assertEquals(3, sup.invoke().size)
        Assert.assertEquals(3, sup.invoke().size)
        Assert.assertEquals(1, sup.invoke().size)
        Assert.assertEquals(0, sup.invoke().size)


        val sup2 = anyDao.pages(UserODB::class, Page(3)) { it[UserODB::id].mod(3) eq 1 }
        val chunkSizes2 = mutableListOf<Int>()
        sup2.forEach { chunkSizes2.add(it.size) }
        Assert.assertEquals(listOf(3, 3, 1), chunkSizes2)


        val sup3 = anyDao.pages(UserODB::class, Page(3)) { it[UserODB::id].mod(3) eq 1 }
        val chunkSizes3 = mutableListOf<Int>()
        sup3.forEachFlat { chunkSizes3.add(1) }
        Assert.assertEquals(listOf(1, 1, 1, 1, 1, 1, 1), chunkSizes3)


        val sup4 = anyDao.pages(UserODB::class, Page(3)) { it[UserODB::id].mod(3) eq 1 }
        val chunkSizes4 = mutableListOf<Int>()
        sup4.forEachUntil { chunkSizes4.add(it.size); false }
        Assert.assertEquals(listOf(3), chunkSizes4)


        val sup5 = anyDao.pages(UserODB::class, Page(3)) { it[UserODB::id].mod(3) eq 1 }
        val chunkSizes5 = mutableListOf<Int>()
        sup5.forEachFlatUntil { chunkSizes5.add(1); chunkSizes5.size < 2 }
        Assert.assertEquals(listOf(1, 1), chunkSizes5)

    }
}