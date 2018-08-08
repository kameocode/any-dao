package com.kameocode.anydao.wraps

import com.kameocode.anydao.QueryUnit
import com.kameocode.anydao.context.PathContext
import com.kameocode.anydao.context.PredicatesExtractor
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
        val predicates = PredicatesExtractor(cb).toPredicates(list)
        if (predicates.isNotEmpty()) {
            getJpaExpression().on(*predicates.toTypedArray())
        }
        return this
    }


}
