package com.felixhoner.schichtplaner.api.graphql.dataloader

import com.felixhoner.schichtplaner.api.business.model.Worker
import com.felixhoner.schichtplaner.api.business.service.WorkerService
import com.felixhoner.schichtplaner.api.graphql.dto.ShiftDto
import com.felixhoner.schichtplaner.api.graphql.dto.TransformerDto
import com.felixhoner.schichtplaner.api.graphql.dto.WorkerDto
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.dataloader.DataLoader
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

@Suppress("unchecked_cast")
class WorkerDataLoaderTest {

    private val workerService: WorkerService = mockk()
    private val transformer: TransformerDto = mockk()

    private val workers = listOf(
        Worker(
            id = 1,
            uuid = UUID.randomUUID(),
            firstname = "Max",
            lastname = "Mustermann",
            email = "max@mustermann",
            shiftIds = listOf(1, 3)
        ),
        Worker(
            id = 2,
            uuid = UUID.randomUUID(),
            firstname = "Sabine",
            lastname = "Mustermann",
            email = "max@mustermann",
            shiftIds = listOf(1)
        ),
        Worker(
            id = 3,
            uuid = UUID.randomUUID(),
            firstname = "Robin",
            lastname = "Siegmund",
            email = "robin@siegmund",
            shiftIds = listOf(2)
        ),
    )
    private val workersDto = listOf(
        workers[0].run { WorkerDto(id, uuid.toString(), firstname, lastname, email) },
        workers[1].run { WorkerDto(id, uuid.toString(), firstname, lastname, email) },
        workers[2].run { WorkerDto(id, uuid.toString(), firstname, lastname, email) }
    )

    private val cut = WorkerDataLoader(
        workerService,
        transformer
    )

    @BeforeEach
    fun `mock response`() {
        every { workerService.getAllByShift(any()) } returns workers
        every { transformer.toDto(workers[0]) } returns workersDto[0]
        every { transformer.toDto(workers[1]) } returns workersDto[1]
        every { transformer.toDto(workers[2]) } returns workersDto[2]
    }

    @Test
    fun `should create dataloader with batch loader`() {
        cut.getInstance()
        verify(exactly = 0) { workerService.getAllByShift(any()) }
    }

    @Test
    fun `should have name productionLoader`() {
        cut.name shouldBe "workerLoader"
    }

    @Test
    fun `should load workers correctly`() {
        val loader = cut.getInstance() as DataLoader<Long, List<ShiftDto>>
        loader.apply {
            load(1)
            loadMany(listOf(4, 3, 2))
        }
        val result = runBlocking { loader.dispatchAndJoin() }
        result shouldHaveSize 4
        result[0] shouldBe listOf(workersDto[0], workersDto[1])
        result[1] shouldBe emptyList()
        result[2] shouldBe listOf(workersDto[0])
        result[3] shouldBe listOf(workersDto[2])
        verify { workerService.getAllByShift(listOf(1, 4, 3, 2)) }
        verify(exactly = 3) { transformer.toDto(any<Worker>()) }
    }

}
