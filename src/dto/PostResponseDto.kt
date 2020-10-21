package com.example.dto

import com.example.model.PostModel
import com.example.model.PostType

data class PostResponseDto(
        val id: Long,
        val source: PostResponseDto? = null,
        val ownerId: Long,
        val author: UserResponseDto,
        val ownerName: String,
        val created: Int,
        val content: String? = null,
        val likes: Int = 0,
        val likedByMe: Boolean = false,
        val dislikes: Int = 0,
        val dislikedByMe: Boolean = false,
        val reposts: Int = 0,
        val repostedByMe: Boolean = false,
        val link: String? = null,
        val type: PostType = PostType.POST,
        val attachment: AttachmentResponseDto?
) {
    companion object {
        fun from(
                post: PostModel,
                source: PostResponseDto?,
                owner: UserResponseDto,
                likedByMe: Boolean = false,
                dislikedByMe: Boolean = false,
                repostedByMe: Boolean = false
        ) = PostResponseDto(
                id = post.id,
                source = source,
                ownerId = owner.id,
                ownerName = owner.username,
                author = UserResponseDto.fromModel(postAuthor),
                created = post.created,
                content = post.content,
                likes = post.likes.size,
                likedByMe = likedByMe,
                dislikes = post.dislikes.size,
                dislikedByMe = dislikedByMe,
                reposts = post.reposts.size,
                repostedByMe = repostedByMe,
                link = post.link,
                type = post.type,
                attachment = post.attachment?.let { AttachmentResponseDto.fromModel(post.attachment) }
        )
    }
}

