package com.kameo.jpasugar.wraps

import com.kameo.jpasugar.IStringExpressionWrap
import com.kameo.jpasugar.context.PathContext
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Path

class StringPathWrap<G>(pc: PathContext<G>,

                        root: Path<String>) : PathWrap<String, G>(pc, root), IStringExpressionWrap<G> {

    override fun lower(): StringExpressionWrap<G> {
        return StringExpressionWrap(pc, pc.cb.lower(root))
    }

    override infix fun like(f: String): PathWrap<String, G> {
        pc.add({ pc.cb.like(root as (Expression<String>), f) })
        return this
    }

    infix fun isNullOrContains(f: Any): PathWrap<String, G> {
        or {
            isNull()
            like("%" + f.toString() + "%")
        }
        return this
    }

    override infix fun like(f: Expression<String>): PathWrap<String, G> {

        pc.add({ pc.cb.like(root as (Expression<String>), f) })
        return this
    }

    override infix fun like(f: ExpressionWrap<String, *>): PathWrap<String, G> {

        pc.add({ pc.cb.like(root as (Expression<String>), f.value) })
        return this
    }


}