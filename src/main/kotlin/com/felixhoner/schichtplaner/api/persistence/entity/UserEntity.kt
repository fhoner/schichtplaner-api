package com.felixhoner.schichtplaner.api.persistence.entity

import jakarta.validation.constraints.Email
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import java.util.UUID

/**
 * Roles are building on top of each other in a transitive manner.
 * A user with the role WRITER always will have the role READER as well.
 */
enum class UserRole {
    READER {
        override fun get() = setOf("READER")
    },
    WRITER {
        override fun get() = setOf("READER", "WRITER")
    },
    ADMIN {
        override fun get() = setOf("READER", "WRITER", "ADMIN")
    };

    abstract fun get(): Set<String>
}

@Entity
@Table(name = "user_account")
class UserEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    val uuid: UUID = UUID.randomUUID(),

    @Email
    val email: String,
    val password: String,

    @Enumerated(EnumType.STRING)
    val role: UserRole
)
