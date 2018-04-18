package com.kameo.jpasugar.wraps

import com.kameo.jpasugar.context.PathContext
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.CriteriaUpdate
import javax.persistence.criteria.Root
import kotlin.reflect.KClass


open class RootWrap<E, G> constructor(pw: PathContext<G>,
                                      override val root: Root<E>)
    : FromWrap<E, G>(pw, root) {

    override fun getJpaExpression(): Root<E> {
        return root
    }


    fun <F : Any> from(sa: KClass<F>): RootWrap<F, G> {
        val criteriaQuery = pw.criteria as? CriteriaQuery<F>
        if (criteriaQuery != null) {
            val from = criteriaQuery.from(sa.java)
            return RootWrap(pw, from)
        } else {
            val criteriaUpdateQuery = pw.criteria as? CriteriaUpdate<F>
            if (criteriaUpdateQuery != null) {
                val from = criteriaUpdateQuery.from(sa.java)
                return RootWrap(pw, from)
            }
        }
        throw IllegalArgumentException("Clause 'from' is supported only for CriteriaQuery and CriteriaUpdate")

    }


}