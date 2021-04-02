package com.felixhoner.schichtplaner.api.business.service

import com.felixhoner.schichtplaner.api.business.model.Worker
import com.felixhoner.schichtplaner.api.persistence.entity.WorkerEntity
import com.felixhoner.schichtplaner.api.persistence.repository.WorkerRepository
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

class WorkerServiceTest {

    private val workerRepository: WorkerRepository = mockk()
    private val transformer: TransformerBo = mockk()

    private val cut = WorkerService(
        workerRepository,
        transformer
    )

    @Test
    fun `should find all by shift ids`() {
        val workerEntities = listOf(
            WorkerEntity(firstname = "Max", lastname = "Mustermann", email = "max@mustermann"),
            WorkerEntity(firstname = "Sabine", lastname = "Mustermann", email = "sabine@mustermann")
        )
        val workerBos = listOf(
            Worker(id = 1, uuid = UUID.randomUUID(), firstname = "Max", lastname = "Mustermann", email = "max@mustermann", emptyList()),
            Worker(
                id = 2,
                uuid = UUID.randomUUID(),
                firstname = "Sabine",
                lastname = "Mustermann",
                email = "sabine@mustermann",
                emptyList()
            )
        )
        every { workerRepository.findAllByShiftIds(any()) } returns workerEntities
        every { transformer.toBo(workerEntities[0]) } returns workerBos[0]
        every { transformer.toBo(workerEntities[1]) } returns workerBos[1]

        val result = cut.findAllByShift(listOf(1, 2, 3))
        result shouldHaveSize 2
        result shouldContainExactlyInAnyOrder workerBos
        verify { transformer.toBo(workerEntities[0]) }
        verify { transformer.toBo(workerEntities[1]) }
    }

}
