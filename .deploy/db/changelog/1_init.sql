CREATE DATABASE IF NOT EXISTS veri;
USE veri;

CREATE TABLE `blacklisted_token`
(
    `token`      VARCHAR(512) NOT NULL,
    `expired_at` DATETIME(6)  NOT NULL,
    PRIMARY KEY (`token`)
);

CREATE TABLE `refresh_token`
(
    `user_id`    BIGINT       NOT NULL,
    `expired_at` DATETIME(6)  NOT NULL,
    `token`      VARCHAR(512) NOT NULL,
    PRIMARY KEY (`user_id`)
);

CREATE TABLE `ocr_result`
(
    `ocr_result_id`     BIGINT AUTO_INCREMENT,
    `image_url`         VARCHAR(2083) NOT NULL,
    `ocr_service`       VARCHAR(255)  NULL,
    `pre_processed_url` VARCHAR(2083) NULL,
    `result_text`       TEXT          NOT NULL,
    PRIMARY KEY (`ocr_result_id`)
);

CREATE TABLE `member`
(
    `member_id`     BIGINT AUTO_INCREMENT,
    `created_at`    DATETIME(6)    NULL,
    `updated_at`    DATETIME(6)    NULL,
    `email`         VARCHAR(255)   NULL,
    `nickname`      VARCHAR(255)   NULL,
    `image`         VARCHAR(2083)  NOT NULL,
    `provider_id`   VARCHAR(255)   NULL,
    `provider_type` ENUM ('KAKAO') NULL,
    PRIMARY KEY (`member_id`),
    CONSTRAINT uk_member_provider UNIQUE (provider_id, provider_type)
);

CREATE TABLE `book`
(
    `book_id`    BIGINT AUTO_INCREMENT,
    `created_at` DATETIME(6)   NULL,
    `updated_at` DATETIME(6)   NULL,
    `author`     VARCHAR(255)  NULL,
    `image`      VARCHAR(2083) NOT NULL,
    `isbn`       VARCHAR(255)  NULL,
    `publisher`  VARCHAR(255)  NULL,
    `title`      VARCHAR(255)  NULL,
    PRIMARY KEY (`book_id`)
);

CREATE TABLE `image`
(
    `image_id`   BIGINT AUTO_INCREMENT,
    `created_at` DATETIME(6)   NULL,
    `updated_at` DATETIME(6)   NULL,
    `image_url`  VARCHAR(2083) NOT NULL,
    `member_id`  BIGINT        NULL,
    PRIMARY KEY (`image_id`),
    CONSTRAINT `fk_image_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
);

CREATE TABLE `reading`
(
    `id`         BIGINT AUTO_INCREMENT,
    `created_at` DATETIME(6) NULL,
    `updated_at` DATETIME(6) NULL,
    `ended_at`   DATETIME(6) NULL,
    `is_public`  BIT         NULL,
    `score`      DOUBLE      NULL,
    `started_at` DATETIME(6) NULL,
    `status`     TINYINT     NULL,
    `book_id`    BIGINT      NULL,
    `member_id`  BIGINT      NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_reading_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`),
    CONSTRAINT `fk_reading_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
    CHECK (`status` BETWEEN 0 AND 2)
);

CREATE TABLE `card`
(
    `card_id`    BIGINT AUTO_INCREMENT,
    `created_at` DATETIME(6)   NULL,
    `updated_at` DATETIME(6)   NULL,
    `content`    TEXT          NULL,
    `image`      VARCHAR(2083) NOT NULL,
    `is_public`  BIT           NOT NULL,
    `member_id`  BIGINT        NULL,
    `reading_id` BIGINT        NULL,
    PRIMARY KEY (`card_id`),
    CONSTRAINT `fk_card_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
    CONSTRAINT `fk_card_reading` FOREIGN KEY (`reading_id`) REFERENCES `reading` (`id`) ON DELETE SET NULL
);

CREATE TABLE `post`
(
    `post_id`    BIGINT AUTO_INCREMENT,
    `created_at` DATETIME(6) NULL,
    `updated_at` DATETIME(6) NULL,
    `content`    TEXT        NOT NULL,
    `is_public`  BIT         NULL,
    `title`      VARCHAR(50) NOT NULL,
    `member_id`  BIGINT      NULL,
    `book_id`    BIGINT      NULL,
    PRIMARY KEY (`post_id`),
    CONSTRAINT `fk_post_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
    CONSTRAINT `fk_post_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`)
);

CREATE TABLE `post_image`
(
    `post_image_id` BIGINT AUTO_INCREMENT,
    `display_order` BIGINT       NOT NULL,
    `image_url`     VARCHAR(255) NOT NULL,
    `post_id`       BIGINT       NOT NULL,
    PRIMARY KEY (`post_image_id`),
    CONSTRAINT `fk_post_image_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`)
);

CREATE TABLE `post_like`
(
    `post_like_id` BIGINT AUTO_INCREMENT,
    `member_id`    BIGINT NULL,
    `post_id`      BIGINT NOT NULL,
    PRIMARY KEY (`post_like_id`),
    CONSTRAINT `fk_post_like_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`),
    CONSTRAINT `fk_post_like_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
);

CREATE TABLE `comment`
(
    `comment_id` BIGINT AUTO_INCREMENT,
    `created_at` DATETIME(6) NULL,
    `updated_at` DATETIME(6) NULL,
    `content`    TEXT        NOT NULL,
    `deleted_at` DATETIME(6) NULL,
    `member_id`  BIGINT      NULL,
    `parent_id`  BIGINT      NULL,
    `post_id`    BIGINT      NULL,
    PRIMARY KEY (`comment_id`),
    CONSTRAINT `fk_comment_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
    CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`),
    CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `comment` (`comment_id`)
);
