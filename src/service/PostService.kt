package com.example.service

import com.example.dto.PostRequestDto
import com.example.dto.PostResponseDto
import com.example.dto.StatisticResponseDto
import com.example.dto.UserResponseDto
import com.example.exception.InvalidOwnerException
import com.example.model.AttachmentModel
import com.example.model.MediaType
import com.example.model.PostModel
import com.example.repository.PostRepository
import io.ktor.features.*

class PostService(private val repo: PostRepository, private val userService: UserService, private val resultSize: Int) {
    suspend fun getAll(myId: Long): List<PostResponseDto> {
        val posts = repo.getAll()
        return combinePostsDto(posts, myId)
    }

    suspend fun getRecent(myId: Long): List<PostResponseDto> {
        val posts = repo.getAll().take(resultSize)
        return combinePostsDto(posts, myId)
    }

    suspend fun getBefore(id: Long, myId: Long): List<PostResponseDto> {
        val posts = repo.getAll().asSequence().filter { it.id < id }.take(resultSize).toList()
        return combinePostsDto(posts, myId)
    }

    suspend fun getAfter(id: Long, myId: Long): List<PostResponseDto> {
        val posts = repo.getAll().asSequence().filter { it.id > id }.take(resultSize).toList()
        return combinePostsDto(posts, myId)
    }

    suspend fun getById(id: Long, myId: Long): PostResponseDto {
        val post = repo.getById(id) ?: throw NotFoundException()
        return combinePostDto(post, myId)
    }

    suspend fun save(input: PostRequestDto, myId: Long): PostResponseDto {
        val model = PostModel(
                id = input.id,
                ownerId = myId,
                content = input.content,
                link = input.link,
                attachment = input.attachmentId?.let {
                    AttachmentModel(input.attachmentId, mediaType = MediaType.IMAGE)
                })

        if (input.id != 0L) {
            // concurrency issues ignored
            val existing = repo.getById(input.id)!!
            if (existing.ownerId != myId) {
                throw InvalidOwnerException()
            }
        }

        val post = repo.save(model)
        val owners = listOf(userService.getById(myId))
        val postDto = mapToPostDto(post, null, owners, myId)

        return postDto
    }

    suspend fun removeById(id: Long, myId: Long) {
        repo.removeByIdAndOwnerId(id, ownerId = myId)
    }

    suspend fun likeById(id: Long, myId: Long): PostResponseDto {
        val post = repo.likeById(id, myId) ?: throw NotFoundException()
        return combinePostDto(post, myId)
    }

    suspend fun dislikeById(id: Long, myId: Long): PostResponseDto {
        val post = repo.dislikeById(id, myId) ?: throw NotFoundException()
        return combinePostDto(post, myId)
    }

    suspend fun repostById(id: Long, myId: Long): PostResponseDto {
        val post = repo.repostById(id, myId) ?: throw NotFoundException()
        return combinePostDto(post, myId)
    }

    suspend fun getPostsByUserId(userId: Long): List<PostResponseDto> {
        val posts = repo.getAll()
        return combinePostsDto(posts, userId)
    }

    suspend fun getStatisticById(id: Long): List<StatisticResponseDto> {
        return repo.getStatisticById(id).map { StatisticResponseDto.fromModel(it) }
    }

    private fun mapToSourceDto(post: PostModel, owners: List<UserResponseDto>, myId: Long): PostResponseDto {
        return PostResponseDto.from(
                post = post,
                source = null,
                owner = owners.find { it.id == post.ownerId } ?: UserResponseDto.unknown(),
                likedByMe = post.likes.contains(myId),
                dislikedByMe = post.dislikes.contains(myId)
        )
    }

    private fun mapToPostDto(post: PostModel, sourcesDto: List<PostResponseDto>, owners: List<UserResponseDto>, myId: Long): PostResponseDto {
        return PostResponseDto.from(
                post = post,
                source = sourcesDto.find { it.id == post.sourceId },
                owner = owners.find { it.id == post.ownerId } ?: UserResponseDto.unknown(),
                likedByMe = post.likes.contains(myId),
                dislikedByMe = post.dislikes.contains(myId)
        )
    }

    private fun mapToPostDto(post: PostModel, sourceDto: PostResponseDto?, owners: List<UserResponseDto>, myId: Long): PostResponseDto {
        return PostResponseDto.from(
                post = post,
                source = sourceDto,
                owner = owners.find { it.id == post.ownerId } ?: UserResponseDto.unknown(),
                likedByMe = post.likes.contains(myId),
                dislikedByMe = post.dislikes.contains(myId)
        )
    }

    private suspend fun combinePostDto(post: PostModel, myId: Long): PostResponseDto {
        val source = post.sourceId?.let { repo.getById(it) }

        val owners = userService.getByIds(listOfNotNull(post.ownerId, source?.ownerId))

        val sourceDto = source?.let { mapToSourceDto(it, owners, myId) }
        val postDto = mapToPostDto(post, sourceDto, owners, myId)

        return postDto
    }


    private suspend fun combinePostsDto(posts: List<PostModel>, myId: Long): List<PostResponseDto> {
        val sources = repo.getByIds(posts.asSequence().map { it.sourceId }.filterNotNull().toList())
        val ownerIds = (posts + sources).map { it.ownerId }
        val owners = userService.getByIds(ownerIds)

        val sourcesDto = sources.map { mapToSourceDto(it, owners, myId) }
        val postsDto = posts.map { mapToPostDto(it, sourcesDto, owners, myId) }

        return postsDto
    }
}