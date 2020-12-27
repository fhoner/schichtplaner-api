package com.felixhoner.schichtplaner.api

import com.felixhoner.schichtplaner.api.persistence.entity.*
import com.felixhoner.schichtplaner.api.persistence.repository.*
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.time.LocalTime.parse

@Component
class TestData(
	private val planRepository: PlanRepository,
	private val productionRepository: ProductionRepository,
	private val shiftRepository: ShiftRepository,
	private val workerRepository: WorkerRepository
) {

	@Bean
	fun insertTestData() {
		shiftRepository.deleteAll()
		workerRepository.deleteAll()
		productionRepository.deleteAll()
		planRepository.deleteAll()

		val mariusReich = WorkerEntity("Marius", "Reich", "marius@reich.de")
		val mikeEggert = WorkerEntity("Mike", "Eggert", "mike@eggert.de")
		val edeltraudBaar = WorkerEntity("Edeltraud", "Baar", "edeltraud@baar.de")
		val robinSigmund = WorkerEntity("Robin", "Sigmund", "robin@sigmund.de")
		val sabineHarst = WorkerEntity("Sabine", "Harst", "sabine@harst.de")
		val gretaLiebert = WorkerEntity("Greta", "Liebert", "greta@liebert.de")
		workerRepository.saveAll(listOf(mariusReich, mikeEggert, edeltraudBaar, robinSigmund, sabineHarst, gretaLiebert))

		val konzert = PlanEntity("Konzert 2021")
		val vtf = PlanEntity("Vatertagsfest 2021")
		val kabarett = PlanEntity("Kabarett 2021")
		planRepository.saveAll(listOf(konzert, vtf, kabarett))

		val konzertEntrance = ProductionEntity("Einlass", konzert)
		val konzertDrinks = ProductionEntity("Getränke", konzert)
		val vtfDrinks = ProductionEntity("Getränke", vtf)
		val vtfFries = ProductionEntity("Pommes", vtf)
		val vtfBeer = ProductionEntity("Bier", vtf)
		val kabarettEntrance = ProductionEntity("Einlass", kabarett)
		productionRepository.saveAll(listOf(konzertEntrance, konzertDrinks, vtfDrinks, vtfFries, vtfBeer, kabarettEntrance))

		val konzertEntranceShift = ShiftEntity(parse("19:00"), parse("20:30"), konzertEntrance)
			.apply { workers.add(gretaLiebert) }
		val vtfDrinksShift1 = ShiftEntity(parse("09:30"), parse("14:00"), vtfDrinks)
			.apply { workers.add(mariusReich) }
		val vtfDrinksShift2 = ShiftEntity(parse("14:00"), parse("18:30"), vtfDrinks)
			.apply { workers.add(mikeEggert) }
		val vtfDrinksShift3 = ShiftEntity(parse("18:30"), parse("23:45"), vtfDrinks)
			.apply { workers.addAll(listOf(mikeEggert, edeltraudBaar)) }
		val vtfFriesShift1 = ShiftEntity(parse("09:30"), parse("14:00"), vtfFries)
		val vtfFriesShift2 = ShiftEntity(parse("14:00"), parse("18:30"), vtfFries)
			.apply { workers.addAll(listOf(robinSigmund, sabineHarst)) }
		val vtfFriesShift3 = ShiftEntity(parse("18:30"), parse("23:45"), vtfFries)
		val vtfBeerShift1 = ShiftEntity(parse("09:30"), parse("14:00"), vtfBeer)
		val vtfBeerShift2 = ShiftEntity(parse("11:00"), parse("15:30"), vtfBeer)
		val vtfBeerShift3 = ShiftEntity(parse("12:00"), parse("16:30"), vtfBeer)
		val vtfBeerShift4 = ShiftEntity(parse("14:00"), parse("18:30"), vtfBeer)
		val vtfBeerShift5 = ShiftEntity(parse("15:30"), parse("20:00"), vtfBeer)
		val vtfBeerShift6 = ShiftEntity(parse("16:30"), parse("21:00"), vtfBeer)
		val vtfBeerShift7 = ShiftEntity(parse("18:30"), parse("23:45"), vtfBeer)
		shiftRepository.saveAll(
			listOf(
				konzertEntranceShift,
				vtfDrinksShift1, vtfDrinksShift2, vtfDrinksShift3,
				vtfFriesShift1, vtfFriesShift2, vtfFriesShift3,
				vtfBeerShift1, vtfBeerShift2, vtfBeerShift3, vtfBeerShift4, vtfBeerShift5, vtfBeerShift6, vtfBeerShift7
			)
		)
	}

}
