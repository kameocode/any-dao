package com.kameo.jpasugar.test.helpers;

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany


@Entity
data class UserODB(@Id
                   @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
                   val id: Long = 0,

                   val email: String,

                   @ManyToOne(cascade = [CascadeType.ALL])
                   var address: AddressODB? = null,

                   @ManyToOne(cascade = [CascadeType.ALL])
                   var task: TaskODB,


                   @OneToMany(cascade = [CascadeType.ALL])
                   var allTasks: List<TaskODB> = emptyList()

) {

    var login: String? = null


}

@Entity
data class AddressODB(@Id
                      @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
                      val id: Long = 0, val city: String);

@Entity
data class TaskODB(@Id
                   @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
                   val id: Long = 0, val name: String);

