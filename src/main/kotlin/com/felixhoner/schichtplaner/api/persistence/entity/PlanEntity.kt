package com.felixhoner.schichtplaner.api.persistence.entity

import jakarta.validation.constraints.NotBlank
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import java.util.UUID

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
