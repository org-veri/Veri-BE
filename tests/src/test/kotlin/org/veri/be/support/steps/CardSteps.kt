package org.veri.be.support.steps

import com.jayway.jsonpath.JsonPath
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.veri.be.domain.card.controller.dto.request.CardCreateRequest
import org.veri.be.domain.card.controller.dto.request.CardUpdateRequest

object CardSteps {
    fun requestCreateCard(mockMvc: MockMvc, objectMapper: ObjectMapper, request: CardCreateRequest): ResultActions {
        return mockMvc.perform(
            post("/api/v1/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
    }

    fun createCard(mockMvc: MockMvc, objectMapper: ObjectMapper, request: CardCreateRequest): Long {
        val response = requestCreateCard(mockMvc, objectMapper, request)
            .andReturn()
            .response
            .contentAsString
        val cardId: Number = JsonPath.read(response, "$.result.cardId")
        return cardId.toLong()
    }

    fun getMyCardCount(mockMvc: MockMvc): ResultActions {
        return mockMvc.perform(get("/api/v1/cards/my/count"))
    }

    fun getMyCards(mockMvc: MockMvc, params: Map<String, String>): ResultActions {
        val builder = get("/api/v1/cards/my")
        params.forEach { (key, value) -> builder.param(key, value) }
        return mockMvc.perform(builder)
    }

    fun getCardDetail(mockMvc: MockMvc, cardId: Long): ResultActions {
        return mockMvc.perform(get("/api/v1/cards/$cardId"))
    }

    fun updateCard(mockMvc: MockMvc, objectMapper: ObjectMapper, cardId: Long, request: CardUpdateRequest): ResultActions {
        return mockMvc.perform(
            patch("/api/v1/cards/$cardId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
    }

    fun deleteCard(mockMvc: MockMvc, cardId: Long): ResultActions {
        return mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/cards/$cardId"))
    }

    fun getCardFeed(mockMvc: MockMvc, params: Map<String, String> = emptyMap()): ResultActions {
        val builder = get("/api/v1/cards")
        params.forEach { (key, value) -> builder.param(key, value) }
        return mockMvc.perform(builder)
    }

    fun updateVisibility(mockMvc: MockMvc, cardId: Long, isPublic: Boolean): ResultActions {
        return mockMvc.perform(
            patch("/api/v1/cards/$cardId/visibility")
                .param("isPublic", isPublic.toString())
        )
    }

    fun requestPresignedUrl(mockMvc: MockMvc, objectMapper: ObjectMapper, url: String, size: Long): ResultActions {
        val request = org.veri.be.global.storage.dto.PresignedUrlRequest("image/png", size)
        return mockMvc.perform(
            post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
    }
}
