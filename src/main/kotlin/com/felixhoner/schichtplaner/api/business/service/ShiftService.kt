package com.felixhoner.schichtplaner.api.business.service

import com.felixhoner.schichtplaner.api.business.model.Shift
import com.felixhoner.schichtplaner.api.persistence.entity.ShiftEntity
import com.felixhoner.schichtplaner.api.persistence.repository.ProductionRepository
import com.felixhoner.schichtplaner.api.persistence.repository.ShiftRepository
import org.springframework.stereotype.Component
import java.time.LocalTime
import java.util.*

@Component
class ShiftService(
    private val shiftRepository: ShiftRepository,
    private val productionRepository: ProductionRepository,
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

    fun createShift(productionUuid: UUID, startTime: String, endTime: String): Shift {
        val production = productionRepository.findByUuid(productionUuid) ?: throw RuntimeException("NOT FOUND")
        val startLt = LocalTime.parse(startTime)
        val endLt = LocalTime.parse(endTime)
        getByProduction(production.id!!).find { it.startTime == startLt && it.endTime == endLt }
            ?.apply { throw RuntimeException("The production already contains a shift [$startTime, $endTime]") }
        val newShift = ShiftEntity(
            startTime = startLt,
            endTime = endLt,
            production = production
        )
        return shiftRepository.save(newShift)
            .let(transformer::toBo)
    }

    private fun getByProduction(productionId: Long): List<ShiftEntity> =
        this.shiftRepository.findAllByProductionIds(listOf(productionId))

}
