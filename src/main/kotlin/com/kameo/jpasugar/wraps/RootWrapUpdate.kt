package com.kameo.jpasugar.wraps

import com.kameo.jpasugar.context.UpdatePathContext
import com.kameo.jpasugar.unaryPlus
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Root
import kotlin.reflect.KProperty1
import kotlin.reflect.KFunction1

class RootWrapUpdate<E, G> constructor(val pw: UpdatePathContext<G>, root: Root<E>) : PathWrap<E, G>(pw, root) {

    operator fun <F> set(sa: KProperty1<E, F>, f: F): RootWrapUpdate<E, G> {
        pw.criteria.set(get(sa).root, f)
        return this
    }
  
    operator fun <F> set(sa: KProperty1<E, F>, f: Expression<F>): RootWrapUpdate<E, G> {
        pw.criteria.set(get(sa).root, f)
        return this
    }

    operator fun <F> set(sa: KProperty1<E, F>, f: PathWrap<F, G>): RootWrapUpdate<E, G> {
        pw.criteria.set(get(sa).root, f.root)
        return this
    }

    operator fun <F> set(sa: KFunction1<E, F>, f: F): RootWrapUpdate<E, G> = set(+sa, f)
    operator fun <F> set(sa: KFunction1<E, F>, f: Expression<F>): RootWrapUpdate<E, G> = set(+sa, f)
    operator fun <F> set(sa: KFunction1<E, F>, f: PathWrap<F, G>): RootWrapUpdate<E, G> = set(+sa, f)

}