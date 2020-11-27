package com.example

import com.example.dto.RegistrationRequestDto
import com.example.exception.ConfigurationException
import com.example.repository.PostRepository
import com.example.repository.PostRepositoryInMemoryWithMutexImpl
import com.example.repository.UserRepository
import com.example.repository.UserRepositoryInMemoryWithMutexImpl
import com.example.route.RoutingV1
import com.example.service.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import kotlinx.coroutines.runBlocking
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton
import org.kodein.di.generic.instance
import org.kodein.di.generic.with
import org.kodein.di.ktor.KodeinFeature
import org.kodein.di.ktor.kodein
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
            serializeNulls()
        }
    }

    install(StatusPages) {
        exception<NotImplementedError> {
            call.respond(HttpStatusCode.NotImplemented)
            throw it
        }
        exception<ParameterConversionException> {
            call.respond(HttpStatusCode.BadRequest)
            throw it
        }
        exception<Throwable> {
            call.respond(HttpStatusCode.InternalServerError)
            throw it
        }
    }

    install(KodeinFeature) {
        constant(tag = "upload-dir") with (environment.config.propertyOrNull("example.upload.dir")?.getString()
            ?: throw ConfigurationException("Upload dir is not specified"))
        constant(tag = "result-size") with (environment.config.propertyOrNull("example.api.result-size")?.getString()?.toInt()
            ?: throw ConfigurationException("API result size is not specified"))
        constant(tag = "jwt-secret") with (environment.config.propertyOrNull("example.jwt.secret")?.getString()
            ?: throw ConfigurationException("JWT Secret is not specified"))
        constant(tag = "fcm-password") with (environment.config.propertyOrNull("example.fcm.password")?.getString()
            ?: throw ConfigurationException("FCM Password is not specified"))
        constant(tag = "fcm-salt") with (environment.config.propertyOrNull("example.fcm.salt")?.getString()
            ?: throw ConfigurationException("FCM Salt is not specified"))
        constant(tag = "fcm-db-url") with (environment.config.propertyOrNull("example.fcm.db-url")?.getString()
            ?: throw ConfigurationException("FCM DB Url is not specified"))
        constant(tag = "fcm-path") with (environment.config.propertyOrNull("example.fcm.path")?.getString()
            ?: throw ConfigurationException("FCM JSON Path is not specified"))
        bind<PasswordEncoder>() with eagerSingleton { BCryptPasswordEncoder() }
        bind<JWTTokenService>() with eagerSingleton { JWTTokenService(instance(tag = "jwt-secret")) }
        bind<PostRepository>() with eagerSingleton { PostRepositoryInMemoryWithMutexImpl() }
        bind<PostService>() with eagerSingleton { PostService(instance(), instance(), instance(tag = "result-size")) }
        bind<FileService>() with eagerSingleton { FileService(instance(tag = "upload-dir")) }
        bind<UserRepository>() with eagerSingleton { UserRepositoryInMemoryWithMutexImpl() }
        bind<UserService>() with eagerSingleton {
            UserService(instance(), instance(), instance()).also {
                runBlocking {
                    it.register(RegistrationRequestDto("vasya", "password"))
                }
            }
        }
        bind<FCMService>() with eagerSingleton {
            FCMService(
                instance(tag = "fcm-db-url"),
                instance(tag = "fcm-password"),
                instance(tag = "fcm-salt"),
                instance(tag = "fcm-path")
            ).also {
                runBlocking {
                    // FIXME: PUT TOKEN FROM DEVICE HERE FOR DEMO PURPOSES
                    it.send(1, "cZAXHjs1S1Sg7yQrh0zHTX:APA91bGG4-WFJlK7kvsEBTFtCa_wEY2LowpRIrTRet44OEyWGunLlWEmX-g-zN79MpA2OnnW0FBb_tlojAZF_r3BoWY3ngUX8t8UKGColHLzpfzfgBqnRlyn5EKyfELFamY89kCGLPrJ", "Your post liked!")
                }
            }
        }
        bind<RoutingV1>() with eagerSingleton {
            RoutingV1(
                instance(tag = "upload-dir"),
                instance(),
                instance(),
                instance()
            )
        }
    }

    install(Authentication) {
        jwt {
            val jwtService by kodein().instance<JWTTokenService>()
            verifier(jwtService.verifier)
            val userService by kodein().instance<UserService>()

            validate {
                val id = it.payload.getClaim("id").asLong()
                userService.getModelById(id)
            }
        }
    }

    install(Routing) {
        val routingV1 by kodein().instance<RoutingV1>()
        routingV1.setup(this)
    }
}

