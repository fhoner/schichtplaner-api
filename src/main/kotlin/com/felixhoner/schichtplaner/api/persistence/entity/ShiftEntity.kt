package com.felixhoner.schichtplaner.api.persistence.entity

import java.time.LocalTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "shift")
class ShiftEntity(

	@NotNull
	val startTime: LocalTime,

	@NotNull
	val endTime: LocalTime,

	@ManyToOne(fetch = FetchType.LAZY)
	val production: ProductionEntity,
) {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long? = null

	@Column(unique = true)
	val uuid: UUID = UUID.randomUUID()

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "shift_worker")
	val workers: MutableList<WorkerEntity> = mutableListOf()

}
