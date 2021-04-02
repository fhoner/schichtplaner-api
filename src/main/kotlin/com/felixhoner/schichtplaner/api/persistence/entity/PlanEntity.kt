package com.felixhoner.schichtplaner.api.persistence.entity

import java.util.*
import javax.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

@Entity
@Table(name = "plan")
class PlanEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @NotBlank
    val name: String,

    @Column(unique = true)
    val uuid: UUID = UUID.randomUUID()
)
