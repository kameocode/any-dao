package com.kameocode.anydao.context


import com.kameocode.anydao.KRoot
import com.kameocode.anydao.KSelect
import com.kameocode.anydao.wraps.RootWrap
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root


private val proxy: EntityManager =
        Proxy.newProxyInstance(EntityManager::class.java.getClassLoader(),
                arrayOf<Class<*>>(EntityManager::class.java),
                object : InvocationHandler {
                    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any {
                        TODO("not implemented")
                    }
                }) as EntityManager

@Suppress("UNCHECKED_CAST")
class PredicatePathContext<G>(override var root: Root<Any>,
                              override val criteria: CriteriaQuery<G>,
                              cb: CriteriaBuilder)
    : PathContext<G>(proxy, criteria, cb) {

    fun <RESULT, E> toPredicate(query: KRoot<E>.(KRoot<E>) -> KSelect<RESULT>): Predicate {
        rootWrap = RootWrap(this, root)
        query.invoke(rootWrap as KRoot<E>, rootWrap as KRoot<E>)
        val predicate = getPredicate()
        if (orders.isNotEmpty())
            criteria.orderBy(orders)
        return predicate ?: cb.conjunction()
    }

}