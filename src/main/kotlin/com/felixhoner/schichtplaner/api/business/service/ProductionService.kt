package com.felixhoner.schichtplaner.api.business.service

import com.felixhoner.schichtplaner.api.persistence.repository.ProductionRepository
import com.felixhoner.schichtplaner.api.business.model.Production
import org.springframework.stereotype.Component

@Component
class ProductionService(
	private val productionRepository: ProductionRepository,
	private val transformer: TransformerBo
) {

	fun getByPlans(planIds: List<Long>): List<List<Production>> {
		val all = productionRepository.findAllByPlanIds(planIds)
		return planIds.map { planId ->
			all
				.filter { production -> planId == production.plan.id }
				.map(transformer::toBo)
		}
	}

}
