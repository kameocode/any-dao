package com.kameocode.anydao.wraps

import com.kameocode.anydao.context.QueryPathContext
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Subquery

open class SubqueryWrap<E, G>(
        pw: QueryPathContext<G>,
        root: Expression<E>,
        val subquery: Subquery<E>) : ExpressionWrap<E, G>(pw, root) {

    override fun getJpaExpression(): Subquery<E> {
        return subquery
    }

    open fun all(): ExpressionWrap<E, G> {
        return ExpressionWrap(pc, pc.cb.all(subquery))
    }

}
