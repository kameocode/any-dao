package com.kameo.jpasugar.wraps

import com.kameo.jpasugar.context.PathContext
import java.time.LocalDate
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Path

class LocalDatePathWrap<G>(pc: PathContext<G>,
                           root: Path<LocalDate>) : PathWrap<LocalDate, G>(pc, root) {
    infix fun before(f: LocalDate): PathWrap<LocalDate, G> {
        pc.add({ cb.lessThan(root as Expression<LocalDate>, f) })
        return this
    }

    infix fun after(f: LocalDate): PathWrap<LocalDate, G> {
        pc.add({ cb.greaterThan(root as Expression<LocalDate>, f) })
        return this
    }

    infix fun lessThan(f: LocalDate): PathWrap<LocalDate, G> {
        pc.add({ cb.lessThan(root as Expression<LocalDate>, f) })
        return this
    }

    infix fun greaterThan(f: LocalDate): PathWrap<LocalDate, G> {

        pc.add({ cb.greaterThan(root as Expression<LocalDate>, f) })
        return this
    }

    infix fun lessThanOrEqualTo(f: LocalDate): PathWrap<LocalDate, G> {
        pc.add({ cb.lessThanOrEqualTo(root as Expression<LocalDate>, f) })
        return this
    }

    infix fun beforeOrEqual(f: LocalDate): PathWrap<LocalDate, G> {
        pc.add({ cb.lessThanOrEqualTo(root as Expression<LocalDate>, f) })
        return this
    }

    infix fun afterOrEqual(f: LocalDate): PathWrap<LocalDate, G> {
        pc.add({ cb.greaterThanOrEqualTo(root as Expression<LocalDate>, f) })
        return this
    }

    infix fun greaterThanOrEqualTo(f: LocalDate): PathWrap<LocalDate, G> {
        pc.add({ cb.greaterThanOrEqualTo(root as Expression<LocalDate>, f) })
        return this
    }

    infix fun ge(f: LocalDate): PathWrap<LocalDate, G> {
        pc.add({ cb.greaterThanOrEqualTo(root as Expression<LocalDate>, f) })
        return this
    }

}