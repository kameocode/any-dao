package com.kameo.jpasugar.test

import com.kameo.jpasugar.test.helpers.BaseTest
import com.kameo.jpasugar.test.helpers.TaskODB
import com.kameo.jpasugar.test.helpers.UserODB
import com.kameo.jpasugar.wraps.JoinWrap
import com.kameo.jpasugar.wraps.SubqueryWrap
import com.kameo.jpasugar.wraps.like
import org.junit.Assert
import org.junit.Test
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Join
import javax.persistence.criteria.JoinType
import javax.persistence.criteria.Subquery

class JpaAccessTest : BaseTest() {

    @Test
    fun `should allow access jpa criteria query`() {
        val u1 = UserODB(email = "email1", task = TaskODB(name = "task1"));
        val u2 = UserODB(email = "email2", task = TaskODB(name = "task2"), allTasks = listOf(TaskODB(name = "allTask2")));
        anyDao.persist(u1, u2)

        val res = anyDao.all(UserODB::class) {
            val expr = it[UserODB::task].getJpaExpression();
            Assert.assertNotNull(expr)
            it predicate { cb -> cb.equal(expr, u1.task) }
        }

        Assert.assertEquals(1, res.size)


        val res2 = anyDao.all(UserODB::class) {
            val expr: Expression<Long> = it[UserODB::task, TaskODB::id].getJpaExpression();
            it predicate { _ ->
                expr.`in`(u1.task.id, u2.task.id)
            }
        }
        Assert.assertEquals(2, res2.size)


        val res3 = anyDao.all(UserODB::class) {
            val task: Join<*, TaskODB> = it.joinList(UserODB::allTasks).getJpaExpression();
            Assert.assertEquals(task.joinType, JoinType.INNER)

            it predicate { cb ->
                cb.equal(task.get<String>("name"), "allTask2")
            }
        }
        Assert.assertEquals(1, res3.size)

        val res4 = anyDao.all(UserODB::class) {
            it predicate { cb ->
                cb.equal(it.join(UserODB::allTasks).getJpaExpression().get<String>("name"), "allTask-na")
            }
        }
        Assert.assertEquals(0, res4.size)

        val res5 = anyDao.all(UserODB::class) {
            it.joinList(UserODB::allTasks) {
                val expr = it.getJpaExpression();
                it predicate { cb ->
                    cb.equal(expr.get<String>("name"), "allTask2")
                }
            }
        }
        Assert.assertEquals(1, res5.size)


        val res6 = anyDao.all(UserODB::class) {
            val subquery: SubqueryWrap<TaskODB, UserODB> = it.subqueryFrom(TaskODB::class) {
                it[TaskODB::name] like "task1"
            }
            it predicate { _ ->
                val jpaSubquery: Subquery<TaskODB> = subquery.getJpaExpression()
                it[UserODB::task].getJpaExpression().`in`(jpaSubquery)
            }

        }
        Assert.assertEquals("Access to subquery should work", 1, res6.size)

        val res7 = anyDao.all(UserODB::class) {
            val subquery: SubqueryWrap<TaskODB, UserODB> = it.subqueryFrom(TaskODB::class) {
                it[TaskODB::name] like "task1"
            }
            it predicate { cb ->
                val jpaSubquery = getJpaCriteria().subquery(TaskODB::class.java);
                val subqueryRoot = jpaSubquery.from(TaskODB::class.java)
                jpaSubquery.select(subqueryRoot)
                jpaSubquery.where(cb.equal(subqueryRoot.get<String>("name"), "task1"))
                it[UserODB::task].getJpaExpression().`in`(jpaSubquery);
            }

        }
        Assert.assertEquals("Access to criteria", 1, res7.size)
    }

}