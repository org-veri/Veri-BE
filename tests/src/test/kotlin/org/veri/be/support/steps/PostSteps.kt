package org.veri.be.support.steps

import com.jayway.jsonpath.JsonPath
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.veri.be.domain.post.dto.request.PostCreateRequest

object PostSteps {
    fun requestCreatePost(mockMvc: MockMvc, objectMapper: ObjectMapper, request: PostCreateRequest): ResultActions {
        return mockMvc.perform(
            post("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
    }

    fun createPost(mockMvc: MockMvc, objectMapper: ObjectMapper, request: PostCreateRequest): Long {
        val response = requestCreatePost(mockMvc, objectMapper, request)
            .andReturn()
            .response
            .contentAsString
        val postId: Number = JsonPath.read(response, "$.result")
        return postId.toLong()
    }

    fun getMyPosts(mockMvc: MockMvc): ResultActions {
        return mockMvc.perform(get("/api/v1/posts/my"))
    }

    fun getFeed(mockMvc: MockMvc, params: Map<String, String> = emptyMap()): ResultActions {
        val builder = get("/api/v1/posts")
        params.forEach { (key, value) -> builder.param(key, value) }
        return mockMvc.perform(builder)
    }

    fun getDetail(mockMvc: MockMvc, postId: Long): ResultActions {
        return mockMvc.perform(get("/api/v1/posts/$postId"))
    }

    fun deletePost(mockMvc: MockMvc, postId: Long): ResultActions {
        return mockMvc.perform(delete("/api/v1/posts/$postId"))
    }

    fun requestPresignedUrl(mockMvc: MockMvc, objectMapper: ObjectMapper, request: org.veri.be.global.storage.dto.PresignedUrlRequest): ResultActions {
        return mockMvc.perform(
            post("/api/v1/posts/image")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
    }
}
