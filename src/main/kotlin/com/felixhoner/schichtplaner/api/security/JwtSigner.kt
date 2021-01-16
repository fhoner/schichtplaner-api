package com.felixhoner.schichtplaner.api.security

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
class JwtSigner {

	@Value("\${authorization.key}")
	lateinit var key: String

	@Value("\${authorization.expireAfterMinutes.accessToken}")
	var accessTokenExpiredAfter: Long = 0

	@Value("\${authorization.expireAfterMinutes.refreshToken}")
	var refreshTokenExpiredAfter: Long = 0

	fun createAccessToken(userId: String, roles: List<String>): String {
		return Jwts.builder()
			.signWith(Keys.hmacShaKeyFor(key.toByteArray()))
			.setSubject(userId)
			.setIssuer("identity")
			.setExpiration(Date.from(Instant.now().plus(Duration.ofMinutes(accessTokenExpiredAfter))))
			.setIssuedAt(Date.from(Instant.now()))
			.claim("roles", roles)
			.claim("type", "access")
			.compact()
	}

	fun createRefreshToken(userId: String, roles: List<String>): String {
		return Jwts.builder()
			.signWith(Keys.hmacShaKeyFor(key.toByteArray()))
			.setSubject(userId)
			.setIssuer("identity")
			.setExpiration(Date.from(Instant.now().plus(Duration.ofMinutes(refreshTokenExpiredAfter))))
			.setIssuedAt(Date.from(Instant.now()))
			.claim("roles", roles)
			.claim("type", "refresh")
			.compact()
	}

	/**
	 * Validate the JWT where it will throw an exception if it isn't valid.
	 */
	fun validateJwt(jwt: String): Jws<Claims> {
		return Jwts.parserBuilder()
			.setSigningKey(Keys.hmacShaKeyFor(key.toByteArray()))
			.build()
			.parseClaimsJws(jwt)
	}
}
