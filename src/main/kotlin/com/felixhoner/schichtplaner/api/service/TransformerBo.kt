package com.felixhoner.schichtplaner.api.service

import com.felixhoner.schichtplaner.api.persistence.entity.PlanEntity
import com.felixhoner.schichtplaner.api.persistence.entity.ProductionEntity
import com.felixhoner.schichtplaner.api.service.model.Plan
import com.felixhoner.schichtplaner.api.service.model.Production
import org.springframework.stereotype.Component

@Component
class TransformerBo {

	fun toBo(plan: PlanEntity) = Plan(
		id = plan.id!!,
		uuid = plan.uuid,
		name = plan.name
	)

	fun toBo(production: ProductionEntity) = Production(
		id = production.id!!,
		uuid = production.uuid,
		name = production.name
	)

}
