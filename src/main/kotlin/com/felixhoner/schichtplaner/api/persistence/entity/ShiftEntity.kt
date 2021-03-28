package com.felixhoner.schichtplaner.api.persistence.entity

import jakarta.validation.constraints.NotNull
import java.time.LocalTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "shift")
data class ShiftEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @NotNull
    val startTime: LocalTime,

    @NotNull
    val endTime: LocalTime,

    @ManyToOne(fetch = FetchType.LAZY)
    val production: ProductionEntity? = null,

    @Column(unique = true)
    val uuid: UUID = UUID.randomUUID(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "shift_worker")
    val workers: MutableList<WorkerEntity> = mutableListOf()
)
