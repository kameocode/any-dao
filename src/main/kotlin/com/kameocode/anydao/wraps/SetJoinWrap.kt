package com.kameocode.anydao.wraps

import com.kameocode.anydao.KSelect
import com.kameocode.anydao.context.PathContext
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Path
import javax.persistence.criteria.SetJoin

class SetJoinWrap<E, G> constructor(pw: PathContext<G>,
                                    override val root: SetJoin<Any, E>,
                                    private val basePath: Path<E>)
    : JoinWrap<E, G>(pw, root) {


    override fun getJpaExpression() = root

    fun isEmpty(): KSelect<G> {
        pc.add { pw.cb.isEmpty(basePath as Expression<Collection<*>>) }
        return this
    }

}

