package com.kameo.anydao.context

import com.kameo.anydao.KSelect
import com.kameo.anydao.SelectWrap
import com.kameo.anydao.wraps.RootWrap
import com.kameo.anydao.wraps.SubqueryWrap
import javax.persistence.EntityManager
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Subquery

@Suppress("UNCHECKED_CAST")
class SubqueryPathContext<G>(clz: Class<*>,
                             em: EntityManager,
                             val parentContext: QueryPathContext<G>,
                             val subquery: Subquery<G>)
    : PathContext<G>(em, parentContext.criteria) {

    lateinit var selector: KSelect<*> // set after execution


    init {
        root = subquery.from(clz as Class<Any>)

        defaultSelection = SelectWrap(root)
        rootWrap = RootWrap(this, root)
    }

    fun <RESULT, E> invokeQuery(query: (RootWrap<E, E>).(RootWrap<E, E>) -> KSelect<RESULT>): SubqueryWrap<RESULT, E> {
        selector = query.invoke(rootWrap as RootWrap<E, E>, rootWrap as RootWrap<E, E>)
        val sell = selector.getJpaSelection() as Expression<G>
        subquery.select(sell).distinct(selector.isDistinct())
        getPredicate()?.let {
            subquery.where(it)
        }
        val groupBy = getGroupBy()
        if (groupBy.isNotEmpty()) {
            subquery.groupBy(groupBy.map { it.getJpaExpression() })
        }
        return SubqueryWrap(parentContext as QueryPathContext<E>, subquery as Expression<RESULT>, subquery as Subquery<RESULT>)
    }


}

