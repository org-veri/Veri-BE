package org.veri.be.unit.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.veri.be.api.common.dto.MemberProfileResponse;
import org.veri.be.domain.comment.entity.Comment;
import org.veri.be.domain.comment.repository.CommentRepository;
import org.veri.be.domain.comment.service.CommentQueryService;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.post.dto.response.PostDetailResponse;
import org.veri.be.lib.exception.CommonErrorCode;
import org.veri.be.support.assertion.ExceptionAssertions;

@ExtendWith(MockitoExtension.class)
class CommentQueryServiceTest {

    @Mock
    CommentRepository commentRepository;

    CommentQueryService commentQueryService;

    @BeforeEach
    void setUp() {
        commentQueryService = new CommentQueryService(commentRepository);
    }

    @Nested
    @DisplayName("getCommentsByPostId")
    class GetCommentsByPostId {

        @Test
        @DisplayName("댓글과 대댓글을 응답으로 매핑한다")
        void mapsCommentsWithReplies() {
            Member author = member(1L, "author@test.com", "author");
            Member replier = member(2L, "replier@test.com", "replier");
            Comment reply = Comment.builder()
                    .id(2L)
                    .author(replier)
                    .content("reply")
                    .build();
            Comment root = Comment.builder()
                    .id(1L)
                    .author(author)
                    .content("root")
                    .replies(List.of(reply))
                    .build();

            given(commentRepository.findByPostIdWithRepliesAndAuthor(10L)).willReturn(List.of(root));

            List<PostDetailResponse.CommentResponse> responses = commentQueryService.getCommentsByPostId(10L);

            assertThat(responses).hasSize(1);
            PostDetailResponse.CommentResponse response = responses.get(0);
            assertThat(response.commentId()).isEqualTo(1L);
            assertThat(response.content()).isEqualTo("root");
            assertThat(response.author()).isEqualTo(MemberProfileResponse.from(author));
            assertThat(response.replies()).hasSize(1);
            assertThat(response.replies().get(0).commentId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("삭제된 댓글은 내용과 작성자를 마스킹한다")
        void masksDeletedComment() {
            Member author = member(1L, "author@test.com", "author");
            Comment deleted = Comment.builder()
                    .id(1L)
                    .author(author)
                    .content("root")
                    .deletedAt(LocalDateTime.now())
                    .build();

            given(commentRepository.findByPostIdWithRepliesAndAuthor(10L)).willReturn(List.of(deleted));

            List<PostDetailResponse.CommentResponse> responses = commentQueryService.getCommentsByPostId(10L);

            assertThat(responses).hasSize(1);
            PostDetailResponse.CommentResponse response = responses.get(0);
            assertThat(response.commentId()).isNull();
            assertThat(response.content()).isEqualTo("삭제된 댓글입니다.");
            assertThat(response.author()).isNull();
            assertThat(response.isDeleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("getCommentById")
    class GetCommentById {

        @Test
        @DisplayName("존재하지 않으면 NotFoundException을 던진다")
        void throwsWhenNotFound() {
            given(commentRepository.findById(1L)).willReturn(java.util.Optional.empty());

            ExceptionAssertions.assertApplicationException(
                    () -> commentQueryService.getCommentById(1L),
                    CommonErrorCode.RESOURCE_NOT_FOUND
            );
        }

        @Test
        @DisplayName("댓글을 조회한다")
        void returnsComment() {
            Comment comment = Comment.builder().id(1L).content("content").build();
            given(commentRepository.findById(1L)).willReturn(java.util.Optional.of(comment));

            Comment found = commentQueryService.getCommentById(1L);

            assertThat(found.getId()).isEqualTo(1L);
            verify(commentRepository).findById(1L);
        }
    }

    private Member member(Long id, String email, String nickname) {
        return Member.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .profileImageUrl("https://example.com/profile.png")
                .providerId("provider-" + nickname)
                .providerType(ProviderType.KAKAO)
                .build();
    }
}
