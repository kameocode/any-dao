package com.kameo.jpasugar.wraps

import com.kameo.jpasugar.context.PathContext
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Path

open class ComparablePathWrap<E : Comparable<E>, G>(pc: PathContext<G>,
                                                    root: Path<E>) : PathWrap<E, G>(pc, root) {
    infix fun before(f: E): PathWrap<E, G> {
        pc.add({ cb.lessThan(root as Expression<E>, f) })
        return this
    }

    infix fun beforeNotNull(f: E?): PathWrap<E, G> {
        if (f != null)
            pc.add({ cb.lessThan(root as Expression<E>, f) })
        return this
    }

    //TODO tu tez dodac comparables x Expression

    infix fun after(f: E): PathWrap<E, G> {
        pc.add({ cb.greaterThan(root as Expression<E>, f) })
        return this
    }

    infix fun ge(f: E): PathWrap<E, G> {
        pc.add({ cb.greaterThanOrEqualTo(root as Expression<E>, f) })
        return this
    }

    infix fun gt(f: E): PathWrap<E, G> {
        pc.add({ cb.greaterThan(root as Expression<E>, f) })
        return this
    }

    infix fun lt(f: E): PathWrap<E, G> {
        pc.add({ cb.lessThan(root as Expression<E>, f) })
        return this
    }

    infix fun le(f: E): PathWrap<E, G> {
        pc.add({ cb.lessThanOrEqualTo(root as Expression<E>, f) })
        return this
    }
}