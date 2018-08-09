package com.kameocode.anydao

import com.kameocode.anydao.context.DeletePathContext
import com.kameocode.anydao.context.PredicatePathContext
import com.kameocode.anydao.context.QueryPathContext
import com.kameocode.anydao.context.UpdatePathContext
import com.kameocode.anydao.wraps.ExpressionWrap
import com.kameocode.anydao.wraps.RootWrapUpdate
import com.kameocode.anydao.wraps.greaterThan
import javax.persistence.EntityManager
import javax.persistence.NoResultException
import javax.persistence.Tuple
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root
import javax.persistence.criteria.Selection
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1


@Suppress("UNUSED_PARAMETER") // parameter resultClass is unused but needed for type safety
class AnyDao(val em: EntityManager) {

    class PathPairSelect<E, F>(val first: KSelect<E>, val second: KSelect<F>, var distinct: Boolean, val cb: CriteriaBuilder) : KSelect<Pair<E, F>> {
        override fun isDistinct(): Boolean = distinct

        fun distinct(): KSelect<Pair<E, F>> {
            this.distinct = true
            return this;
        }

        override fun getJpaSelection(): Selection<Tuple> {
            return cb.tuple(first.getJpaSelection(), second.getJpaSelection())
        }
    }

    class PathTripleSelect<E, F, G>(val first: KSelect<E>, val second: KSelect<F>, val third: KSelect<G>, var distinct: Boolean, val cb: CriteriaBuilder) : KSelect<Triple<E, F, G>> {
        override fun isDistinct(): Boolean = distinct

        fun distinct(): KSelect<Triple<E, F, G>> {
            this.distinct = true
            return this;
        }

        override fun getJpaSelection(): Selection<Tuple> {
            return cb.tuple(first.getJpaSelection(), second.getJpaSelection(), third.getJpaSelection())
        }

    }

    class PathQuadrupleSelect<E, F, G, H>(val first: KSelect<E>,
                                          val second: KSelect<F>,
                                          val third: KSelect<G>,
                                          val fourth: KSelect<H>,
                                          var distinct: Boolean, val cb: CriteriaBuilder) : KSelect<Quadruple<E, F, G, H>> {
        override fun isDistinct(): Boolean = distinct

        fun distinct(): KSelect<Quadruple<E, F, G, H>> {
            this.distinct = true
            return this;
        }

        override fun getJpaSelection(): Selection<Tuple> {
            return cb.tuple(first.getJpaSelection(), second.getJpaSelection(), third.getJpaSelection(), fourth.getJpaSelection())
        }

    }

    class PathArraySelect(var distinct: Boolean, val cb: CriteriaBuilder, vararg val pw1: KSelect<*>) : KSelect<Array<Any>> {
        override fun isDistinct(): Boolean = distinct

        fun distinct(): KSelect<Array<Any>> {
            this.distinct = true
            return this;
        }

        override fun getJpaSelection(): Selection<Tuple> {
            val sel = pw1.map { it.getJpaSelection() }.toTypedArray()
            return cb.tuple(*sel)
        }
    }

    class PathTupleSelect(var distinct: Boolean, val cb: CriteriaBuilder, vararg val selects: KSelect<*>) : KSelect<TupleWrap> {
        override fun isDistinct(): Boolean = distinct

        fun distinct(): KSelect<TupleWrap> {
            this.distinct = true
            return this;
        }

        override fun getJpaSelection(): Selection<Tuple> {
            val sel = selects.map { it.getJpaSelection() }.toTypedArray()
            return cb.tuple(*sel)
        }

    }

    class PathObjectSelect<E : Any>(val clz: KClass<E>, var distinct: Boolean, val cb: CriteriaBuilder, vararg val expr: ExpressionWrap<*, *>) : KSelect<E> {
        override fun isDistinct(): Boolean = distinct

        fun distinct(): KSelect<E> {
            this.distinct = true
            return this;
        }

        override fun getJpaSelection(): Selection<E> {
            val sel = expr.map { it.getJpaExpression() }.toTypedArray()
            return cb.construct(clz.java, *sel)
        }
    }

    fun <T> delete(entity: T): T {
        em.remove(entity)
        return entity
    }

    fun persist(vararg entities: Any) {
        entities.forEach(em::persist)
    }

    fun <E : Any> find(clz: KClass<E>, primaryKey: Any): E {
        return em.find(clz.java, primaryKey)
    }


    fun <E : Any, RESULT> all(clz: Class<E>, resultClass: Class<RESULT>, query: (KRoot<E>.(KRoot<E>) -> KSelect<RESULT>)? = null): List<RESULT> {
        val qq: (KRoot<E>.(KRoot<E>) -> KSelect<RESULT>) = query ?: { this as KSelect<RESULT> }
        val pc = QueryPathContext<RESULT>(clz, em)
        val res = pc.invokeQuery(qq).resultList
        return pc.mapToPluralsIfNeeded<RESULT>(res)
    }

    inline fun <reified E : Any> all(clz: KClass<E>): List<E> {
        return all(clz.java, E::class.java)
    }

    inline fun <E : Any, reified RESULT> all(clz: KClass<E>, noinline query: (KRoot<E>.(KRoot<E>) -> KSelect<RESULT>)? = null): List<RESULT> {
        return all(clz.java, RESULT::class.java, query)
    }

    @Throws(NoResultException::class)
    fun <E : Any, RESULT> one(clz: Class<E>, resultClass: Class<RESULT>, query: (KRoot<E>.(KRoot<E>) -> KSelect<RESULT>)): RESULT {
        val pc = QueryPathContext<RESULT>(clz, em)
        val jpaQuery = pc.invokeQuery(query)
        return pc.mapToPluralsIfNeeded<RESULT>(jpaQuery.singleResult)
    }

    @Throws(NoResultException::class)
    inline fun <E : Any, reified RESULT> one(clz: KClass<E>, noinline query: (KRoot<E>.(KRoot<E>) -> KSelect<RESULT>)): RESULT {
        return one(clz.java, RESULT::class.java, query)
    }

    fun <E : Any, RESULT> first(clz: Class<E>, resultClass: Class<RESULT>, query: (KRoot<E>.(KRoot<E>) -> KSelect<RESULT>)): RESULT? {
        val pc = QueryPathContext<RESULT>(clz, em)
        val jpaQuery = pc.invokeQuery(query)
        jpaQuery.maxResults = 1
        return pc.mapToPluralsIfNeeded<RESULT>(jpaQuery.resultList).firstOrNull()
    }

    fun <E : Any> update(clz: Class<E>, query: (RootWrapUpdate<E, E>) -> Unit): Int {
        val pc = UpdatePathContext<E>(clz, em)
        return pc.invokeUpdate(query).executeUpdate()
    }

    fun <E : Any> exists(clz: KClass<E>, query: KQuery<E, *>): Boolean {
        return exists(clz.java, query)
    }

    fun <E : Any> exists(clz: Class<E>, query: KQuery<E, *>): Boolean {
        val queryExists: KRoot<E>.(KRoot<E>) -> KSelect<Long> = {
            val invoke: KSelect<*> = query.invoke(it, it)
            it.select(ExpressionWrap(it.pc, em.criteriaBuilder.count(invoke.getJpaSelection() as Expression<*>)))
        }
        return one(clz, Long::class.java, queryExists) > 0
    }

    fun <E : Any> count(clz: KClass<E>, query: (KRoot<E>.(KRoot<E>) -> KSelect<*>)? = null): Long {
        return count(clz.java, query)
    }

    fun <E : Any> count(clz: Class<E>, query: (KRoot<E>.(KRoot<E>) -> KSelect<*>)? = null): Long {
        val wrapperQuery: KRoot<E>.(KRoot<E>) -> (KSelect<Long>) = {
            if (query != null) {
                val invoke: KSelect<*> = query.invoke(it, it)
                it.select(ExpressionWrap(it.pc, em.criteriaBuilder.count(invoke.getJpaSelection() as Expression<*>)))
            } else {
                it.select(it.count())
            }
        }
        return one(clz, Long::class.java, wrapperQuery)
    }

    inline fun <E : Any, reified RESULT> first(clz: KClass<E>, noinline query: (KRoot<E>.(KRoot<E>) -> KSelect<RESULT>)): RESULT? {
        return first(clz.java, RESULT::class.java, query)
    }

    inline fun <E : Any, reified RESULT> allMutable(clz: KClass<E>, noinline query: (KRoot<E>.(KRoot<E>) -> KSelect<RESULT>)? = null): MutableList<RESULT> {
        return all(clz.java, RESULT::class.java, query) as MutableList<RESULT>
    }

    inline fun <E : Any, reified RESULT> pages(clz: KClass<E>, KPage: KPage = KPage(),
                                               noinline query: (KRoot<E>.(KRoot<E>) -> KSelect<RESULT>)
    ): KPagesResult<RESULT> {

        return pages(clz.java, RESULT::class.java, KPage, query)
    }

    fun <E : Any, RESULT> pages(clz: Class<E>, resultClz: Class<RESULT>, KPage: KPage = KPage(),
                                query: (KRoot<E>.(KRoot<E>) -> KSelect<RESULT>)
    ): KPagesResult<RESULT> {
        return object : KPagesResult<RESULT>(KPage.pageSize) {
            var currentpage = KPage;

            override fun beforeForeach() {
                currentpage = KPage
            }

            override fun invoke(): List<RESULT> {
                val results = this@AnyDao.page(clz, resultClz, currentpage, query);
                currentpage = currentpage.next();
                return results;
            }
        };
    }

    inline fun <E : Any, reified RESULT> page(clz: KClass<E>, KPage: KPage,
                                              noinline query: (KRoot<E>.(KRoot<E>) -> KSelect<RESULT>)): List<RESULT> {

        return page(clz.java, RESULT::class.java, KPage, query)
    }

    fun <E : Any, RESULT> page(clz: Class<E>, resultClass: Class<RESULT>, KPage: KPage,
                               query: (KRoot<E>.(KRoot<E>) -> KSelect<RESULT>)): List<RESULT> {

        val wrapperQuery: KRoot<E>.(KRoot<E>) -> (KSelect<RESULT>) = {
            val result = query.invoke(this, this);
            this.limit(KPage.pageSize);
            this.skip(KPage.offset)
            result;
        }

        return all(clz, resultClass, wrapperQuery)
    }

    inline fun <E : Any, NUM, reified RESULT> pageSorted(clz: KClass<E>, prop: KProperty1<E, NUM>, num: NUM?, KPage: KPage,
                                                         noinline query: (KRoot<E>.(KRoot<E>) -> KSelect<RESULT>)): List<RESULT> where NUM : Number, NUM : Comparable<NUM> {
        return pageSorted(clz.java, RESULT::class.java, prop, num, KPage, query);
    }

    fun <E : Any, NUM, RESULT> pageSorted(clz: Class<E>, resultClz: Class<RESULT>, prop: KProperty1<E, NUM>, num: NUM?, KPage: KPage,
                                          query: (KRoot<E>.(KRoot<E>) -> KSelect<RESULT>)): List<RESULT> where NUM : Number, NUM : Comparable<NUM> {

        val wrapperQuery: KRoot<E>.(KRoot<E>) -> (KSelect<RESULT>) = {
            if (num != null)
                this[prop].greaterThan(num)
            this.orderBy(prop)
            val result = query.invoke(this, this);
            this.limit(KPage.pageSize);
            this.skip(0)

            result;
        }

        return all(clz, resultClz, wrapperQuery)
    }


    inline fun <reified E : Any, reified NUM> pagesSorted(clz: KClass<E>, prop: KProperty1<E, NUM>, KPage: KPage = KPage(),
                                                          noinline query: (KRoot<E>.(KRoot<E>) -> KSelect<E>)
    ): KPagesResult<E> where NUM : Number, NUM : Comparable<NUM> {
        return pagesSorted(clz.java, E::class.java, prop, KPage, query);
    }

    fun <E : Any, NUM> pagesSorted(clz: Class<E>, resultClass: Class<E>, prop: KProperty1<E, NUM>, KPage: KPage = KPage(),
                                   query: (KRoot<E>.(KRoot<E>) -> KSelect<E>)
    ): KPagesResult<E> where NUM : Number, NUM : Comparable<NUM> {

        return object : KPagesResult<E>(KPage.pageSize) {
            var currentpage = KPage;
            var num: NUM? = null;

            override fun beforeForeach() {
                currentpage = KPage
                num = null
            }

            override fun invoke(): List<E> {
                val results = this@AnyDao.pageSorted(clz, resultClass, prop, num, currentpage, query);
                currentpage = currentpage.next();
                if (!results.isEmpty())
                    num = prop.get(results.last())
                return results;
            }
        };
    }

    fun <E : Any> delete(clz: Class<E>, query: (KRoot<E>) -> Unit): Int {
        val pc = DeletePathContext<E>(clz, em)
        return pc.invokeDelete(query).executeUpdate()
    }

    fun <E : Any> delete(clz: KClass<E>, query: (KRoot<E>) -> Unit): Int {
        return delete(clz.java, query)
    }

    /**
     * Works with updatable=false fields
     */
    fun <E : Any> update(clz: KClass<E>, query: (RootWrapUpdate<E, E>) -> Unit): Int {
        return update(clz.java, query)
    }

    fun clear() {
        em.clear()
    }

    companion object {
        @JvmStatic
        fun <E : Any, RESULT> getPredicate(root: Root<E>, criteriaQuery: CriteriaQuery<*>, cb: CriteriaBuilder,
                                                 query: KRoot<E>.(KRoot<E>) -> KSelect<RESULT>): Predicate {
            val pc = PredicatePathContext(root as Root<Any>, criteriaQuery, cb);
            return pc.toPredicate(query)
        }
    }

}


