package com.felixhoner.schichtplaner.api.persistence.repository

import com.felixhoner.schichtplaner.api.persistence.entity.ShiftEntity
import com.felixhoner.schichtplaner.api.persistence.entity.WorkerEntity
import com.felixhoner.schichtplaner.api.util.DatabaseTest
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalTime.parse

@DatabaseTest
class WorkerRepositoryTest {

    @Autowired
    lateinit var shiftRepository: ShiftRepository

    @Autowired
    lateinit var cut: WorkerRepository

    @Test
    fun `should find workers by shifts`() {
        val max = WorkerEntity(firstname = "Max", lastname = "Mustermann", email = "max@mustermann")
        val sabine = WorkerEntity(firstname = "Sabine", lastname = "Mustermann", email = "sabine@mustermann")
        val mike = WorkerEntity(firstname = "Mike", lastname = "Eggert", email = "mike@eggert.de")
        cut.saveAll(listOf(max, sabine, mike))

        val shift1 = ShiftEntity(startTime = parse("14:00"), endTime = parse("15:00"), workers = mutableListOf(max))
        val shift2 = ShiftEntity(startTime = parse("14:00"), endTime = parse("15:00"), workers = mutableListOf(sabine))
        val shift3 = ShiftEntity(startTime = parse("14:00"), endTime = parse("15:00"), workers = mutableListOf(sabine, max))
        val shift4 = ShiftEntity(startTime = parse("14:00"), endTime = parse("15:00"), workers = mutableListOf())
        val shifts = listOf(shift1, shift2, shift3, shift4)
        shiftRepository.saveAll(shifts)

        val result = cut.findAllByShiftIds(listOf(shift1.id!!, shift2.id!!, shift3.id!!, 4711))
        result shouldHaveSize 2
        result shouldContainExactlyInAnyOrder listOf(max, sabine)
    }

}
