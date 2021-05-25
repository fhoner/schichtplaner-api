package com.felixhoner.schichtplaner.api.business.service

import com.felixhoner.schichtplaner.api.business.exception.DuplicateShiftTimeException
import com.felixhoner.schichtplaner.api.business.exception.InvalidStartEndTimeException
import com.felixhoner.schichtplaner.api.business.exception.NotFoundException
import com.felixhoner.schichtplaner.api.business.model.Shift
import com.felixhoner.schichtplaner.api.persistence.entity.ShiftEntity
import com.felixhoner.schichtplaner.api.persistence.repository.ProductionRepository
import com.felixhoner.schichtplaner.api.persistence.repository.ShiftRepository
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class ShiftService(
    private val shiftRepository: ShiftRepository,
    private val productionRepository: ProductionRepository,
    private val transformer: TransformerBo
) {

    /**
     * Gets all shifts grouped by production id. The result list will have the
     * same size as the input parameter list, so could also contain empty lists.
     * @param productionIds All productions to search for.
     */
    fun getByProductions(productionIds: List<Long>): List<List<Shift>> {
        val all = shiftRepository.findAllByProductionIds(productionIds)
        // return a list with all given productionIds, no matter if a result was found or not
        // also, the result list order must match the parameter list
        return productionIds.map { productionId ->
            all
                .filter { shift -> productionId == shift.production?.id }
                .map(transformer::toBo)
        }
    }

    /**
     * Creates a new shift in the given production.
     * @param productionUuid The uuid of the production to add the new shift to.
     * @param startTime Start time of the shift.
     * @param endTime End time of the shift.
     * @throws NotFoundException Thrown if no production with [productionUuid] exists.
     * @throws InvalidStartEndTimeException Thrown if end is before start.
     * @throws DuplicateShiftTimeException Thrown if the production already contains a shift with the given [startTime] and [endTime].
     * @throws
     */
    fun createShift(productionUuid: UUID, start: Instant, end: Instant): Shift {
        val production = productionRepository.findByUuid(productionUuid)
            ?: throw NotFoundException("No production with uuid [$productionUuid] was found")
        validateNewShift(production.shifts, start, end)
        val newShift = ShiftEntity(startTime = start, endTime = end, production = production)
        return shiftRepository.save(newShift)
            .let(transformer::toBo)
    }

    private fun validateNewShift(existingShifts: List<ShiftEntity>, start: Instant, end: Instant) {
        if (!start.isBefore(end)) {
            throw InvalidStartEndTimeException("Start time must be before end time")
        }
        if (existingShifts.any { it.startTime == start && it.endTime == end }) {
            throw DuplicateShiftTimeException("The production already contains a shift with [startTime, endTime] = [$start, $end]")
        }
    }

}
