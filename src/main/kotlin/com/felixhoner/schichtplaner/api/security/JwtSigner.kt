package com.felixhoner.schichtplaner.api.security

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.*
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
class JwtSigner(
	private val objectMapper: ObjectMapper
) {

	private val key = "secret".repeat(25)

	fun createJwt(userId: String, roles: List<String>): String {
		return Jwts.builder()
			.signWith(SignatureAlgorithm.HS512, key)
			.setSubject(userId)
			.setIssuer("identity")
			.setExpiration(Date.from(Instant.now().plus(Duration.ofMinutes(60))))
			.setIssuedAt(Date.from(Instant.now()))
			.claim("roles", roles)
			.compact()
	}

	/**
	 * Validate the JWT where it will throw an exception if it isn't valid.
	 */
	fun validateJwt(jwt: String): Jws<Claims> {
		return Jwts.parserBuilder()
			.setSigningKey(key)
			.build()
			.parseClaimsJws(jwt)
	}
}
