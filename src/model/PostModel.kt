package com.example.model

data class PostModel(
    val id: Long,
    val sourceId: Long? = null,
    val ownerId: Long,
    val author: UserModel,
    val content: String? = null,
    val created: Int = (System.currentTimeMillis() / 1000).toInt(),
    val likes: Set<Long> = setOf(),
    val dislikes: Set<Long> = setOf(),
    val reposts: Map<Long, Long> = mapOf(),
    val link: String? = null,
    val type: PostType = PostType.POST,
    val attachment: AttachmentModel?
)

enum class PostType {
    POST, REPOST
}
