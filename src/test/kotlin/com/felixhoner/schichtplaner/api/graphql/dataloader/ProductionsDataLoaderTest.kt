package com.felixhoner.schichtplaner.api.graphql.dataloader

import com.felixhoner.schichtplaner.api.business.model.Production
import com.felixhoner.schichtplaner.api.business.service.ProductionService
import com.felixhoner.schichtplaner.api.graphql.dto.ProductionDto
import com.felixhoner.schichtplaner.api.graphql.dto.TransformerDto
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.dataloader.DataLoader
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

@Suppress("unchecked_cast")
class ProductionsDataLoaderTest {

    private val productionService: ProductionService = mockk()
    private val transformer: TransformerDto = mockk()

    private val productions = listOf(
        listOf(
            Production(id = 1, uuid = UUID.randomUUID(), name = "Production 1"),
            Production(id = 2, uuid = UUID.randomUUID(), name = "Production 2")
        ),
        emptyList(),
        listOf(
            Production(id = 3, uuid = UUID.randomUUID(), name = "Production 3")
        )
    )
    private val productionsDto = listOf(
        listOf(
            ProductionDto(id = 1, uuid = productions[0][0].uuid.toString(), name = "Production 1"),
            ProductionDto(id = 2, uuid = productions[0][1].uuid.toString(), name = "Production 2"),
        ),
        emptyList(),
        listOf(
            ProductionDto(id = 3, uuid = productions[2][0].uuid.toString(), name = "Production 3"),
        )
    )

    private val cut = ProductionsDataLoader(
        productionService,
        transformer
    )

    @BeforeEach
    fun `mock response`() {
        every { productionService.getByPlans(any()) } returns productions
        every { transformer.toDto(productions[0][0]) } returns productionsDto[0][0]
        every { transformer.toDto(productions[0][1]) } returns productionsDto[0][1]
        every { transformer.toDto(productions[2][0]) } returns productionsDto[2][0]
    }

    @Test
    fun `should create dataloader with batch loader`() {
        cut.getInstance()
        verify(exactly = 0) { productionService.getByPlans(any()) }
    }

    @Test
    fun `should have name productionLoader`() {
        cut.name shouldBe "productionLoader"
    }

    @Test
    fun `should load productions correctly`() {
        val loader = cut.getInstance() as DataLoader<Long, List<ProductionDto>>
        loader.apply {
            load(1)
            load(2)
            load(3)
        }
        val result = runBlocking { loader.dispatchAndJoin() }
        result shouldHaveSize 3
        result[0] shouldBe listOf(productionsDto[0][0], productionsDto[0][1])
        result[1] shouldBe emptyList()
        result[2] shouldBe listOf(productionsDto[2][0])
        verify(exactly = 1) { productionService.getByPlans(listOf(1, 2, 3)) }
        verify(exactly = 3) { transformer.toDto(any<Production>()) }
    }
}
