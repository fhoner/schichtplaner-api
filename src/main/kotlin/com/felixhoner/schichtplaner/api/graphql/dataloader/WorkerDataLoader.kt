package com.felixhoner.schichtplaner.api.graphql.dataloader

import com.felixhoner.schichtplaner.api.business.service.WorkerService
import com.felixhoner.schichtplaner.api.graphql.dto.TransformerDto
import com.felixhoner.schichtplaner.api.graphql.dto.WorkerDto
import org.dataloader.DataLoader
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class WorkerDataLoader(
    private val workerService: WorkerService,
    private val transformer: TransformerDto
) : SchichtplanerDataLoader {

    override val name: String get() = "workerLoader"

    override fun getInstance(): DataLoader<*, *> = DataLoader<Long, List<WorkerDto>> { ids ->
        CompletableFuture.supplyAsync {
            // create a map [shiftId,workers] to guarantee that result list matches query id list
            val result = ids.map { Pair(it, mutableSetOf<WorkerDto>()) }.toMap()
            // find all workers by database
            val allWorkers = workerService.getAllByShift(ids)
            // convert all of them to dto already; one worker may be part of multiple shifts
            val workerDtos = allWorkers.map(transformer::toDto)

            // iterate through the result map
            result.forEach { shiftId ->
                val matched = allWorkers
                    // find workers that are part of that shift
                    .filter { worker -> worker.shiftIds.any { it == shiftId.key } }
                    // get dto from already converted list
                    .map { workerDtos.find { wrk -> it.id == wrk.id }!! }
                shiftId.value.addAll(matched)
            }
            // convert sets to lists
            result.values.map { it.toList() }
        }
    }

}
