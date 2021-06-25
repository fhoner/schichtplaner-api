package com.felixhoner.schichtplaner.api.persistence.entity

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToMany
import javax.persistence.Table
import java.util.*

@Entity
@Table(name = "worker")
class WorkerEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    val uuid: UUID = UUID.randomUUID(),

    @NotBlank
    val firstname: String,

    @NotBlank
    val lastname: String,

    @Email
    val email: String,

    @ManyToMany(mappedBy = "workers", fetch = FetchType.LAZY)
    val shifts: MutableList<ShiftEntity> = mutableListOf()

)
