package com.kameo.jpasugar;

import com.kameo.jpasugar.context.PathContext
import com.kameo.jpasugar.wraps.ComparableExpressionWrap
import com.kameo.jpasugar.wraps.ExpressionWrap
import com.kameo.jpasugar.wraps.PathWrap
import com.kameo.jpasugar.wraps.RootWrap
import com.kameo.jpasugar.wraps.StringExpressionWrap
import org.junit.Test
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Selection


interface IExpression<F, G> {
    fun getExpression(): Expression<F>
    infix fun eq(expr: IExpression<F, *>): IExpression<F, G>
    infix fun eq(expr: F): IExpression<F, G>
}

interface IStringExpressionWrap<G> : IExpression<String, G> {
    infix fun like(f: String): IExpression<String, G>
    infix fun like(f: Expression<String>): IExpression<String, G>
    infix fun like(f: ExpressionWrap<String, *>): IExpression<String, G>
    fun lower(): StringExpressionWrap<G>
}

class NumberExpressionWrap<F, G> constructor(
        pc: PathContext<G>,
        value: Expression<F>) : ComparableExpressionWrap<F, G>(pc, value) where F : Number, F : Comparable<F>

/*
public class FakeMutable<T, R>(prop: KProperty1<T, R>) : KProperty1<T, R> by prop, KMutableProperty1<T, R> {
    override fun set(receiver: T, value: R) {

    }

    override val setter: Setter<T, R>


}

@Suppress("UNCHECKED_CAST")
operator fun <T, R> KProperty1<T, R?>.unaryPlus(): KMutableProperty1<T, R> {
    val foo = this
    if (foo is KMutableProperty1)
        return foo as KMutableProperty1<T, R>
    return FakeMutable(foo as KProperty1<T, R>);
}


operator fun <E, G, R> PathWrap<E, G>.minus(foo: KProperty1<E, R?>): PathWrap<R?, G> {
    val pw = this;
    return if (foo is KMutableProperty1)
        pw.get(foo);
    else
        pw.get(FakeMutable(foo));
}*/

/*operator infix fun <E: Any?, G, R> PathWrap<E, G>.get(foo: KProperty1<E, R?>): PathWrap<R, G>  {
    val pw = this;
    return if (foo is KMutableProperty1)
        pw.get(foo) as PathWrap<R, G>;
    else
        pw.get(FakeMutable(foo)) as PathWrap<R, G>;
}*/
/*infix fun <E, G, R> PathWrap<E, G>.geta(foo: KProperty1<E, R>): PathWrap<R, G>  {
    val pw = this;
    return if (foo is KMutableProperty1)
        pw.get(foo as KProperty1<E,R>) as PathWrap<R, G>;
    else
        pw.get(FakeMutable(foo) as KProperty1<E,R>) as PathWrap<R, G>;
}
operator fun <E: Any?, G, R, R2> PathWrap<E, G>.get(foo: KProperty1<E, R?>, foo2: KProperty1<R, R2?>): PathWrap<R2, G>  {
    val pw = this as PathWrap<E,G>;
    return if (foo is KMutableProperty1) {
        pw.get(foo).get(foo2 as KProperty1<R?, R?>) as PathWrap<R2, G>;
    }
    else
        (pw.get(FakeMutable(foo)) as PathWrap<R, G>).get(foo2) as PathWrap<R2, G>;
}*/


/*operator fun <E, G, R, R2> PathWrap<E?, G>.get(foo: KProperty1<E, R?>, foo2: KProperty1<R, R2?>): PathWrap<R2?, G>  {
    val pw = this as PathWrap<E,G>;
    return if (foo is KMutableProperty1) {
        pw.get(foo).get(foo2 as KProperty1<R?, R?>) as PathWrap<R2?, G>;
    }
    else
        (pw.get(FakeMutable(foo)) as PathWrap<R, G>).get(foo2);
}*/


typealias Root<E> = RootWrap<E, E>
typealias KPath<E> = PathWrap<E, E>
/*
operator fun <E, G, R> PathWrap<E, G>.minus(foo: KMutableProperty1<E, R?>): PathWrap<R?, G>  {
    val pw = this;
    //if (foo is KMutableProperty1)

        return pw.get(foo);
    //FuturePath(foo)
    //return // FuturePath(FakeMutable(foo as KProperty1<G, R>) as KMutableProperty1<G, R?>);
     //   pw.get(FakeMutable(foo as KProperty1<T, R>) as PathWrap<R?, R>;
}*/



interface ISugarQuerySelect<E> {
    fun getSelection(): Selection<*>
    fun isSingle(): Boolean
    fun isDistinct(): Boolean
}

class SelectWrap<E> constructor(val select: Selection<E>, val distinct: Boolean = false) : ISugarQuerySelect<E> {
    override fun getSelection(): Selection<E> {
        return select
    }

    override fun isSingle(): Boolean {
        return distinct
    }

    override fun isDistinct(): Boolean {
        return false
    }
}

interface ISelectExpressionProvider<E> {
    fun getDirectSelection(): ISugarQuerySelect<E>
}

data class Page(val pageSize: Int = 10, val offset: Int = 0) {
    fun next() = Page(pageSize, offset + pageSize);
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