package com.felixhoner.schichtplaner.api.systemtest

import com.felixhoner.schichtplaner.api.GRAPHQL_ENDPOINT
import com.felixhoner.schichtplaner.api.GRAPHQL_MEDIA_TYPE
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON
import java.util.*

class GraphQLSysTest : BaseSystemTest() {

    @BeforeEach
    fun setup() {
        clearData()
        prepareData()
    }

    @Test
    fun `should return plans successfully`() {
        val query = """
			query {
			  getPlans {
				name
				productions {
				  name
				  shifts {
					startTime
					endTime
				  }
				}
			  }
			}
		"""
            .replace("\n", "")
            .replace("\t", " ")
            .replace(" +".toRegex(), " ")

        val (accessToken, _) = super.doSuccessfulLogin().block()!!

        testClient.post()
            .uri(GRAPHQL_ENDPOINT)
            .accept(APPLICATION_JSON)
            .contentType(GRAPHQL_MEDIA_TYPE)
            .cookie("access_token", accessToken)
            .bodyValue(query)
            .exchange()
            .expectBody()
            .jsonPath("$.errors").doesNotExist()
            .jsonPath("$.data.getPlans").isArray
            .json(fileContents("getPlans"))
    }

    @Test
    fun `should deny creating shift with insufficient permissions`() {
        val request = mapOf(
            "query" to """
                        mutation { 
                            createShift(productionUuid: "${UUID.randomUUID()}", startTime: "12:00", endTime: "14:00") {
                                uuid   
                            } 
                        }
                       """.trimIndent(),
            "variables" to mapOf(
                "productionUuid" to 4711,
                "startTime" to "12:00",
                "endTime" to "14:00"
            )
        )

        val (accessToken, _) = super.doSuccessfulLogin().block()!!

        testClient.post()
            .uri(GRAPHQL_ENDPOINT)
            .accept(APPLICATION_JSON)
            .contentType(APPLICATION_JSON)
            .cookie("access_token", accessToken)
            .bodyValue(request)
            .exchange()
            .expectBody()
            .jsonPath("$.errors[0].message", "Exception while fetching data (createShift) : Role(s) ADMIN required")
    }
}
