package com.kameo.jpasugar.wraps

import com.kameo.jpasugar.context.QueryPathContext
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Subquery

open class SubqueryWrap<E, G>(
        val pw: QueryPathContext<G>,
        root: Expression<E>, val subquery: Subquery<E>) : ExpressionWrap<E, G>(pw, root) {
   /* override val it: SubqueryWrap<E, G> by lazy {
        this
    }*/

}
