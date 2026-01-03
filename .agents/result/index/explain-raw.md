# Result: EXPLAIN Raw

## Metadata
- **Status**: Completed
- **Date**: 2026-01-03
- **Source Request**: 적용 전/1차/2차 EXPLAIN 원본 출력 정리

## Body
## SQL 1

```sql
EXPLAIN SELECT b.book_id, r.id, b.title, b.author, b.image, r.score, r.started_at, r.status, r.is_public
FROM reading r
JOIN book b ON r.book_id = b.book_id
WHERE r.member_id = 1 AND r.status IN (0,1,2)
ORDER BY r.created_at DESC
LIMIT 20 OFFSET 0;
```

### 적용 전

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	r	NULL	ref	fk_reading_book,fk_reading_member	fk_reading_member	9	const	2	30.00	Using where; Using filesort
1	SIMPLE	b	NULL	eq_ref	PRIMARY	PRIMARY	8	veri.r.book_id	1	100.00	NULL
```

### 1차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	r	NULL	ref	uk_reading_member_book,fk_reading_book,idx_reading_member_status_created_at	uk_reading_member_book	9	const	2	30.00	Using index condition; Using where; Using filesort
1	SIMPLE	b	NULL	eq_ref	PRIMARY	PRIMARY	8	veri.r.book_id	1	100.00	NULL
```

### 2차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	r	NULL	ref	uk_reading_member_book,fk_reading_book,idx_reading_member_status_created_at,idx_reading_member_created_at	idx_reading_member_created_at	9	const	2	30.00	Using where
1	SIMPLE	b	NULL	eq_ref	PRIMARY	PRIMARY	8	veri.r.book_id	1	100.00	NULL
```

## SQL 2

```sql
EXPLAIN SELECT b.book_id, r.id, b.title, b.author, b.image, r.score, r.started_at, r.status, r.is_public
FROM reading r
JOIN book b ON r.book_id = b.book_id
WHERE r.member_id = 1 AND r.status IN (0,1,2)
ORDER BY r.score DESC
LIMIT 20 OFFSET 0;
```

### 적용 전

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	r	NULL	ref	fk_reading_book,fk_reading_member	fk_reading_member	9	const	2	30.00	Using where; Using filesort
1	SIMPLE	b	NULL	eq_ref	PRIMARY	PRIMARY	8	veri.r.book_id	1	100.00	NULL
```

### 1차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	r	NULL	ref	uk_reading_member_book,fk_reading_book,idx_reading_member_status_created_at	uk_reading_member_book	9	const	2	30.00	Using index condition; Using where; Using filesort
1	SIMPLE	b	NULL	eq_ref	PRIMARY	PRIMARY	8	veri.r.book_id	1	100.00	NULL
```

### 2차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	r	NULL	ref	uk_reading_member_book,fk_reading_book,idx_reading_member_status_created_at,idx_reading_member_created_at	uk_reading_member_book	9	const	2	30.00	Using index condition; Using where; Using filesort
1	SIMPLE	b	NULL	eq_ref	PRIMARY	PRIMARY	8	veri.r.book_id	1	100.00	NULL
```

## SQL 3

```sql
EXPLAIN SELECT COUNT(*) FROM reading r WHERE r.status = 2 AND r.member_id = 1;
```

### 적용 전

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	r	NULL	ref	fk_reading_member	fk_reading_member	9	const	2	10.00	Using where
```

### 1차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	r	NULL	ref	uk_reading_member_book,idx_reading_member_status_created_at	idx_reading_member_status_created_at	11	const,const	1	100.00	Using index
```

### 2차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	r	NULL	ref	uk_reading_member_book,idx_reading_member_status_created_at,idx_reading_member_created_at	idx_reading_member_status_created_at	11	const,const	1	100.00	Using index
```

## SQL 4

```sql
EXPLAIN SELECT r.id FROM reading r WHERE r.member_id = 1 AND r.book_id = 1 LIMIT 1;
```

### 적용 전

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	r	NULL	ref	fk_reading_book,fk_reading_member	fk_reading_book	9	const	1	16.67	Using where
```

### 1차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	NULL	NULL	NULL	NULL	NULL	NULL	NULL	NULL	NULL	no matching row in const table
```

### 2차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	NULL	NULL	NULL	NULL	NULL	NULL	NULL	NULL	NULL	no matching row in const table
```

## SQL 5

```sql
EXPLAIN SELECT r.id
FROM reading r
JOIN book b ON r.book_id = b.book_id
WHERE r.member_id = 1 AND b.title = 't' AND b.author = 'a'
LIMIT 1;
```

### 적용 전

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	r	NULL	ref	fk_reading_book,fk_reading_member	fk_reading_member	9	const	2	100.00	Using where
1	SIMPLE	b	NULL	eq_ref	PRIMARY	PRIMARY	8	veri.r.book_id	1	8.33	Using where
```

### 1차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	r	NULL	ref	uk_reading_member_book,fk_reading_book,idx_reading_member_status_created_at	uk_reading_member_book	9	const	2	100.00	Using where; Using index
1	SIMPLE	b	NULL	eq_ref	PRIMARY	PRIMARY	8	veri.r.book_id	1	8.33	Using where
```

### 2차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	r	NULL	ref	uk_reading_member_book,fk_reading_book,idx_reading_member_status_created_at,idx_reading_member_created_at	uk_reading_member_book	9	const	2	100.00	Using where; Using index
1	SIMPLE	b	NULL	eq_ref	PRIMARY	PRIMARY	8	veri.r.book_id	1	8.33	Using where
```

## SQL 6

```sql
EXPLAIN SELECT b.image, b.title, b.author, b.publisher, b.isbn
FROM reading r
JOIN book b ON r.book_id = b.book_id
WHERE r.created_at >= '2026-01-01' AND r.created_at < '2026-01-08'
GROUP BY r.book_id
ORDER BY COUNT(*) DESC
LIMIT 20;
```

### 적용 전

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	r	NULL	ALL	fk_reading_book	NULL	NULL	NULL	12	11.11	Using where; Using temporary; Using filesort
1	SIMPLE	b	NULL	eq_ref	PRIMARY	PRIMARY	8	veri.r.book_id	1	100.00	NULL
```

### 1차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	r	NULL	range	uk_reading_member_book,fk_reading_book,idx_reading_created_at_book_id	idx_reading_created_at_book_id	9	NULL	1	100.00	Using where; Using index; Using temporary; Using filesort
1	SIMPLE	b	NULL	eq_ref	PRIMARY	PRIMARY	8	veri.r.book_id	1	100.00	NULL
```

### 2차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	r	NULL	range	uk_reading_member_book,fk_reading_book,idx_reading_created_at_book_id	idx_reading_created_at_book_id	9	NULL	1	100.00	Using where; Using index; Using temporary; Using filesort
1	SIMPLE	b	NULL	eq_ref	PRIMARY	PRIMARY	8	veri.r.book_id	1	100.00	NULL
```

## SQL 7

```sql
EXPLAIN SELECT c.card_id, b.title, c.content, c.image, c.created_at, c.is_public
FROM card c
JOIN reading r ON c.reading_id = r.id
JOIN book b ON r.book_id = b.book_id
WHERE c.member_id = 1
ORDER BY c.created_at DESC
LIMIT 20;
```

### 적용 전

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	c	NULL	ref	fk_card_member,fk_card_reading	fk_card_member	9	const	2	100.00	Using where; Using filesort
1	SIMPLE	r	NULL	eq_ref	PRIMARY,fk_reading_book	PRIMARY	8	veri.c.reading_id	1	100.00	Using where
1	SIMPLE	b	NULL	eq_ref	PRIMARY	PRIMARY	8	veri.r.book_id	1	100.00	NULL
```

### 1차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	c	NULL	ref	fk_card_reading,idx_card_member_created_at	idx_card_member_created_at	9	const	2	100.00	Using where; Backward index scan
1	SIMPLE	r	NULL	eq_ref	PRIMARY,fk_reading_book	PRIMARY	8	veri.c.reading_id	1	100.00	Using where
1	SIMPLE	b	NULL	eq_ref	PRIMARY	PRIMARY	8	veri.r.book_id	1	100.00	NULL
```

### 2차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	c	NULL	ref	fk_card_reading,idx_card_member_created_at	idx_card_member_created_at	9	const	2	100.00	Using where; Backward index scan
1	SIMPLE	r	NULL	eq_ref	PRIMARY,fk_reading_book	PRIMARY	8	veri.c.reading_id	1	100.00	Using where
1	SIMPLE	b	NULL	eq_ref	PRIMARY	PRIMARY	8	veri.r.book_id	1	100.00	NULL
```

## SQL 8

```sql
EXPLAIN SELECT c.card_id, c.member_id, b.title, c.content, c.image, c.created_at, c.is_public
FROM card c
JOIN reading r ON c.reading_id = r.id
JOIN book b ON r.book_id = b.book_id
WHERE c.is_public = 1
ORDER BY c.created_at DESC
LIMIT 20;
```

### 적용 전

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	c	NULL	ALL	fk_card_reading	NULL	NULL	NULL	12	50.00	Using where; Using filesort
1	SIMPLE	r	NULL	eq_ref	PRIMARY,fk_reading_book	PRIMARY	8	veri.c.reading_id	1	100.00	Using where
1	SIMPLE	b	NULL	eq_ref	PRIMARY	PRIMARY	8	veri.r.book_id	1	100.00	NULL
```

### 1차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	c	NULL	ALL	fk_card_reading	NULL	NULL	NULL	12	50.00	Using where; Using filesort
1	SIMPLE	r	NULL	eq_ref	PRIMARY,fk_reading_book	PRIMARY	8	veri.c.reading_id	1	100.00	Using where
1	SIMPLE	b	NULL	eq_ref	PRIMARY	PRIMARY	8	veri.r.book_id	1	100.00	NULL
```

### 2차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	c	NULL	ref	fk_card_reading,idx_card_public_created_at	idx_card_public_created_at	1	const	2	100.00	Using where
1	SIMPLE	r	NULL	eq_ref	PRIMARY,fk_reading_book	PRIMARY	8	veri.c.reading_id	1	100.00	Using where
1	SIMPLE	b	NULL	eq_ref	PRIMARY	PRIMARY	8	veri.r.book_id	1	100.00	NULL
```

## SQL 9

```sql
EXPLAIN SELECT p.post_id, p.title, p.content, pi.image_url, p.member_id, p.book_id,
(SELECT COUNT(*) FROM post_like l WHERE l.post_id = p.post_id) AS like_count,
(SELECT COUNT(*) FROM comment c WHERE c.post_id = p.post_id) AS comment_count,
p.created_at, p.is_public
FROM post p
LEFT JOIN post_image pi ON pi.post_id = p.post_id AND pi.display_order = 1
WHERE p.is_public = 1
ORDER BY p.created_at DESC
LIMIT 20;
```

### 적용 전

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	PRIMARY	p	NULL	ALL	NULL	NULL	NULL	NULL	3	50.00	Using where; Using filesort
1	PRIMARY	pi	NULL	ref	fk_post_image_post	fk_post_image_post	8	veri.p.post_id	1	100.00	Using where
3	DEPENDENT SUBQUERY	c	NULL	ref	fk_comment_post	fk_comment_post	9	veri.p.post_id	1	100.00	Using index
2	DEPENDENT SUBQUERY	l	NULL	ref	fk_post_like_post	fk_post_like_post	8	veri.p.post_id	1	100.00	Using index
```

### 1차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	PRIMARY	p	NULL	ref	idx_post_public_created_at	idx_post_public_created_at	2	const	3	100.00	Backward index scan
1	PRIMARY	pi	NULL	ref	idx_post_image_post_display_order	idx_post_image_post_display_order	16	veri.p.post_id,const	1	100.00	NULL
3	DEPENDENT SUBQUERY	c	NULL	ref	idx_comment_post_parent_created_at	idx_comment_post_parent_created_at	9	veri.p.post_id	1	100.00	Using index
2	DEPENDENT SUBQUERY	l	NULL	ref	uk_post_like_post_member	uk_post_like_post_member	8	veri.p.post_id	1	100.00	Using index
```

### 2차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	PRIMARY	p	NULL	ref	idx_post_public_created_at	idx_post_public_created_at	2	const	3	100.00	Backward index scan
1	PRIMARY	pi	NULL	ref	idx_post_image_post_display_order	idx_post_image_post_display_order	16	veri.p.post_id,const	1	100.00	NULL
3	DEPENDENT SUBQUERY	c	NULL	ref	idx_comment_post_parent_created_at	idx_comment_post_parent_created_at	9	veri.p.post_id	1	100.00	Using index
2	DEPENDENT SUBQUERY	l	NULL	ref	uk_post_like_post_member	uk_post_like_post_member	8	veri.p.post_id	1	100.00	Using index
```

## SQL 10

```sql
EXPLAIN SELECT p.post_id, p.title, p.content, pi.image_url, p.member_id, p.book_id,
(SELECT COUNT(*) FROM post_like l WHERE l.post_id = p.post_id) AS like_count,
(SELECT COUNT(*) FROM comment c WHERE c.post_id = p.post_id) AS comment_count,
p.created_at, p.is_public
FROM post p
LEFT JOIN post_image pi ON pi.post_id = p.post_id AND pi.display_order = 1
WHERE p.member_id = 1
ORDER BY p.created_at DESC
LIMIT 20;
```

### 적용 전

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	PRIMARY	p	NULL	ref	fk_post_member	fk_post_member	9	const	1	100.00	Using filesort
1	PRIMARY	pi	NULL	ref	fk_post_image_post	fk_post_image_post	8	veri.p.post_id	1	100.00	Using where
3	DEPENDENT SUBQUERY	c	NULL	ref	fk_comment_post	fk_comment_post	9	veri.p.post_id	1	100.00	Using index
2	DEPENDENT SUBQUERY	l	NULL	ref	fk_post_like_post	fk_post_like_post	8	veri.p.post_id	1	100.00	Using index
```

### 1차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	PRIMARY	p	NULL	ref	fk_post_member	fk_post_member	9	const	1	100.00	Using filesort
1	PRIMARY	pi	NULL	ref	idx_post_image_post_display_order	idx_post_image_post_display_order	16	veri.p.post_id,const	1	100.00	NULL
3	DEPENDENT SUBQUERY	c	NULL	ref	idx_comment_post_parent_created_at	idx_comment_post_parent_created_at	9	veri.p.post_id	1	100.00	Using index
2	DEPENDENT SUBQUERY	l	NULL	ref	uk_post_like_post_member	uk_post_like_post_member	8	veri.p.post_id	1	100.00	Using index
```

### 2차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	PRIMARY	p	NULL	ref	idx_post_member_created_at	idx_post_member_created_at	9	const	1	100.00	NULL
1	PRIMARY	pi	NULL	ref	idx_post_image_post_display_order	idx_post_image_post_display_order	16	veri.p.post_id,const	1	100.00	NULL
3	DEPENDENT SUBQUERY	c	NULL	ref	idx_comment_post_parent_created_at	idx_comment_post_parent_created_at	9	veri.p.post_id	1	100.00	Using index
2	DEPENDENT SUBQUERY	l	NULL	ref	uk_post_like_post_member	uk_post_like_post_member	8	veri.p.post_id	1	100.00	Using index
```

## SQL 11

```sql
EXPLAIN SELECT c.comment_id, c.created_at
FROM comment c
WHERE c.post_id = 1 AND c.parent_id IS NULL
ORDER BY c.created_at ASC
LIMIT 100;
```

### 적용 전

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	c	NULL	ref	fk_comment_parent,fk_comment_post	fk_comment_post	9	const	1	100.00	Using where; Using filesort
```

### 1차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	c	NULL	ref	fk_comment_parent,idx_comment_post_parent_created_at	idx_comment_post_parent_created_at	18	const,const	1	100.00	Using where; Using index
```

### 2차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	c	NULL	ref	fk_comment_parent,idx_comment_post_parent_created_at	idx_comment_post_parent_created_at	18	const,const	1	100.00	Using where; Using index
```

## SQL 12

```sql
EXPLAIN SELECT COUNT(*) FROM post_like l WHERE l.post_id = 1;
```

### 적용 전

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	l	NULL	ref	fk_post_like_post	fk_post_like_post	8	const	3	100.00	Using index
```

### 1차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	l	NULL	ref	uk_post_like_post_member	uk_post_like_post_member	8	const	3	100.00	Using index
```

### 2차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	l	NULL	ref	uk_post_like_post_member	uk_post_like_post_member	8	const	3	100.00	Using index
```

## SQL 13

```sql
EXPLAIN SELECT 1 FROM post_like l WHERE l.post_id = 1 AND l.member_id = 1 LIMIT 1;
```

### 적용 전

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	l	NULL	ref	fk_post_like_post,fk_post_like_member	fk_post_like_member	9	const	1	75.00	Using where
```

### 1차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	l	NULL	const	uk_post_like_post_member,fk_post_like_member	uk_post_like_post_member	17	const,const	1	100.00	Using index
```

### 2차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	l	NULL	const	uk_post_like_post_member,fk_post_like_member	uk_post_like_post_member	17	const,const	1	100.00	Using index
```

## SQL 14

```sql
EXPLAIN SELECT i.image_url FROM image i WHERE i.member_id = 1 LIMIT 20;
```

### 적용 전

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	i	NULL	ref	fk_image_member	fk_image_member	9	const	5	100.00	NULL
```

### 1차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	i	NULL	ref	fk_image_member	fk_image_member	9	const	5	100.00	NULL
```

### 2차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	i	NULL	ref	fk_image_member	fk_image_member	9	const	5	100.00	NULL
```

## SQL 15

```sql
EXPLAIN SELECT 1 FROM member m WHERE m.provider_id = 'pid' AND m.provider_type = 'KAKAO' LIMIT 1;
```

### 적용 전

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	m	NULL	ALL	NULL	NULL	NULL	NULL	4	25.00	Using where
```

### 1차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	m	NULL	ALL	NULL	NULL	NULL	NULL	4	25.00	Using where
```

### 2차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	m	NULL	ALL	NULL	NULL	NULL	NULL	4	25.00	Using where
```

## SQL 16

```sql
EXPLAIN SELECT 1 FROM member m WHERE m.nickname = 'nick' LIMIT 1;
```

### 적용 전

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	m	NULL	ALL	NULL	NULL	NULL	NULL	4	25.00	Using where
```

### 1차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	NULL	NULL	NULL	NULL	NULL	NULL	NULL	NULL	NULL	no matching row in const table
```

### 2차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	NULL	NULL	NULL	NULL	NULL	NULL	NULL	NULL	NULL	no matching row in const table
```

## SQL 17

```sql
EXPLAIN SELECT b.book_id FROM book b WHERE b.isbn = 'isbn' LIMIT 1;
```

### 적용 전

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	b	NULL	ALL	NULL	NULL	NULL	NULL	12	10.00	Using where
```

### 1차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	NULL	NULL	NULL	NULL	NULL	NULL	NULL	NULL	NULL	no matching row in const table
```

### 2차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	NULL	NULL	NULL	NULL	NULL	NULL	NULL	NULL	NULL	no matching row in const table
```

## SQL 18

```sql
EXPLAIN SELECT COUNT(*) FROM card c WHERE c.member_id = 1;
```

### 적용 전

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	c	NULL	ref	fk_card_member	fk_card_member	9	const	2	100.00	Using index
```

### 1차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	c	NULL	ref	idx_card_member_created_at	idx_card_member_created_at	9	const	2	100.00	Using index
```

### 2차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	c	NULL	ref	idx_card_member_created_at	idx_card_member_created_at	9	const	2	100.00	Using index
```

## SQL 19

```sql
EXPLAIN SELECT COUNT(*) FROM reading r WHERE r.member_id = 1;
```

### 적용 전

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	r	NULL	ref	fk_reading_member	fk_reading_member	9	const	2	100.00	Using index
```

### 1차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	r	NULL	ref	uk_reading_member_book,idx_reading_member_status_created_at	uk_reading_member_book	9	const	2	100.00	Using index
```

### 2차 개선

```text
id	select_type	table	partitions	type	possible_keys	key	key_len	ref	rows	filtered	Extra
1	SIMPLE	r	NULL	ref	uk_reading_member_book,idx_reading_member_status_created_at,idx_reading_member_created_at	uk_reading_member_book	9	const	2	100.00	Using index
```
