package org.veri.be.support

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

open class ControllerTestSupport {
    protected lateinit var mockMvc: MockMvc
    protected val objectMapper: ObjectMapper = ObjectMapper().findAndRegisterModules()

    protected fun postJson(url: String, request: Any): ResultActions {
        return mockMvc.perform(
            MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
    }

    protected fun post(url: String): ResultActions {
        return mockMvc.perform(MockMvcRequestBuilders.post(url))
    }

    protected fun putJson(url: String, request: Any): ResultActions {
        return mockMvc.perform(
            MockMvcRequestBuilders.put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
    }

    protected fun patchJson(url: String, request: Any): ResultActions {
        return mockMvc.perform(
            MockMvcRequestBuilders.patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
    }

    protected fun patch(url: String): ResultActions {
        return mockMvc.perform(MockMvcRequestBuilders.patch(url))
    }

    protected fun get(url: String, params: Map<String, String> = emptyMap()): ResultActions {
        val builder = MockMvcRequestBuilders.get(url)
        params.forEach { (key, value) -> builder.param(key, value) }
        return mockMvc.perform(builder)
    }

    protected fun delete(url: String): ResultActions {
        return mockMvc.perform(MockMvcRequestBuilders.delete(url))
    }
}
