package com.kameo.jpasugar.wraps

import com.kameo.jpasugar.KSelect
import com.kameo.jpasugar.context.UpdatePathContext
import com.kameo.jpasugar.unaryPlus
import javax.persistence.criteria.Root
import kotlin.reflect.KFunction1
import kotlin.reflect.KProperty1

class RootWrapUpdate<E, G> constructor(val pw: UpdatePathContext<G>, root: Root<E>) : PathWrap<E, G>(pw, root) {

    operator fun <F> set(sa: KProperty1<E, F>, f: F): KSelect<G> {
        pw.criteria.set(get(sa).root, f)
        return this
    }

    operator fun <F> set(sa: KProperty1<E, F>, f: ExpressionWrap<F, G>): KSelect<G> {
        pw.criteria.set(get(sa).root, f.expression)
        return this
    }

    operator fun <F> set(sa: KFunction1<E, F>, f: F): KSelect<G> = set(+sa, f)
    operator fun <F> set(sa: KFunction1<E, F>, f: ExpressionWrap<F, G>): KSelect<G> = set(+sa, f)

}