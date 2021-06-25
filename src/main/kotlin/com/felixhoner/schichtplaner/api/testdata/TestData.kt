package com.felixhoner.schichtplaner.api.testdata

import com.felixhoner.schichtplaner.api.persistence.entity.PlanEntity
import com.felixhoner.schichtplaner.api.persistence.entity.ProductionEntity
import com.felixhoner.schichtplaner.api.persistence.entity.ShiftEntity
import com.felixhoner.schichtplaner.api.persistence.entity.UserEntity
import com.felixhoner.schichtplaner.api.persistence.entity.UserRole
import com.felixhoner.schichtplaner.api.persistence.entity.WorkerEntity
import com.felixhoner.schichtplaner.api.persistence.repository.PlanRepository
import com.felixhoner.schichtplaner.api.persistence.repository.ProductionRepository
import com.felixhoner.schichtplaner.api.persistence.repository.ShiftRepository
import com.felixhoner.schichtplaner.api.persistence.repository.UserRepository
import com.felixhoner.schichtplaner.api.persistence.repository.WorkerRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import java.time.LocalTime.parse

@Component
@Profile("testdata")
class TestData(
    private val planRepository: PlanRepository,
    private val productionRepository: ProductionRepository,
    private val shiftRepository: ShiftRepository,
    private val workerRepository: WorkerRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder
) {

    @Bean
    fun insertTestData() {
        shiftRepository.deleteAll()
        workerRepository.deleteAll()
        productionRepository.deleteAll()
        planRepository.deleteAll()
        userRepository.deleteAll()

        val mariusReich = WorkerEntity(firstname = "Marius", lastname = "Reich", email = "marius@reich.de")
        val mikeEggert = WorkerEntity(firstname = "Mike", lastname = "Eggert", email = "mike@eggert.de")
        val edeltraudBaar = WorkerEntity(firstname = "Edeltraud", lastname = "Baar", email = "edeltraud@baar.de")
        val robinSigmund = WorkerEntity(firstname = "Robin", lastname = "Sigmund", email = "robin@sigmund.de")
        val sabineHarst = WorkerEntity(firstname = "Sabine", lastname = "Harst", email = "sabine@harst.de")
        val gretaLiebert = WorkerEntity(firstname = "Greta", lastname = "Liebert", email = "greta@liebert.de")
        workerRepository.saveAll(listOf(mariusReich, mikeEggert, edeltraudBaar, robinSigmund, sabineHarst, gretaLiebert))

        val konzert = PlanEntity(name = "Konzert 2021")
        val vtf = PlanEntity(name = "Vatertagsfest 2021")
        val kabarett = PlanEntity(name = "Kabarett 2021")
        planRepository.saveAll(listOf(konzert, vtf, kabarett))

        val konzertEntrance = ProductionEntity(name = "Einlass", plan = konzert)
        val konzertDrinks = ProductionEntity(name = "Getränke", plan = konzert)
        val vtfDrinks = ProductionEntity(name = "Getränke", plan = vtf)
        val vtfFries = ProductionEntity(name = "Pommes", plan = vtf)
        val vtfBeer = ProductionEntity(name = "Bier", plan = vtf)
        val kabarettEntrance = ProductionEntity(name = "Einlass", plan = kabarett)
        productionRepository.saveAll(listOf(konzertEntrance, konzertDrinks, vtfDrinks, vtfFries, vtfBeer, kabarettEntrance))

        val konzertEntranceShift = ShiftEntity(startTime = parse("19:00"), endTime = parse("20:30"), production = konzertEntrance)
            .apply { workers.add(gretaLiebert) }
        val vtfDrinksShift1 = ShiftEntity(startTime = parse("09:30"), endTime = parse("14:00"), production = vtfDrinks)
            .apply { workers.add(mariusReich) }
        val vtfDrinksShift2 = ShiftEntity(startTime = parse("14:00"), endTime = parse("18:30"), production = vtfDrinks)
            .apply { workers.add(mikeEggert) }
        val vtfDrinksShift3 = ShiftEntity(startTime = parse("18:30"), endTime = parse("23:45"), production = vtfDrinks)
            .apply { workers.addAll(listOf(mikeEggert, edeltraudBaar)) }
        val vtfFriesShift1 = ShiftEntity(startTime = parse("09:30"), endTime = parse("14:00"), production = vtfFries)
        val vtfFriesShift2 = ShiftEntity(startTime = parse("14:00"), endTime = parse("18:30"), production = vtfFries)
            .apply { workers.addAll(listOf(robinSigmund, sabineHarst)) }
        val vtfFriesShift3 = ShiftEntity(startTime = parse("18:30"), endTime = parse("23:45"), production = vtfFries)
        val vtfBeerShift1 = ShiftEntity(startTime = parse("09:30"), endTime = parse("14:00"), production = vtfBeer)
        val vtfBeerShift2 = ShiftEntity(startTime = parse("11:00"), endTime = parse("15:30"), production = vtfBeer)
        val vtfBeerShift3 = ShiftEntity(startTime = parse("12:00"), endTime = parse("16:30"), production = vtfBeer)
        val vtfBeerShift4 = ShiftEntity(startTime = parse("14:00"), endTime = parse("18:30"), production = vtfBeer)
        val vtfBeerShift5 = ShiftEntity(startTime = parse("15:30"), endTime = parse("20:00"), production = vtfBeer)
        val vtfBeerShift6 = ShiftEntity(startTime = parse("16:30"), endTime = parse("21:00"), production = vtfBeer)
        val vtfBeerShift7 = ShiftEntity(startTime = parse("18:30"), endTime = parse("23:45"), production = vtfBeer)
        shiftRepository.saveAll(
            listOf(
                konzertEntranceShift,
                vtfDrinksShift1, vtfDrinksShift2, vtfDrinksShift3,
                vtfFriesShift1, vtfFriesShift2, vtfFriesShift3,
                vtfBeerShift1, vtfBeerShift2, vtfBeerShift3, vtfBeerShift4, vtfBeerShift5, vtfBeerShift6, vtfBeerShift7
            )
        )

        val fhoner = UserEntity(
            email = "felix.honer@novatec-gmbh.de",
            password = passwordEncoder.encode("felix"),
            role = UserRole.ADMIN
        )
        val ssicher = UserEntity(
            email = "siegfried.sicher@mail.com",
            password = passwordEncoder.encode("siegfried"),
            role = UserRole.WRITER
        )
        val mmueller = UserEntity(
            email = "manfred.müller@mail.de",
            password = passwordEncoder.encode("manfred"),
            role = UserRole.READER
        )
        userRepository.saveAll(listOf(fhoner, ssicher, mmueller))
    }

}
