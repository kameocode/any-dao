package com.kameo.jpasugar.wraps

import com.kameo.jpasugar.context.PathContext
import com.kameo.jpasugar.unaryPlus
import javax.persistence.criteria.Join
import javax.persistence.criteria.JoinType
import javax.persistence.criteria.MapJoin
import kotlin.reflect.KFunction1
import kotlin.reflect.KProperty1


@Suppress("UNCHECKED_CAST")
open class JoinWrap<E, G> constructor(val pw: PathContext<G>,
                                      override val root: Join<Any, E>)
    : PathWrap<E, G>(pw, root) {


    override fun getJpaExpression(): Join<Any, E> {
        return expression as Join<Any, E>
    }

    fun <F> join(sa: KProperty1<E, F?>, joinType: JoinType = JoinType.INNER): JoinWrap<F, G> {
        val join = root.join<E, F>(sa.name, joinType) as Join<Any, F>
        return JoinWrap(pw, join)
    }

    fun <F> join(sa: KFunction1<E, F?>, joinType: JoinType = JoinType.INNER): JoinWrap<F, G> {
        return join(+sa, joinType)
    }

    fun <F> join(sa: KProperty1<E, F?>, joinType: JoinType = JoinType.INNER, andClause: (JoinWrap<F, G>) -> Unit): JoinWrap<F, G> {
        val join = root.join<E, F>(sa.name, joinType) as Join<Any, F>
        val jw = JoinWrap(pw, join)
        jw.newAnd();
        andClause.invoke(jw);
        jw.finishClause();
        return jw;
    }

    fun <F> join(sa: KFunction1<E, F?>, joinType: JoinType = JoinType.INNER, andClause: (JoinWrap<F, G>) -> Unit) =
            this.join(+sa, joinType, andClause)

    fun <F> joinList(sa: KProperty1<E, List<F>>, joinType: JoinType = JoinType.INNER): JoinWrap<F, G> {
        val join = root.join<E, F>(sa.name, joinType) as Join<Any, F>
        return JoinWrap(pw, join)
    }

    fun <F> joinList(sa: KFunction1<E, List<F>>, joinType: JoinType = JoinType.INNER) = joinList(+sa, joinType)


    fun <F> joinSet(sa: KProperty1<E, Set<F>>, joinType: JoinType = JoinType.INNER): JoinWrap<F, G> {
        val join = root.join<E, F>(sa.name, joinType) as Join<Any, F>
        return JoinWrap(pw, join)
    }

    fun <F> joinSet(sa: KFunction1<E, Set<F>>, joinType: JoinType = JoinType.INNER) = joinSet(+sa, joinType)


    fun <F> joinCollection(sa: KProperty1<E, Collection<F>>, joinType: JoinType = JoinType.INNER): JoinWrap<F, G> {
        val join = root.join<E, F>(sa.name, joinType) as Join<Any, F>
        return JoinWrap(pw, join)
    }

    fun <F> joinCollection(sa: KFunction1<E, Collection<F>>, joinType: JoinType = JoinType.INNER) = joinCollection(+sa, joinType)

    fun <KEY, VALUE> joinMap(sa: KProperty1<E, Map<KEY, VALUE>>,
                             joinType: JoinType = JoinType.INNER,
                             andClause: (MapJoinWrap<KEY, VALUE, G>.(MapJoinWrap<KEY, VALUE, G>) -> Unit)? = null)
            : MapJoinWrap<KEY, VALUE, G> {
        val join = root.join<E, KEY>(sa.name, joinType) as MapJoin<Any, KEY, VALUE>
        val jw = MapJoinWrap(pw, join)
        if (andClause != null) {
            jw.newAnd()
            andClause.invoke(jw, jw)
            jw.finishClause()
        }
        return jw
    }

    fun <KEY, VALUE> joinMap(sa: KFunction1<E, Map<KEY, VALUE>>,
                             joinType: JoinType = JoinType.INNER,
                             andClause: (MapJoinWrap<KEY, VALUE, G>.(MapJoinWrap<KEY, VALUE, G>) -> Unit)? = null)
            = joinMap(+sa, joinType, andClause)


    //TODO license, TODO

}
