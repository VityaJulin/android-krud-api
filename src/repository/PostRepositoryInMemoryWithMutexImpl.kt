package com.example.repository

import com.example.dto.UserResponseDto
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.example.model.PostModel
import com.example.model.Reaction
import com.example.model.ReactionType
import java.util.*

class PostRepositoryInMemoryWithMutexImpl : PostRepository {

    private var nextId = 1L
    private val items = mutableListOf<PostModel>()
    private val mutex = Mutex()

    override suspend fun getAll(): List<PostModel> {
        mutex.withLock {
            return items.reversed()
        }
    }

    override suspend fun getById(id: Long): PostModel? {
        mutex.withLock {
            return items.find { it.id == id }
        }
    }

    override suspend fun getByIds(ids: Collection<Long>): List<PostModel> {
        mutex.withLock {
            return items.filter { ids.contains(it.id) }
        }
    }

    override suspend fun save(item: PostModel): PostModel {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == item.id }) {
                -1 -> {
                    val copy = item.copy(id = nextId++)
                    items.add(copy)
                    copy
                }
                else -> {
                    val copy = items[index].copy(content = item.content)
                    items[index] = copy
                    copy
                }
            }
        }
    }

    override suspend fun removeByIdAndOwnerId(id: Long, ownerId: Long) {
        mutex.withLock {
            items.removeIf { it.id == id && it.ownerId == ownerId }
        }
    }

    override suspend fun likeById(id: Long, user: UserResponseDto): PostModel? {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == id }) {
                -1 -> null
                else -> {
                    val item = items[index]
                    val copy = item.copy(
                            likes = item.likes.plus(
                                    Reaction(
                                            user,
                                            Date().time,
                                            ReactionType.LIKE
                                    )
                            )
                    )
                    items[index] = copy
                    copy
                }
            }
        }
    }

    override suspend fun dislikeById(id: Long, user: UserResponseDto): PostModel? {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == id }) {
                -1 -> null
                else -> {
                    val item = items[index]
                    val copy = item.copy(
                            dislikes = item.dislikes.plus(
                                    Reaction(
                                            user,
                                            Date().time,
                                            ReactionType.DISLIKE
                                    )
                            )
                    )
                    items[index] = copy
                    copy
                }
            }
        }
    }

    override suspend fun repostById(id: Long, myId: Long): PostModel? {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == id }) {
                -1 -> null
                else -> {
                    val item = items[index]
                    val copy = item.copy(reposts = item.reposts)
                    items[index] = copy
                    copy
                }
            }
        }
    }

    override suspend fun getPostsByUserId(userId: Long): List<PostModel> {
        mutex.withLock {
            return items.filter { it.ownerId == userId }
        }
    }

    override suspend fun getStatisticById(postId: Long): List<Reaction> {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == postId }) {
                -1 -> emptyList()
                else -> {
                    val item = items[index]
                    item.likes.plus(item.dislikes).toList()
                }
            }
        }
    }
}

