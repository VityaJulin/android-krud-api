package com.example.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.example.model.UserModel

class UserRepositoryInMemoryWithMutexImpl : UserRepository {
    private var nextId = 1L
    private val items = mutableListOf<UserModel>()
    private val mutex = Mutex()

    override suspend fun getAll(): List<UserModel> {
        mutex.withLock {
            return items.toList()
        }
    }

    override suspend fun getById(id: Long): UserModel? {
        mutex.withLock {
            return items.find { it.id == id }
        }
    }

    override suspend fun getByIds(ids: Collection<Long>): List<UserModel> {
        mutex.withLock {
            return items.filter { ids.contains(it.id) }
        }
    }

    override suspend fun getByUsername(username: String): UserModel? {
        mutex.withLock {
            return items.find { it.username == username }
        }
    }

    override suspend fun saveByNameIfNotExists(item: UserModel): UserModel? {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.username == item.username }) {
                -1 -> {
                    val copy = item.copy(id = nextId++)
                    items.add(copy)
                    copy
                }
                else -> null
            }
        }
    }

}

