package com.felixhoner.schichtplaner.api.persistence.entity

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(name = "production")
data class ProductionEntity(
	@NotBlank
	val name: String,

	@field:ManyToOne(fetch = FetchType.LAZY)
	@NotNull
	val plan: PlanEntity,
) {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long? = null

	@Column(unique = true)
	val uuid: UUID = UUID.randomUUID()

	@OneToMany(mappedBy = "production", fetch = FetchType.LAZY)
	val shifts: MutableList<ShiftEntity> = mutableListOf()
}
