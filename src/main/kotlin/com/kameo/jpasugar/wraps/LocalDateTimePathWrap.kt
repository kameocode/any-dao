package com.kameo.jpasugar.wraps

import com.kameo.jpasugar.context.PathContext
import java.time.LocalDateTime
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Path

class LocalDateTimePathWrap<G>(pc: PathContext<G>,
                               root: Path<LocalDateTime>) : PathWrap<LocalDateTime, G>(pc, root) {
    infix fun before(f: LocalDateTime): PathWrap<LocalDateTime, G> {
        pc.add({ cb.lessThan(root as Expression<LocalDateTime>, f) })
        return this
    }

    infix fun beforeOrEqual(f: LocalDateTime): PathWrap<LocalDateTime, G> {
        pc.add({ cb.lessThanOrEqualTo(root as Expression<LocalDateTime>, f) })
        return this
    }

    infix fun after(f: LocalDateTime): PathWrap<LocalDateTime, G> {
        pc.add({ cb.greaterThan(root as Expression<LocalDateTime>, f) })
        return this
    }

    infix fun afterOrEqual(f: LocalDateTime): PathWrap<LocalDateTime, G> {
        pc.add({ cb.greaterThanOrEqualTo(root as Expression<LocalDateTime>, f) })
        return this
    }

    infix fun lessThan(f: LocalDateTime): PathWrap<LocalDateTime, G> {
        pc.add({ cb.lessThan(root as Expression<LocalDateTime>, f) })
        return this
    }

    infix fun greaterThan(f: LocalDateTime): PathWrap<LocalDateTime, G> {
        pc.add({ cb.greaterThan(root as Expression<LocalDateTime>, f) })
        return this
    }

    infix fun lessThanOrEqualTo(f: LocalDateTime): PathWrap<LocalDateTime, G> {
        pc.add({ cb.lessThanOrEqualTo(root as Expression<LocalDateTime>, f) })
        return this
    }

    infix fun greaterThanOrEqualTo(f: LocalDateTime): PathWrap<LocalDateTime, G> {
        pc.add({ cb.greaterThanOrEqualTo(root as Expression<LocalDateTime>, f) })
        return this
    }
}