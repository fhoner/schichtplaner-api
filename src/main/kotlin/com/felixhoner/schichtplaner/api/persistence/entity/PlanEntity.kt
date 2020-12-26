package com.felixhoner.schichtplaner.api.persistence.entity

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "plan")
class PlanEntity(
	@NotBlank
	val name: String,
) {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	var id: Long? = null

	@Column(unique = true)
	val uuid: UUID = UUID.randomUUID()
}
