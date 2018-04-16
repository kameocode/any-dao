# AnyDAO - kotlin syntactic sugar for JPA criteria builder ##
* Kotlin wrapper on JPA API
* Shorter and cleaner queries
* Can replace static Metamodel generation with type safe kotlin properties 
* Supports JPA critera update and criteria delete 
* Pagination
* Multiselects


**Example AnyDAO query (kotlin):**
```
    val results = anyDao.all(UserODB::class) 
            { it[UserODB::email] like "email1" }
```
**Same query, build with jpa criteria builder (plain java):**
```
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<UserODB> criteriaQuery = cb.createQuery(UserODB.class);
    Root<UserODB> root = criteriaQuery.from(UserODB.class);
    criteriaQuery.select(root);
    criteriaQuery.where(cb.like(root.get("email"),"email1"));
    TypedQuery<UserODB> query = em.createQuery(criteriaQuery);
    List<UserODB> result = query.getResultList();
```  

###How to use: ###

Create instance of AnyDAO:
```
    import com.kameo.jpasugar.AnyDAO
    ...
    val em: EntityManager = ...
    val anyDao = AnyDAO(em)
```
Use like this:
```
    val results: List<UserODB> = anyDao.all(UserODB::class) { it[UserODB::email] like "email1" }  
```
There is number of utility methods, most with signature `method(clz: KClass<E>, noinline query: KRoot<E>.(KRoot<E>) -> (ISugarQuerySelect<RESULT>))`:
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
If fields are private, use getter references instead of property references 
(`UserODB::getEmail` instead of `UserODB::email`)


###Examples: ###

> Get all entities with custom criteria
```
    val results = anyDao.all(UserODB::class) { it[UserODB::address, AddressODB:city] like "Cracow" }
```
> Nested or/and clauses 
```
    val results = anyDao.all(UserODB::class) { 
        or {
            it[UserODB::getAddress, AddressODB::getCity] like "Cracow"
            it[UserODB::getAddress, AddressODB::getCity] like "Warsaw"
        }
    }
```
or shorter:
```
    val results = anyDao.all(UserODB::class) { 
        it[UserODB::getAddress, AddressODB::getCity] or {
            it like "Cracow"
            it like "Warsaw"
        }
    }
```


###How to ###