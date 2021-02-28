package com.felixhoner.schichtplaner.api.business.service

import com.felixhoner.schichtplaner.api.business.model.Shift
import com.felixhoner.schichtplaner.api.persistence.repository.ShiftRepository
import org.springframework.stereotype.Component

@Component
class ShiftService(
    private val shiftRepository: ShiftRepository,
    private val transformer: TransformerBo
) {

    fun getByProductions(productionIds: List<Long>): List<List<Shift>> {
        val all = shiftRepository.findAllByProductionIds(productionIds)
        return productionIds.map { productionId ->
            all
                .filter { shift -> productionId == shift.production.id }
                .map(transformer::toBo)
        }
    }

}
