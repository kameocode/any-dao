package com.kameo.jpasugar.wraps

import com.kameo.jpasugar.IExpression
import com.kameo.jpasugar.IStringExpressionWrap
import com.kameo.jpasugar.context.PathContext
import javax.persistence.criteria.Expression

class StringExpressionWrap<G> constructor(
        pc: PathContext<G>,
        value: Expression<String>) : ExpressionWrap<String, G>(pc, value), IStringExpressionWrap<G> {

    override infix fun like(f: String): IExpression<String, G> {
        pc.add({ pc.cb.like(value, f) })
        return this
    }

    override infix fun like(f: Expression<String>): IExpression<String, G> {
        pc.add({ pc.cb.like(value, f) })
        return this
    }

    override infix fun like(f: ExpressionWrap<String, *>): IExpression<String, G> {
        pc.add({ pc.cb.like(value, f.value) })
        return this
    }

    override fun lower(): StringExpressionWrap<G> {
        return StringExpressionWrap(pc, pc.cb.lower(value))
    }

    override fun concat(s: String): StringExpressionWrap<G> {
        return StringExpressionWrap(pc, pc.cb.concat(value, s))
    }
    override fun concat(expr: ExpressionWrap<String, *>): StringExpressionWrap<G> {
        return StringExpressionWrap(pc, pc.cb.concat(value, expr.getJpaExpression()))
    }
}