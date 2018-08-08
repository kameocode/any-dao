package com.kameocode.anydao.test.helpers;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(JavaUserODB.class)
public abstract class JavaUserODB_ {

    public static volatile SingularAttribute<JavaUserODB, Boolean> valid;
    public static volatile SingularAttribute<JavaUserODB, JavaAddressODB> address;
    public static volatile ListAttribute<JavaUserODB, String> rolesList;
    public static volatile SingularAttribute<JavaUserODB, Long> id;
    public static volatile SetAttribute<JavaUserODB, String> rolesSet;
    public static volatile SingularAttribute<JavaUserODB, String> email;
    public static volatile MapAttribute<JavaUserODB, String, Integer> userRolesMap;

}

