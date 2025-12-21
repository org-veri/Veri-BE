package org.veri.be.unit.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.veri.be.domain.comment.dto.request.CommentPostRequest;
import org.veri.be.domain.comment.entity.Comment;
import org.veri.be.domain.comment.repository.CommentRepository;
import org.veri.be.domain.comment.service.CommentCommandService;
import org.veri.be.domain.comment.service.CommentQueryService;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.post.entity.Post;
import org.veri.be.domain.post.service.PostQueryService;

@ExtendWith(MockitoExtension.class)
class CommentCommandServiceTest {

    @Mock
    CommentRepository commentRepository;

    @Mock
    CommentQueryService commentQueryService;

    @Mock
    PostQueryService postQueryService;

    Clock fixedClock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneId.of("UTC"));

    CommentCommandService commentCommandService;

    @Captor
    ArgumentCaptor<Comment> commentCaptor;

    @BeforeEach
    void setUp() {
        commentCommandService = new CommentCommandService(
                commentRepository,
                commentQueryService,
                postQueryService,
                fixedClock
        );
    }

    @Nested
    @DisplayName("postComment")
    class PostComment {

        @Test
        @DisplayName("댓글을 저장하고 ID를 반환한다")
        void savesComment() {
            Member member = member(1L, "member@test.com", "member");
            Post post = Post.builder().id(10L).title("title").content("content").build();
            CommentPostRequest request = new CommentPostRequest(10L, "content");

            given(postQueryService.getPostById(10L)).willReturn(post);
            given(commentRepository.save(org.mockito.ArgumentMatchers.any(Comment.class)))
                    .willAnswer(invocation -> {
                        Comment saved = invocation.getArgument(0);
                        ReflectionTestUtils.setField(saved, "id", 1L);
                        return saved;
                    });

            Long result = commentCommandService.postComment(request, member);

            verify(commentRepository).save(commentCaptor.capture());
            Comment saved = commentCaptor.getValue();
            assertThat(saved.getPost()).isEqualTo(post);
            assertThat(saved.getAuthor()).isEqualTo(member);
            assertThat(saved.getContent()).isEqualTo("content");
            assertThat(result).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("postReply")
    class PostReply {

        @Test
        @DisplayName("대댓글을 저장하고 ID를 반환한다")
        void savesReply() {
            Member member = member(2L, "reply@test.com", "reply");
            Post post = Post.builder().id(10L).title("title").content("content").build();
            Comment parent = Comment.builder().id(5L).post(post).author(member).content("parent").build();

            given(commentQueryService.getCommentById(5L)).willReturn(parent);
            given(commentRepository.save(org.mockito.ArgumentMatchers.any(Comment.class)))
                    .willAnswer(invocation -> {
                        Comment saved = invocation.getArgument(0);
                        ReflectionTestUtils.setField(saved, "id", 6L);
                        return saved;
                    });

            Long result = commentCommandService.postReply(5L, "reply", member);

            verify(commentRepository).save(commentCaptor.capture());
            Comment saved = commentCaptor.getValue();
            assertThat(saved.getParent()).isEqualTo(parent);
            assertThat(saved.getPost()).isEqualTo(post);
            assertThat(saved.getAuthor()).isEqualTo(member);
            assertThat(saved.getContent()).isEqualTo("reply");
            assertThat(result).isEqualTo(6L);
        }
    }

    @Nested
    @DisplayName("editComment")
    class EditComment {

        @Test
        @DisplayName("댓글 내용을 수정한다")
        void editsComment() {
            Member member = member(1L, "member@test.com", "member");
            Comment comment = Comment.builder().id(1L).author(member).content("before").build();

            given(commentQueryService.getCommentById(1L)).willReturn(comment);

            commentCommandService.editComment(1L, "after", member);

            verify(commentRepository).save(commentCaptor.capture());
            assertThat(commentCaptor.getValue().getContent()).isEqualTo("after");
        }
    }

    @Nested
    @DisplayName("deleteComment")
    class DeleteComment {

        @Test
        @DisplayName("댓글을 삭제 처리한다")
        void deletesComment() {
            Member member = member(1L, "member@test.com", "member");
            Comment comment = Comment.builder().id(1L).author(member).content("content").build();

            given(commentQueryService.getCommentById(1L)).willReturn(comment);

            commentCommandService.deleteComment(1L, member);

            verify(commentRepository).save(commentCaptor.capture());
            assertThat(commentCaptor.getValue().isDeleted()).isTrue();
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
