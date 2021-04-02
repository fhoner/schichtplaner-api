package com.felixhoner.schichtplaner.api.business.service

import com.felixhoner.schichtplaner.api.business.exception.InvalidCredentialsException
import com.felixhoner.schichtplaner.api.business.exception.InvalidTokenException
import com.felixhoner.schichtplaner.api.business.exception.NotFoundException
import com.felixhoner.schichtplaner.api.business.model.User
import com.felixhoner.schichtplaner.api.graphql.dto.UserRoleDto
import com.felixhoner.schichtplaner.api.persistence.entity.UserEntity
import com.felixhoner.schichtplaner.api.persistence.entity.UserRole
import com.felixhoner.schichtplaner.api.persistence.repository.UserRepository
import com.felixhoner.schichtplaner.api.security.JwtSigner
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import reactor.test.StepVerifier


class UserServiceTest {

    private val userRepository: UserRepository = mockk()
    private val jwtSigner: JwtSigner = mockk()
    private val passwordEncoder: BCryptPasswordEncoder = mockk()
    private val transformer: TransformerBo = mockk()

    private val cut: UserService = UserService(
        userRepository,
        jwtSigner,
        passwordEncoder,
        transformer
    )

    val user: UserEntity = UserEntity(email = "max@mustermann", password = "pw", role = UserRole.READER)

    @Nested
    inner class Login {

        @Test
        fun `should create access and refresh token if credentials are valid`() {
            every { userRepository.findByEmail(any()) } returns user
            every { passwordEncoder.matches(any(), any()) } returns true
            every { jwtSigner.createAccessToken(any(), any()) } returns "access"
            every { jwtSigner.createRefreshToken(any(), any()) } returns "refresh"

            val result = cut.login("max@mustermann", "pw")
            StepVerifier.create(result)
                .expectNext(Pair("access", "refresh"))
                .verifyComplete()
            verify { userRepository.findByEmail("max@mustermann") }
            verify { passwordEncoder.matches("pw", "pw") }
            verify { jwtSigner.createAccessToken("max@mustermann", UserRole.READER.get().toList()) }
            verify { jwtSigner.createRefreshToken("max@mustermann", UserRole.READER.get().toList()) }
        }

        @Test
        fun `login should fail if user was not found`() {
            every { userRepository.findByEmail(any()) } returns null

            val result = cut.login("max@mustermann", "pw")
            StepVerifier.create(result)
                .expectErrorMatches { it is InvalidCredentialsException && it.message == "Username and password do not match" }
                .verify()
            verify(exactly = 0) { jwtSigner.createRefreshToken(any(), any()) }
            verify(exactly = 0) { jwtSigner.createAccessToken(any(), any()) }
        }

        @Test
        fun `login should fail if password does not match`() {
            every { userRepository.findByEmail(any()) } returns user
            every { passwordEncoder.matches(any(), any()) } returns false

            val result = cut.login("max@mustermann", "wrongPw")
            StepVerifier.create(result)
                .expectErrorMatches { it is InvalidCredentialsException && it.message == "Username and password do not match" }
                .verify()
            verify(exactly = 0) { jwtSigner.createRefreshToken(any(), any()) }
            verify(exactly = 0) { jwtSigner.createAccessToken(any(), any()) }
        }

    }

    @Nested
    inner class GetUser {

        @Test
        fun `should get user by email successfully`() {
            val userBo: User = mockk()
            every { userRepository.findByEmail(any()) } returns user
            every { transformer.toBo(any<UserEntity>()) } returns userBo

            val result = cut.getUserByEmail("max@mustermann")
            StepVerifier.create(result)
                .expectNext(userBo)
                .verifyComplete()
            verify { userRepository.findByEmail("max@mustermann") }
            verify { transformer.toBo(user) }
        }

        @Test
        fun `should return error if user was not found`() {
            every { userRepository.findByEmail(any()) } returns null

            val result = cut.getUserByEmail("max@mustermann")
            StepVerifier.create(result)
                .expectErrorMatches { it is NotFoundException && it.message == "User with email max@mustermann not found" }
                .verify()
            verify { userRepository.findByEmail("max@mustermann") }
            verify(exactly = 0) { transformer.toBo(user) }
        }

    }

    @Nested
    inner class RefreshToken {

        @Test
        fun `should get new refresh token successfully`() {
            val jws: Jws<Claims> = mockk()
            val claims: Claims = mockk()
            every { jws.body } returns claims
            every { claims["type"] } returns "refresh"
            every { claims["roles"] } returns listOf("READER")
            every { claims.subject } returns "max@mustermann"
            every { jwtSigner.validateJwt(any()) } returns jws
            every { jwtSigner.createRefreshToken(any(), any()) } returns "newRefreshToken"

            val result = cut.refreshToken("acbxyz", TokenType.REFRESH)
            StepVerifier.create(result)
                .expectNext("newRefreshToken")
                .verifyComplete()
            verify { jwtSigner.validateJwt("acbxyz") }
            verify { jwtSigner.createRefreshToken("max@mustermann", listOf("READER")) }
            verify(exactly = 0) { jwtSigner.createAccessToken(any(), any()) }
        }

        @Test
        fun `should get new access token successfully`() {
            val jws: Jws<Claims> = mockk()
            val claims: Claims = mockk()
            every { jws.body } returns claims
            every { claims["type"] } returns "refresh"
            every { claims["roles"] } returns listOf("READER")
            every { claims.subject } returns "max@mustermann"
            every { jwtSigner.validateJwt(any()) } returns jws
            every { jwtSigner.createAccessToken(any(), any()) } returns "newAccessToken"

            val result = cut.refreshToken("acbxyz", TokenType.ACCESS)
            StepVerifier.create(result)
                .expectNext("newAccessToken")
                .verifyComplete()
            verify { jwtSigner.validateJwt("acbxyz") }
            verify { jwtSigner.createAccessToken("max@mustermann", listOf("READER")) }
            verify(exactly = 0) { jwtSigner.createRefreshToken(any(), any()) }
        }

        @Test
        fun `should fail if token is invalid`() {
            every { jwtSigner.validateJwt(any()) } throws Exception()

            val result = cut.refreshToken("acbxyz", TokenType.ACCESS)
            StepVerifier.create(result)
                .expectErrorMatches { it is InvalidTokenException }
                .verify()
            verify { jwtSigner.validateJwt("acbxyz") }
            verify(exactly = 0) { jwtSigner.createRefreshToken(any(), any()) }
            verify(exactly = 0) { jwtSigner.createAccessToken(any(), any()) }
        }

        @Test
        fun `should fail if claims type is not refresh`() {
            val jws: Jws<Claims> = mockk()
            val claims: Claims = mockk()
            every { jws.body } returns claims
            every { claims["type"] } returns "anything"
            every { jwtSigner.validateJwt(any()) } returns jws

            val result = cut.refreshToken("abcxyz", TokenType.ACCESS)
            StepVerifier.create(result)
                .expectErrorMatches { it is InvalidTokenException }
                .verify()
        }

    }

    @Test
    fun `should create user successfully`() {
        val userEntity: UserEntity = mockk()
        val userBo: User = mockk()
        val savedUser = slot<UserEntity>()
        every { userRepository.save(capture(savedUser)) } returns userEntity
        every { transformer.toBo(any<UserEntity>()) } returns userBo
        every { transformer.toBo(any<UserRoleDto>()) } returns UserRole.READER
        every { passwordEncoder.encode("pw") } returns "pwhash"

        val result = cut.createUser("max@mustermann", "pw", UserRoleDto.READER)
        StepVerifier.create(result)
            .expectNext(userBo)
            .verifyComplete()
        verify { passwordEncoder.encode("pw") }
        verify { transformer.toBo(UserRoleDto.READER) }
        savedUser.captured.apply {
            email shouldBe "max@mustermann"
            password shouldBe "pwhash"
            role shouldBe UserRole.READER
        }
        verify { transformer.toBo(userEntity) }
    }

}
