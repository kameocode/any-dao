package com.kameocode.anydao.wraps

import com.kameocode.anydao.IExpression
import com.kameocode.anydao.ISelectExpressionProvider
import com.kameocode.anydao.KSelect
import com.kameocode.anydao.SelectWrap
import com.kameocode.anydao.context.PathContext
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
        KSelect<G>,
        IExpression<E, G> {
    val cb = pc.cb

    override fun getDirectSelection(): SelectWrap<E> {
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

/**
 * Number extension functions
 */
fun <G, NUM : Number, T : ExpressionWrap<NUM, G>> T.max(): ExpressionWrap<NUM, G> {
    return ExpressionWrap<NUM, G>(pc, pc.cb.max(expression))
}

fun <G, NUM : Number, T : ExpressionWrap<NUM, G>> T.min(): ExpressionWrap<NUM, G> {
    return ExpressionWrap<NUM, G>(pc, pc.cb.min(expression))
}


infix fun <G, NUM : Number, T : ExpressionWrap<out NUM, G>> T.diff(y: NUM): ExpressionWrap<NUM, G> {
    return ExpressionWrap<NUM, G>(pc, pc.cb.diff(expression, y))
}

infix fun <G, NUM : Number, T : ExpressionWrap<out NUM, G>> T.diff(y: ExpressionWrap<out NUM, *>): ExpressionWrap<NUM, G> {
    return ExpressionWrap<NUM, G>(pc, pc.cb.diff(expression, y.getJpaExpression()))
}

infix fun <G, NUM : Number, T : ExpressionWrap<NUM, G>> NUM.diff(y: ExpressionWrap<out NUM, G>): ExpressionWrap<NUM, G> {
    return ExpressionWrap<NUM, G>(y.pc, y.pc.cb.diff(this, y.getJpaExpression()))
}

fun <G, NUM : Number, T : ExpressionWrap<NUM, G>> T.sum(): ExpressionWrap<NUM, G> {
    return ExpressionWrap<NUM, G>(pc, pc.cb.sum(expression))
}

infix fun <G, NUM : Number, T : ExpressionWrap<out NUM, G>> T.sum(y: ExpressionWrap<out NUM, *>): ExpressionWrap<NUM, G> {
    return ExpressionWrap<NUM, G>(pc, pc.cb.sum(expression, y.getJpaExpression()))
}

infix fun <G, NUM : Number, T : ExpressionWrap<out NUM, G>> T.sum(y: NUM): ExpressionWrap<NUM, G> {
    return ExpressionWrap<NUM, G>(pc, pc.cb.sum(expression, y))
}

infix fun <G, NUM : Number, T : ExpressionWrap<NUM, G>> NUM.sum(y: ExpressionWrap<out NUM, G>): ExpressionWrap<NUM, G> {
    return ExpressionWrap<NUM, G>(y.pc, y.pc.cb.sum(this, y.getJpaExpression()))
}

infix fun <G, NUM : Number, T : ExpressionWrap<out NUM, G>> T.prod(y: NUM): ExpressionWrap<NUM, G> {
    return ExpressionWrap<NUM, G>(pc, pc.cb.prod(expression, y))
}

infix fun <G, NUM : Number, T : ExpressionWrap<out NUM, G>> T.prod(y: ExpressionWrap<out NUM, *>): ExpressionWrap<NUM, G> {
    return ExpressionWrap<NUM, G>(pc, pc.cb.prod(expression, y.getJpaExpression()))
}

infix fun <G, NUM : Number> NUM.prod(y: ExpressionWrap<out NUM, G>): ExpressionWrap<NUM, G> {
    return ExpressionWrap<NUM, G>(y.pc, y.pc.cb.prod(this, y.getJpaExpression()))
}


infix fun <G, NUM : Number, T : ExpressionWrap<out NUM, G>> T.quot(y: Number): ExpressionWrap<Number, G> {
    return ExpressionWrap<Number, G>(pc, pc.cb.quot(expression, y))
}

infix fun <G, NUM : Number, T : ExpressionWrap<NUM, G>> T.quot(y: ExpressionWrap<out Number, *>): ExpressionWrap<Number, G> {
    return ExpressionWrap<Number, G>(pc, pc.cb.quot(expression, y.getJpaExpression()))
}

infix fun <G, NUM : Number, T : ExpressionWrap<NUM, G>> NUM.quot(y: ExpressionWrap<out Number, G>): ExpressionWrap<Number, G> {
    return ExpressionWrap<Number, G>(y.pc, y.pc.cb.quot(this, y.getJpaExpression()))
}

infix fun <G, NUM : Number, T : ExpressionWrap<out NUM, G>> T.mod(y: Int): ExpressionWrap<Int, G> {
    return ExpressionWrap<Int, G>(pc, pc.cb.mod(expression as Expression<Int>, y))
}

infix fun <G, NUM : Number, T : ExpressionWrap<out NUM, G>> T.mod(y: ExpressionWrap<out NUM, *>): ExpressionWrap<Int, G> {
    return ExpressionWrap<Int, G>(pc, pc.cb.mod(expression as Expression<Int>, y.getJpaExpression() as Expression<Int>))
}

infix fun <G, NUM : Number, T : ExpressionWrap<NUM, G>> Int.mod(y: ExpressionWrap<NUM, G>): ExpressionWrap<Int, G> {
    return ExpressionWrap<Int, G>(y.pc, y.pc.cb.mod(this, y.getJpaExpression() as Expression<Int>))
}

fun <G, NUM : Number, T : ExpressionWrap<NUM, G>> T.sqrt(): ExpressionWrap<Double, G> {
    return ExpressionWrap<Double, G>(pc, pc.cb.sqrt(expression))
}

fun <G, NUM : Number, T : ExpressionWrap<NUM, G>> T.abs(): ExpressionWrap<NUM, G> {
    return ExpressionWrap(pc, pc.cb.abs(expression))
}

fun <G, NUM : Number, T : ExpressionWrap<NUM, G>> T.neg(): ExpressionWrap<NUM, G> {
    return ExpressionWrap(pc, pc.cb.neg(expression))
}

fun <G, NUM : Number, T : ExpressionWrap<NUM, G>> T.avg(): ExpressionWrap<Double, G> {
    return ExpressionWrap(pc, pc.cb.avg(expression))
}


infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<out NUM, G>> T.gt(f: NUM): KSelect<G> {
    return greaterThan(f)
}

infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<out NUM, G>> T.gt(f: ExpressionWrap<out NUM, *>): KSelect<G> {
    return greaterThan(f)
}

infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<out NUM, G>> T.ge(f: NUM): KSelect<G> {
    return greaterThanOrEqualTo(f)
}

infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<out NUM, G>> T.ge(f: ExpressionWrap<out NUM, *>): KSelect<G> {
    return greaterThanOrEqualTo(f)
}

infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<out NUM, G>> T.lt(f: NUM): KSelect<G> {
    return lessThan(f)
}

infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<out NUM, G>> T.lt(f: ExpressionWrap<out NUM, *>): KSelect<G> {
    return lessThan(f)
}

infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<out NUM, G>> T.le(f: NUM): KSelect<G> {
    return lessThanOrEqualTo(f)
}


infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<out NUM, G>> T.le(f: ExpressionWrap<out NUM, *>): KSelect<G> {
    return lessThanOrEqualTo(f)
}

infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<out NUM, G>> T.greaterThan(f: NUM): KSelect<G> {
    pc.add({ cb.greaterThan(expression, f) })
    return this
}

infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<out NUM, G>> T.greaterThan(f: ExpressionWrap<out NUM, *>): KSelect<G> {
    pc.add({ cb.greaterThan(expression, f.getJpaExpression()) })
    return this
}

infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<out NUM, G>> T.greaterThanOrEqualTo(f: NUM): KSelect<G> {
    pc.add({ cb.greaterThanOrEqualTo(expression, f) })
    return this
}


infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<out NUM, G>> T.greaterThanOrEqualTo(f: ExpressionWrap<out NUM, *>): KSelect<G> {
    pc.add({ cb.greaterThanOrEqualTo(expression, f.getJpaExpression()) })
    return this
}

infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<out NUM, G>> T.lessThan(f: NUM): KSelect<G> {
    pc.add({ cb.lessThan(expression, f) })
    return this
}

infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<out NUM, G>> T.lessThan(f: ExpressionWrap<out NUM, *>): KSelect<G> {
    pc.add({ cb.lessThan(expression, f.getJpaExpression()) })
    return this
}

infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<out NUM, G>> T.lessThanOrEqualTo(f: NUM): KSelect<G> {
    pc.add({ cb.lessThanOrEqualTo(expression, f) })
    return this
}

infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<out NUM, G>> T.lessThanOrEqualTo(f: ExpressionWrap<out NUM, *>): KSelect<G> {
    pc.add({ cb.lessThanOrEqualTo(expression, f.getJpaExpression()) })
    return this
}

infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<out NUM, G>> T.between(pair: Pair<NUM, NUM>): KSelect<G> {
    pc.add({ cb.between(expression, pair.first, pair.second) })
    return this
}

@JvmName("betweenExpression")
infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<out NUM, G>> T.between(pair: Pair<ExpressionWrap<out NUM, *>, ExpressionWrap<out NUM, *>>): KSelect<G> {
    pc.add({ cb.between(expression, pair.first.getJpaExpression(), pair.second.getJpaExpression()) })
    return this
}


infix fun <G, T : ExpressionWrap<String, G>> T.like(f: String): KSelect<G> {
    pc.add({ pc.cb.like(expression, f) })
    return this
}

infix fun <G, T : ExpressionWrap<String, G>> T.like(f: ExpressionWrap<String, *>): KSelect<G> {
    pc.add({ pc.cb.like(expression, f.getJpaExpression()) })
    return this
}

fun <G, T : ExpressionWrap<String, G>> T.lower(): ExpressionWrap<String, G> {
    return ExpressionWrap(pc, pc.cb.lower(expression))
}

fun <G, T : ExpressionWrap<String, G>> T.upper(): ExpressionWrap<String, G> {
    return ExpressionWrap(pc, pc.cb.upper(expression))
}

fun <G, T : ExpressionWrap<String, G>> T.length(): ExpressionWrap<Int, G> {
    return ExpressionWrap(pc, pc.cb.length(expression))
}

fun <G, T : ExpressionWrap<String, G>> T.substring(from: Int): ExpressionWrap<String, G> {
    return ExpressionWrap(pc, pc.cb.substring(expression, from))
}

fun <G, T : ExpressionWrap<String, G>> T.substring(from: Int, len: Int): ExpressionWrap<String, G> {
    return ExpressionWrap(pc, pc.cb.substring(expression, from, len))
}

fun <G, T : ExpressionWrap<String, G>> T.substring(from: ExpressionWrap<Int, *>): ExpressionWrap<String, G> {
    return ExpressionWrap(pc, pc.cb.substring(expression, from.getJpaExpression()))
}

fun <G, T : ExpressionWrap<String, G>> T.substring(from: ExpressionWrap<Int, *>, len: ExpressionWrap<Int, *>): ExpressionWrap<String, G> {
    return ExpressionWrap(pc, pc.cb.substring(expression, from.getJpaExpression(), len.getJpaExpression()))
}

fun <G, T : ExpressionWrap<String, G>> T.trim(): ExpressionWrap<String, G> {
    return ExpressionWrap(pc, pc.cb.trim(expression))
}

fun <G, T : ExpressionWrap<String, G>> T.trim(t: Char): ExpressionWrap<String, G> {
    return ExpressionWrap(pc, pc.cb.trim(t, expression))
}

fun <G, T : ExpressionWrap<String, G>> T.trim(ts: CriteriaBuilder.Trimspec, t: Char): ExpressionWrap<String, G> {
    return ExpressionWrap(pc, pc.cb.trim(ts, t, expression))
}

fun <G, T : ExpressionWrap<String, G>> T.locate(pattern: String): ExpressionWrap<Int, G> {
    return ExpressionWrap(pc, pc.cb.locate(expression, pattern))
}

infix fun <G, T : PathWrap<String, G>> T.contains(string: String): KSelect<G> {
    like("%$string%")
    return this
}

infix fun <G, T : PathWrap<String, G>> T.isNullOrContains(string: String): KSelect<G> {
    or {
        isNull()
        it like ("%$string%")
    }
    return this
}

fun <G, T : ExpressionWrap<String, G>> T.concat(s: String): ExpressionWrap<String, G> {
    return ExpressionWrap(pc, pc.cb.concat(expression, s))
}

fun <G, T : ExpressionWrap<String, G>> T.concat(expr: ExpressionWrap<String, *>): ExpressionWrap<String, G> {
    return ExpressionWrap(pc, pc.cb.concat(expression, expr.getJpaExpression()))
}


fun <G, T : ExpressionWrap<Boolean, G>> T.isTrue(): KSelect<G> {
    pc.add({ pc.cb.isTrue(expression) })
    return this
}

infix fun <G, H, COL : Collection<H>, T : ExpressionWrap<COL, G>> T.isMember(elem: H): KSelect<G> {
    pc.add({ pc.cb.isMember(elem, expression) })
    return this
}

infix fun <G, H, COL : Collection<H>, T : ExpressionWrap<COL, G>> T.isMember(elem: ExpressionWrap<H, *>): KSelect<G> {
    pc.add({ pc.cb.isMember(elem.getJpaExpression(), expression) })
    return this
}

infix fun <G, H, COL : Collection<H>, T : ExpressionWrap<COL, G>> T.isNotMember(elem: H): KSelect<G> {
    pc.add({ pc.cb.isNotMember(elem, expression) })
    return this
}

infix fun <G, H, COL : Collection<H>, T : ExpressionWrap<COL, G>> T.isNotMember(elem: ExpressionWrap<H, *>): KSelect<G> {
    pc.add({ pc.cb.isNotMember(elem.getJpaExpression(), expression) })
    return this
}


fun <G, H, COL : Collection<H>, T : ExpressionWrap<COL, G>> T.isEmpty(): KSelect<G> {
    pc.add({ pc.cb.isEmpty(expression) })
    return this
}
fun <G, H, COL : Collection<H>, T : ExpressionWrap<COL, G>> T.isNotEmpty(): KSelect<G> {
    pc.add({ pc.cb.isNotEmpty(expression) })
    return this
}
fun <G, H, COL : Collection<H>, T : ExpressionWrap<COL, G>> T.size(): ExpressionWrap<Int, G> {
    return ExpressionWrap(pc, pc.cb.size(expression))
}
