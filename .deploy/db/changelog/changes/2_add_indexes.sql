CREATE UNIQUE INDEX uk_book_isbn ON book (isbn);
CREATE UNIQUE INDEX uk_member_nickname ON member (nickname);
CREATE UNIQUE INDEX uk_reading_member_book ON reading (member_id, book_id);
CREATE UNIQUE INDEX uk_post_like_post_member ON post_like (post_id, member_id);

CREATE INDEX idx_reading_member_status_created_at ON reading (member_id, status, created_at);
CREATE INDEX idx_reading_created_at_book_id ON reading (created_at, book_id);
CREATE INDEX idx_post_public_created_at ON post (is_public, created_at);
CREATE INDEX idx_card_member_created_at ON card (member_id, created_at);
CREATE INDEX idx_post_image_post_display_order ON post_image (post_id, display_order);
CREATE INDEX idx_comment_post_parent_created_at ON comment (post_id, parent_id, created_at);
