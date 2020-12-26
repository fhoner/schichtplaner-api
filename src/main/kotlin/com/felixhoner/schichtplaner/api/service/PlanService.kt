package com.felixhoner.schichtplaner.api.service

import com.felixhoner.schichtplaner.api.persistence.repository.PlanRepository
import com.felixhoner.schichtplaner.api.service.model.Plan
import org.springframework.stereotype.Component

@Component
class PlanService(
	private val planRepository: PlanRepository,
	private val transformer: TransformerBo
) {

	fun getAll(): List<Plan> = planRepository.findAll().map(transformer::toBo)

}
