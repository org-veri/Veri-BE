package org.veri.be.support.steps

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.veri.be.domain.comment.dto.request.CommentEditRequest
import org.veri.be.domain.comment.dto.request.CommentPostRequest
import org.veri.be.domain.comment.dto.request.ReplyPostRequest

object CommentSteps {
    fun postComment(mockMvc: MockMvc, objectMapper: ObjectMapper, request: CommentPostRequest): ResultActions {
        return mockMvc.perform(
            post("/api/v1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
    }

    fun postReply(mockMvc: MockMvc, objectMapper: ObjectMapper, request: ReplyPostRequest): ResultActions {
        return mockMvc.perform(
            post("/api/v1/comments/reply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
    }

    fun editComment(mockMvc: MockMvc, objectMapper: ObjectMapper, commentId: Long, request: CommentEditRequest): ResultActions {
        return mockMvc.perform(
            patch("/api/v1/comments/$commentId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
    }

    fun deleteComment(mockMvc: MockMvc, commentId: Long): ResultActions {
        return mockMvc.perform(delete("/api/v1/comments/$commentId"))
    }
}
