package com.felixhoner.schichtplaner.api.util

import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test", "db-test", "ldev")
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DatabaseTest
