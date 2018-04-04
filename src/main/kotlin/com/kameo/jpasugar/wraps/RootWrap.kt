package com.kameo.jpasugar.wraps

import com.kameo.jpasugar.context.PathContext
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.CriteriaUpdate
import javax.persistence.criteria.From
import javax.persistence.criteria.Join
import javax.persistence.criteria.JoinType
import javax.persistence.criteria.Root
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

open class RootWrap<E, G> constructor(
        val pw: PathContext<G>,
        root: Root<E>) : PathWrap<E, G>(pw, root) {

  /*  override val it: RootWrap<E, G> by lazy {
        this
    }*/

    override fun getExpression(): Root<E> {
        return value as Root<E>
    }

    @Suppress("UNCHECKED_CAST")
    fun <F> join(sa: KProperty1<E, F?>, joinType: JoinType? = JoinType.INNER): JoinWrap<F, G> {
        val join = (root as From<Any, E>).join<E, F>(sa.name, joinType) as Join<Any, F>
        return JoinWrap(pw, join)
    }

    @Suppress("UNCHECKED_CAST")
    fun <F> join(sa: KProperty1<E, F?>, joinType: JoinType = JoinType.INNER, andClause: JoinWrap<F, G>.(JoinWrap<F, G>) -> Unit): JoinWrap<F, G> {
        val join = (root as From<Any, E>).join<E, F>(sa.name, joinType) as Join<Any, F>
        val jw = JoinWrap(pw, join)
        jw.newAnd();
        andClause.invoke(jw, jw);
        jw.finishClause();
        return jw;
    }

    @JvmName("joinList")
    @Suppress("UNCHECKED_CAST")
    fun <F> join(sa: KProperty1<E, List<F>>, joinType: JoinType = JoinType.INNER): JoinWrap<F, G> {
        val join = (root as From<Any, E>).join<E, F>(sa.name, joinType) as Join<Any, F>
        return JoinWrap(pw, join)
    }

    @Suppress("UNCHECKED_CAST")
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