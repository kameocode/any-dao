package com.kameo.anydao.context

import com.kameo.anydao.wraps.RootWrapUpdate
import javax.persistence.EntityManager
import javax.persistence.Query
import javax.persistence.criteria.CriteriaUpdate
import javax.persistence.criteria.Root

@Suppress("UNCHECKED_CAST")
class UpdatePathContext<G>(clz: Class<*>,
                           em: EntityManager,
                           override val criteria: CriteriaUpdate<G> = em.criteriaBuilder.createCriteriaUpdate(clz) as CriteriaUpdate<G>)
    : PathContext<G>(em, criteria) {

    init {
        root = criteria.from(clz as Class<G>) as Root<Any>
        rootWrap = RootWrapUpdate(this, root)
    }

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