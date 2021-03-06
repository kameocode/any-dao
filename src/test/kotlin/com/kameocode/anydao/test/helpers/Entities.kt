package com.kameocode.anydao.test.helpers;

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.CascadeType
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Temporal
import javax.persistence.TemporalType

enum class UserRole {
    ADMIN,
    NORMAL,
    GUEST
}

interface CommonODB

@Entity
data class UserODB(@Id
                   @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
                   val id: Long = 0,

                   val email: String,

                   val emailNullable: String? = null,

                   @ManyToOne(cascade = [CascadeType.ALL])
                   var address: AddressODB? = null,

                   @ManyToOne(cascade = [CascadeType.ALL])
                   var task: TaskODB,

                   @ManyToOne(cascade = [CascadeType.ALL])
                   var taskNullable: TaskODB? = null,

                   val valid: Boolean = true,

                   @OneToMany(cascade = [CascadeType.ALL])
                   var allTasks: List<TaskODB> = emptyList(),

                   @OneToMany(cascade = [CascadeType.ALL])
                   var allAddressSet: Set<AddressODB> = emptySet(),

                   @Enumerated
                   val userRole: UserRole = UserRole.NORMAL,

                   @ElementCollection
                   val userRoles: Set<UserRole> = emptySet(),

                   @ElementCollection
                   val userRoles2: Map<UserRole, Boolean> = emptyMap(),

                   @ElementCollection
                   val userRoles3: Map<String, String> = emptyMap(),


                   @ElementCollection
                   val userRoles4: Map<String, Int> = emptyMap(),

                   @ElementCollection
                   val userRoles5: List<String> = emptyList(),

                   @ElementCollection
                   val userRoles6: List<String>? = null,

                   val localDateTime: LocalDateTime = LocalDateTime.now(),

                   val localDateTimeNullable: LocalDateTime? = null,

                   val localDate: LocalDate = LocalDate.now(),

                   @Temporal(TemporalType.TIMESTAMP)
                   val timestamp: Date = Date(),
                   @Temporal(TemporalType.TIMESTAMP)
                   val timestampNullable: Date? = null,
                   @Temporal(TemporalType.TIMESTAMP)
                   val timestampNullable2: Date? = null,
                   val counter: Int = 0,
                   val counterNullable: Int? = null,
                   val counterDouble: Double = 0.0,
                   val counterDoubleNullable: Double? = null

) : CommonODB {

    var login: String? = null


}

@Entity
data class AddressODB(@Id
                      @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
                      val id: Long = 0, val city: String, val cityNullable: String? = null);

@Entity
data class TaskODB(@Id
                   @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
                   val id: Long = 0, val name: String,
                   @ManyToOne(cascade = [CascadeType.ALL])
                   var address: AddressODB = AddressODB(city = "City1"),

                   @ManyToOne(cascade = [CascadeType.ALL])
                   var nextTaskNullable: TaskODB? = null,

                   @ManyToOne(cascade = [CascadeType.ALL])
                   var addressNullable: AddressODB? = null,
                   val createDateTime: LocalDateTime = LocalDateTime.now()) : CommonODB;
