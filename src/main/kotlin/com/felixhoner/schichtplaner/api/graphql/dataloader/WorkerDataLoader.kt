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
            val result = ids.map { Pair(it, mutableSetOf<WorkerDto>()) }.toMap()
            val allWorkers = workerService.findAllByShift(ids)
            result.forEach { m ->
                val matched = allWorkers
                    .filter { worker -> worker.shiftIds.any { it == m.key } }
                    .map(transformer::toDto)
                m.value.addAll(matched)
            }
            result.values.map { it.toList() }
        }
    }

}
