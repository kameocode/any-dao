package com.kameocode.anydao.context

import com.kameocode.anydao.IExpression
import com.kameocode.anydao.KSelect
import com.kameocode.anydao.wraps.PathWrap
import jdk.nashorn.internal.objects.NativeArray.forEach
import javax.persistence.EntityManager
import javax.persistence.criteria.CommonAbstractCriteria
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Order
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root


val OrPred: () -> Predicate? = { null }

abstract class PathContext<G>
constructor(
        val em: EntityManager,
        open val criteria: CommonAbstractCriteria) {

    val cb: CriteriaBuilder = em.criteriaBuilder
    val orders: MutableList<Order> = mutableListOf()
    private val arraysStack: MutableList<MutableList<() -> Predicate?>> = mutableListOf()
    private var currentArray: MutableList<() -> Predicate?> = mutableListOf()

    var skip: Int? = null
    var take: Int? = null

    lateinit var root: Root<Any>
        protected set
    lateinit var rootWrap: PathWrap<*, G>
        protected set
    var defaultSelection: KSelect<Any>? = null
        protected set
    private var groupByList: MutableList<IExpression<*, *>>? = null

    fun addOrder(o: Order) {
        orders.add(o)
    }


    fun add(function: () -> Predicate?) {
        currentArray.add(function)
    }

    private fun calculateOr(list: List<() -> Predicate?>): Predicate? {
        val predicates = toPredicates(list)
        return if (predicates.isNotEmpty())
            if (predicates.size == 1)
                predicates[0]
            else
                cb.or(*predicates.toTypedArray())
        else
            null
    }

    private fun calculateAnd(list: List<() -> Predicate?>): Predicate? {
        val predicates = toPredicates(list)
        return if (predicates.isNotEmpty())
            if (predicates.size == 1)
                predicates[0]
            else
                cb.and(*predicates.toTypedArray())
        else
            null
    }

    fun toPredicates(list: List<() -> Predicate?>): MutableList<Predicate> {
        val orPreds = list.find { it== OrPred }
        if (orPreds!=null) {

            val orChunks : MutableList<Predicate> = mutableListOf()
            var fi = 0;
            for ((index, item) in list.withIndex()) {
                if (item == OrPred) {
                    val prevList: List<() -> Predicate?> = list.subList(fi, index).toList();
                    val previous: Predicate? = calculateAnd(prevList)
                    if (previous!=null) {
                        orChunks.add(previous)
                    }
                    fi = index+1;
                }
            }
            if (fi<list.size) {
                val prevList: List<() -> Predicate?> = list.subList(fi, list.size).toList();
                val previous: Predicate? = calculateAnd(prevList)
                if (previous!=null) {
                    orChunks.add(previous)
                }
            }

            return mutableListOf(cb.or(*orChunks.toTypedArray()))

        }
        return list.asSequence()
                .mapNotNull { it.invoke() }
                .toMutableList()
    }


    fun stackNewArray(newArr: MutableList<() -> Predicate?>) {
        arraysStack.add(currentArray)
        currentArray = newArr
    }

    fun unstackArray() {
        currentArray = arraysStack.last()
        arraysStack.remove(currentArray)
    }


    fun getPredicate(): Predicate? {
        if (arraysStack.isNotEmpty())
            throw IllegalArgumentException("In or Or clause has not been closed")
        val predicates = toPredicates(currentArray)
        return when {
            predicates.isEmpty() -> null
            predicates.size == 1 -> predicates[0]
            else -> {
                cb.and(*predicates.toTypedArray())
            }
        }
    }

    fun groupBy(expressions: Array<out IExpression<*, *>>) {
        groupByList = expressions.toMutableList()
    }

    protected fun getGroupBy(): List<IExpression<*, *>> = groupByList ?: emptyList()


}