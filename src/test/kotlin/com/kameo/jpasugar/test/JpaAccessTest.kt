package com.kameo.jpasugar.test

import com.kameo.jpasugar.test.helpers.BaseTest
import com.kameo.jpasugar.test.helpers.TaskODB
import com.kameo.jpasugar.test.helpers.UserODB
import org.junit.Assert
import org.junit.Test
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Join
import javax.persistence.criteria.JoinType

class JpaAccessTest : BaseTest() {

    @Test
    fun `should allow access jpa criteria query`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"));
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"), allTasks = listOf(TaskODB(name = "allTask2")));
        anyDao.persist(u1, u2)

        val res = anyDao.all(UserODB::class) {
            val expr = it[UserODB::task].getExpression();
            Assert.assertNotNull(expr)
            it predicate { cb -> cb.equal(expr, u1.task) }
        }

        Assert.assertEquals(1, res.size)


        val res2 = anyDao.all(UserODB::class) {
            val expr: Expression<Long> = it[UserODB::task, TaskODB::id].getExpression();
            it predicate { _ ->
                expr.`in`(u1.task.id, u2.task.id)
            }
        }
        Assert.assertEquals(2, res2.size)


        val res3 = anyDao.all(UserODB::class) {
            val task: Join<Any, TaskODB> = it.join(UserODB::allTasks).getExpression();
            Assert.assertEquals(task.joinType, JoinType.INNER)

            it predicate { cb ->
                cb.equal(task.get<String>("name"), "allTask2")
            }
        }
        Assert.assertEquals(1, res3.size)

        val res4 = anyDao.all(UserODB::class) {
            it predicate { cb ->
                cb.equal(it.join(UserODB::allTasks).getExpression().get<String>("name"), "allTask-na")
            }
        }
        Assert.assertEquals(0, res4.size)

        val res5 = anyDao.all(UserODB::class) {
            it.join(UserODB::allTasks) {
                val expr = it.getExpression();
                it predicate { cb ->
                    cb.equal(expr.get<String>("name"), "allTask2")
                }
            }
        }
        Assert.assertEquals(1, res5.size)


/*        val res6 = anyDao.all(UserODB::class) {
            val expr: KRoot<UserODB> = it.getExpression();
            it predicate { cb ->
                //TODO access to subquery
                cb.equal(expr.get<String>("name"), "allTask2")
            }

        }
        Assert.assertEquals(1, res6.size)*/
    }

}