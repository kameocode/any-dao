package com.kameo.jpasugar.wraps


import com.kameo.jpasugar.AnyDAO
import com.kameo.jpasugar.IExpression
import com.kameo.jpasugar.ISelectExpressionProvider
import com.kameo.jpasugar.KClause
import com.kameo.jpasugar.KRoot
import com.kameo.jpasugar.KSelect
import com.kameo.jpasugar.SelectWrap
import com.kameo.jpasugar.context.PathContext
import com.kameo.jpasugar.context.QueryPathContext
import com.kameo.jpasugar.context.SubqueryPathContext
import com.kameo.jpasugar.unaryPlus
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Selection
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


    infix fun groupBy(expr: KProperty1<E, *>) {
        return pc.groupBy(arrayOf(ExpressionWrap<E, G>(pc, root.get(expr.name))))
    }

    infix fun groupBy(expr: ExpressionWrap<E, *>) {
        return pc.groupBy(arrayOf(ExpressionWrap(pc, expr.getJpaExpression())))
    }

    fun groupBy(vararg exprs: KProperty1<E, *>) {
        return pc.groupBy(exprs.map { ExpressionWrap<E, G>(pc, root.get(it.name)) }.toTypedArray())
    }

    override fun getDirectSelection(): KSelect<E> {
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


    infix fun <F> select(pw: KMutableProperty1<E, F>): KSelect<F> {
        return select(get(pw))
    }

    infix fun <F> select(pw: ExpressionWrap<F, G>): KSelect<F> {
        return pw.getDirectSelection()
    }

    infix fun <F> selectDistinct(pw: ExpressionWrap<F, G>): KSelect<F> {
        return SelectWrap(pw.getDirectSelection().getJpaSelection() as Selection<F>, true)
    }

    fun <F, G> select(pw1: ISelectExpressionProvider<F>, pw2: ISelectExpressionProvider<G>, distinct: Boolean = false): AnyDAO.PathPairSelect<F, G> {
        return AnyDAO.PathPairSelect(pw1.getDirectSelection(), pw2.getDirectSelection(), distinct, pc.cb)
    }

    fun <F, G, H> select(pw1: ISelectExpressionProvider<F>, pw2: ISelectExpressionProvider<G>, pw3: ISelectExpressionProvider<H>, distinct: Boolean = false): AnyDAO.PathTripleSelect<F, G, H> {
        return AnyDAO.PathTripleSelect(pw1.getDirectSelection(), pw2.getDirectSelection(), pw3.getDirectSelection(), distinct, pc.cb)
    }

    fun <F, G, H, I> select(pw1: ISelectExpressionProvider<F>, pw2: ISelectExpressionProvider<G>,
                            pw3: ISelectExpressionProvider<H>,
                            pw4: ISelectExpressionProvider<I>,
                            distinct: Boolean = false): AnyDAO.PathQuadrupleSelect<F, G, H, I> {
        return AnyDAO.PathQuadrupleSelect(pw1.getDirectSelection(),
                pw2.getDirectSelection(),
                pw3.getDirectSelection(),
                pw4.getDirectSelection(),
                distinct, cb)
    }

    fun selectArray(vararg pw1: ISelectExpressionProvider<*>,
                    distinct: Boolean = false): AnyDAO.PathArraySelect {
        return AnyDAO.PathArraySelect(distinct, pc.cb, *pw1.map { it.getDirectSelection() }.toTypedArray())
    }

    fun selectTuple(vararg pw1: ISelectExpressionProvider<*>,
                    distinct: Boolean = false): AnyDAO.PathTupleSelect {
        return AnyDAO.PathTupleSelect(distinct, pc.cb, *pw1.map { it.getDirectSelection() }.toTypedArray())
    }

    fun <F : Any> select(clz: KClass<F>, vararg expr: ExpressionWrap<*, *>, distinct: Boolean = false): AnyDAO.PathObjectSelect<F> {
        return AnyDAO.PathObjectSelect(clz, distinct, pc.cb, *expr)
    }

    infix fun eqId(id: Long): PathWrap<E, G> {
        pc.add({ cb.equal(root.get<Path<Long>>("id"), id) })
        return this
    }

    fun isNull(): PathWrap<E, G> {
        pc.add({ cb.isNull(root) })
        return this
    }

    fun isNotNull(): PathWrap<E, G> {
        pc.add({ cb.isNotNull(root) })
        return this
    }

    @JvmName("isNullInfix")
    infix fun isNull(p: () -> Unit): PathWrap<E, G> {
        pc.add({ cb.isNull(root) })
        return this
    }

    @JvmName("isNotNullInfix")
    infix fun isNotNull(p: () -> Unit): PathWrap<E, G> {
        pc.add({ cb.isNotNull(root) })
        return this
    }

    infix fun inIds(ids: Collection<Long>): PathWrap<E, G> {
        pc.add({ root.get<Path<Long>>("id").`in`(ids) })
        return this
    }


    class ClousureWrap<E, G>(//var innerList:MutableList<() -> Predicate?> = mutableListOf<() -> Predicate?>(),
            pc: PathContext<G>,
            root: Path<E>
    ) : PathWrap<E, G>(pc, root)

    fun <I, J> ref(ref: PathWrap<I, J>, clause: (PathWrap<I, J>) -> Unit): PathWrap<E, G> {
        clause.invoke(ref)
        return this
    }

    fun ref(clause: (PathWrap<E, G>) -> Unit): PathWrap<E, G> {
        clause.invoke(this)
        return this
    }


    fun <I, J> ref(ref: PathWrap<I, J>): PathWrap<I, J> {
        return ref
    }

    fun finishClause(): PathWrap<E, G> {
        pc.unstackArray()
        return this
    }

    fun newOr(): ClousureWrap<E, G> {
        val list = mutableListOf<() -> Predicate?>()
        val pw = ClousureWrap(pc, root)
        pc.add({ calculateOr(list) })
        pc.stackNewArray(list)
        return pw
    }

    fun internalNot(notClause: PathWrap<E, G>.(PathWrap<E, G>) -> Unit): PathWrap<E, G> {
        val list = mutableListOf<() -> Predicate?>()
        pc.add({ calculateNot(list) })
        pc.stackNewArray(list)
        notClause.invoke(this, this)
        pc.unstackArray()
        return this
    }

    fun internalOr(orClause: PathWrap<E, G>.(PathWrap<E, G>) -> Unit): PathWrap<E, G> {
        val list = mutableListOf<() -> Predicate?>()
        pc.add({ calculateOr(list) })
        pc.stackNewArray(list)
        orClause.invoke(this, this)
        pc.unstackArray()
        return this
    }

    fun internalAnd(orClause: PathWrap<E, G>.(PathWrap<E, G>) -> Unit): PathWrap<E, G> {
        val list = mutableListOf<() -> Predicate?>()
        pc.add({ calculateAnd(list) })
        pc.stackNewArray(list)
        orClause.invoke(this, this)
        pc.unstackArray()
        return this
    }

    fun newAnd(): ClousureWrap<E, G> {
        val list = mutableListOf<() -> Predicate?>()
        val pw = ClousureWrap(pc, root)
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
        val predicates = list
                .asSequence()
                .mapNotNull { it.invoke() }
                .toMutableList()
        return predicates
    }


    fun inIds(vararg ids: Long): PathWrap<E, G> {
        pc.add({ root.get<Path<Long>>("id").`in`(ids) })
        return this
    }


    override infix fun eq(expr: E): PathWrap<E, G> {
        super.eq(expr)
        return this
    }

    override infix fun eq(expr: IExpression<E, *>): PathWrap<E, G> {
        super.eq(expr)
        return this
    }

    override infix fun notEq(expr: E): PathWrap<E, G> {
        super.notEq(expr)
        return this
    }

    override infix fun notEq(expr: IExpression<E, *>): PathWrap<E, G> {
        super.notEq(expr)
        return this
    }


    fun <F> eq(sa: SingularAttribute<E, F>, f: F): PathWrap<E, G> {
        pc.add { cb.equal(root.get(sa), f) }
        return this
    }

    fun <F> eq(sa: KProperty1<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.equal(root.get<Path<F>>(sa.name), f) })
        return this
    }

    fun <F> eqId(sa: KProperty1<E, F>, id: Long): PathWrap<E, G> {
        pc.add({ cb.equal(root.get<Path<F>>(sa.name).get<Long>("id"), id) })
        return this
    }

    fun <F> eq(exp1: ExpressionWrap<F, G>, f: F): PathWrap<E, G> {
        pc.add({ cb.equal(exp1.getJpaExpression(), f) })
        return this
    }

    fun <F> notEq(sa: KProperty1<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.notEqual(root.get<F>(sa.name), f) })
        return this
    }

    fun <F> notEq(sa: SingularAttribute<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.notEqual(root.get(sa), f) })
        return this
    }

    operator fun <F> rangeTo(sa: KMutableProperty1<E, F>): PathWrap<F, G> {
        return get(sa)
    }

    fun eqIdToPred(id: Long): Predicate {
        return cb.equal(root.get<Path<Long>>("id"), id)
    }


    override infix fun isIn(list: List<E>): PathWrap<E, G> {
        super.isIn(list)
        return this
    }

    override infix fun isIn(expr: ExpressionWrap<E, *>): PathWrap<E, G> {
        super.isIn(expr)
        return this
    }

    override infix fun isIn(expr: SubqueryWrap<E, *>): PathWrap<E, G> {
        super.isIn(expr)
        return this
    }

    fun <J : Any> isIn(clz: KClass<J>, subqueryQuery: KRoot<J>.(KRoot<J>) -> (KSelect<E>)): PathWrap<E, G> {
        newAnd()
        isIn(subqueryFrom(clz, subqueryQuery))
        finishClause()
        return this
    }

    override infix fun exists(expr: SubqueryWrap<*, *>): PathWrap<E, G> {
        super.exists(expr)
        return this
    }

    override infix fun notExists(expr: SubqueryWrap<*, *>): PathWrap<E, G> {
        super.notExists(expr)
        return this
    }


    infix fun orderBy(sa: KProperty1<E, *>): PathWrap<E, G> {
        return this.orderByAsc(sa)
    }

    infix fun orderByAsc(sa: KProperty1<E, *>): PathWrap<E, G> {
        pc.addOrder(cb.asc(root.get<Any>(sa.name)))
        return this
    }

    infix fun orderByDesc(sa: KProperty1<E, *>): PathWrap<E, G> {
        pc.addOrder(cb.desc(root.get<Any>(sa.name)))
        return this
    }

    infix fun orderBy(pathWrap: PathWrap<*, *>): PathWrap<E, G> {
        pc.addOrder(cb.asc(pathWrap.root))
        return this
    }

    infix fun orderBy(pw: Pair<PathWrap<*, *>, Boolean>) {
        val (pathWrap, asc) = pw
        pc.addOrder(if (asc) cb.asc(pathWrap.root) else cb.desc(pathWrap.root))
    }

    fun orderBy(vararg pw: Pair<PathWrap<*, *>, Boolean>) {
        for ((pathWrap, asc) in pw) {
            pc.addOrder(if (asc) cb.asc(pathWrap.root) else cb.desc(pathWrap.root))
        }
    }


    // type safety class to not use get with lists paremters
    class UseGetListOnJoinInstead


    fun <E : Any, RESULT> subqueryFrom(clz: KClass<E>, query: RootWrap<E, E>.(RootWrap<E, E>) -> (KSelect<RESULT>)): SubqueryWrap<RESULT, G> {
        val criteriaQuery = pc.criteria as CriteriaQuery<E>
        val subqueryPc = SubqueryPathContext(clz.java, pc.em, pc as QueryPathContext<G>, criteriaQuery.subquery(clz.java) as Subquery<G>)
        val returnedExpression = subqueryPc.invokeQuery(query)

        return returnedExpression as SubqueryWrap<RESULT, G>
    }


    fun <J : Any> exists(clz: KClass<J>, subqueryQuery: KRoot<J>.(KRoot<J>) -> (KSelect<E>)): PathWrap<E, G> {
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


    fun orderBy(sa: KFunction1<E, *>): PathWrap<E, G> = orderBy(+sa)
    fun orderByAsc(sa: KFunction1<E, *>): PathWrap<E, G> = orderByAsc(+sa)
    fun orderByDesc(sa: KFunction1<E, *>): PathWrap<E, G> = orderByDesc(+sa)
    fun <F> get(sa: KFunction1<E, List<F>>): UseGetListOnJoinInstead = get(+sa)


    fun <F> get(sa: KProperty1<E, List<F>>): UseGetListOnJoinInstead {
        return UseGetListOnJoinInstead()
    }


    infix operator fun <F> get(sa: SingularAttribute<E, F>): PathWrap<F, G> = PathWrap<F, G>(pc, root.get(sa))

    @JvmName("getNullable")
    infix operator fun <F> get(sa: KProperty1<E, F?>): PathWrap<F, G> = PathWrap(pc, root.get(sa.name))

    @JvmName("getNullable")
    infix operator fun <F> get(sa: KFunction1<E, F?>): PathWrap<F, G> = get(+sa)


    @JvmName("getNullable2")
    operator fun <F, F2> get(sa: KProperty1<E, F?>, sa2: KProperty1<F, F2>): PathWrap<F2, G> {
        val p: Path<F> = root.get(sa.name)
        return PathWrap(pc, p.get(sa2.name))
    }

    @JvmName("getNullable2")
    operator fun <F, F2> get(sa: KFunction1<E, F?>, sa2: KFunction1<F, F2>): PathWrap<F2, G> = get(+sa, +sa2)

    infix fun applyClause(query: KClause<E>): PathWrap<E, G> {
        query.invoke(this as PathWrap<E, Any>, this as PathWrap<E, Any>)
        return this;
    }

}


@Suppress("UNCHECKED_CAST")
infix fun <E, G, T : PathWrap<E, G>> T.or(orClause: T.(T) -> Unit): T {
    return this.internalOr(orClause as PathWrap<E, G>.(PathWrap<E, G>) -> Unit) as T
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

/**
 * Number extension functions
 */
fun <G, NUM, T : PathWrap<NUM, G>> T.max(): ExpressionWrap<NUM, G> where NUM : Number, NUM : Comparable<NUM> {
    return ExpressionWrap<NUM, G>(pc, pc.cb.max(root))
}

fun <G, NUM, T : PathWrap<NUM, G>> T.min(): ExpressionWrap<NUM, G> where NUM : Number, NUM : Comparable<NUM> {
    return ExpressionWrap<NUM, G>(pc, pc.cb.min(root))
}

fun <G, NUM, T : PathWrap<NUM, G>> T.sum(): ExpressionWrap<NUM, G> where NUM : Number, NUM : Comparable<NUM> {
    return ExpressionWrap<NUM, G>(pc, pc.cb.sum(root))
}

fun <G, NUM, T : PathWrap<NUM, G>> T.ave(): ExpressionWrap<Double, G> where NUM : Number, NUM : Comparable<NUM> {
    return ExpressionWrap(pc, pc.cb.avg(root))
}


fun <G, NUM, T : PathWrap<NUM, G>> T.mod(y: Int): ExpressionWrap<Int, G> where NUM : Number, NUM : Comparable<NUM> {
    return ExpressionWrap<Int, G>(pc, pc.cb.mod(root as Expression<Int>, y))
}

infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<NUM, G>> T.greaterThan(f: NUM): T {
    pc.add({ cb.greaterThan(expression, f) })
    return this
}

infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<NUM, G>> T.greaterThan(f: ExpressionWrap<NUM, *>): T {
    pc.add({ cb.greaterThan(expression, f.getJpaExpression()) })
    return this
}

infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<NUM, G>> T.greaterThanOrEqualTo(f: NUM): T {
    pc.add({ cb.greaterThanOrEqualTo(expression, f) })
    return this
}

infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<NUM, G>> T.greaterThanOrEqualTo(f: ExpressionWrap<NUM, *>): T {
    pc.add({ cb.greaterThanOrEqualTo(expression, f.getJpaExpression()) })
    return this
}

infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<NUM, G>> T.lessThan(f: NUM): T {
    pc.add({ cb.lessThan(expression, f) })
    return this
}

infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<NUM, G>> T.lessThan(f: ExpressionWrap<NUM, *>): T {
    pc.add({ cb.lessThan(expression, f.getJpaExpression()) })
    return this
}

infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<NUM, G>> T.lessThanOrEqualTo(f: NUM): T {
    pc.add({ cb.lessThanOrEqualTo(expression, f) })
    return this
}

infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<NUM, G>> T.lessThanOrEqualTo(f: ExpressionWrap<NUM, *>): T {
    pc.add({ cb.lessThanOrEqualTo(expression, f.getJpaExpression()) })
    return this
}

infix fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<NUM, G>> T.between(pair: Pair<NUM, NUM>): T {
    pc.add({ cb.between(expression, pair.first, pair.second) })
    return this
}

@JvmName("betweenExpression")
fun <G, NUM : Comparable<NUM>, T : ExpressionWrap<NUM, G>> T.between(pair: Pair<ExpressionWrap<NUM, *>, ExpressionWrap<NUM, *>>): T {
    pc.add({ cb.between(expression, pair.first.getJpaExpression(), pair.second.getJpaExpression()) })
    return this
}


infix fun <G, T : PathWrap<String, G>> T.like(f: String): T {
    pc.add({ pc.cb.like(root as (Expression<String>), f) })
    return this
}

infix fun <G, T : PathWrap<String, G>> T.like(f: Expression<String>): T {
    pc.add({ pc.cb.like(root as (Expression<String>), f) })
    return this
}

infix fun <G, T : PathWrap<String, G>> T.like(f: ExpressionWrap<String, *>): T {
    pc.add({ pc.cb.like(root as (Expression<String>), f.getJpaExpression()) })
    return this
}

fun <G, T : ExpressionWrap<String, G>> T.lower(): ExpressionWrap<String, G> {
    return ExpressionWrap(pc, pc.cb.lower(expression))
}

fun <G, T : ExpressionWrap<String, G>> T.upper(): ExpressionWrap<String, G> {
    return ExpressionWrap(pc, pc.cb.upper(expression))
}

fun <G, T : PathWrap<String, G>> T.length(): ExpressionWrap<Int, G> {
    return ExpressionWrap(pc, pc.cb.length(root as (Expression<String>)))
}

fun <G, T : PathWrap<String, G>> T.substring(from: Int): ExpressionWrap<String, G> {
    return ExpressionWrap(pc, pc.cb.substring(root as (Expression<String>), from))
}

fun <G, T : PathWrap<String, G>> T.substring(from: Int, len: Int): ExpressionWrap<String, G> {
    return ExpressionWrap(pc, pc.cb.substring(root as (Expression<String>), from, len))
}

fun <G, T : PathWrap<String, G>> T.substring(from: ExpressionWrap<Int, *>): ExpressionWrap<String, G> {
    return ExpressionWrap(pc, pc.cb.substring(root as (Expression<String>), from.getJpaExpression()))
}

fun <G, T : PathWrap<String, G>> T.substring(from: ExpressionWrap<Int, *>, len: ExpressionWrap<Int, *>): ExpressionWrap<String, G> {
    return ExpressionWrap(pc, pc.cb.substring(root as (Expression<String>), from.getJpaExpression(), len.getJpaExpression()))
}

fun <G, T : PathWrap<String, G>> T.trim(): ExpressionWrap<String, G> {
    return ExpressionWrap(pc, pc.cb.trim(root as (Expression<String>)))
}

fun <G, T : PathWrap<String, G>> T.trim(t: Char): ExpressionWrap<String, G> {
    return ExpressionWrap(pc, pc.cb.trim(t, root as (Expression<String>)))
}

fun <G, T : PathWrap<String, G>> T.trim(ts: CriteriaBuilder.Trimspec, t: Char): ExpressionWrap<String, G> {
    return ExpressionWrap(pc, pc.cb.trim(ts, t, root as (Expression<String>)))
}

fun <G, T : PathWrap<String, G>> T.locate(pattern: String): ExpressionWrap<Int, G> {
    return ExpressionWrap(pc, pc.cb.locate(root as (Expression<String>), pattern))
}

infix fun <G, T : PathWrap<String, G>> T.contains(f: Any): T {
    like("%" + f.toString() + "%")
    return this
}

infix fun <G, T : PathWrap<String, G>> T.isNullOrContains(f: Any): T {
    or {
        isNull()
        like("%" + f.toString() + "%")
    }
    return this
}

fun <G, T : ExpressionWrap<String, G>> T.concat(s: String): ExpressionWrap<String, G> {
    return ExpressionWrap(pc, pc.cb.concat(expression, s))
}

fun <G, T : ExpressionWrap<String, G>> T.concat(expr: ExpressionWrap<String, *>): ExpressionWrap<String, G> {
    return ExpressionWrap(pc, pc.cb.concat(expression, expr.getJpaExpression()))
}