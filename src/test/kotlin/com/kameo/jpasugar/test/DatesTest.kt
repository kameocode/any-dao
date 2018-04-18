package com.kameo.jpasugar.test

import com.kameo.jpasugar.test.helpers.BaseTest
import com.kameo.jpasugar.test.helpers.TaskODB
import com.kameo.jpasugar.test.helpers.UserODB
import com.kameo.jpasugar.wraps.ExpressionWrap
import com.kameo.jpasugar.wraps.between
import com.kameo.jpasugar.wraps.greaterThan
import com.kameo.jpasugar.wraps.lessThan
import com.kameo.jpasugar.wraps.lessThanOrEqualTo
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


class DatesTest : BaseTest() {


    @Test
    fun `should search for LocalDateTime`() {
        val daysAgo5 = LocalDateTime.now().minusDays(5)
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), localDateTime = daysAgo5)
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task1"), localDateTime = LocalDateTime.now().minusDays(3))
        anyDao.persist(u1, u2)


        val res = anyDao.all(UserODB::class) {
            it[UserODB::localDateTime] greaterThan LocalDateTime.now().minusDays(4)
        }
        Assert.assertEquals(setOf(u2).map { it.id }.toSet(), res.map { it.id }.toSet())

        val res2 = anyDao.all(UserODB::class) {
            it[UserODB::localDateTime] eq daysAgo5
        }
        Assert.assertEquals(setOf(u1).map { it.id }.toSet(), res2.map { it.id }.toSet())
    }

    @Test
    fun `should search for LocalDateTime - nullable`() {
        val daysAgo5 = LocalDateTime.now().minusDays(5)
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), localDateTimeNullable = daysAgo5)
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task1"), localDateTimeNullable = LocalDateTime.now().minusDays(3))
        val u3 = UserODB(email = "email2", task = TaskODB(name = "task1"), localDateTimeNullable = null)
        anyDao.persist(u1, u2, u3)


        val res = anyDao.all(UserODB::class) {
            it[UserODB::localDateTimeNullable] greaterThan LocalDateTime.now().minusDays(4)
        }
        Assert.assertEquals(setOf(u2).map { it.id }.toSet(), res.map { it.id }.toSet())

        val res2 = anyDao.all(UserODB::class) {
            it[UserODB::localDateTimeNullable] eq daysAgo5
        }
        Assert.assertEquals(setOf(u1).map { it.id }.toSet(), res2.map { it.id }.toSet())

        val res3 = anyDao.all(UserODB::class) {
            it[UserODB::localDateTimeNullable] isNull {}
        }
        Assert.assertEquals(setOf(u3).map { it.id }.toSet(), res3.map { it.id }.toSet())
    }

    @Test
    fun `should search for LocalDate`() {
        val daysAgo5 = LocalDate.now().minusDays(5)
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), localDate = daysAgo5)
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task1"), localDate = LocalDate.now().minusDays(3))
        anyDao.persist(u1, u2)


        val res = anyDao.all(UserODB::class) {
            it[UserODB::localDate] greaterThan LocalDate.now().minusDays(4)
        }
        Assert.assertEquals(setOf(u2).map { it.id }.toSet(), res.map { it.id }.toSet())

        val res2 = anyDao.all(UserODB::class) {
            it[UserODB::localDate] eq daysAgo5
        }
        Assert.assertEquals(setOf(u1).map { it.id }.toSet(), res2.map { it.id }.toSet())
    }

    @Test
    fun `should compare two LocalDateTime fields`() {
        val daysAgo5 = LocalDateTime.now().minusDays(5)
        val daysAgo3 = LocalDateTime.now().minusDays(3)
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), localDateTimeNullable = daysAgo3, localDateTime = daysAgo5)
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task1"), localDateTimeNullable = null, localDateTime = daysAgo3)
        val u3 = UserODB(email = "email2", task = TaskODB(name = "task1"), localDateTimeNullable = null)
        anyDao.persist(u1, u2, u3)

        val res = anyDao.all(UserODB::class) {
            it[UserODB::localDateTimeNullable] greaterThan it[UserODB::localDateTime]
        }
        Assert.assertEquals(setOf(u1).map { it.id }.toSet(), res.map { it.id }.toSet())
        val res2 = anyDao.all(UserODB::class) {
            it[UserODB::localDateTime] greaterThan it[UserODB::localDateTimeNullable]
        }
        Assert.assertEquals(emptySet<Long>(), res2.map { it.id }.toSet())
    }

    @Test
    fun `should search for Date (timestamp)`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), timestamp = Date())
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task1"), timestamp = Date(1))
        anyDao.persist(u1, u2)

        val res = anyDao.all(UserODB::class) {
            it[UserODB::timestamp] lessThan Date()
        }
        Assert.assertEquals(setOf(u1, u2).map { it.id }.toSet(), res.map { it.id }.toSet())

        val res2 = anyDao.all(UserODB::class) {
            it[UserODB::timestamp] lessThanOrEqualTo Date(1)
        }
        Assert.assertEquals(setOf(u2).map { it.id }.toSet(), res2.map { it.id }.toSet())

        val res3 = anyDao.all(UserODB::class) {
            it[UserODB::timestamp] between Pair(Date(0), Date(1000))
        }
        Assert.assertEquals(setOf(u2).map { it.id }.toSet(), res3.map { it.id }.toSet())

        val res4 = anyDao.all(UserODB::class) {
            it[UserODB::timestamp] between (Date(0) to Date(1000))
        }
        Assert.assertEquals(setOf(u2).map { it.id }.toSet(), res4.map { it.id }.toSet())
    }


    @Test
    fun `should extract year`() {

        val u1 = UserODB(email = "email2", task = TaskODB(name = "task1"),
                timestamp = Date(0),
                localDateTime = LocalDateTime.of(2000, 1, 1, 20, 20),
                localDateTimeNullable = LocalDateTime.of(2001, 1, 1, 20, 20)
        )
        anyDao.persist(u1)
        val res1 = anyDao.all(UserODB::class) {
            select(it[UserODB::timestamp].function("year", Int::class))
        }

        val calendar = GregorianCalendar()
        calendar.time = u1.timestamp
        val year = calendar.get(Calendar.YEAR)

        Assert.assertEquals(setOf(year), res1.toSet())

        val res2 = anyDao.all(UserODB::class) {
            select(it[UserODB::localDateTime].function("year", Int::class))
        }
        Assert.assertEquals(setOf(2000), res2.toSet())

        val res3 = anyDao.all(UserODB::class) {
            select(it[UserODB::localDateTimeNullable].function("year", Int::class))
        }
        Assert.assertEquals(setOf(2001), res3.toSet())
    }

    @Test
    fun `should work with current timestamp`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"), timestamp = Date(0))
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task1"), timestamp = Date(System.currentTimeMillis()+10000))
        anyDao.persist(u1, u2)

        val res1 = anyDao.all(UserODB::class) {
            it[UserODB::timestamp] lessThan ExpressionWrap(pc, cb.currentDate()) as ExpressionWrap<Date, *>
        }
        Assert.assertEquals(setOf(u1).map { it.id }.toSet(), res1.map { it.id }.toSet())
    }


}