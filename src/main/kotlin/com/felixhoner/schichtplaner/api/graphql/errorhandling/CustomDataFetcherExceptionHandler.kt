package com.felixhoner.schichtplaner.api.graphql.errorhandling

import com.felixhoner.schichtplaner.api.business.exception.ErrorCodes
import graphql.GraphQLError
import graphql.GraphqlErrorException
import graphql.execution.DataFetcherExceptionHandler
import graphql.execution.DataFetcherExceptionHandlerParameters
import graphql.execution.DataFetcherExceptionHandlerResult
import mu.KotlinLogging

/**
 * Custom exception handler that will transform [GraphQLException] into
 * extensions properly. They are then part of the extensions object in the
 * result json.
 */
class CustomDataFetcherExceptionHandler : DataFetcherExceptionHandler {

    private val logger = KotlinLogging.logger {}

    override fun onException(handlerParameters: DataFetcherExceptionHandlerParameters): DataFetcherExceptionHandlerResult {
        val exception = handlerParameters.exception
        val sourceLocation = handlerParameters.sourceLocation
        val path = handlerParameters.path
        val extensions = when (exception) {
            is GraphQLException -> exception.error.toMap()
            else -> ErrorCodes.UNEXPECTED_ERROR.toMap()
        }
        logger.warn("Exception caught in data fetcher: $exception")
        logger.info { exception.stackTraceToString() }
        logger.warn("Extensions: $extensions")
        val error: GraphQLError = GraphqlErrorException.newErrorException()
            .cause(exception)
            .message(exception.message)
            .sourceLocation(sourceLocation)
            .path(path.toList())
            .build()
        logger.warn { "Returning error" }
        return DataFetcherExceptionHandlerResult.newResult().error(error).build()
    }

}
