package org.goorm.veri.veribe.domain.image.service;

import org.goorm.veri.veribe.domain.member.entity.Member;

public interface ImageCommandService {
    String processImageOcrAndSave(String imageUrl, Member member) throws Exception;
}
