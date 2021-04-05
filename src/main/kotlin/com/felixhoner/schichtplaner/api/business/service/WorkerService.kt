package com.felixhoner.schichtplaner.api.business.service

import com.felixhoner.schichtplaner.api.business.model.Worker
import com.felixhoner.schichtplaner.api.persistence.repository.WorkerRepository
import org.springframework.stereotype.Component

@Component
class WorkerService(
    private val workerRepository: WorkerRepository,
    private val transformer: TransformerBo,
) {

    fun getAllByShift(shiftIds: List<Long>): List<Worker> = workerRepository.findAllByShiftIds(shiftIds).map(transformer::toBo)
}
