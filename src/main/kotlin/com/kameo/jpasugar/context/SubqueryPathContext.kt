package com.kameo.jpasugar.context

import com.kameo.jpasugar.ISugarQuerySelect
import com.kameo.jpasugar.SelectWrap
import com.kameo.jpasugar.wraps.RootWrap
import com.kameo.jpasugar.wraps.SubqueryWrap
import javax.persistence.EntityManager
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Subquery

class SubqueryPathContext<G>(clz: Class<*>,
                             em: EntityManager,
                             val parentContext: QueryPathContext<G>,
                             val subquery: Subquery<G>)
    : PathContext<G>(em, parentContext.criteria) {

    var selector: ISugarQuerySelect<*>? = null // set after execution


    init {
        root = subquery.from(clz as Class<Any>)

        defaultSelection = SelectWrap(root)
        rootWrap = RootWrap(this, root)
    }

    //TODO execute from QueryPathContext
    fun <RESULT, E> invokeQuery(query: (RootWrap<E, E>) -> ISugarQuerySelect<RESULT>): SubqueryWrap<RESULT, E> {
        selector = query.invoke(rootWrap as RootWrap<E, E>)
        val sell = selector!!.getSelection()
        val ss = subquery.select(sell as Expression<G>).distinct(selector!!.isDistinct())




        subquery.where(getPredicate())
        val groupBy = getGroupBy()
        if (groupBy.isNotEmpty()) {
            subquery.groupBy(groupBy.map { it.getExpression() })
        }

        // criteria.subquery()


        // return SubqueryWrap<RESULT,E>(parentContext as QueryPathContext<E>,ss, subquery as Subquery<RESULT>) as SubqueryWrap<RESULT, E>;
        return SubqueryWrap(parentContext as QueryPathContext<E>, subquery as Expression<RESULT>, subquery as Subquery<RESULT>)
    }


}

