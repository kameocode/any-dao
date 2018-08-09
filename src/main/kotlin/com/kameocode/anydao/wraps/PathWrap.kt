package com.kameocode.anydao.wraps

import com.kameocode.anydao.ISelectExpressionProvider
import com.kameocode.anydao.KClause
import com.kameocode.anydao.KRoot
import com.kameocode.anydao.KSelect
import com.kameocode.anydao.SelectWrap
import com.kameocode.anydao.context.OrPred
import com.kameocode.anydao.context.PathContext
import com.kameocode.anydao.context.PredicatesExtractor
import com.kameocode.anydao.context.QueryPathContext
import com.kameocode.anydao.context.SubqueryPathContext
import com.kameocode.anydao.unaryPlus
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Subquery
import javax.persistence.metamodel.SingularAttribute
import kotlin.reflect.KClass
import kotlin.reflect.KFunction1
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

@Suppress("UNCHECKED_CAST", "UNUSED")
open class PathWrap<E, G> constructor(
        pc: PathContext<G>,
        open val root: Path<E>
) : ExpressionWrap<E, G>(pc, root) {

    override fun getDirectSelection(): SelectWrap<E> {
        return SelectWrap(root)
    }

    infix fun skip(skip: Int): PathWrap<E, G> {
        pc.skip = skip
        return this
    }

    infix fun limit(take: Int): PathWrap<E, G> {
        pc.take = take
        return this
    }

    infix fun <F> select(pw: KProperty1<E, F>): SelectWrap<F> {
        return select(get(pw))
    }

    infix fun <F> select(pw: KMutableProperty1<E, F>): SelectWrap<F> {
        return select(get(pw))
    }

    infix fun <F> select(pw: ExpressionWrap<F, G>): SelectWrap<F> {
        return pw.getDirectSelection()
    }


    infix fun <F> selectNullable(pw: ExpressionWrap<F, G>): SelectWrap<F?> {
        return pw.getDirectSelection() as SelectWrap<F?>
    }

    fun <F, G> select(pw1: ISelectExpressionProvider<F>, pw2: ISelectExpressionProvider<G>): com.kameocode.anydao.AnyDao.PathPairSelect<F, G> {
        return com.kameocode.anydao.AnyDao.PathPairSelect(pw1.getDirectSelection(), pw2.getDirectSelection(), false, pc.cb)
    }

    fun <F, G, H> select(pw1: ISelectExpressionProvider<F>, pw2: ISelectExpressionProvider<G>, pw3: ISelectExpressionProvider<H>): com.kameocode.anydao.AnyDao.PathTripleSelect<F, G, H> {
        return com.kameocode.anydao.AnyDao.PathTripleSelect(pw1.getDirectSelection(), pw2.getDirectSelection(), pw3.getDirectSelection(), false, pc.cb)
    }

    fun <F, G, H, I> select(pw1: ISelectExpressionProvider<F>, pw2: ISelectExpressionProvider<G>,
                            pw3: ISelectExpressionProvider<H>,
                            pw4: ISelectExpressionProvider<I>): com.kameocode.anydao.AnyDao.PathQuadrupleSelect<F, G, H, I> {
        return com.kameocode.anydao.AnyDao.PathQuadrupleSelect(pw1.getDirectSelection(),
                pw2.getDirectSelection(),
                pw3.getDirectSelection(),
                pw4.getDirectSelection(),
                false, cb)
    }

    fun selectArray(vararg pw1: ISelectExpressionProvider<*>,
                    distinct: Boolean = false): com.kameocode.anydao.AnyDao.PathArraySelect {
        return com.kameocode.anydao.AnyDao.PathArraySelect(distinct, pc.cb, *pw1.map { it.getDirectSelection() }.toTypedArray())
    }

    fun selectTuple(vararg pw1: ISelectExpressionProvider<*>,
                    distinct: Boolean = false): com.kameocode.anydao.AnyDao.PathTupleSelect {
        return com.kameocode.anydao.AnyDao.PathTupleSelect(distinct, pc.cb, *pw1.map { it.getDirectSelection() }.toTypedArray())
    }

    fun <F : Any> select(clz: KClass<F>, vararg expr: ExpressionWrap<*, *>, distinct: Boolean = false): com.kameocode.anydao.AnyDao.PathObjectSelect<F> {
        return com.kameocode.anydao.AnyDao.PathObjectSelect(clz, distinct, pc.cb, *expr)
    }

    infix fun eqId(id: Long): KSelect<G> {
        pc.add({ cb.equal(root.get<Path<Long>>("id"), id) })
        return this
    }

    infix fun inIds(ids: Collection<Long>): KSelect<G> {
        pc.add({ root.get<Path<Long>>("id").`in`(ids) })
        return this
    }

    fun inIds(vararg ids: Long): KSelect<G> {
        pc.add({ root.get<Path<Long>>("id").`in`(ids) })
        return this
    }


    class ClosureWrap<E, G>(
            pc: PathContext<G>,
            root: Path<E>
    ) : PathWrap<E, G>(pc, root)

    fun finishClause(): PathWrap<E, G> {
        pc.unstackArray()
        return this
    }

    internal fun internalNot(notClause: PathWrap<E, G>.(PathWrap<E, G>) -> Unit): PathWrap<E, G> {
        return newClause(notClause, this::calculateNot)
    }

    internal fun internalOr(orClause: PathWrap<E, G>.(PathWrap<E, G>) -> Unit): PathWrap<E, G> {
        return newClause(orClause, this::calculateOr)
    }

    internal fun internalAnd(andClause: PathWrap<E, G>.(PathWrap<E, G>) -> Unit): PathWrap<E, G> {
        return newClause(andClause, this::calculateAnd)
    }

    private fun newClause(orClause: PathWrap<E, G>.(PathWrap<E, G>) -> Unit, toPredicate: (list: MutableList<() -> Predicate?>) -> Predicate?): PathWrap<E, G> {
        val list = mutableListOf<() -> Predicate?>()
        pc.add({ toPredicate.invoke(list) })
        pc.stackNewArray(list)
        orClause.invoke(this, this)
        pc.unstackArray()
        return this
    }

    fun newAnd(): ClosureWrap<E, G> {
        val list = mutableListOf<() -> Predicate?>()
        val pw = ClosureWrap(pc, root)
        pc.add({ calculateAnd(list) })
        pc.stackNewArray(list)
        return pw
    }

    private fun calculateOr(list: MutableList<() -> Predicate?>): Predicate? {
        val predicates = toPredicates(list)
        return if (predicates.isNotEmpty())
            cb.or(*predicates.toTypedArray())
        else
            null
    }

    private fun calculateNot(list: MutableList<() -> Predicate?>): Predicate? {
        val predicates = toPredicates(list)
        return if (predicates.isNotEmpty() && predicates.size == 1)
            cb.not(predicates[0])
        else if (predicates.isNotEmpty())
            cb.not(calculateAnd(list))
        else
            null
    }

    private fun calculateAnd(list: MutableList<() -> Predicate?>): Predicate? {
        val predicates = toPredicates(list)
        return if (predicates.isNotEmpty())
            cb.and(*predicates.toTypedArray())
        else
            null
    }

    protected fun toPredicates(list: MutableList<() -> Predicate?>): MutableList<Predicate> {
        return PredicatesExtractor(cb).toPredicates(list)
    }

    fun <J : Any> isIn(clz: KClass<J>, subqueryQuery: KRoot<J>.(KRoot<J>) -> (KSelect<E>)): PathWrap<E, G> {
        newAnd()
        isIn(subqueryFrom(clz, subqueryQuery))
        finishClause()
        return this
    }

    infix fun orderBy(sa: KProperty1<E, *>): KSelect<G> {
        return this.orderByAsc(sa)
    }

    infix fun orderByAsc(sa: KProperty1<E, *>): KSelect<G> {
        pc.addOrder(cb.asc(root.get<Any>(sa.name)))
        return this
    }

    infix fun orderByDesc(sa: KProperty1<E, *>): KSelect<G> {
        pc.addOrder(cb.desc(root.get<Any>(sa.name)))
        return this
    }

    infix fun orderBy(pathWrap: ExpressionWrap<*, *>): KSelect<G> {
        pc.addOrder(cb.asc(pathWrap.expression))
        return this
    }

    infix fun orderBy(pw: Pair<PathWrap<*, *>, Boolean>): KSelect<G> {
        val (pathWrap, asc) = pw
        pc.addOrder(if (asc) cb.asc(pathWrap.root) else cb.desc(pathWrap.root))
        return this
    }

    fun orderBy(vararg pw: Pair<PathWrap<*, *>, Boolean>): KSelect<G> {
        for ((pathWrap, asc) in pw) {
            pc.addOrder(if (asc) cb.asc(pathWrap.root) else cb.desc(pathWrap.root))
        }
        return this
    }

    fun <E : Any, RESULT> subqueryFrom(clz: KClass<E>, query: RootWrap<E, E>.(RootWrap<E, E>) -> (KSelect<RESULT>)): SubqueryWrap<RESULT, G> {
        val criteriaQuery = pc.criteria as CriteriaQuery<E>
        val subqueryPc = SubqueryPathContext(clz.java, pc.em, pc, criteriaQuery.subquery(clz.java) as Subquery<G>)
        val returnedExpression = subqueryPc.invokeQuery(query)

        return returnedExpression as SubqueryWrap<RESULT, G>
    }


    fun <J : Any> exists(clz: KClass<J>, subqueryQuery: KRoot<J>.(KRoot<J>) -> (KSelect<E>)): KSelect<G> {
        newAnd()
        exists(subqueryFrom(clz, subqueryQuery))
        finishClause()
        return this
    }

    fun <J : Any> notExists(clz: KClass<J>, subqueryQuery: KRoot<J>.(KRoot<J>) -> (KSelect<E>)): PathWrap<E, G> {
        newAnd()
        notExists(subqueryFrom(clz, subqueryQuery))
        finishClause()
        return this
    }


    infix fun orderBy(sa: KFunction1<E, *>): KSelect<G> = orderBy(+sa)
    infix fun orderByAsc(sa: KFunction1<E, *>): KSelect<G> = orderByAsc(+sa)
    infix fun orderByDesc(sa: KFunction1<E, *>): KSelect<G> = orderByDesc(+sa)


    infix operator fun <F> get(sa: SingularAttribute<E, F>): PathWrap<F, G> = PathWrap<F, G>(pc, root.get(sa))

    @JvmName("getNullable")
    infix operator fun <F> get(sa: KProperty1<E, F?>): PathWrap<F, G> = PathWrap(pc, root.get(sa.name))


    @JvmName("getNullable")
    infix operator fun <F> get(sa: KFunction1<E, F?>): PathWrap<F, G> = get(+sa)


    operator fun <F, F2> get(sa: KProperty1<E, F?>, sa2: KProperty1<F, F2?>): PathWrap<F2, G> {
        val p: Path<F> = root.get(sa.name)
        return PathWrap(pc, p.get(sa2.name))
    }

    operator fun <F, F2> get(sa: KFunction1<E, F?>, sa2: KFunction1<F, F2?>): PathWrap<F2, G> =
            get(+sa, +sa2)

    operator fun <F, F2, F3> get(sa: KProperty1<E, F?>, sa2: KProperty1<F, F2?>, sa3: KProperty1<F2, F3?>): PathWrap<F3, G> {
        val p: Path<F2> = root.get(sa.name)
        val p2: Path<F3> = p.get(sa2.name)
        return PathWrap(pc, p2.get(sa3.name))
    }

    operator fun <F, F2, F3> get(sa: KFunction1<E, F?>, sa2: KFunction1<F, F2?>, sa3: KFunction1<F2, F3?>): PathWrap<F3, G> =
            get(+sa, +sa2, +sa3)

    operator fun <F, F2, F3, F4> get(sa: KProperty1<E, F?>, sa2: KProperty1<F, F2?>, sa3: KProperty1<F2, F3?>, sa4: KProperty1<F3, F4?>): PathWrap<F4, G> {
        val p: Path<F> = root.get(sa.name)
        val p2: Path<F2> = p.get(sa2.name)
        val p3: Path<F3> = p2.get(sa3.name)
        return PathWrap(pc, p3.get(sa4.name))
    }

    operator fun <F, F2, F3, F4> get(sa: KFunction1<E, F?>, sa2: KFunction1<F, F2?>, sa3: KFunction1<F2, F3?>, sa4: KFunction1<F3, F4?>): PathWrap<F4, G> =
            get(+sa, +sa2, +sa3, +sa4)


    infix fun applyClause(query: KClause<E>): PathWrap<E, G> {
        query.invoke(this as PathWrap<E, Any>, this as PathWrap<E, Any>)
        return this;
    }


    infix fun groupBy(expr: KProperty1<E, *>): KSelect<G> {
        pc.groupBy(arrayOf(get(expr)))
        return this
    }

    fun groupBy(vararg expr: KProperty1<E, *>): KSelect<G> {
        pc.groupBy(expr.map { get(it) }.toTypedArray())
        return this
    }

    infix fun groupBy(expr: KFunction1<E, *>): KSelect<G> {
        pc.groupBy(arrayOf(get(expr)))
        return this
    }

    fun groupBy(vararg expr: KFunction1<E, *>): KSelect<G> {
        pc.groupBy(expr.map { get(it) }.toTypedArray())
        return this
    }

}


@Suppress("UNCHECKED_CAST")
infix fun <E, G, T : PathWrap<E, G>> T.or(orClause: T.(T) -> Unit): T {
    return this.internalOr(orClause as PathWrap<E, G>.(PathWrap<E, G>) -> Unit) as T
}

@Suppress("UNCHECKED_CAST")
infix fun <E, G, T : PathWrap<E, G>> T.orr(clause: T.(T) -> Unit): T {
    pc.add(OrPred)
    return this.internalAnd(clause as PathWrap<E, G>.(PathWrap<E, G>) -> Unit) as T
}

@Suppress("UNCHECKED_CAST")
infix fun <E, G, T : PathWrap<E, G>> T.and(orClause: T.(T) -> Unit): T {
    return this.internalAnd(orClause as PathWrap<E, G>.(PathWrap<E, G>) -> Unit) as T
}

@Suppress("UNCHECKED_CAST")
infix fun <E, G, T : PathWrap<E, G>> T.not(orClause: T.(T) -> Unit): T {
    return this.internalNot(orClause as PathWrap<E, G>.(PathWrap<E, G>) -> Unit) as T
}

infix fun <E, G, T : PathWrap<E, G>> T.clause(orClause: T.(T) -> Unit): T.(T) -> Unit {
    return orClause
}

operator fun  <E, G, F>  KProperty1<E, F>.get(sa: PathWrap<E, G>): PathWrap<F, G> =
        PathWrap(sa.pc, sa.root.get(this.name) )
