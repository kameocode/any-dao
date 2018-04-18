package com.kameo.jpasugar

import com.kameo.jpasugar.context.DeletePathContext
import com.kameo.jpasugar.context.QueryPathContext
import com.kameo.jpasugar.context.UpdatePathContext
import com.kameo.jpasugar.wraps.ExpressionWrap
import com.kameo.jpasugar.wraps.RootWrapUpdate
import javax.persistence.EntityManager
import javax.persistence.NoResultException
import javax.persistence.Tuple
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Selection
import kotlin.reflect.KClass

//typealias KQuery<E, RESULT> = KRoot<E>.() -> (ISugarQuerySelect<RESULT>)
typealias PageConsumer<RESULT> = (List<RESULT>) -> Boolean

@Suppress("UNUSED_PARAMETER") // parameter resltClass is unused but needed for type safety
class AnyDAO(val em: EntityManager) {

    class PathPairSelect<E, F>(val first: ISugarQuerySelect<E>, val second: ISugarQuerySelect<F>, val distinct: Boolean, val cb: CriteriaBuilder) : ISugarQuerySelect<Pair<E, F>> {
        override fun isDistinct(): Boolean = distinct

        override fun getJpaSelection(): Selection<Tuple> {
            return cb.tuple(first.getJpaSelection(), second.getJpaSelection())
        }

        override fun isSingle(): Boolean = false
    }

    class PathTripleSelect<E, F, G>(val first: ISugarQuerySelect<E>, val second: ISugarQuerySelect<F>, val third: ISugarQuerySelect<G>, val distinct: Boolean, val cb: CriteriaBuilder) : ISugarQuerySelect<Triple<E, F, G>> {
        override fun isDistinct(): Boolean = distinct

        override fun getJpaSelection(): Selection<Tuple> {
            return cb.tuple(first.getJpaSelection(), second.getJpaSelection(), third.getJpaSelection())
        }

        override fun isSingle(): Boolean = false
    }

    class PathQuadrupleSelect<E, F, G, H>(val first: ISugarQuerySelect<E>,
                                          val second: ISugarQuerySelect<F>,
                                          val third: ISugarQuerySelect<G>,
                                          val fourth: ISugarQuerySelect<H>,
                                          val distinct: Boolean, val cb: CriteriaBuilder) : ISugarQuerySelect<Quadruple<E, F, G, H>> {
        override fun isDistinct(): Boolean = distinct

        override fun getJpaSelection(): Selection<Tuple> {
            return cb.tuple(first.getJpaSelection(), second.getJpaSelection(), third.getJpaSelection(), fourth.getJpaSelection())
        }

        override fun isSingle(): Boolean = false
    }
    class PathArraySelect(val distinct: Boolean, val cb: CriteriaBuilder, vararg val pw1: ISugarQuerySelect<*>) : ISugarQuerySelect<Array<Any>> {
        override fun isDistinct(): Boolean = distinct

        override fun getJpaSelection(): Selection<Tuple> {
            val sel = pw1.map { it.getJpaSelection() }.toTypedArray();
            return cb.tuple(*sel)
        }

        override fun isSingle(): Boolean = false
    }

    class PathTupleSelect(val distinct: Boolean, val cb: CriteriaBuilder, vararg val selects: ISugarQuerySelect<*>) : ISugarQuerySelect<TupleWrap> {
        override fun isDistinct(): Boolean = distinct

        override fun getJpaSelection(): Selection<Tuple> {
            val sel = selects.map { it.getJpaSelection() }.toTypedArray();
            return cb.tuple(*sel)
        }

        override fun isSingle(): Boolean = false
    }
    class PathObjectSelect<E : Any>(val clz: KClass<E>, val distinct: Boolean, val cb: CriteriaBuilder, vararg val expr: ExpressionWrap<*, *>) : ISugarQuerySelect<E> {
        override fun isDistinct(): Boolean = distinct

        override fun getJpaSelection(): Selection<E> {
            val sel = expr.map { it.getJpaExpression() }.toTypedArray();
            return cb.construct(clz.java, *sel)
        }

        override fun isSingle(): Boolean = false
    }

    fun clear() {
        em.clear()
    }

    fun <T> merge(entity: T): T {
        return em.merge(entity)
    }

    fun <T> remove(entity: T): T {
        em.remove(entity)
        return entity
    }

    fun persist(entity: Any) {
        em.persist(entity)
    }

    fun persist(vararg entities: Any) {
        entities.forEach(em::persist)
    }

    fun <E : Any> find(clz: KClass<E>, primaryKey: Any): E {
        return em.find(clz.java, primaryKey)
    }


    fun <E : Any, RESULT : Any> all(clz: Class<E>, resultClass: Class<RESULT>, query: KRoot<E>.(KRoot<E>) -> ISugarQuerySelect<RESULT>): List<RESULT> {
        val pc = QueryPathContext<RESULT>(clz, em)
        val res = pc.invokeQuery(query).resultList
        return pc.mapToPluralsIfNeeded<RESULT>(res)
    }

    @Throws(NoResultException::class)
    fun <E : Any, RESULT : Any> one(clz: Class<E>, resultClass: Class<RESULT>, query: KRoot<E>.(KRoot<E>) -> ISugarQuerySelect<RESULT>): RESULT {
        val pc = QueryPathContext<RESULT>(clz, em)
        val jpaQuery = pc.invokeQuery(query)
        jpaQuery.maxResults = 1
        return pc.mapToPluralsIfNeeded<RESULT>(jpaQuery.singleResult)
    }

    @Throws(NoResultException::class)
    inline fun <E : Any, reified RESULT : Any> one(clz: KClass<E>, noinline query: KRoot<E>.(KRoot<E>) -> ISugarQuerySelect<RESULT>): RESULT {
        return one(clz.java, RESULT::class.java, query)
    }

    fun <E : Any, RESULT : Any> first(clz: Class<E>, resultClass: Class<RESULT>, query: KRoot<E>.(KRoot<E>) -> ISugarQuerySelect<RESULT>): RESULT? {
        val pc = QueryPathContext<RESULT>(clz, em)
        val jpaQuery = pc.invokeQuery(query)
        jpaQuery.maxResults = 1
        return pc.mapToPluralsIfNeeded<RESULT>(jpaQuery.resultList).firstOrNull()
    }


    fun <E : Any> update(clz: Class<E>, query: (RootWrapUpdate<E, E>) -> Unit): Int {
        val pc = UpdatePathContext<E>(clz, em)
        return pc.invokeUpdate(query).executeUpdate()
    }

    fun <E : Any> remove(clz: Class<E>, query: (KRoot<E>) -> Unit): Int {
        val pc = DeletePathContext<E>(clz, em)
        return pc.invokeDelete(query).executeUpdate()
    }

    fun <E : Any> exists(clz: Class<E>, query: KRoot<E>.(KRoot<E>) -> ISugarQuerySelect<*>): Boolean {
        val queryExists: KRoot<E>.(KRoot<E>) -> ISugarQuerySelect<Long> = {
            val invoke: ISugarQuerySelect<*> = query.invoke(it, it)
            it.select(ExpressionWrap(it.pc, em.criteriaBuilder.count(invoke.getJpaSelection() as Expression<*>)))
        }
        return one(clz, Long::class.java, queryExists) > 0
    }


    private fun <E : Any> ensureIsOnlyOne(res: List<E>): E {
        if (res.isEmpty() || res.size > 1)
            throw IllegalArgumentException("Expected exactly 1 result but query returned ${res.size} results.")
        return res.first()
    }

    fun <E : Any> exists(clz: KClass<E>, query: KRoot<E>.(KRoot<E>) -> ISugarQuerySelect<*>): Boolean {
        return exists(clz.java, query)
    }

    fun <E : Any> count(clz: KClass<E>): Long {
        return count(clz, {})
    }

    fun <E : Any> count(clz: KClass<E>, query: KRoot<E>.(KRoot<E>) -> Unit): Long {
        val wrapperQuery: KRoot<E>.(KRoot<E>) -> (ISugarQuerySelect<Long>) = {
            query.invoke(this, this);
            it.select(it.count())
        }
        return one(clz.java, Long::class.java, wrapperQuery)
    }


    inline fun <E : Any, reified RESULT : Any> first(clz: KClass<E>, noinline query: KRoot<E>.(KRoot<E>) -> ISugarQuerySelect<RESULT>): RESULT? {
        return first(clz.java, RESULT::class.java, query)
    }

    inline fun <E : Any, reified RESULT : Any> allMutable(clz: KClass<E>, noinline query: KRoot<E>.(KRoot<E>) -> ISugarQuerySelect<RESULT>): MutableList<RESULT> {
        return all(clz.java, RESULT::class.java, query) as MutableList<RESULT>
    }

    inline fun <reified RESULT : Any> all(clz: KClass<RESULT>): List<RESULT> {
        return all(clz, { this });
    }

    inline fun <E : Any, reified RESULT : Any> all(clz: KClass<E>, noinline query: KRoot<E>.(KRoot<E>) -> (ISugarQuerySelect<RESULT>)): List<RESULT> {
        return all(clz.java, RESULT::class.java, query)
    }
   /* @JvmName("allDefaultReturn")
    inline fun <reified E : Any> all(clz: KClass<E>, noinline query: KRoot<E>.(KRoot<E>) -> (Int)): List<E> {
        val queryWrapp: KRoot<E>.(KRoot<E>) -> (ISugarQuerySelect<E>) = {
            query.invoke(it, it)
            it
        }
        return all(clz.java, E::class.java, queryWrapp)
    }*/

    inline fun <E : Any, reified RESULT : Any> pages(clz: KClass<E>, page: Page,
                                                     noinline query: KRoot<E>.(KRoot<E>) -> ISugarQuerySelect<RESULT>
    ): PagesResult<RESULT> {

        return object : PagesResult<RESULT>(page.pageSize) {
            var currentpage = page;

            override fun invoke(): List<RESULT> {
                val results = this@AnyDAO.page(clz, currentpage, query);
                currentpage = currentpage.next();
                return results;
            }
        };


    }


    inline fun <E : Any, reified RESULT : Any> page(clz: KClass<E>, page: Page,
                                                    noinline query: KRoot<E>.(KRoot<E>) -> ISugarQuerySelect<RESULT>): List<RESULT> {

        val wrapperQuery: KRoot<E>.(KRoot<E>) -> (ISugarQuerySelect<RESULT>) = {
            val result = query.invoke(this, this);
            this.limit(page.pageSize);
            this.skip(page.offset)
            result;
        }

        return all(clz.java, RESULT::class.java, wrapperQuery)
    }

    fun <E : Any> remove(clz: KClass<E>, query: (KRoot<E>) -> Unit): Int {
        return remove(clz.java, query)
    }

    /**
     * Works with updatable=false fields
     */
    fun <E : Any> update(clz: KClass<E>, query: (RootWrapUpdate<E, E>) -> Unit): Int {
        return update(clz.java, query)
    }


}


