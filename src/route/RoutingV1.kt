package com.example.route

import com.example.dto.AuthenticationRequestDto
import com.example.dto.PostRequestDto
import com.example.dto.RegistrationRequestDto
import com.example.dto.UserResponseDto
import com.example.model.UserModel
import com.example.service.FileService
import com.example.service.PostService
import com.example.service.UserService
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

class RoutingV1(
        private val staticPath: String,
        private val postService: PostService,
        private val fileService: FileService,
        private val userService: UserService
) {
    fun setup(configuration: Routing) {
        with(configuration) {
            route("/api/v1/") {
                static("/static") {
                    files(staticPath)
                }

                route("/") {
                    post("/registration") {
                        val input = call.receive<RegistrationRequestDto>()
                        val response = userService.register(input)
                        call.respond(response)
                    }

                    post("/authentication") {
                        val input = call.receive<AuthenticationRequestDto>()
                        val response = userService.authenticate(input)
                        call.respond(response)
                    }
                }

                authenticate {
                    route("/me") {
                        get {
                            val me = call.authentication.principal<UserModel>()!!
                            call.respond(UserResponseDto.fromModel(me))
                        }

                        get("users/{id}") {
                            val userId = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                    "id",
                                    "Long"
                            )
                            val me = call.authentication.principal<UserModel>()
                            val response = userService.getById(userId)
                            call.respond(response)
                        }

                        route("/media") {
                            post {
                                val multipart = call.receiveMultipart()
                                val response = fileService.save(multipart)
                                call.respond(response)
                            }
                        }
                    }

                    route("/posts") {
                        get {
                            val me = call.authentication.principal<UserModel>()!!
                            val response = postService.getAll(myId = me.id)
                            call.respond(response)
                        }
                        get("/recent") {
                            val me = call.authentication.principal<UserModel>()!!
                            val response = postService.getRecent(myId = me.id)
                            call.respond(response)
                        }
                        get("/before/{id}") {
                            val me = call.authentication.principal<UserModel>()!!
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                    "id",
                                    "Long"
                            )
                            val response = postService.getBefore(id, myId = me.id)
                            call.respond(response)
                        }
                        get("/after/{id}") {
                            val me = call.authentication.principal<UserModel>()!!
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                    "id",
                                    "Long"
                            )
                            val response = postService.getAfter(id, myId = me.id)
                            call.respond(response)
                        }
                        get("/{id}") {
                            val me = call.authentication.principal<UserModel>()!!
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                    "id",
                                    "Long"
                            )
                            val response = postService.getById(id, myId = me.id)
                            call.respond(response)
                        }
                        post {
                            val me = call.authentication.principal<UserModel>()!!
                            val input = call.receive<PostRequestDto>()
                            val response = postService.save(input, myId = me.id)
                            call.respond(response)
                        }
                        delete("/{id}") {
                            val me = call.authentication.principal<UserModel>()!!
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                    "id",
                                    "Long"
                            )
                            postService.removeById(id, myId = me.id)
                            call.respond(HttpStatusCode.NoContent)
                        }
                        post("/{id}/likes") {
                            val me = call.authentication.principal<UserModel>()!!
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                    "id",
                                    "Long"
                            )
                            val response = postService.likeById(id, myId = me.id)
                            call.respond(response)
                        }
                        delete("/{id}/likes") {
                            val me = call.authentication.principal<UserModel>()!!
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                    "id",
                                    "Long"
                            )
                            val response = postService.dislikeById(id, myId = me.id)
                            call.respond(response)
                        }

                        post("/{id}/dislikes") {
                            val me = call.authentication.principal<UserModel>()!!
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                    "id",
                                    "Long"
                            )
                            val response = postService.dislikeById(id, myId = me.id)
                            call.respond(response)
                        }

                        post("/{id}/reposts") {
                            val me = call.authentication.principal<UserModel>()!!
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                    "id",
                                    "Long"
                            )
                            val response = postService.repostById(id, myId = me.id)
                            call.respond(response)
                        }

                        get("user/{id}") {
                            val userId = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                    "id",
                                    "Long"
                            )
                            val me = call.authentication.principal<UserModel>()
                            val response = postService.getPostsByUserId(userId)
                            call.respond(response)
                        }

                        get("reactions/{id}") {
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                    "id",
                                    "Long"
                            )
                            val response = postService.getStatisticById(id)
                            call.respond(response)

                        }
                    }
                }
            }
        }
    }
}