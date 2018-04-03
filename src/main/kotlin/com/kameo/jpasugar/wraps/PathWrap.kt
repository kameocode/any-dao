package com.kameo.jpasugar.wraps


import com.kameo.jpasugar.AnyDAONew
import com.kameo.jpasugar.IExpression
import com.kameo.jpasugar.ISelectExpressionProvider
import com.kameo.jpasugar.ISugarQuerySelect
import com.kameo.jpasugar.Root
import com.kameo.jpasugar.SelectWrap
import com.kameo.jpasugar.context.PathContext
import com.kameo.jpasugar.context.QueryPathContext
import com.kameo.jpasugar.context.SubqueryPathContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Selection
import javax.persistence.criteria.Subquery
import javax.persistence.metamodel.SingularAttribute
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

@Suppress("UNCHECKED_CAST", "UNUSED")
open class PathWrap<E, G> constructor(
        pc: PathContext<G>,
        open val root: Path<E>
) : ExpressionWrap<E, G>(pc, root) {
   /* override val it: PathWrap<E, G> by lazy {
        this
    }*/

    infix fun groupBy(expr: KProperty1<E, *>) {
        return pc.groupBy(arrayOf(ExpressionWrap<E, G>(pc, root.get(expr.name))))
    }

    infix fun groupBy(expr: ExpressionWrap<E, *>) {
        return pc.groupBy(arrayOf(ExpressionWrap(pc, expr.getExpression())))
    }

    fun groupBy(vararg exprs: KProperty1<E, *>) {
        return pc.groupBy(exprs.map { ExpressionWrap<E, G>(pc, root.get(it.name)) }.toTypedArray())
    }

    override fun getDirectSelection(): ISugarQuerySelect<E> {
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


    infix fun <F> select(pw: KMutableProperty1<E, F>): ISugarQuerySelect<F> {
        return select(get(pw))
    }

    infix fun <F> select(pw: ExpressionWrap<F, G>): ISugarQuerySelect<F> {
        return pw.getDirectSelection()
    }

    infix fun <F> selectDistinct(pw: ExpressionWrap<F, G>): ISugarQuerySelect<F> {
        return SelectWrap(pw.getDirectSelection().getSelection() as Selection<F>, true)
    }

    fun <F, G> select(pw1: ISelectExpressionProvider<F>, pw2: ISelectExpressionProvider<G>, distinct: Boolean = false): AnyDAONew.PathPairSelect<F, G> {
        return AnyDAONew.PathPairSelect(pw1.getDirectSelection(), pw2.getDirectSelection(), distinct, pc.cb)
    }


    fun <F, G, H> select(pw1: ISelectExpressionProvider<F>, pw2: ISelectExpressionProvider<G>, pw3: ISelectExpressionProvider<H>, distinct: Boolean = false): AnyDAONew.PathTripleSelect<F, G, H> {
        return AnyDAONew.PathTripleSelect(pw1.getDirectSelection(), pw2.getDirectSelection(), pw3.getDirectSelection(), distinct, pc.cb)
    }

    infix fun eqId(id: Long): PathWrap<E, G> {
        pc.add({ pc.cb.equal(root.get<Path<Long>>("id"), id) })
        return this
    }

    // should be forbidden on root....
    fun isNull(): PathWrap<E, G> {
        pc.add({ pc.cb.isNull(root) })
        return this
    }

    @JvmName("isNullInfix")
    infix fun isNull(p: () -> Unit): PathWrap<E, G> {
        pc.add({ pc.cb.isNull(root) })
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

    private fun calculateAnd(list: MutableList<() -> Predicate?>): Predicate? {
        val predicates = toPredicates(list)
        return if (predicates.isNotEmpty())
            cb.and(*predicates.toTypedArray())
        else
            null
    }

    private fun toPredicates(list: MutableList<() -> Predicate?>): MutableList<Predicate> {
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

    fun <F> eq(sa: SingularAttribute<E, F>, f: F): PathWrap<E, G> {
        pc.add { cb.equal(root.get(sa), f) }
        return this
    }

    fun like(sa: KProperty1<E, String>, f: String): PathWrap<E, G> {
        pc.add { cb.like(root.get<Path<String>>(sa.name) as (Expression<String>), f) }
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
        pc.add({ cb.equal(exp1.getExpression(), f) })
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

    fun <F : Comparable<F>> after(sa: SingularAttribute<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.greaterThan(root.get(sa), f) })
        return this
    }

    fun <F : Comparable<F>> after(sa: KProperty1<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.greaterThan(root.get(sa.name), f) })
        return this
    }

    fun <F : Comparable<F>> greaterThan(sa: KProperty1<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.greaterThan(root.get(sa.name), f) })
        return this
    }

    fun <F : Comparable<F>> greaterThanOrEqualTo(sa: KProperty1<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.greaterThanOrEqualTo(root.get(sa.name), f) })
        return this
    }


    fun <F : Comparable<F>> lessThan(sa: KProperty1<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.lessThan(root.get(sa.name), f) })
        return this
    }

    fun <F : Comparable<F>> lessThanOrEqualTo(sa: KProperty1<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.lessThanOrEqualTo(root.get(sa.name), f) })
        return this
    }

    fun <F : Comparable<F>> before(sa: KProperty1<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.lessThan(root.get(sa.name), f) })
        return this
    }

    @JvmName("afterDate")
    fun after(sa: KProperty1<E, Date?>, f: Date): PathWrap<E, G> {
        pc.add({ cb.greaterThan(root.get(sa.name), f) })
        return this
    }

    @JvmName("after")
    fun after(sa: KProperty1<E, LocalDateTime?>, f: LocalDateTime): PathWrap<E, G> {
        pc.add({ cb.greaterThan(root.get(sa.name), f) })
        return this
    }

    @JvmName("before")
    fun before(sa: KProperty1<E, LocalDateTime?>, f: LocalDateTime): PathWrap<E, G> {
        pc.add({ cb.lessThan(root.get(sa.name), f) })
        return this
    }

    fun before(sa: KProperty1<E, Long?>, f: Long): PathWrap<E, G> {
        pc.add({ cb.lessThan(root.get(sa.name), f) })
        return this
    }

    @JvmName("beforeDate")
    fun before(sa: KProperty1<E, Date?>, f: Date): PathWrap<E, G> {
        pc.add({ cb.lessThan(root.get(sa.name), f) })
        return this
    }

    operator fun <F> rangeTo(sa: KMutableProperty1<E, F>): PathWrap<F, G> {
        return get(sa)
    }

    fun eqIdToPred(id: Long): Predicate {
        return cb.equal(root.get<Path<Long>>("id"), id)
    }

    override infix fun notEq(expr: IExpression<E, *>): PathWrap<E, G> {
        super.notEq(expr)
        return this
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

    fun <J : Any> isIn(clz: KClass<J>, subqueryQuery: Root<J>.(Root<J>) -> (ISugarQuerySelect<E>)): PathWrap<E, G> {
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

    fun <F : Number> max(sa: KProperty1<E, F>): ExpressionWrap<F, G> {
        return ExpressionWrap<F, G>(pc, pc.cb.max(root.get(sa.name)))
    }

    fun <F : Number> min(sa: KProperty1<E, F>): ExpressionWrap<F, G> {
        return ExpressionWrap<F, G>(pc, pc.cb.min(root.get(sa.name)))
    }


    infix fun orderByAsc(sa: KProperty1<E, *>): PathWrap<E, G> {
        pc.addOrder(cb.asc(root.get<Any>(sa.name)))
        return this
    }

    infix fun orderByDesc(sa: KProperty1<E, *>): PathWrap<E, G> {
        pc.addOrder(cb.desc(root.get<Any>(sa.name)))
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


    infix fun notEq(f: E): PathWrap<E, G> {
        pc.add { cb.notEqual(root, f) }
        return this
    }

    fun <E : Any, RESULT> subqueryFrom(clz: KClass<E>, query: RootWrap<E, E>.(RootWrap<E, E>) -> (ISugarQuerySelect<RESULT>)): SubqueryWrap<RESULT, G> {
        val criteriaQuery = pc.criteria as CriteriaQuery<E>
        val subqueryPc = SubqueryPathContext(clz.java, pc.em, pc as QueryPathContext<G>, criteriaQuery.subquery(clz.java) as Subquery<G>)
        val returnedExpression = subqueryPc.invokeQuery(query)

        return returnedExpression as SubqueryWrap<RESULT, G>
    }


    fun <J : Any> exists(clz: KClass<J>, subqueryQuery: Root<J>.(Root<J>) -> (ISugarQuerySelect<E>)): PathWrap<E, G> {
        newAnd()
        exists(subqueryFrom(clz, subqueryQuery))
        finishClause()
        return this
    }

    fun <J : Any> notExists(clz: KClass<J>, subqueryQuery: Root<J>.(Root<J>) -> (ISugarQuerySelect<E>)): PathWrap<E, G> {
        newAnd()
        notExists(subqueryFrom(clz, subqueryQuery))
        finishClause()
        return this
    }

    @Suppress("UNUSED_PARAMETER")
    fun <F> get(sa: KProperty1<E, List<F>>): UseGetListOnJoinInstead {
        //val join = (root as Join<Any, E>).join<E, F>(sa.name) as Join<Any, F>
        return UseGetListOnJoinInstead()
    }


    @JvmName("getAsComparable")
    infix fun <F : Comparable<F>> get(sa: KMutableProperty1<E, F>): ComparablePathWrap<F, G> =
            ComparablePathWrap(pc, root.get(sa.name))

    @JvmName("getAsLocalDateTime")
    infix fun get(sa: KMutableProperty1<E, LocalDateTime>): LocalDateTimePathWrap<G> =
            LocalDateTimePathWrap(pc, root.get(sa.name))

    @JvmName("getAsLocalDate")
    infix fun get(sa: KMutableProperty1<E, LocalDate>): LocalDatePathWrap<G> =
            LocalDatePathWrap(pc, root.get(sa.name))


    @JvmName("getAsNumber")
    infix operator fun <F> get(sa: KProperty1<E, F>): NumberPathWrap<F, G> where F : Number, F : Comparable<F> {
        return NumberPathWrap(pc, root.get(sa.name))
    }


    infix operator fun <F> get(sa: SingularAttribute<E, F>): PathWrap<F, G> = PathWrap<F, G>(pc, root.get(sa))

    @JvmName("getNullable")
    infix operator fun <F> get(sa: KProperty1<E, F?>): PathWrap<F, G> = PathWrap(pc, root.get(sa.name))

    @JvmName("getNullable2")
    operator fun <F, F2> get(sa: KProperty1<E, F?>, sa2: KProperty1<F, F2>): PathWrap<F2, G> {
        val p: Path<F> = root.get(sa.name)
        return PathWrap(pc, p.get(sa2.name))
    }

    @JvmName("getAsString")
    infix operator fun get(sa: KProperty1<E, String>): StringPathWrap<G> =
            StringPathWrap(pc, root.get(sa.name))


    @JvmName("get2AsString")
    operator fun <F> get(sa: KProperty1<E, F?>, sa2: KProperty1<F, String>): StringPathWrap<G> {
        val p: Path<F> = root.get(sa.name)
        return StringPathWrap(pc, p.get(sa2.name))
    }
/*
    open infix fun and(andClause: (PathWrap<E, G>) -> Unit): PathWrap<E, G> {
        return internalAnd(andClause);
    }

    open infix  fun or(orClause: (PathWrap<E, G>) -> Unit): PathWrap<E, G> {
        return internalOr(orClause)
    }
    open infix fun orJoin(orClause: (JoinWrap<E, G>) -> Unit): PathWrap<E, G> {
        return internalOr(orClause as (PathWrap<E, G>) -> Unit)
    }
    open infix fun andJoin(andClause: (JoinWrap<E, G>) -> Unit): PathWrap<E, G> {
        return internalAnd(andClause as (PathWrap<E, G>) -> Unit)
    }*/


}



@Suppress("UNCHECKED_CAST")
infix fun <E, G, T : PathWrap<E, G>> T.or(orClause: T.(T) -> Unit): T {
    return this.internalOr(orClause as PathWrap<E, G>.(PathWrap<E, G>) -> Unit) as T
}
@Suppress("UNCHECKED_CAST")
infix fun <E, G, T : PathWrap<E, G>> T.and(orClause: T.(T) -> Unit): T {
    return this.internalAnd(orClause as PathWrap<E, G>.(PathWrap<E, G>) -> Unit) as T
}

infix fun <E, G, T : PathWrap<E, G>> T.clause(orClause: T.(T) -> Unit):  T.(T) -> Unit {
    return orClause;
}
