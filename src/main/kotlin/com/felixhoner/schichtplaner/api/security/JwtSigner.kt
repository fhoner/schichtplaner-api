package com.felixhoner.schichtplaner.api.security

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
class JwtSigner {

	private val key =
		Keys.hmacShaKeyFor("3ruDg8e2OPHWIJrMEdwahJFFBmVLwRdxis7TFPkml-Sb2Hqr9HbhKcEpBSHS_91ohipPeCuOGvCU97FJFLvD3RsRPyqN5eN2hehT87XnRRYSSMG91hd3P_5h4jPGijBfAqW_66tEFqtMG6hmK6EzgkZdT1Dy1fpddxl-aTw2KZt3PH_nJ1K9jCjkNIRCOSHv5Et8olr-jDDoi9MSqE1hg-zXWFmETWWv-aIDxfRcWVuj_i1jCrfpSCBcStRhI-rao3j6lBppYRrE9MkGzNCAbyiflxK6iwYek63yxAky2stlYj5oKuIh-4tLASWOyBHJY4vjr5wZ2fCJ2KePrUMNnw".toByteArray())

	fun createAccessToken(userId: String, roles: List<String>): String {
		return Jwts.builder()
			.signWith(key)
			.setSubject(userId)
			.setIssuer("identity")
			.setExpiration(Date.from(Instant.now().plus(Duration.ofMinutes(15))))
			.setIssuedAt(Date.from(Instant.now()))
			.claim("roles", roles)
			.claim("type", "access")
			.compact()
	}

	fun createRefreshToken(userId: String, roles: List<String>): String {
		return Jwts.builder()
			.signWith(key)
			.setSubject(userId)
			.setIssuer("identity")
			.setExpiration(Date.from(Instant.now().plus(Duration.ofMinutes(1440))))
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
			.setSigningKey(key)
			.build()
			.parseClaimsJws(jwt)
	}
}
