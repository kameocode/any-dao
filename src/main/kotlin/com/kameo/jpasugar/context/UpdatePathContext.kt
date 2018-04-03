package com.kameo.jpasugar.context

import com.kameo.jpasugar.wraps.RootWrapUpdate
import javax.persistence.EntityManager
import javax.persistence.Query
import javax.persistence.criteria.CriteriaUpdate
import javax.persistence.criteria.Root

class UpdatePathContext<G>(clz: Class<*>,
                           em: EntityManager,
                           override val criteria: CriteriaUpdate<G> = em.criteriaBuilder.createCriteriaUpdate(clz) as CriteriaUpdate<G>)
    : PathContext<G>(em, criteria) {

    init {
        root = criteria.from(clz as Class<G>) as Root<Any>
        rootWrap = RootWrapUpdate(this, root)
    }

    @Suppress("UNCHECKED_CAST")
    fun <E> invokeUpdate(query: (RootWrapUpdate<E, E>) -> Unit): Query {
        query.invoke(rootWrap as RootWrapUpdate<E, E>)
        calculateWhere(criteria)
        return em.createQuery(criteria)
    }

    private fun calculateWhere(cq: CriteriaUpdate<*>) {
        getPredicate()?.let {
            cq.where(it)
        }
    }
}