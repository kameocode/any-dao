package com.kameo.jpasugar

import com.kameo.jpasugar.context.DeletePathContext
import com.kameo.jpasugar.context.QueryPathContext
import com.kameo.jpasugar.context.UpdatePathContext
import com.kameo.jpasugar.wraps.ExpressionWrap
import com.kameo.jpasugar.wraps.PathWrap
import com.kameo.jpasugar.wraps.RootWrapUpdate
import com.kameo.jpasugar.wraps.greaterThan
import javax.persistence.EntityManager
import javax.persistence.NoResultException
import javax.persistence.Tuple
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Selection
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1


@Suppress("UNUSED_PARAMETER") // parameter resltClass is unused but needed for type safety
class AnyDAO(val em: EntityManager): EntityManager by em {

    class PathPairSelect<E, F>(val first: KSelect<E>, val second: KSelect<F>, val distinct: Boolean, val cb: CriteriaBuilder) : KSelect<Pair<E, F>> {
        override fun isDistinct(): Boolean = distinct

        override fun getJpaSelection(): Selection<Tuple> {
            return cb.tuple(first.getJpaSelection(), second.getJpaSelection())
        }
    }

    class PathTripleSelect<E, F, G>(val first: KSelect<E>, val second: KSelect<F>, val third: KSelect<G>, val distinct: Boolean, val cb: CriteriaBuilder) : KSelect<Triple<E, F, G>> {
        override fun isDistinct(): Boolean = distinct

        override fun getJpaSelection(): Selection<Tuple> {
            return cb.tuple(first.getJpaSelection(), second.getJpaSelection(), third.getJpaSelection())
        }
    }

    class PathQuadrupleSelect<E, F, G, H>(val first: KSelect<E>,
                                          val second: KSelect<F>,
                                          val third: KSelect<G>,
                                          val fourth: KSelect<H>,
                                          val distinct: Boolean, val cb: CriteriaBuilder) : KSelect<Quadruple<E, F, G, H>> {
        override fun isDistinct(): Boolean = distinct

        override fun getJpaSelection(): Selection<Tuple> {
            return cb.tuple(first.getJpaSelection(), second.getJpaSelection(), third.getJpaSelection(), fourth.getJpaSelection())
        }
    }
    class PathArraySelect(val distinct: Boolean, val cb: CriteriaBuilder, vararg val pw1: KSelect<*>) : KSelect<Array<Any>> {
        override fun isDistinct(): Boolean = distinct

        override fun getJpaSelection(): Selection<Tuple> {
            val sel = pw1.map { it.getJpaSelection() }.toTypedArray()
            return cb.tuple(*sel)
        }

    }

    class PathTupleSelect(val distinct: Boolean, val cb: CriteriaBuilder, vararg val selects: KSelect<*>) : KSelect<TupleWrap> {
        override fun isDistinct(): Boolean = distinct

        override fun getJpaSelection(): Selection<Tuple> {
            val sel = selects.map { it.getJpaSelection() }.toTypedArray()
            return cb.tuple(*sel)
        }
    }
    class PathObjectSelect<E : Any>(val clz: KClass<E>, val distinct: Boolean, val cb: CriteriaBuilder, vararg val expr: ExpressionWrap<*, *>) : KSelect<E> {
        override fun isDistinct(): Boolean = distinct

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


    fun <E : Any, RESULT : Any> all(clz: Class<E>, resultClass: Class<RESULT>, query: (KRoot<E>.(KRoot<E>) -> KSelect<RESULT>)? = null): List<RESULT> {

        val qq: (KRoot<E>.(KRoot<E>) -> KSelect<RESULT>) = query ?: {this as KSelect<RESULT>}
        val pc = QueryPathContext<RESULT>(clz, em)
        val res = pc.invokeQuery(qq).resultList
        return pc.mapToPluralsIfNeeded<RESULT>(res)
    }

    inline fun <reified E : Any> all(clz: KClass<E>): List<E> {
        return all(clz.java, E::class.java)
    }
    inline fun <E : Any, reified RESULT : Any> all(clz: KClass<E>, noinline query: (KRoot<E>.(KRoot<E>) -> KSelect<RESULT>)? = null): List<RESULT> {
        return all(clz.java, RESULT::class.java, query)
    }

    @Throws(NoResultException::class)
    fun <E : Any, RESULT : Any> one(clz: Class<E>, resultClass: Class<RESULT>, query: KRoot<E>.(KRoot<E>) -> KSelect<RESULT>): RESULT {
        val pc = QueryPathContext<RESULT>(clz, em)
        val jpaQuery = pc.invokeQuery(query)
        // jpaQuery.maxResults = 1
        return pc.mapToPluralsIfNeeded<RESULT>(jpaQuery.singleResult)
    }

    @Throws(NoResultException::class)
    inline fun <E : Any, reified RESULT : Any> one(clz: KClass<E>, noinline query: KRoot<E>.(KRoot<E>) -> KSelect<RESULT>): RESULT {
        return one(clz.java, RESULT::class.java, query)
    }

    fun <E : Any, RESULT : Any> first(clz: Class<E>, resultClass: Class<RESULT>, query: KRoot<E>.(KRoot<E>) -> KSelect<RESULT>): RESULT? {
        val pc = QueryPathContext<RESULT>(clz, em)
        val jpaQuery = pc.invokeQuery(query)
        jpaQuery.maxResults = 1
        return pc.mapToPluralsIfNeeded<RESULT>(jpaQuery.resultList).firstOrNull()
    }


    fun <E : Any> update(clz: Class<E>, query: (RootWrapUpdate<E, E>) -> Unit): Int {
        val pc = UpdatePathContext<E>(clz, em)
        return pc.invokeUpdate(query).executeUpdate()
    }



    fun <E : Any> exists(clz: KClass<E>, query: KRoot<E>.(KRoot<E>) -> KSelect<*>): Boolean {
        val queryExists: KRoot<E>.(KRoot<E>) -> KSelect<Long> = {
            val invoke: KSelect<*> = query.invoke(it, it)
            it.select(ExpressionWrap(it.pc, em.criteriaBuilder.count(invoke.getJpaSelection() as Expression<*>)))
        }
        return one(clz.java, Long::class.java, queryExists) > 0
    }


    fun <E : Any> count(clz: KClass<E>, query: (KRoot<E>.(KRoot<E>) -> KSelect<*>)? = null): Long {
        val wrapperQuery: KRoot<E>.(KRoot<E>) -> (KSelect<Long>) = {
            if (query != null) {
                val invoke: KSelect<*> = query.invoke(it, it)
                it.select(ExpressionWrap(it.pc, em.criteriaBuilder.count(invoke.getJpaSelection() as Expression<*>)))
            } else {
                it.select(it.count())
            }
        }
        return one(clz.java, Long::class.java, wrapperQuery)
    }


    inline fun <E : Any, reified RESULT : Any> first(clz: KClass<E>, noinline query: KRoot<E>.(KRoot<E>) -> KSelect<RESULT>): RESULT? {
        return first(clz.java, RESULT::class.java, query)
    }

    inline fun <E : Any, reified RESULT : Any> allMutable(clz: KClass<E>, noinline query: (KRoot<E>.(KRoot<E>) -> KSelect<RESULT>)? = null): MutableList<RESULT> {
        return all(clz.java, RESULT::class.java, query) as MutableList<RESULT>
    }


   /* @JvmName("allDefaultReturn")
    inline fun <reified E : Any> all(clz: KClass<E>, noinline query: KRoot<E>.(KRoot<E>) -> (Int)): List<E> {
        val queryWrapp: KRoot<E>.(KRoot<E>) -> (KSelect<E>) = {
            query.invoke(it, it)
            it
        }
        return all(clz.java, E::class.java, queryWrapp)
    }*/

    inline fun <E : Any, reified RESULT : Any> pages(clz: KClass<E>, page: Page = Page(),
                                                     noinline query: KRoot<E>.(KRoot<E>) -> KSelect<RESULT>
    ): PagesResult<RESULT> {

        return object : PagesResult<RESULT>(page.pageSize) {
            var currentpage = page;

            override fun beforeForeach() {
                currentpage = page
            }

            override fun invoke(): List<RESULT> {
                val results = this@AnyDAO.page(clz, currentpage, query);
                currentpage = currentpage.next();
                return results;
            }
        };
    }

    inline fun <E : Any, reified RESULT : Any> page(clz: KClass<E>, page: Page,
                                                    noinline query: KRoot<E>.(KRoot<E>) -> KSelect<RESULT>): List<RESULT> {

        val wrapperQuery: KRoot<E>.(KRoot<E>) -> (KSelect<RESULT>) = {
            val result = query.invoke(this, this);
            this.limit(page.pageSize);
            this.skip(page.offset)
            result;
        }

        return all(clz.java, RESULT::class.java, wrapperQuery)
    }

   inline fun <E : Any, NUM, reified RESULT : Any> pageSorted(clz: KClass<E>, prop: KProperty1<E, NUM>, num: NUM?, page: Page,
                                                    noinline query: KRoot<E>.(KRoot<E>) -> KSelect<RESULT>): List<RESULT> where NUM: Number, NUM: Comparable<NUM> {

        val wrapperQuery: KRoot<E>.(KRoot<E>) -> (KSelect<RESULT>) = {
            if (num!=null)
                this[prop].greaterThan(num)
            this.orderBy(prop)
            val result = query.invoke(this, this);
            this.limit(page.pageSize);
            this.skip(0)

            result;
        }

        return all(clz.java, RESULT::class.java, wrapperQuery)
}
    inline fun <reified E : Any, NUM> pagesSorted(clz: KClass<E>, prop: KProperty1<E, NUM>, page: Page = Page(),
                                                  noinline query: KRoot<E>.(KRoot<E>) -> KSelect<E>
    ): PagesResult<E> where NUM: Number, NUM: Comparable<NUM> {

        return object : PagesResult<E>(page.pageSize) {
            var currentpage = page;
            var num: NUM? = null;

            override fun beforeForeach() {
                currentpage = page
                num = null
            }

            override fun invoke(): List<E> {
                val results = this@AnyDAO.pageSorted(clz, prop, num, currentpage, query);
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


}


