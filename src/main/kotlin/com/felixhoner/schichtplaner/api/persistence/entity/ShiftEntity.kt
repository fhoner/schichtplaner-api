package com.felixhoner.schichtplaner.api.persistence.entity

import java.time.LocalTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "shift")
class ShiftEntity(

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long,

	@Column(unique = true)
	val uuid: UUID,

	@NotNull
	val startTime: LocalTime,

	@NotNull
	val endTime: LocalTime,

	@ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
	val production: ProductionEntity,

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "shift_worker")
	val workers: MutableList<WorkerEntity> = mutableListOf()
)
