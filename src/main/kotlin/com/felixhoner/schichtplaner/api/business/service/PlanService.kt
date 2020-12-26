package com.felixhoner.schichtplaner.api.business.service

import com.felixhoner.schichtplaner.api.persistence.repository.PlanRepository
import com.felixhoner.schichtplaner.api.business.model.Plan
import org.springframework.stereotype.Component

@Component
class PlanService(
	private val planRepository: PlanRepository,
	private val transformer: TransformerBo
) {

	fun getAll(): List<Plan> = planRepository.findAll().map(transformer::toBo)

}
