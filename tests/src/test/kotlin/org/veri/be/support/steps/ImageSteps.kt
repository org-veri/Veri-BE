package org.veri.be.support.steps

import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

object ImageSteps {
    fun ocrV0(mockMvc: MockMvc, imageUrl: String?): ResultActions {
        val builder = post("/api/v0/images/ocr")
        if (imageUrl != null) {
            builder.param("imageUrl", imageUrl)
        }
        return mockMvc.perform(builder)
    }

    fun ocrV1(mockMvc: MockMvc, imageUrl: String?): ResultActions {
        val builder = post("/api/v1/images/ocr")
        if (imageUrl != null) {
            builder.param("imageUrl", imageUrl)
        }
        return mockMvc.perform(builder)
    }
}
