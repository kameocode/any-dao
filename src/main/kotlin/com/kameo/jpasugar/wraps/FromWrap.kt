package com.kameo.jpasugar.wraps

import com.kameo.jpasugar.KFromClause
import com.kameo.jpasugar.QueryUnit
import com.kameo.jpasugar.context.PathContext
import com.kameo.jpasugar.unaryPlus
import javax.persistence.criteria.From
import javax.persistence.criteria.JoinType
import kotlin.reflect.KFunction1
import kotlin.reflect.KProperty1


open class FromWrap<E, G> constructor(val pw: PathContext<G>,
                                      override val root: From<*, E>)
    : PathWrap<E, G>(pw, root) {
/*

    override fun getJpaExpression(): From<*, E> {
        return root
    }*/

    fun <F> join(sa: KProperty1<E, F?>,
                 joinType: JoinType = JoinType.INNER,
                 andClause: QueryUnit<FromWrap<F, G>>? = null): JoinWrap<F, G> {
        val join = root.join<E, F>(sa.name, joinType)
        val jw = JoinWrap(pw, join)
        if (andClause != null) {
            jw.newAnd()
            andClause.invoke(jw, jw)
            jw.finishClause()
        }
        return jw
    }

    fun <F> join(sa: KFunction1<E, F?>,
                 joinType: JoinType = JoinType.INNER,
                 andClause: QueryUnit<FromWrap<F, G>>? = null) =
            this.join(+sa, joinType, andClause)

    fun <F> joinList(sa: KProperty1<E, List<F>?>,
                     joinType: JoinType = JoinType.INNER,
                     andClause: QueryUnit<FromWrap<F, G>>? = null): JoinWrap<F, G> {
        val join = root.join<E, F>(sa.name, joinType)
        val jw = JoinWrap(pw, join)
        if (andClause != null) {
            jw.newAnd()
            andClause.invoke(jw, jw)
            jw.finishClause()
        }
        return jw
    }

    fun <F> joinList(sa: KFunction1<E, List<F>?>,
                     joinType: JoinType = JoinType.INNER,
                     andClause: QueryUnit<FromWrap<F, G>>? = null) =
            joinList(+sa, joinType, andClause)


    fun <F> joinSet(sa: KProperty1<E, Set<F>?>,
                    joinType: JoinType = JoinType.INNER,
                    andClause: QueryUnit<FromWrap<F, G>>? = null): JoinWrap<F, G> {
        val join = root.join<E, F>(sa.name, joinType)
        val jw = JoinWrap(pw, join)
        if (andClause != null) {
            jw.newAnd()
            andClause.invoke(jw, jw)
            jw.finishClause()
        }
        return jw
    }

    fun <F> joinSet(sa: KFunction1<E, Set<F>?>, joinType: JoinType = JoinType.INNER, andClause: QueryUnit<FromWrap<F, G>>? = null) =
            joinSet(+sa, joinType, andClause)


    fun <F> joinCollection(sa: KProperty1<E, Collection<F>?>,
                           joinType: JoinType = JoinType.INNER,
                           andClause: QueryUnit<FromWrap<F, G>>? = null): JoinWrap<F, G> {
        val join = root.join<E, F>(sa.name, joinType)
        val jw = JoinWrap(pw, join)
        if (andClause != null) {
            jw.newAnd()
            andClause.invoke(jw, jw)
            jw.finishClause()
        }
        return jw
    }

    fun <F> joinCollection(sa: KFunction1<E, Collection<F>?>,
                           joinType: JoinType = JoinType.INNER,
                           andClause: QueryUnit<FromWrap<F, G>>? = null) =
            joinCollection(+sa, joinType, andClause)

    fun <KEY, VALUE> joinMap(sa: KProperty1<E, Map<KEY, VALUE>?>,
                             joinType: JoinType = JoinType.INNER,
                             andClause: QueryUnit<MapJoinWrap<KEY, VALUE, G>>? = null)
            : MapJoinWrap<KEY, VALUE, G> {
        val join = root.joinMap<Any, KEY, VALUE>(sa.name, joinType)
        val jw = MapJoinWrap(pw, join)
        if (andClause != null) {
            jw.newAnd()
            andClause.invoke(jw, jw)
            jw.finishClause()
        }
        return jw
    }

    fun <KEY, VALUE> joinMap(sa: KFunction1<E, Map<KEY, VALUE>?>,
                             joinType: JoinType = JoinType.INNER,
                             andClause: QueryUnit<MapJoinWrap<KEY, VALUE, G>>? = null) =
            joinMap(+sa, joinType, andClause)


    @JvmName("applyFrom")
    infix fun applyClause(query: KFromClause<E>): FromWrap<E, G> {
        query.invoke(this as FromWrap<E, Any>, this as FromWrap<E, Any>)
        return this;
    }


}
