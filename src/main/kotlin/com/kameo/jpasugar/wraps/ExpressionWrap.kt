package com.kameo.jpasugar.wraps

import com.kameo.jpasugar.IExpression
import com.kameo.jpasugar.ISelectExpressionProvider
import com.kameo.jpasugar.ISugarQuerySelect
import com.kameo.jpasugar.SelectWrap
import com.kameo.jpasugar.context.PathContext
import javax.persistence.criteria.CommonAbstractCriteria
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Selection
import kotlin.reflect.KClass

open class ExpressionWrap<E, G> constructor(
        val pc: PathContext<G>,
        val expression: Expression<E>
) :
        ISelectExpressionProvider<E>,
        ISugarQuerySelect<G>, //by pathSelect,
        IExpression<E, G> {
    val cb = pc.cb

    override fun getJpaSelection(): Selection<*> {
        return pc.defaultSelection!!.getJpaSelection()
    }

    override fun isDistinct(): Boolean {
        return pc.defaultSelection!!.isDistinct()
    }

    override fun isSingle(): Boolean {
        return pc.defaultSelection!!.isSingle()
    }

    open infix fun predicate(predicate: ExpressionWrap<E, G>.(cb: CriteriaBuilder) -> Predicate?): ExpressionWrap<E, G> {
        pc.add({ predicate.invoke(this, cb) })
        return this
    }

    fun getJpaCriteria(): CommonAbstractCriteria {
        return pc.criteria
    }

    fun alias(alias: String): ExpressionWrap<E, G> {
        getJpaExpression().alias(alias)
        return this
    }

    override infix fun eq(expr: E): ExpressionWrap<E, G> {
        pc.add({ cb.equal(this.expression, expr) })
        return this
    }

    override infix fun eq(expr: IExpression<E, *>): ExpressionWrap<E, G> {
        pc.add({ cb.equal(this.expression, expr.getJpaExpression()) })
        return this
    }

    override infix fun notEq(expr: E): ExpressionWrap<E, G> {
        pc.add({ cb.not(cb.equal(this.expression, expr)) })
        return this
    }

    override infix fun notEq(expr: IExpression<E, *>): ExpressionWrap<E, G> {
        pc.add({ cb.notEqual(this.expression, expr.getJpaExpression()) })
        return this
    }

/*    fun pred(expr:E): Expression<Boolean> {
        return pc.cb.equal(this.expression, expr)
    }*/
    /*  fun equals(expr: E): Boolean {
           pc.add({ cb.equal(expression, expr) })
           return true
       }*/


    open infix fun isIn(list: List<E>): ExpressionWrap<E, G> {
        pc.add({ expression.`in`(list) })
        return this
    }

    open infix fun isIn(expr: ExpressionWrap<E, *>): ExpressionWrap<E, G> {
        pc.add({ expression.`in`(expr.expression) })
        return this
    }

    open infix fun isIn(expr: SubqueryWrap<E, *>): ExpressionWrap<E, G> {
        pc.add({ expression.`in`(expr.subquery) })
        return this
    }

    open infix fun exists(expr: SubqueryWrap<*, *>): ExpressionWrap<E, G> {
        pc.add({ cb.exists(expr.subquery) })
        return this
    }

    open infix fun notExists(expr: SubqueryWrap<*, *>): ExpressionWrap<E, G> {
        pc.add({ cb.not(cb.exists(expr.subquery)) })
        return this
    }



    override fun getDirectSelection(): ISugarQuerySelect<E> {
        return SelectWrap(expression)
    }

    override fun getJpaExpression(): Expression<E> {
        return expression
    }

    fun groupBy(vararg expr: IExpression<*, *>): ExpressionWrap<E, G> {
        pc.groupBy(expr)
        return this
    }

    infix fun groupBy(expr: IExpression<*, *>): ExpressionWrap<E, G> {
        pc.groupBy(arrayOf(expr))
        return this
    }

    fun count(): ExpressionWrap<Long, G> {
        return ExpressionWrap(pc, cb.count(expression))
    }

    fun <F : Any> function(functionName: String, clz: KClass<F>): ExpressionWrap<F, G> {
        val expr = getJpaExpression()
        val expression: Expression<F> = cb.function(functionName, clz.java, expr)
        return ExpressionWrap(pc, expression)
    }
}