package com.kameo.jpasugar.wraps

import com.kameo.jpasugar.context.PathContext
import javax.persistence.criteria.MapJoin


class MapJoinWrap<KEY, VALUE, G> constructor(pw: PathContext<G>,
                                             root: MapJoin<Any, KEY, VALUE>)
    : JoinWrap<VALUE, G>(pw, root) {

    @Suppress("UNCHECKED_CAST")
    override fun getJpaExpression() = root as MapJoin<Any, KEY, VALUE>

    fun key(): PathWrap<KEY, G> =
            PathWrap(pc, getJpaExpression().key())

    fun value(): PathWrap<VALUE, G> =
            PathWrap(pc, getJpaExpression().value())

    //TODO entry
    //TODO on
}

