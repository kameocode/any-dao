package com.kameo.jpasugar.wraps

import com.kameo.jpasugar.context.PathContext
import com.kameo.jpasugar.unaryPlus
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.CriteriaUpdate
import javax.persistence.criteria.From
import javax.persistence.criteria.Join
import javax.persistence.criteria.JoinType
import javax.persistence.criteria.MapJoin
import javax.persistence.criteria.Root
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KFunction1

private typealias AndClause<F,G> = (JoinWrap<F, G>.(JoinWrap<F, G>) -> Unit)?
private typealias AndMapClause<KEY, VALUE, G> = (MapJoinWrap<KEY, VALUE, G>.(MapJoinWrap<KEY, VALUE, G>) -> Unit)?

@Suppress("UNCHECKED_CAST")
open class RootWrap<E, G> constructor(
        val pw: PathContext<G>,
        root: Root<E>) : PathWrap<E, G>(pw, root) {


    override fun getJpaExpression(): Root<E> {
        return expression as Root<E>
    }


    fun <F> join(sa: KProperty1<E, F?>, joinType: JoinType? = JoinType.INNER, andClause: AndClause<F,G> = null): JoinWrap<F, G> {
        val join = (root as From<Any, E>).join<E, F>(sa.name, joinType) as Join<Any, F>
        return internalJoin(join, andClause)
    }




    fun <F> join(sa: KFunction1<E, F?>, joinType: JoinType? = JoinType.INNER, andClause: AndClause<F,G> = null): JoinWrap<F, G>
            = join(+sa, joinType, andClause)


    fun <F> joinList(sa: KProperty1<E, List<F>>, joinType: JoinType = JoinType.INNER, andClause: AndClause<F,G> = null): JoinWrap<F, G> {
        val join = (root as From<Any, E>).join<E, F>(sa.name, joinType) as Join<Any, F>
        return internalJoin(join, andClause)
    }
    fun <F> joinList(sa: KFunction1<E, List<F>>, joinType: JoinType = JoinType.INNER, andClause: AndClause<F,G> = null): JoinWrap<F, G>
            = joinList(+sa, joinType, andClause)
    @JvmName("joinListNullable")
    fun <F> joinList(sa: KProperty1<E, List<F>?>, joinType: JoinType = JoinType.INNER, andClause: AndClause<F,G> = null): JoinWrap<F, G> {
        val join = (root as From<Any, E>).join<E, F>(sa.name, joinType) as Join<Any, F>
        return internalJoin(join, andClause)
    }
    @JvmName("joinListNullable")
    fun <F> joinList(sa: KFunction1<E, List<F>?>, joinType: JoinType = JoinType.INNER, andClause: AndClause<F,G> = null): JoinWrap<F, G>
            = joinList(+sa, joinType, andClause)

    fun <F> joinSet(sa: KProperty1<E, Set<F>>, joinType: JoinType = JoinType.INNER, andClause: AndClause<F,G> = null): JoinWrap<F, G> {
        val join = (root as From<Any, E>).join<E, F>(sa.name, joinType) as Join<Any, F>
        return internalJoin(join, andClause)
    }
    fun <F> joinSet(sa: KFunction1<E, Set<F>>, joinType: JoinType = JoinType.INNER, andClause: AndClause<F,G> = null)
            = joinSet(+sa, joinType, andClause)
    @JvmName("joinSetNullable")
    fun <F> joinSet(sa: KProperty1<E, Set<F>?>, joinType: JoinType = JoinType.INNER, andClause: AndClause<F,G> = null): JoinWrap<F, G> {
        val join = (root as From<Any, E>).join<E, F>(sa.name, joinType) as Join<Any, F>
        return internalJoin(join, andClause)
    }
    @JvmName("joinSetNullable")
    fun <F> joinSet(sa: KFunction1<E, Set<F>?>, joinType: JoinType = JoinType.INNER, andClause: AndClause<F,G> = null)
            = joinSet(+sa, joinType, andClause)

    fun <KEY, VALUE> joinMap(sa: KProperty1<E, Map<KEY, VALUE>>, joinType: JoinType = JoinType.INNER, andClause: AndMapClause<KEY, VALUE, G> = null): MapJoinWrap<KEY, VALUE, G> {
        val join = (root as From<Any, E>).join<E, KEY>(sa.name, joinType) as MapJoin<Any, KEY, VALUE>
        return internalJoin(join, andClause)
    }
    fun <KEY, VALUE> joinMap(sa: KFunction1<E, Map<KEY, VALUE>>, joinType: JoinType = JoinType.INNER, andClause: AndMapClause<KEY, VALUE, G> = null)
            = joinMap(+sa, joinType, andClause)

    @JvmName("joinMapNullable")
    fun <KEY, VALUE> joinMap(sa: KProperty1<E, Map<KEY, VALUE>?>, joinType: JoinType = JoinType.INNER, andClause: AndMapClause<KEY, VALUE, G> = null): MapJoinWrap<KEY, VALUE, G> {
        val join = (root as From<Any, E>).join<E, KEY>(sa.name, joinType) as MapJoin<Any, KEY, VALUE>
        return internalJoin(join, andClause)
    }
    @JvmName("joinMapNullable")
    fun <KEY, VALUE> joinMap(sa: KFunction1<E, Map<KEY, VALUE>?>, joinType: JoinType = JoinType.INNER, andClause: AndMapClause<KEY, VALUE, G> = null)
            = joinMap(+sa, joinType, andClause)


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

    private fun <KEY, VALUE> internalJoin(join: MapJoin<Any, KEY, VALUE>, andClause: AndMapClause<KEY, VALUE, G>): MapJoinWrap<KEY, VALUE, G> {
        val jw = MapJoinWrap(pw, join)
        if (andClause != null) {
            jw.newAnd()
            andClause.invoke(jw, jw)
            jw.finishClause()
        }
        return jw
    }
    private fun <F> internalJoin(join: Join<Any, F>, andClause: AndClause<F, G>): JoinWrap<F, G> {
        val jw = JoinWrap(pw, join)
        if (andClause != null) {
            jw.newAnd()
            andClause.invoke(jw, jw)
            jw.finishClause()
        }
        return jw
    }

}