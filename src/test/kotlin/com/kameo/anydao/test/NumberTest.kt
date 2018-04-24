package com.kameo.anydao.test

import com.kameo.anydao.test.helpers.BaseTest
import com.kameo.anydao.test.helpers.TaskODB
import com.kameo.anydao.test.helpers.UserODB
import com.kameo.anydao.wraps.abs
import com.kameo.anydao.wraps.avg
import com.kameo.anydao.wraps.diff
import com.kameo.anydao.wraps.max
import com.kameo.anydao.wraps.min
import com.kameo.anydao.wraps.mod
import com.kameo.anydao.wraps.neg
import com.kameo.anydao.wraps.prod
import com.kameo.anydao.wraps.quot
import com.kameo.anydao.wraps.sqrt
import com.kameo.anydao.wraps.sum
import org.junit.Assert
import org.junit.Test


class NumberTest : BaseTest() {


    @Test
    fun `should support diff method`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), counter = 1, counterNullable = 1, counterDouble = 1.0, counterDoubleNullable = 1.0)
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task3"), counter = 2, counterNullable = 2, counterDouble = 2.0, counterDoubleNullable = 2.0)
        val u3 = UserODB(email = "email2", task = TaskODB(name = "task3"), counter = 3, counterDouble = 3.0)
        anyDao.persist(u1, u2, u3)


        val res: List<Int> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counter] diff 1)
        }
        Assert.assertEquals(listOf(0, 1, 2).toList(), res.map { it }.toList())
        val res1a: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counter] diff 1.5)
        }
        Assert.assertEquals(listOf(-0.5, 0.5, 1.5).toList(), res1a.map { it }.toList())
        val res1b: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counterDouble] diff 1)
        }
        Assert.assertEquals(listOf(0.0, 1.0, 2.0).toList(), res1b.map { it }.toList())
        val res1c: List<Double> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counterDouble] diff 1.0)
        }
        Assert.assertEquals(listOf(0.0, 1.0, 2.0).toList(), res1c.map { it }.toList())


        val res2: List<Int> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counter] diff it[UserODB::counterNullable])
        }
        Assert.assertEquals(0, res2[0])
        Assert.assertEquals(0, res2[1])
        Assert.assertNull(res2[2])
        val res2a: List<Double> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counterDouble] diff it[UserODB::counterDoubleNullable])
        }
        Assert.assertEquals(0.0, res2a[0], 0.0)
        Assert.assertEquals(0.0, res2a[1], 0.0)
        Assert.assertNull(res2a[2])
        val res2b: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counterDouble] diff it[UserODB::counterNullable])
        }
        Assert.assertEquals(0.0, res2b[0])
        Assert.assertEquals(0.0, res2b[1])
        Assert.assertNull(res2b[2])
        val res2c: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counter] diff it[UserODB::counterDoubleNullable])
        }
        Assert.assertEquals(0.0, res2c[0])
        Assert.assertEquals(0.0, res2c[1])
        Assert.assertNull(res2c[2])
        val res2d: List<Double> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counterDouble] diff it[UserODB::counterDoubleNullable])
        }
        Assert.assertEquals(0.0, res2d[0], 0.0)
        Assert.assertEquals(0.0, res2d[1], 0.0)
        Assert.assertNull(res2d[2])

        val res3: List<Int> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(1 diff it[UserODB::counter])
        }
        Assert.assertEquals(listOf(0, -1, -2).toList(), res3.map { it }.toList())
        val res3a: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(1.0 diff it[UserODB::counter])
        }
        Assert.assertEquals(listOf(0.0, -1.0, -2.0).toList(), res3a.map { it }.toList())
        val res3b: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(1 diff it[UserODB::counterDouble])
        }
        Assert.assertEquals(listOf(0.0, -1.0, -2.0).toList(), res3b.map { it }.toList())
        val res3c: List<Double> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(1.0 diff it[UserODB::counterDouble])
        }
        Assert.assertEquals(listOf(0.0, -1.0, -2.0).toList(), res3c.map { it }.toList())

    }

    @Test
    fun `should support sum method`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), counter = 1, counterNullable = 1, counterDouble = 1.0, counterDoubleNullable = 1.0)
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task3"), counter = 2, counterNullable = 2, counterDouble = 2.0, counterDoubleNullable = 2.0)
        val u3 = UserODB(email = "email2", task = TaskODB(name = "task3"), counter = 3, counterDouble = 3.0)
        anyDao.persist(u1, u2, u3)


        val res: List<Int> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counter] sum 1)
        }
        Assert.assertEquals(listOf(2, 3, 4).toList(), res.map { it }.toList())
        val res1a: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counter] sum 1.0)
        }
        Assert.assertEquals(listOf(2.0, 3.0, 4.0).toList(), res1a.map { it }.toList())
        val res1b: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counterDouble] sum 1)
        }
        Assert.assertEquals(listOf(2.0, 3.0, 4.0).toList(), res1b.map { it }.toList())
        val res1c: List<Double> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counterDouble] sum 1.0)
        }
        Assert.assertEquals(listOf(2.0, 3.0, 4.0).toList(), res1c.map { it }.toList())

        val res2: List<Int> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counter] sum it[UserODB::counterNullable])
        }
        Assert.assertEquals(2, res2[0])
        Assert.assertEquals(4, res2[1])
        Assert.assertNull(res2[2])
        val res2a: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counterDouble] sum it[UserODB::counterNullable])
        }
        Assert.assertEquals(2.0, res2a[0])
        Assert.assertEquals(4.0, res2a[1])
        Assert.assertNull(res2a[2])
        val res2b: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counter] sum it[UserODB::counterDoubleNullable])
        }
        Assert.assertEquals(2.0, res2b[0])
        Assert.assertEquals(4.0, res2b[1])
        Assert.assertNull(res2b[2])
        val res2c: List<Double> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counterDouble] sum it[UserODB::counterDoubleNullable])
        }
        Assert.assertEquals(2.0, res2c[0], 0.0)
        Assert.assertEquals(4.0, res2c[1], 0.0)
        Assert.assertNull(res2c[2])


        val res3: List<Int> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(1 sum it[UserODB::counter])
        }
        Assert.assertEquals(listOf(2, 3, 4).toList(), res3.map { it }.toList())
        val res3a: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(1.0 sum it[UserODB::counter])
        }
        Assert.assertEquals(listOf(2.0, 3.0, 4.0).toList(), res3a.map { it }.toList())
        val res3b: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(1 sum it[UserODB::counterDouble])
        }
        Assert.assertEquals(listOf(2.0, 3.0, 4.0).toList(), res3b.map { it }.toList())
        val res3c: List<Double> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(1.0 sum it[UserODB::counterDouble])
        }
        Assert.assertEquals(listOf(2.0, 3.0, 4.0).toList(), res3c.map { it }.toList())

        val res4: List<Int> = anyDao.all(UserODB::class) {
            val sum = it[UserODB::counter].sum()
            it groupBy it[UserODB::task, TaskODB::name]
            it orderBy sum
            select(sum)
        }
        Assert.assertEquals(listOf(1, 5).toList(), res4.map { it }.toList())
        val res4a: List<Double> = anyDao.all(UserODB::class) {
            val sum = it[UserODB::counterDouble].sum()
            it groupBy it[UserODB::task, TaskODB::name]
            it orderBy sum
            select(sum)
        }
        Assert.assertEquals(listOf(1.0, 5.0).toList(), res4a.map { it }.toList())

    }

    @Test
    fun `should support prod method`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), counter = 1, counterNullable = 1, counterDouble = 1.0, counterDoubleNullable = 1.0)
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task3"), counter = 2, counterNullable = 2, counterDouble = 2.0, counterDoubleNullable = 2.0)
        val u3 = UserODB(email = "email2", task = TaskODB(name = "task3"), counter = 3, counterDouble = 3.0)
        anyDao.persist(u1, u2, u3)


        val res1: List<Int> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counter] prod 2)
        }
        Assert.assertEquals(listOf(2, 4, 6).toList(), res1.map { it }.toList())
        val res1a: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counter] prod 2.0)
        }
        Assert.assertEquals(listOf(2.0, 4.0, 6.0).toList(), res1a.map { it }.toList())
        val res1b: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counterDouble] prod 2)
        }
        Assert.assertEquals(listOf(2.0, 4.0, 6.0).toList(), res1b.map { it }.toList())
        val res1c: List<Double> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counterDouble] prod 2.0)
        }
        Assert.assertEquals(listOf(2.0, 4.0, 6.0).toList(), res1c.map { it }.toList())


        val res2: List<Int> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counter] prod it[UserODB::counterNullable])
        }
        Assert.assertEquals(1, res2[0])
        Assert.assertEquals(4, res2[1])
        Assert.assertNull(res2[2])
        val res2a: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counterDouble] prod it[UserODB::counterNullable])
        }
        Assert.assertEquals(1.0, res2a[0])
        Assert.assertEquals(4.0, res2a[1])
        Assert.assertNull(res2a[2])
        val res2b: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counter] prod it[UserODB::counterDoubleNullable])
        }
        Assert.assertEquals(1.0, res2b[0])
        Assert.assertEquals(4.0, res2b[1])
        Assert.assertNull(res2b[2])
        val res2c: List<Double> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counterDouble] prod it[UserODB::counterDoubleNullable])
        }
        Assert.assertEquals(1.0, res2c[0], 0.0)
        Assert.assertEquals(4.0, res2c[1], 0.0)
        Assert.assertNull(res2c[2])

        val res3: List<Int> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(2 prod it[UserODB::counter])
        }
        Assert.assertEquals(listOf(2, 4, 6).toList(), res3.map { it }.toList())
        val res3a: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(2.0 prod it[UserODB::counter])
        }
        Assert.assertEquals(listOf(2.0, 4.0, 6.0).toList(), res3a.map { it }.toList())
        val res3b: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(2 prod it[UserODB::counterDouble])
        }
        Assert.assertEquals(listOf(2.0, 4.0, 6.0).toList(), res3b.map { it }.toList())
        val res3c: List<Double> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(2.0 prod it[UserODB::counterDouble])
        }
        Assert.assertEquals(listOf(2.0, 4.0, 6.0).toList(), res3c.map { it }.toList())

    }

    @Test
    fun `should support quot method`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), counter = 1, counterNullable = 1, counterDouble = 1.0, counterDoubleNullable = 1.0)
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task3"), counter = 2, counterNullable = 2, counterDouble = 2.0, counterDoubleNullable = 2.0)
        val u3 = UserODB(email = "email2", task = TaskODB(name = "task3"), counter = 3, counterDouble = 3.0)
        anyDao.persist(u1, u2, u3)


        val res1: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counter] quot 2)
        }
        Assert.assertEquals(listOf(0, 1, 1).toList(), res1.map { it }.toList())
        val res1a: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counter] quot 2.0)
        }
        Assert.assertEquals(listOf(0.5, 1.0, 1.5).toList(), res1a.map { it }.toList())
        val res1b: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counterDouble] quot 2.0)
        }
        Assert.assertEquals(listOf(0.5, 1.0, 1.5).toList(), res1b.map { it }.toList())
        val res1c: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counterDouble] quot 2)
        }
        Assert.assertEquals(listOf(0.5, 1.0, 1.5).toList(), res1c.map { it }.toList())


        val res2: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counter] quot it[UserODB::counterNullable])
        }
        Assert.assertEquals(1, res2[0])
        Assert.assertEquals(1, res2[1])
        Assert.assertNull(res2[2])

        val res2a: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counter] quot (it[UserODB::counterNullable] prod 0.5))
        }
        Assert.assertEquals(1.0 / 0.5, res2a[0])
        Assert.assertEquals(2.0 / 1.0, res2a[1])
        Assert.assertNull(res2a[2])
        val res2b: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counterDouble] quot it[UserODB::counterDoubleNullable])
        }
        Assert.assertEquals(1.0, res2b[0])
        Assert.assertEquals(1.0, res2b[1])
        Assert.assertNull(res2b[2])


        val res3: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(2 quot it[UserODB::counter])
        }
        Assert.assertEquals(listOf(2, 1, 0).toList(), res3.map { it }.toList())

        val res3a: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(2.0 quot it[UserODB::counter])
        }
        Assert.assertEquals(listOf(2.0, 1.0, 2.0 / 3.0).toList(), res3a.map { it }.toList())

        val res3b: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(2.0 quot it[UserODB::counterDouble])
        }
        Assert.assertEquals(listOf(2.0, 1.0, 2.0 / 3.0).toList(), res3b.map { it }.toList())
        val res3c: List<Number> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(2 quot it[UserODB::counterDouble])
        }
        Assert.assertEquals(listOf(2.0, 1.0, 2.0 / 3.0).toList(), res3c.map { it }.toList())

    }

    @Test
    fun `should support mod method`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), counter = 3, counterNullable = 1, counterDouble = 3.0, counterDoubleNullable = 1.0)
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task3"), counter = 4, counterNullable = 2, counterDouble = 4.0, counterDoubleNullable = 2.0)
        val u3 = UserODB(email = "email2", task = TaskODB(name = "task3"), counter = 5, counterDouble = 5.0)
        anyDao.persist(u1, u2, u3)

        val res1: List<Int> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counter] mod 3)
        }
        Assert.assertEquals(listOf(0, 1, 2).toList(), res1.map { it }.toList())
        val res1a: List<Int> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counterDouble] mod 3)
        }
        Assert.assertEquals(listOf(0, 1, 2).toList(), res1a.map { it }.toList())

        val res2: List<Int> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(5 mod it[UserODB::counter])
        }
        Assert.assertEquals(listOf(2, 1, 0).toList(), res2.map { it }.toList())
        val res2a: List<Int> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(5 mod it[UserODB::counterDouble])
        }
        Assert.assertEquals(listOf(2, 1, 0).toList(), res2a.map { it }.toList())

        val res3: List<Int> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counter] mod it[UserODB::counterNullable])
        }
        Assert.assertEquals(0, res3[0])
        Assert.assertEquals(0, res3[1])
        Assert.assertNull(res3[2])
        val res3a: List<Int> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counterDouble] mod it[UserODB::counterDoubleNullable])
        }
        Assert.assertEquals(0, res3a[0])
        Assert.assertEquals(0, res3a[1])
        Assert.assertNull(res3a[2])
    }

    @Test
    fun `should support abs method`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), counter = -3, counterNullable = 1, counterDouble = -3.0, counterDoubleNullable = 1.0)
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task3"), counter = -4, counterNullable = 2, counterDouble = -4.0, counterDoubleNullable = 2.0)
        val u3 = UserODB(email = "email2", task = TaskODB(name = "task3"), counter = 5, counterDouble = 5.0)
        anyDao.persist(u1, u2, u3)

        val res1: List<Int> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counter].abs())
        }
        Assert.assertEquals(listOf(3, 4, 5).toList(), res1.map { it }.toList())

        val res2: List<Double> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counterDouble].abs())
        }
        Assert.assertEquals(listOf(3.0, 4.0, 5.0).toList(), res2.map { it }.toList())
    }

    @Test
    fun `should support neg method`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), counter = -3, counterNullable = 1, counterDouble = -3.0, counterDoubleNullable = 1.0)
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task3"), counter = -4, counterNullable = 2, counterDouble = -4.0, counterDoubleNullable = 2.0)
        val u3 = UserODB(email = "email2", task = TaskODB(name = "task3"), counter = 5, counterDouble = 5.0)
        anyDao.persist(u1, u2, u3)

        val res1: List<Int> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counter].neg())
        }
        Assert.assertEquals(listOf(3, 4, -5).toList(), res1.map { it }.toList())

        val res2: List<Double> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counterDouble].neg())
        }
        Assert.assertEquals(listOf(3.0, 4.0, -5.0).toList(), res2.map { it }.toList())
    }


    @Test
    fun `should support sqrt method`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), counter = 1, counterNullable = 1)
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"), counter = 2, counterNullable = 4)
        val u3 = UserODB(email = "email2", task = TaskODB(name = "task2"), counter = 3)
        anyDao.persist(u1, u2, u3)


        val res1: List<Double> = anyDao.all(UserODB::class) {
            it orderBy UserODB::id
            select(it[UserODB::counterNullable].sqrt())
        }

        Assert.assertEquals(1.0, res1[0], 0.0)
        Assert.assertEquals(2.0, res1[1], 0.0)
        Assert.assertNull(res1[2])
    }

    @Test
    fun `should support avg method`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), counter = 1, counterDoubleNullable = 1.0)
        val u2 = UserODB(email = "email1", task = TaskODB(name = "task2"), counter = 2, counterDoubleNullable = 4.0)
        val u3 = UserODB(email = "email1", task = TaskODB(name = "task2"), counter = 3)
        anyDao.persist(u1, u2, u3)


        val res1: List<Double> = anyDao.all(UserODB::class) {
            it groupBy UserODB::email
            select(it[UserODB::counter].avg())
        }
        Assert.assertEquals(listOf(2.0), res1.map { it }.toList())

        val res2: List<Double> = anyDao.all(UserODB::class) {
            it groupBy UserODB::email
            select(it[UserODB::counterDoubleNullable].avg())
        }
        Assert.assertEquals(listOf(2.5), res2.map { it }.toList())
    }

    @Test
    fun `should support min and max method`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), counter = 1, counterDoubleNullable = 1.0)
        val u2 = UserODB(email = "email1", task = TaskODB(name = "task2"), counter = 2, counterDoubleNullable = 4.0)
        val u3 = UserODB(email = "email1", task = TaskODB(name = "task2"), counter = 3)
        anyDao.persist(u1, u2, u3)
        val res1: List<Int> = anyDao.all(UserODB::class) {
            it groupBy UserODB::email
            select(it[UserODB::counter].min())
        }
        Assert.assertEquals(listOf(1), res1.map { it }.toList())

        val res2: List<Int> = anyDao.all(UserODB::class) {
            it groupBy UserODB::email
            select(it[UserODB::counter].max())
        }
        Assert.assertEquals(listOf(3), res2.map { it }.toList())
    }

}