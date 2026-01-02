package org.veri.be.support.steps

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.veri.be.global.auth.dto.ReissueTokenRequest

object AuthSteps {
    fun reissueToken(mockMvc: MockMvc, objectMapper: ObjectMapper, request: ReissueTokenRequest): ResultActions {
        return mockMvc.perform(
            post("/api/v1/auth/reissue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
    }

    fun logout(mockMvc: MockMvc, accessToken: String?): ResultActions {
        val builder = post("/api/v1/auth/logout")
        if (accessToken != null) {
            builder.requestAttr("token", accessToken)
        }
        return mockMvc.perform(builder)
    }
}
