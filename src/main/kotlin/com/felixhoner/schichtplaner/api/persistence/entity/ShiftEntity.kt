package com.felixhoner.schichtplaner.api.persistence.entity

import jakarta.validation.constraints.NotNull
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.Table
import java.time.Instant
import java.util.*

@Entity
@Table(name = "shift")
data class ShiftEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @NotNull
    val startTime: Instant,

    @NotNull
    val endTime: Instant,

    @ManyToOne(fetch = FetchType.LAZY)
    val production: ProductionEntity? = null,

    @Column(unique = true)
    val uuid: UUID = UUID.randomUUID(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "shift_worker")
    val workers: MutableList<WorkerEntity> = mutableListOf()
)
