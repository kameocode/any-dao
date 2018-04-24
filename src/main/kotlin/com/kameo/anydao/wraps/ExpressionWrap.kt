package com.kameo.anydao.wraps

import com.kameo.anydao.IExpression
import com.kameo.anydao.ISelectExpressionProvider
import com.kameo.anydao.KSelect
import com.kameo.anydao.SelectWrap
import com.kameo.anydao.context.PathContext
import javax.persistence.criteria.CommonAbstractCriteria
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Selection
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

open class ExpressionWrap<E, G> constructor(
        val pc: PathContext<G>,
        val expression: Expression<E>
) :
        ISelectExpressionProvider<E>,
        KSelect<G>,
        IExpression<E, G> {
    val cb = pc.cb

    override fun getDirectSelection(): KSelect<E> {
        return SelectWrap(expression)
    }

    override fun getJpaExpression(): Expression<E> {
        return expression
    }

    override fun getJpaSelection(): Selection<E> {
        return pc.defaultSelection!!.getJpaSelection() as Selection<E>
    }

    fun getJpaCriteria(): CommonAbstractCriteria {
        return pc.criteria
    }

    override fun isDistinct(): Boolean {
        return pc.defaultSelection!!.isDistinct()
    }

    infix fun <F> expression(expr: Expression<F>): ExpressionWrap<F, G> {
        return ExpressionWrap(pc, expr)
    }

    infix fun <T> literal(t: T): ExpressionWrap<T, G> {
        return ExpressionWrap(pc, pc.cb.literal(t))
    }
    infix fun <T: Any> nullLiteral(t: KClass<T>): ExpressionWrap<T, G> {
        return ExpressionWrap(pc, pc.cb.nullLiteral(t.java))
    }
    fun count(): ExpressionWrap<Long, G> {
        return ExpressionWrap(pc, cb.count(expression))
    }

    fun <F : Any> function(functionName: String, clz: KClass<F>): ExpressionWrap<F, G> {
        val expr = getJpaExpression()
        val expression: Expression<F> = cb.function(functionName, clz.java, expr)
        return ExpressionWrap(pc, expression)
    }


    open infix fun predicate(predicate: ExpressionWrap<E, G>.(cb: CriteriaBuilder) -> Predicate?): KSelect<G> {
        pc.add({ predicate.invoke(this, cb) })
        return this
    }

    fun isNull(): KSelect<G> {
        pc.add({ cb.isNull(expression) })
        return this
    }

    fun isNotNull(): KSelect<G> {
        pc.add({ cb.isNotNull(expression) })
        return this
    }


    @JvmName("isNullInfix")
    @Suppress("UNUSED_PARAMETER")
    infix fun isNull(p: () -> Unit): KSelect<G> {
        pc.add({ cb.isNull(expression) })
        return this
    }

    @JvmName("isNotNullInfix")
    @Suppress("UNUSED_PARAMETER")
    infix fun isNotNull(p: () -> Unit): KSelect<G> {
        pc.add({ cb.isNotNull(expression) })
        return this
    }

    fun alias(alias: String): KSelect<G> {
        getJpaExpression().alias(alias)
        return this
    }

    infix fun eq(expr: E): KSelect<G> {
        pc.add({ cb.equal(this.expression, expr) })
        return this
    }

    infix fun eq(expr: IExpression<out E, *>): KSelect<G> {
        pc.add({ cb.equal(this.expression, expr.getJpaExpression()) })
        return this
    }

    infix fun notEq(expr: E): KSelect<G> {
        pc.add({ cb.notEqual(this.expression, expr) })
        return this
    }

    infix fun notEq(expr: IExpression<out E, *>): KSelect<G> {
        pc.add({ cb.notEqual(this.expression, expr.getJpaExpression()) })
        return this
    }

    infix fun isIn(collection: Collection<E>): KSelect<G> {
        pc.add({ expression.`in`(collection) })
        return this
    }

    infix fun isIn(expr: ExpressionWrap<out E, *>): KSelect<G> {
        pc.add({ expression.`in`(expr.expression) })
        return this
    }

    infix fun isIn(expr: SubqueryWrap<out E, *>): KSelect<G> {
        pc.add({ expression.`in`(expr.subquery) })
        return this
    }

    infix fun isNotIn(collection: Collection<E>): KSelect<G> {
        pc.add({ cb.not(expression.`in`(collection)) })
        return this
    }

    infix fun isNotIn(expr: ExpressionWrap<out E, *>): KSelect<G> {
        pc.add({ cb.not(expression.`in`(expr.expression)) })
        return this
    }

    infix fun isNotIn(expr: SubqueryWrap<out E, *>): KSelect<G> {
        pc.add({ cb.not(expression.`in`(expr.subquery)) })
        return this
    }

    infix fun exists(expr: SubqueryWrap<*, *>): KSelect<G> {
        pc.add({ cb.exists(expr.subquery) })
        return this
    }

    infix fun notExists(expr: SubqueryWrap<*, *>): KSelect<G> {
        pc.add({ cb.not(cb.exists(expr.subquery)) })
        return this
    }

    fun groupBy(vararg expr: IExpression<*, *>): KSelect<G> {
        pc.groupBy(expr)
        return this
    }

    infix fun groupBy(expr: IExpression<*, *>): KSelect<G> {
        pc.groupBy(arrayOf(expr))
        return this
    }

}