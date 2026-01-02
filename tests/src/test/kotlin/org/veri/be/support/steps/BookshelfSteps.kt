package org.veri.be.support.steps

import com.jayway.jsonpath.JsonPath
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.veri.be.domain.book.dto.book.AddBookRequest

object BookshelfSteps {
    fun addBook(mockMvc: MockMvc, objectMapper: ObjectMapper, request: AddBookRequest): ResultActions {
        return mockMvc.perform(
            post("/api/v2/bookshelf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
    }

    fun createReadingId(mockMvc: MockMvc, objectMapper: ObjectMapper, request: AddBookRequest): Int {
        val responseString = addBook(mockMvc, objectMapper, request)
            .andReturn()
            .response
            .contentAsString
        return JsonPath.read(responseString, "$.result.memberBookId")
    }

    fun getMyBooks(mockMvc: MockMvc, params: Map<String, List<String>> = emptyMap()): ResultActions {
        val builder = get("/api/v2/bookshelf/my")
        params.forEach { (key, values) -> values.forEach { value -> builder.param(key, value) } }
        return mockMvc.perform(builder)
    }

    fun searchBooks(mockMvc: MockMvc, params: Map<String, String>): ResultActions {
        val builder = get("/api/v2/bookshelf/search")
        params.forEach { (key, value) -> builder.param(key, value) }
        return mockMvc.perform(builder)
    }

    fun getMyCount(mockMvc: MockMvc): ResultActions {
        return mockMvc.perform(get("/api/v2/bookshelf/my/count"))
    }

    fun modifyReading(mockMvc: MockMvc, id: Int, jsonBody: String): ResultActions {
        return mockMvc.perform(
            patch("/api/v2/bookshelf/$id/modify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
        )
    }

    fun rateReading(mockMvc: MockMvc, id: Int, jsonBody: String): ResultActions {
        return mockMvc.perform(
            patch("/api/v2/bookshelf/$id/rate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
        )
    }

    fun startReading(mockMvc: MockMvc, id: Int): ResultActions {
        return mockMvc.perform(patch("/api/v2/bookshelf/$id/status/start"))
    }

    fun finishReading(mockMvc: MockMvc, id: Int): ResultActions {
        return mockMvc.perform(patch("/api/v2/bookshelf/$id/status/over"))
    }

    fun updateVisibility(mockMvc: MockMvc, id: Int, isPublic: Boolean): ResultActions {
        return mockMvc.perform(
            patch("/api/v2/bookshelf/$id/visibility")
                .param("isPublic", isPublic.toString())
        )
    }

    fun deleteReading(mockMvc: MockMvc, id: Int): ResultActions {
        return mockMvc.perform(delete("/api/v2/bookshelf/$id"))
    }
}
