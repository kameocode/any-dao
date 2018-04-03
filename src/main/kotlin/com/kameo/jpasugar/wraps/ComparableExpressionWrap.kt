package com.kameo.jpasugar.wraps

import com.kameo.jpasugar.context.PathContext
import javax.persistence.criteria.Expression

open class ComparableExpressionWrap<E : Comparable<E>, G>(pc: PathContext<G>,
                                                          value: Expression<E>) : ExpressionWrap<E, G>(pc, value) {
    infix fun before(f: E): ComparableExpressionWrap<E, G> {
        pc.add({ cb.lessThan(value, f) })
        return this
    }

    infix fun before(f: ExpressionWrap<E, G>): ComparableExpressionWrap<E, G> {
        pc.add({ cb.lessThan(value, f.getExpression()) })
        return this
    }

    infix fun after(f: E): ComparableExpressionWrap<E, G> {
        pc.add({ cb.greaterThan(value, f) })
        return this
    }

    infix fun after(f: ExpressionWrap<E, G>): ComparableExpressionWrap<E, G> {
        pc.add({ cb.greaterThan(value, f.getExpression()) })
        return this
    }

    infix fun ge(f: E): ComparableExpressionWrap<E, G> {
        pc.add({ cb.greaterThanOrEqualTo(value, f) })
        return this
    }

    infix fun ge(f: ExpressionWrap<E, G>): ComparableExpressionWrap<E, G> {
        pc.add({ cb.greaterThanOrEqualTo(value, f.getExpression()) })
        return this
    }

    infix fun gt(f: E): ComparableExpressionWrap<E, G> {
        pc.add({ cb.greaterThan(value, f) })
        return this
    }

    infix fun gt(f: ExpressionWrap<E, G>): ComparableExpressionWrap<E, G> {
        pc.add({ cb.greaterThan(value, f.getExpression()) })
        return this
    }

    infix fun lt(f: E): ComparableExpressionWrap<E, G> {
        pc.add({ cb.lessThan(value, f) })
        return this
    }

    infix fun lt(f: ExpressionWrap<E, G>): ComparableExpressionWrap<E, G> {
        pc.add({ cb.lessThan(value, f.getExpression()) })
        return this
    }

    infix fun le(f: E): ComparableExpressionWrap<E, G> {
        pc.add({ cb.lessThanOrEqualTo(value, f) })
        return this
    }

    infix fun le(f: ExpressionWrap<E, G>): ComparableExpressionWrap<E, G> {
        pc.add({ cb.lessThanOrEqualTo(value, f.getExpression()) })
        return this
    }
}