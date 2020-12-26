package com.felixhoner.schichtplaner.api.service.model

import java.time.LocalTime
import java.util.*

data class Shift(
	val id: Long,
	val uuid: UUID,
	val startTime: LocalTime,
	val endTime: LocalTime,
)
