package com.kameo.jpasugar.wraps

import com.kameo.jpasugar.context.PathContext
import javax.persistence.criteria.Join
import javax.persistence.criteria.JoinType
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

class JoinWrap<E, G> constructor(val pw: PathContext<G>,
                                 override val root: Join<Any, E>)
    : PathWrap<E, G>(pw, root) {

    override val it: JoinWrap<E, G> by lazy {
        this
    }

    @Suppress("UNCHECKED_CAST")
    fun <F> join(sa: KProperty1<E, F?>, joinType: JoinType = JoinType.INNER): JoinWrap<F, G> {
        val join = root.join<E, F>(sa.name, joinType) as Join<Any, F>
        return JoinWrap(pw, join)
    }

    @Suppress("UNCHECKED_CAST")
    fun <F> join(sa: KProperty1<E, F?>, joinType: JoinType = JoinType.INNER, andClause: (JoinWrap<F, G>) -> Unit): JoinWrap<F, G> {
        val join = root.join<E, F>(sa.name, joinType) as Join<Any, F>
        val jw = JoinWrap(pw, join)
        jw.newAnd();
        andClause.invoke(jw);
        jw.finishClause();
        return jw;
    }

    @Suppress("UNCHECKED_CAST")
            // perhaps we want to create here dedicated class
    fun <F> joinList(sa: KMutableProperty1<E, List<F>>, joinType: JoinType = JoinType.INNER): JoinWrap<F, G> {
        val join = root.join<E, F>(sa.name, joinType) as Join<Any, F>
        return JoinWrap(pw, join)
    }


}
