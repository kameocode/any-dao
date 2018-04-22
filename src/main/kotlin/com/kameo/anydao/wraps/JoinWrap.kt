package com.kameo.anydao.wraps

import com.kameo.anydao.QueryUnit
import com.kameo.anydao.context.PathContext
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Join
import javax.persistence.criteria.Predicate


open class JoinWrap<E, G> constructor(pw: PathContext<G>,
                                      override val root: Join<*, E>)
    : FromWrap<E, G>(pw, root) {

    override fun getJpaExpression(): Join<*, E> {
        return root
    }

    infix fun on(exp: Expression<Boolean>): JoinWrap<E, G> {
        getJpaExpression().on(exp)
        return this
    }

    infix fun on(exp: Predicate): JoinWrap<E, G> {
        getJpaExpression().on(exp)
        return this
    }

    infix fun on(expressionWrap: ExpressionWrap<Boolean, G>): JoinWrap<E, G> {
        getJpaExpression().on(expressionWrap.getJpaExpression())
        return this
    }

    infix fun on(onClause: QueryUnit<JoinWrap<E, G>>): JoinWrap<E, G> {
        val list = mutableListOf<() -> Predicate?>()
        pc.stackNewArray(list)
        onClause.invoke(this, this)
        pc.unstackArray()
        val predicates = toPredicates(list)
        if (predicates.isNotEmpty()) {
            getJpaExpression().on(*predicates.toTypedArray())
        }
        return this
    }


}
