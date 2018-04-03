package com.kameo.jpasugar.context


import com.kameo.jpasugar.ISugarQuerySelect
import com.kameo.jpasugar.Root
import com.kameo.jpasugar.SelectWrap
import com.kameo.jpasugar.wraps.RootWrap
import javax.persistence.EntityManager
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Selection

@Suppress("UNCHECKED_CAST")
class QueryPathContext<G>(clz: Class<*>,
                          em: EntityManager,
                          override val criteria: CriteriaQuery<G> = em.criteriaBuilder.createQuery(clz) as CriteriaQuery<G>)
    : PathContext<G>(em, criteria) {

    var selector: ISugarQuerySelect<*>? = null // set after execution


    init {
        root = criteria.from(clz as Class<Any>)
        defaultSelection = SelectWrap(root)
        rootWrap = RootWrap(this, root)
    }


    fun <RESULT, E> invokeQuery(query: Root<E>.(Root<E>) -> ISugarQuerySelect<RESULT>): TypedQuery<RESULT> {
        selector = query.invoke(rootWrap as Root<E>, rootWrap as Root<E>)
        val sell = selector!!.getSelection()
        criteria.select(sell as Selection<out G>).distinct(selector!!.isDistinct())

        val groupBy = getGroupBy()
        if (groupBy.isNotEmpty()) {
            criteria.groupBy(groupBy.map { it.getExpression() })
        }
        return calculateWhere(em) as TypedQuery<RESULT>
    }


    private fun calculateWhere(em: EntityManager): TypedQuery<*> {
        getPredicate()?.let {
            criteria.where(it)
        }
        if (orders.isNotEmpty())
            criteria.orderBy(orders)
        val jpaQuery = em.createQuery(criteria)
        applyPage(jpaQuery)
        return jpaQuery
    }

    private fun applyPage(jpaQuery: TypedQuery<*>) {
        val skip = skip
        if (skip != null)
            jpaQuery.firstResult = skip
        val take = take
        if (take != null)
            jpaQuery.maxResults = take
    }

    fun <RESULT : Any> mapToPluralsIfNeeded(res: RESULT): RESULT {
        if (res is Array<*>) {

            if (res.size == 2) {
                return Pair(res[0], res[1]) as RESULT

            } else if (res.size == 3) {
                return Triple(res[0], res[1], res[2]) as RESULT

            }
        }
        return res;
    }

    fun <RESULT : Any> mapToPluralsIfNeeded(res: List<RESULT>): List<RESULT> {
        if (res.isNotEmpty()) {
            if (!selector!!.isSingle()) {
                if (res.first() is Array<*>) {

                    val rows = res as List<Array<Any>>
                    val row = rows.first()
                    if (row.size == 2) {
                        return rows.map({
                            Pair(it[0], it[1]) as RESULT
                        })
                    } else if (row.size == 3) {
                        return rows.map({
                            Triple(it[0], it[1], it[2]) as RESULT
                        })
                    }
                }
            }
        }
        return res
    }

}