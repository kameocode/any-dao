package com.kameo.anydao.wraps

import com.kameo.anydao.context.PathContext
import javax.persistence.criteria.MapJoin

class MapJoinWrap<KEY, VALUE, G> constructor(pw: PathContext<G>,
                                             override val root: MapJoin<Any, KEY, VALUE>)
    : JoinWrap<VALUE, G>(pw, root) {


    override fun getJpaExpression() = root

    fun key(): PathWrap<KEY, G> =
            PathWrap(pc, getJpaExpression().key())

    fun value(): PathWrap<VALUE, G> =
            PathWrap(pc, getJpaExpression().value())

    fun entry(): ExpressionWrap<MutableMap.MutableEntry<KEY, VALUE>, G> {
        val jpaEntry = getJpaExpression().entry()
        return ExpressionWrap(pc, jpaEntry)
    }
}

