package com.kameocode.anydao.test

import com.kameocode.anydao.test.helpers.BaseTest
import com.kameocode.anydao.wraps.like
import com.kameocode.anydao.wraps.or
import org.junit.Assert
import org.junit.Test

class AccessJavaModelTest : BaseTest() {

    @Test
    fun `should allow queries on java model`() {
        val u1 = com.kameocode.anydao.test.helpers.JavaUserODB()
        u1.email = "email1"
        u1.address = com.kameocode.anydao.test.helpers.JavaAddressODB()
        u1.address.city = "Cracow"
        val u2 = com.kameocode.anydao.test.helpers.JavaUserODB()
        u2.email = "email2"
        u2.address = com.kameocode.anydao.test.helpers.JavaAddressODB()
        u2.address.city = "Cracow"
        val u3 = com.kameocode.anydao.test.helpers.JavaUserODB()
        u3.email = "email3"
        u3.address = com.kameocode.anydao.test.helpers.JavaAddressODB()
        u3.address.city = "Warsaw"
        u3.userRolesMap = HashMap()
        u3.userRolesMap.put("a",1)
        val u4 = com.kameocode.anydao.test.helpers.JavaUserODB()
        u4.email = "email4"
        u4.address = com.kameocode.anydao.test.helpers.JavaAddressODB()
        u4.address.city = "Poznan"
        u4.isValid = true
        u4.userRolesMap = HashMap()
        u4.userRolesMap.put("b",1)
        u4.rolesSet = HashSet()
        u4.rolesSet.add("A")
        u4.rolesList = mutableListOf()
        u4.rolesList.add("A")

        anyDao.persist(u1, u2, u3, u4)
        val results = anyDao.all(com.kameocode.anydao.test.helpers.JavaUserODB::class) {
            it[com.kameocode.anydao.test.helpers.JavaUserODB::getAddress, com.kameocode.anydao.test.helpers.JavaAddressODB::getCity] like "Cracow"
        }
        Assert.assertEquals(2, results.size)

        val results1 = anyDao.all(com.kameocode.anydao.test.helpers.JavaUserODB::class) {
            get(com.kameocode.anydao.test.helpers.JavaUserODB::getAddress).get(com.kameocode.anydao.test.helpers.JavaAddressODB::getCity) like "Cracow"
        }
        Assert.assertEquals(2, results1.size)

        val results2 = anyDao.all(com.kameocode.anydao.test.helpers.JavaUserODB::class) {
            it[com.kameocode.anydao.test.helpers.JavaUserODB::getAddress, com.kameocode.anydao.test.helpers.JavaAddressODB::getCity] or {
                it like "Cracow"
                it like "Warsaw"
            }
        }
        Assert.assertEquals(3, results2.size)

        val results3 = anyDao.all(com.kameocode.anydao.test.helpers.JavaUserODB::class) {
            get(com.kameocode.anydao.test.helpers.JavaUserODB::isValid) eq true
        }
        Assert.assertEquals(listOf(u4).map { it.id }.toList(), results3.map { it.id }.sorted().toList())

        val results4 = anyDao.all(com.kameocode.anydao.test.helpers.JavaUserODB::class) {
            joinMap(com.kameocode.anydao.test.helpers.JavaUserODB::getUserRolesMap) {
                it.value() eq 1
            }
        }
        Assert.assertEquals(listOf(u3, u4).map { it.id }.toSet(), results4.map { it.id }.toSet())

        val results5 = anyDao.all(com.kameocode.anydao.test.helpers.JavaUserODB::class) {
            joinCollection(com.kameocode.anydao.test.helpers.JavaUserODB::getRolesSet) {
                it like "A"
            }
        }
        Assert.assertEquals(listOf(u4).map { it.id }.toSet(), results5.map { it.id }.toSet())

        val results6 = anyDao.all(com.kameocode.anydao.test.helpers.JavaUserODB::class) {
            joinSet(com.kameocode.anydao.test.helpers.JavaUserODB::getRolesSet) {
                it like "A"
            }
        }
        Assert.assertEquals(listOf(u4).map { it.id }.toSet(), results6.map { it.id }.toSet())

        val results7 = anyDao.all(com.kameocode.anydao.test.helpers.JavaUserODB::class) {
            joinList(com.kameocode.anydao.test.helpers.JavaUserODB::getRolesList) {
                it like "A"
            }
        }
        Assert.assertEquals(listOf(u4).map { it.id }.toSet(), results7.map { it.id }.toSet())
    }

    @Test
    fun `should not allow function that is not getter`() {
        try {
            anyDao.all(com.kameocode.anydao.test.helpers.JavaUserODB::class) {
                get(com.kameocode.anydao.test.helpers.JavaUserODB::variable) eq "true"
            }
            Assert.fail("variable is not proper getter function")
        } catch (e: Throwable) {
            // do nothing
        }
     }

    @Test
    fun `should update field in java model`() {
        val u1 = com.kameocode.anydao.test.helpers.JavaUserODB()
        u1.email = "email1"
        u1.address = com.kameocode.anydao.test.helpers.JavaAddressODB()
        u1.address.city = "Cracow"
        val u2 = com.kameocode.anydao.test.helpers.JavaUserODB()
        u2.email = "email2"
        u2.address = com.kameocode.anydao.test.helpers.JavaAddressODB()
        u2.address.city = "Cracow"

        anyDao.persist(u1, u2)


        val res1 = anyDao.update(com.kameocode.anydao.test.helpers.JavaUserODB::class) {
            it.set(com.kameocode.anydao.test.helpers.JavaUserODB::getEmail, "email0")
        }

        anyDao.clear()

        Assert.assertEquals(2, res1)
        anyDao.all(com.kameocode.anydao.test.helpers.JavaUserODB::class).forEach {
            Assert.assertEquals("email0", it.email)
        }

        val res2 = anyDao.update(com.kameocode.anydao.test.helpers.JavaUserODB::class) {
            it.set(com.kameocode.anydao.test.helpers.JavaUserODB::getEmail, "email0")
        }

    }


}