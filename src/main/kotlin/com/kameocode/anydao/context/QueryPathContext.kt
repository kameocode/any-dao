package com.kameocode.anydao.context


import com.kameocode.anydao.AnyDAO
import com.kameocode.anydao.KSelect
import com.kameocode.anydao.KRoot
import com.kameocode.anydao.Quadruple
import com.kameocode.anydao.SelectWrap
import com.kameocode.anydao.TupleWrap
import com.kameocode.anydao.wraps.RootWrap
import javax.persistence.EntityManager
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Selection

@Suppress("UNCHECKED_CAST")
class QueryPathContext<G>(clz: Class<*>,
                          em: EntityManager,
                          override val criteria: CriteriaQuery<G> = em.criteriaBuilder.createQuery(clz) as CriteriaQuery<G>)
    : PathContext<G>(em, criteria) {

    private lateinit var selector: KSelect<*> // set after execution (invokeQuery)

    init {
        root = criteria.from(clz as Class<Any>)
        defaultSelection = SelectWrap(root)
        rootWrap = RootWrap(this, root)
    }

    fun <RESULT, E> invokeQuery(query: KRoot<E>.(KRoot<E>) -> KSelect<RESULT>): TypedQuery<RESULT> {
        selector = query.invoke(rootWrap as KRoot<E>, rootWrap as KRoot<E>)
        val sell = selector.getJpaSelection() as Selection<out G>
        criteria.select(sell).distinct(selector.isDistinct())
        val groupBy = getGroupBy()
        if (groupBy.isNotEmpty()) {
            criteria.groupBy(groupBy.map { it.getJpaExpression() })
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
        if (selector is com.kameocode.anydao.AnyDAO.PathArraySelect ) {
            return res;
        }
        if (selector is com.kameocode.anydao.AnyDAO.PathTupleSelect) {
            val row = res as Array<Any>
            val elementList = (selector as com.kameocode.anydao.AnyDAO.PathTupleSelect).selects.map { it.getJpaSelection() }.toMutableList()
            return  TupleWrap(row, elementList) as RESULT
        }

        if (res is Array<*>) {
            return when (res.size) {
                2 -> Pair(res[0], res[1]) as RESULT
                3 -> Triple(res[0], res[1], res[2]) as RESULT
                4 -> Quadruple(res[0], res[1], res[2], res[3]) as RESULT
                else -> throw IllegalArgumentException("More than three parameteres are not supported")
            }
        }
        return res
    }

    fun <RESULT : Any> mapToPluralsIfNeeded(res: List<RESULT>): List<RESULT> {
        if (selector is com.kameocode.anydao.AnyDAO.PathArraySelect) {
            return res;
        }
        if (selector is com.kameocode.anydao.AnyDAO.PathTupleSelect && res.isNotEmpty() && res.first() is Array<*>) {
            val rows = res as List<Array<Any>>
            val elementList = (selector as com.kameocode.anydao.AnyDAO.PathTupleSelect).selects.map { it.getJpaSelection() }.toMutableList()
            return rows.map { TupleWrap(it, elementList) as RESULT };
        }
        if (res.isNotEmpty() && /*!selector.isSingle() &&*/ res.first() is Array<*>) {
            val rows = res as List<Array<Any>>
            val row = rows.first()
            return when (row.size) {
                2 -> rows.map({ Pair(it[0], it[1]) as RESULT })
                3 -> rows.map({ Triple(it[0], it[1], it[2]) as RESULT })
                4 -> rows.map({ Quadruple(it[0], it[1], it[2], it[3]) as RESULT })
                else -> throw IllegalArgumentException("More than three parameteres are not supported")
            }
        }
        return res
    }

}