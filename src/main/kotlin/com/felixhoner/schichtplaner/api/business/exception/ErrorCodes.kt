package com.felixhoner.schichtplaner.api.business.exception

data class ErrorDefinition(
    val errorCode: String,
    val message: String
) {
    fun toMap() = mapOf(
        "errorCode" to errorCode,
        "errorMessage" to message
    )
}

object ErrorCodes {
    val LOGIN_FAILED = ErrorDefinition(
        errorCode = "LOGIN_FAILED",
        message = "Invalid credentials provided"
    )

    val UNEXPECTED_ERROR = ErrorDefinition(
        errorCode = "UNEXPECTED_ERROR",
        message = "An unexpected error occurred"
    )
}
