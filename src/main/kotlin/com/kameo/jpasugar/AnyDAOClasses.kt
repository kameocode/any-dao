package com.kameo.jpasugar;

import com.kameo.jpasugar.wraps.FromWrap
import com.kameo.jpasugar.wraps.PathWrap
import com.kameo.jpasugar.wraps.RootWrap
import java.io.Serializable
import javax.persistence.Tuple
import javax.persistence.TupleElement
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Selection
import kotlin.reflect.KFunction1
import kotlin.reflect.KProperty1


typealias KRoot<E> = RootWrap<E, E>
typealias QueryUnit<T> = T.(T) -> Unit
typealias KQuery<E, RESULT> = KRoot<E>.(KRoot<E>) -> (KSelect<RESULT>)
typealias KClause<E> = PathWrap<E, Any>.(PathWrap<E, Any>) -> Unit
typealias KFromClause<E> = FromWrap<E, Any>.(FromWrap<E, Any>) -> Unit

interface IExpression<F, G> {
    fun getJpaExpression(): Expression<F>
    infix fun eq(expr: IExpression<F, *>): IExpression<F, G>
    infix fun eq(expr: F): IExpression<F, G>
    infix fun notEq(expr: IExpression<F, *>): IExpression<F, G>
    infix fun notEq(expr: F): IExpression<F, G>
}


private class Dummy(val prop: String)

private class FakeProperty<T, R>(val function: KFunction1<T, R>) : KProperty1<T, R> by Dummy::prop as KProperty1<T, R> {
    override val name: String
        get() = getPropertyNameFromGetter(function)

    internal fun getPropertyNameFromGetter(sa: KFunction1<*, *>): String {
        if (sa.name.startsWith("get")) {
            return sa.name[3].toLowerCase() + sa.name.substring(4)
        } else if (sa.name.startsWith("is")) {
            return sa.name[3].toLowerCase() + sa.name.substring(4)
        }
        return sa.name
    }
}

operator fun <T, R> KFunction1<T, R>.unaryPlus(): KProperty1<T, R> {
    return FakeProperty(this)
}

data class Quadruple<out A, out B, out C, out D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
) : Serializable {

    override fun toString(): String = "($first, $second, $third, $fourth)"
}


interface KSelect<E> {
    fun getJpaSelection(): Selection<*>
    fun isDistinct(): Boolean
}

class SelectWrap<E> constructor(val select: Selection<E>, val distinct: Boolean = false) : KSelect<E> {
    override fun getJpaSelection(): Selection<E> {
        return select
    }

    override fun isDistinct(): Boolean {
        return distinct
    }
}

interface ISelectExpressionProvider<E> {
    fun getDirectSelection(): KSelect<E>
}

class TupleWrap(val arr: Array<Any>, val elementList: MutableList<out TupleElement<*>>) : Tuple {

    override fun toArray(): Array<Any> {
        return arr;
    }

    override fun getElements(): MutableList<out TupleElement<*>> {
        return elementList
    }

    override fun <X : Any?> get(tupleElement: TupleElement<X>): X {
        val index: Int = elements.indexOf(tupleElement)
        return arr[index] as X
    }

    operator fun <X : Any?> get(tupleElement: IExpression<X, *>): X {
        val index: Int = elements.indexOf((tupleElement as ISelectExpressionProvider<X>).getDirectSelection().getJpaSelection() as Any)
        return arr[index] as X
    }

    override fun <X : Any?> get(alias: String, type: Class<X>?): X {
        val element = elements.find { it.alias == alias }!!
        return get(element) as X
    }

    override fun get(alias: String): Any {
        val element = elements.find { it.alias == alias }!!
        return get(element)
    }

    override fun <X : Any?> get(i: Int, type: Class<X>?) = arr[i] as X
    override fun get(i: Int): Any = arr[i]


}

data class Page(val pageSize: Int = 10, val offset: Int = 0) {
    fun next() = Page(pageSize, offset + pageSize)
}

abstract class PagesResult<E>(val pageSize: Int) {
    abstract fun invoke(): List<E>

    fun forEachFlat(consumer: (E) -> Unit) {
        this.forEach { it.forEach(consumer) }
    }

    fun forEachFlatUntil(consumer: (E) -> Boolean) {
        this.forEachUntil {
            var shouldContinue = true;
            for (e in it) {
                shouldContinue = consumer.invoke(e);
                if (!shouldContinue)
                    break;
            }
            shouldContinue;
        }
    }

    fun forEach(consumer: (List<E>) -> Unit) {
        this.forEachUntil { consumer.invoke(it); true }
    }

    fun forEachUntil(consumer: (List<E>) -> Boolean) {
        do {
            val si = this.invoke();
            if (si.isEmpty())
                break;
            val shouldContinue = consumer.invoke(si);
            if (!shouldContinue || si.size < pageSize)
                break;
        } while (true)
    }


}

