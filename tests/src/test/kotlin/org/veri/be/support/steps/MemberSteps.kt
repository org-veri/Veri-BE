package org.veri.be.support.steps

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.veri.be.domain.member.dto.UpdateMemberInfoRequest

object MemberSteps {
    fun getMyInfo(mockMvc: MockMvc): ResultActions {
        return mockMvc.perform(get("/api/v1/members/me"))
    }

    fun updateInfo(mockMvc: MockMvc, objectMapper: ObjectMapper, request: UpdateMemberInfoRequest): ResultActions {
        return mockMvc.perform(
            patch("/api/v1/members/me/info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
    }

    fun checkNickname(mockMvc: MockMvc, nickname: String?): ResultActions {
        val builder = get("/api/v1/members/nickname/exists")
        if (nickname != null) {
            builder.param("nickname", nickname)
        }
        return mockMvc.perform(builder)
    }
}
