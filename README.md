[![Build Status](https://travis-ci.org/kameocode/any-dao.svg?branch=master)](https://travis-ci.org/kameocode/any-dao)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.kameocode/any-dao/badge.svg)](http://repo1.maven.org/maven2/com/kameocode/any-dao/1.0.0/)
# AnyDao #

AnyDao is a [Kotlin](http://www.kotlinlang.org/) JPA (Java Persistence Api) wrapper library which makes your queries short, 
clean and easy to read. 


* DSL that is syntactic sugar for JPA criteria builder
* Shorter and cleaner queries (see examples below)
* Use of kotlin properties as type-sefe static Metamodel replacement
* Supports JPA critera update and criteria delete (compatible with JPA 2.1)
* One-line pagination with `forEach`, `forEachFlat` support
* Type-safe multiselects (Pair, Triple, Quadruple)


**AnyDao query (kotlin):**
```
    val results = anyDao.all(UserODB::class) { 
        it[UserODB::email] like "email1" 
    }
```
**Same query, built with jpa criteria builder (plain java):**
```
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<UserODB> criteriaQuery = cb.createQuery(UserODB.class);
    Root<UserODB> root = criteriaQuery.from(UserODB.class);
    criteriaQuery.select(root);
    criteriaQuery.where(cb.like(root.get("email"),"email1"));
    TypedQuery<UserODB> query = em.createQuery(criteriaQuery);
    List<UserODB> result = query.getResultList();
```  
**More complicated example - AnyDao query (kotlin):**
```
    val res = anyDao.all(UserODB::class) {
        it[UserODB::task] isIn subqueryFrom(TaskODB::class) {
            it[TaskODB::name] like "task1"
            or {
                it[TaskODB::name] like "task2"
                it[TaskODB::createDateTime] lessThan LocalDateTime.now().minusDays(1)
            }
        }
        it[UserODB::userRole] notEq UserRole.ADMIN
    }
```
**Same query, built with jpa criteria builder (plain java):**
```
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<UserODB> criteriaQuery = cb.createQuery(UserODB.class);
    Root<UserODB> root = criteriaQuery.from(UserODB.class);
    criteriaQuery.select(root);
    Subquery<TaskODB> subquery = criteriaQuery.subquery(TaskODB.class);
    Root<TaskODB> taskRoot = subquery.from(TaskODB.class);
    subquery.select(taskRoot);
    subquery.where(
        cb.or(
            cb.like(taskRoot.get("name"), "task1"),
            cb.and(
                cb.like(taskRoot.get("name"), "task2"),
                cb.lessThan(taskRoot.get("createDateTime"), LocalDateTime.now().minusDays(1))
            )
        )
     );
     criteriaQuery.where(
        root.get("task").in(subquery),
        cb.not(cb.equal(root.get("userRole"), UserRole.ADMIN))
     );
     TypedQuery<UserODB> query = em.createQuery(criteriaQuery);
     List<UserODB> result = query.getResultList();
```

### How to use:

Add to your project maven/gradle dependency:
```
<dependency>
    <groupId>com.kameocode</groupId>
    <artifactId>any-dao</artifactId>
    <version>1.0.0</version>
</dependency>
```
```
compile 'com.kameocode:any-dao:1.0.0'
```
Create instance of AnyDao:
```
    import com.kameocode.anydao.AnyDao
    ...
    val em: EntityManager = ...
    val anyDao = AnyDao(em)
```
Use like this:
```
    val results: List<UserODB> = anyDao.all(UserODB::class) { it[UserODB::id] lessThan 10L }  
```
Example application with SpringBoot can be found at [any-dao-example](https://github.com/kameocode/any-dao-example).

There is number of utility methods, most with parameters `clz: KClass<E>, query: KRoot<E>.(KRoot<E>) -> (KSelect<RESULT>)`:
* `anyDao.all`
* `anyDao.one`
* `anyDao.first`
* `anyDao.count`
* `anyDao.exists`
* `anyDao.allMutable`
* `anyDao.update`
* `anyDao.remove`
* `anyDao.page`
* `anyDao.pages`

Construct paths using indexed access operator or get methods:
```
    { it[UserODB::address, AddressODB::city] like "Cracow" }  
    { it.get(UserODB::address).get(AddressODB::city) like "Cracow" }  
```
If fields are private (or entities were written in plain java), 
use getter references instead of property references: 
(`UserODB::getEmail` instead of `UserODB::email`)

You can also assign paths and expressionsó to variables and reference them later:
```
    val listOfUsers = anyDao.all(UserODB::class) {
        val taskName = it[UserODB::task, TaskODB::name]
        val a = it[UserODB::counter].prod(1)
        val b = it[UserODB::counter].abs()
        val c = taskName.length()
        a eq 1      
        b greatherThan 0     
        c greatherThan 0     
    }
```

### How to 
> Return other type than in query

Use `select` as **last** clause in order to auto detect return type. 
Below query returns list of Strings:
```
     val listOfStrings = anyDao.all(UserODB::class) {
        select(it[UserODB::task, TaskODB::name])
     }
```
> Use logical expressions (or/and/not predicates)
```
    val listOfUsers = anyDao.all(UserODB::class) { 
        or {
            it[UserODB::getAddress, AddressODB::city] like "Cracow"
            it[UserODB::getAddress, AddressODB::city] like "Warsaw"
        }
    }
```
or shorter:
```
    val listOfUsers = anyDao.all(UserODB::class) { 
        it[UserODB::getAddress, AddressODB::city] or {
            it like "Cracow"
            it like "Warsaw"
        }
    }
```
By default, predicates are AND-ed. All predicates applied inside `or` are OR-ed.
For the above example, code will be translated into sql query `[...] or (city like 'Cracow' and city like 'Warsaw')`. 
You can also use `or` without clause, like this:
```
    val listOfUsers = anyDao.all(UserODB::class) { 
        it[UserODB::getAddress, AddressODB::city] like "Cracow"
        or
        it[UserODB::getAddress, AddressODB::city] like "Warsaw"
    }
```
Expressions _or_, _and_, _not_ can be nested into each other. 


> Use logical expressions (not predicate)

Add `not` clause
```
    val listOfUsers = anyDao.all(UserODB::class) {
        not { it[UserODB::email] like "task1" }
    }
```
For negating equality, you can use directly:
```
    val listOfUsers = anyDao.all(UserODB::class) {
        it[UserODB::email] notEq "task1" 
    }
```
> Use arithmetic/string expressions

These expressions should be accessible directly on expression/path elements:
```
    val listOfUsers = anyDao.all(UserODB::class) {
        it[UserODB::counter].prod(it[UserODB::severity]) eq 1      
        it[UserODB::counter].abs() greatherThan 2     
        it[UserODB::email].length() greatherThan 0     
    }
```
> Use joins
```
    val listOfUsers = anyDao.all(UserODB::class) {
        it.join(UserODB::task) {
            it[TaskODB::name] like "task1"
        }
    }
```
```
    val listOfUsers = anyDao.all(UserODB::class) {
        it.joinList(UserODB::allTasks) {
            it[TaskODB::name] like "task1"
    }
```
```
    val listOfUsers = anyDao.all(UserODB::class) {
        it.joinMap(UserODB::userRoles) {
            it.key() like "GUEST"
            it.value() like "true"
        }
    }
```
> Use subquery
```
    val listOfUsers = anyDao.all(UserODB::class) {
        it[UserODB::task] isIn subqueryFrom(TaskODB::class) {
            it[TaskODB::name] like "task1"
        }
    }
```
> Use group by/having
```
    val listOfPopularTaskNames = anyDao.all(UserODB::class) {
        it.groupBy(it[UserODB::task, TaskODB::name])
        it.having {
            it[UserODB::task, TaskODB::name].count() greaterThan 100L
        }
        select(it[UserODB::task, TaskODB::name])
    }
```
> Perform criteria update query
```
    val updatedCount = anyDao.update(UserODB::class) {
        it[UserODB::email] = "email0"
        it[UserODB::email] like "email2"
    }
```
> Perform criteria delete query
```
    val removedCount = anyDao.delete(UserODB::class) { 
        it[UserODB::email] like "email1" 
    }
```
> Apply JPA predicate
```
    val listOfUsers = anyDao.all(UserODB::class) {
        val root = it.getJpaExpression();
        it predicate { cb -> cb.equal(root.get<String>("email"), "email1") }
    }
```
> Combine query from external predicates 
```
 // define predicates as function literals with receiver of proper type:
 
 fun KRoot<UserODB>.hasHighCounter(): KSelect<UserODB> {
    return this[UserODB::counter] ge 15
 }
 private fun KRoot<UserODB>.hasEmail1InName() =
     this[UserODB::email] like "%email1%"
 
   
 // then use them wherever makes sense:
 val res = anyDao.all(UserODB::class) {
    hasHighCounter()
    or
    hasEmail1InName()
 }
```

> Paging
```
    val pagedListOfUsers = anyDao.pages(UserODB::class, Page(100)) { 
        it[UserODB::email] like "email1" 
    }
    pagedListOfUsers.forEach { list->
        list.forEach { println(it) }
    }
    pagedListOfUsers.forEachFlat {
        println(it) 
    }
```
> Typesafe multiselects  
```
    val listOfPairs: List<Pair<TaskODB, String>> = anyDao.all(UserODB::class) {
        select(it[UserODB::task], it[UserODB::email], it[UserODB::id])
    }
    val listOfTriples: List<Triple<TaskODB, String, Long>> = anyDao.all(UserODB::class) {
        select(it[UserODB::task], it[UserODB::email], it[UserODB::id])
    }
    val listOfQuadruples: List<Quadruple<TaskODB, String, Long, Long>> = anyDao.all(UserODB::class) {
        select(it[UserODB::task], it[UserODB::email], it[UserODB::id], it[UserODB::id])
    }
```
> Return custom DTO
```
    class UserTaskDTO(val concatenated: String, val userId: Long, val taskName: String)
  
    val listOfDTOs: List<UserTaskDTO> = anyDao.all(UserODB::class) {
            val taskName = it[UserODB::task, TaskODB::name]
            select(UserTaskDTO::class,
                it[UserODB::email].concat("_").concat(taskName),
                it[UserODB::id],
                taskName)
    }
```

For more examples, see: [tests](src/test/kotlin/com/kameocode/anydao/test/)


**Changelog**

**v 1.0.1**
* AnyDao is now AnyDao 
* added support for pure java classes for anyDao.exist and anyDao.count 