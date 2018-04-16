package com.kameo.jpasugar.test

import com.kameo.jpasugar.test.helpers.BaseTest
import com.kameo.jpasugar.test.helpers.JavaAddressODB
import com.kameo.jpasugar.test.helpers.JavaUserODB
import com.kameo.jpasugar.wraps.like
import com.kameo.jpasugar.wraps.or
import org.junit.Assert
import org.junit.Test

class AccessJavaModelTest : BaseTest() {

    @Test
    fun `should allow queries on java model`() {
        val u1 = JavaUserODB()
        u1.email = "email1"
        u1.address = JavaAddressODB()
        u1.address.city = "Cracow"
        val u2 = JavaUserODB()
        u2.email = "email2"
        u2.address = JavaAddressODB()
        u2.address.city = "Cracow"
        val u3 = JavaUserODB()
        u3.email = "email3"
        u3.address = JavaAddressODB()
        u3.address.city = "Warsaw"
        val u4 = JavaUserODB()
        u4.email = "email4"
        u4.address = JavaAddressODB()
        u4.address.city = "Poznan"

        anyDao.persist(u1, u2, u3, u4)
        val results = anyDao.all(JavaUserODB::class) {
            it[JavaUserODB::getAddress, JavaAddressODB::getCity] like "Cracow"
        }
        Assert.assertEquals(2, results.size)

        val results1 = anyDao.all(JavaUserODB::class) {
            get(JavaUserODB::getAddress).get(JavaAddressODB::getCity) like "Cracow"
        }
        Assert.assertEquals(2, results1.size)

        val results2 = anyDao.all(JavaUserODB::class) {
            it[JavaUserODB::getAddress, JavaAddressODB::getCity] or {
                it like "Cracow"
                it like "Warsaw"
            }
        }
        Assert.assertEquals(3, results2.size)
    }


}