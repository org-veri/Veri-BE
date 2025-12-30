package org.veri.be.integration.support.stub

import org.veri.be.global.storage.dto.PresignedPostFormResponse
import org.veri.be.global.storage.dto.PresignedUrlResponse
import org.veri.be.global.storage.service.StorageService
import java.time.Duration

class StubStorageService : StorageService {
    override fun generatePresignedUrl(
        contentType: String,
        contentLength: Long,
        prefix: String,
        duration: Duration
    ): PresignedUrlResponse = PresignedUrlResponse("http://stub.presigned.url", null)

    override fun generatePresignedUrlOfDefault(
        contentType: String,
        contentLength: Long
    ): PresignedUrlResponse = PresignedUrlResponse("http://stub.presigned.url", null)

    override fun generatePresignedPost(
        contentType: String,
        fileSize: Long,
        prefix: String,
        duration: Duration
    ): PresignedPostFormResponse =
        PresignedPostFormResponse("http://stub.s3.url", mapOf("key" to "value"))
}
