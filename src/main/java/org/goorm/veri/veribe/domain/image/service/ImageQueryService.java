package org.goorm.veri.veribe.domain.image.service;

import org.goorm.veri.veribe.domain.image.dto.response.PageResponse;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface ImageQueryService {
    PageResponse<List<String>> fetchUploadedImages(Long memberId, Pageable pageable);
}
