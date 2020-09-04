package com.example.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

class JWTTokenService(private val secret: String) {
    private val algo = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT.require(algo).build()

    fun generate(id: Long): String = JWT.create()
        .withClaim("id", id)
//        TODO: Uncomment this to enable expiration
//        .withExpiresAt(Date(System.currentTimeMillis() + 24 * 3600 * 1000))
        .sign(algo)
}