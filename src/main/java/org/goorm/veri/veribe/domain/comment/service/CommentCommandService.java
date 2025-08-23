package org.goorm.veri.veribe.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.comment.repository.CommentRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentCommandService {

    private final CommentRepository commentRepository;
}
