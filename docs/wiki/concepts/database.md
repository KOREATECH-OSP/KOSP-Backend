# 💾 데이터베이스 스키마 (Database Schema)

프로젝트의 ERD는 **[dbdiagram.io](https://dbdiagram.io/)**를 통해 관리됩니다.
아래의 [DBML 소스 코드](#dbml-source-code)를 사용하여 ERD를 수정하거나 최신화할 수 있습니다.

## 1. ERD
<iframe width="100%" height="600" src="https://dbdiagram.io/e/6953276e39fa3db27bc581d4/6953277e39fa3db27bc58220" frameborder="0" allowfullscreen></iframe>

---

## 2. DBML Source Code
dbdiagram.io에 복사/붙여넣기하여 사용할 수 있는 소스 코드입니다.

<details>
<summary>📂 <strong>Click to view DBML Code</strong></summary>

```dbml
// ==========================================
// 1. MySQL - Main Schema (kosp)
// ==========================================
Table users {
  id bigint [pk, increment]
  created_at timestamp
  updated_at timestamp
  name varchar
  kut_id varchar [unique, note: '교내 학번']
  kut_email varchar [unique, note: '교내 이메일']
  password varchar
  introduction varchar
  github_id bigint [ref: - github_user.github_id]
  is_deleted bit
}

Table github_user {
  github_id bigint [pk]
  created_at timestamp
  updated_at timestamp
  github_login varchar
  github_name varchar
  github_avatar_url varchar
  github_token text
  last_crawling datetime
}

Table role {
  id bigint [pk, increment]
  name varchar [unique]
  description varchar
}

Table policy {
  id bigint [pk, increment]
  name varchar [unique]
  description varchar
}

Table permission {
  id bigint [pk, increment]
  name varchar [unique]
  description varchar
}

Table user_role {
  user_id bigint [pk, ref: > users.id]
  role_id bigint [pk, ref: > role.id]
}

Table role_policy {
  role_id bigint [pk, ref: > role.id]
  policy_id bigint [pk, ref: > policy.id]
}

Table policy_permission {
  policy_id bigint [pk, ref: > policy.id]
  permission_id bigint [pk, ref: > permission.id]
}

// Community Domain
Table board {
  id bigint [pk, increment]
  name varchar
  description varchar
  is_recruit_allowed bit
}

Table article {
  id bigint [pk, increment]
  title varchar
  content mediumtext
  board_id bigint [ref: > board.id]
  author_id bigint [ref: > users.id]
  views int
  likes int
  comments_count int
  dtype varchar
  is_deleted bit
  is_pinned bit
}

Table article_tags {
  article_id bigint [ref: > article.id]
  tag varchar
}

Table comment {
  id bigint [pk, increment]
  content text
  article_id bigint [ref: > article.id]
  author_id bigint [ref: > users.id]
  likes int
}

Table article_like {
  id bigint [pk, increment]
  user_id bigint [ref: > users.id]
  article_id bigint [ref: > article.id]
}

Table article_bookmark {
  id bigint [pk, increment]
  user_id bigint [ref: > users.id]
  article_id bigint [ref: > article.id]
}

Table comment_like {
  id bigint [pk, increment]
  user_id bigint [ref: > users.id]
  comment_id bigint [ref: > comment.id]
}

// Team & Recruit
Table team {
  id bigint [pk, increment]
  name varchar
  description varchar
  image_url varchar
}

Table team_member {
  id bigint [pk, increment]
  team_id bigint [ref: > team.id]
  user_id bigint [ref: > users.id]
  role varchar
}

Table recruit {
  id bigint [pk, ref: - article.id]
  team_id bigint [ref: > team.id]
  status varchar
  start_date timestamp
  end_date timestamp
}

Table recruit_apply {
  id bigint [pk, increment]
  recruit_id bigint [ref: > recruit.id]
  user_id bigint [ref: > users.id]
  status varchar
  reason varchar
  portfolio_url varchar
}

// Challenge & Report
Table challenge {
  id bigint [pk, increment]
  name varchar
  description varchar
  condition text
  tier int
  image_url varchar
}

Table challenge_history {
  id bigint [pk, increment]
  user_id bigint [ref: > users.id]
  challenge_id bigint [ref: > challenge.id]
  is_achieved bit
  achieved_at timestamp
}

Table report {
  id bigint [pk, increment]
  reporter_id bigint [ref: > users.id]
  target_type varchar
  target_id bigint
  reason varchar
  description text
  status varchar
  processed_at timestamp
}

// ==========================================
// 2. MySQL - Batch Schema (kosp_batch)
// ==========================================
Table BATCH_JOB_INSTANCE {
  JOB_INSTANCE_ID bigint [pk]
  VERSION bigint
  JOB_NAME varchar
  JOB_KEY varchar [unique]
}

Table BATCH_JOB_EXECUTION {
  JOB_EXECUTION_ID bigint [pk]
  VERSION bigint
  JOB_INSTANCE_ID bigint [ref: > BATCH_JOB_INSTANCE.JOB_INSTANCE_ID]
  CREATE_TIME datetime
  START_TIME datetime
  END_TIME datetime
  STATUS varchar
  EXIT_CODE varchar
}

Table BATCH_JOB_EXECUTION_PARAMS {
  JOB_EXECUTION_ID bigint [ref: > BATCH_JOB_EXECUTION.JOB_EXECUTION_ID]
  PARAMETER_NAME varchar
  PARAMETER_TYPE varchar
  PARAMETER_VALUE varchar
  IDENTIFYING char
}

Table BATCH_STEP_EXECUTION {
  STEP_EXECUTION_ID bigint [pk]
  VERSION bigint
  JOB_EXECUTION_ID bigint [ref: > BATCH_JOB_EXECUTION.JOB_EXECUTION_ID]
  STEP_NAME varchar
  START_TIME datetime
  END_TIME datetime
  STATUS varchar
  COMMIT_COUNT bigint
  READ_COUNT bigint
  FILTER_COUNT bigint
  WRITE_COUNT bigint
  READ_SKIP_COUNT bigint
  WRITE_SKIP_COUNT bigint
  PROCESS_SKIP_COUNT bigint
  ROLLBACK_COUNT bigint
  EXIT_CODE varchar
}

// ==========================================
// 3. MongoDB (Document Store)
// ==========================================
TableGroup MongoDB {
  // Not strictly relational, but represented for visualization
  GithubProfile
  GithubRepository
  GithubTrend
}

Table GithubProfile {
  _id ObjectId [pk]
  githubId Long
  username String
  bio String
  tier Integer
  followers Integer
  following Integer
  score Double
}

Table GithubRepository {
  _id String [pk]
  ownerId Long [note: 'Refers to GithubProfile.githubId']
  name String
  url String
  language String
  stars Integer
  forks Integer
}

Table GithubTrend {
  _id String [pk]
  githubId Long
  period String
  commits Integer
  pullRequests Integer
  issues Integer
}
```
</details>
