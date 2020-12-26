package com.felixhoner.schichtplaner.api.business.model

import java.time.LocalTime
import java.util.*

data class Shift(
	val id: Long,
	val uuid: UUID,
	val startTime: LocalTime,
	val endTime: LocalTime,
)
