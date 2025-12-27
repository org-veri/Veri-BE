package org.veri.be.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.veri.be.domain.book.client.BookSearchClient;
import org.veri.be.domain.image.service.OcrService;
import org.veri.be.global.storage.service.StorageService;
import org.veri.be.integration.support.stub.StubBookSearchClient;
import org.veri.be.integration.support.stub.StubOcrService;
import org.veri.be.integration.support.stub.StubStorageService;

@TestConfiguration
public class SharedTestConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    // MistralOcrService depends on OcrPort and Executor which are complex to stub directly 
    // if we want to replace the whole service. 
    // However, MistralOcrService is a class. 
    // The ImageCommandService injects `OcrService`.
    // We should override the OcrService bean.
    // But MistralOcrService is annotated with @Service.
    
    @Bean("mistralOcrService")
    @Primary
    public OcrService mistralOcrService() {
        return new StubOcrService();
    }

    @Bean
    @Primary
    public StorageService storageService() {
        return new StubStorageService();
    }

    @Bean
    @Primary
    public BookSearchClient bookSearchClient() {
        return new StubBookSearchClient();
    }
}
