package com.felixhoner.schichtplaner.api.business.model

import com.felixhoner.schichtplaner.api.persistence.entity.UserRole
import java.util.UUID

data class User(
    val id: Long,
    val uuid: UUID,
    val email: String,
    val password: String,
    val role: UserRole
)
