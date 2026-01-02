package org.veri.be.support.steps

import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

object SocialReadingSteps {
    fun getPopular(mockMvc: MockMvc): ResultActions {
        return mockMvc.perform(get("/api/v2/bookshelf/popular"))
    }

    fun getDetail(mockMvc: MockMvc, readingId: Long): ResultActions {
        return mockMvc.perform(get("/api/v2/bookshelf/$readingId"))
    }
}
