package com.felixhoner.schichtplaner.api.persistence.repository

import com.felixhoner.schichtplaner.api.persistence.PostgresContainerTest
import com.felixhoner.schichtplaner.api.persistence.entity.PlanEntity
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)

class PlanRepositoryTest {

	private val db = PostgresContainerTest()

	@Autowired
	lateinit var cut: PlanRepository

	@Test
	fun insert() {
		val plan = PlanEntity("Konzert 2021")
		cut.save(plan)
	}

}
