# 数据库设计 — Business Config v0.1

> 对应规格：`specs/v0.1/domain-model.md`
> 本文件定义 MySQL 表结构、索引与数据访问模式（Design 层，How）。
> 层说明：Spec 层见 `specs/v0.1/domain-model.md`；索引见 `specs/v0.1/README.md`。

## 1. ER 关系

```text
config_page
    │ 1:N
    ▼
config_group
    │ 1:N
    ▼
config_item
```

## 2. 建表语句

```sql
CREATE TABLE config_page (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(128)  NOT NULL,
    name        VARCHAR(128)  NOT NULL,
    description VARCHAR(512)  NULL,
    status      VARCHAR(32)   NOT NULL DEFAULT 'ACTIVE',
    sort        INT           NOT NULL DEFAULT 0,
    created_at  DATETIME      NOT NULL,
    updated_at  DATETIME      NOT NULL,
    deleted     TINYINT(1)    NOT NULL DEFAULT 0,
    CONSTRAINT uk_config_page_code UNIQUE (code)
);

CREATE TABLE config_group (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    page_id     BIGINT        NOT NULL,
    code        VARCHAR(128)  NOT NULL,
    name        VARCHAR(128)  NOT NULL,
    description VARCHAR(512)  NULL,
    status      VARCHAR(32)   NOT NULL DEFAULT 'ACTIVE',
    sort        INT           NOT NULL DEFAULT 0,
    created_at  DATETIME      NOT NULL,
    updated_at  DATETIME      NOT NULL,
    deleted     TINYINT(1)    NOT NULL DEFAULT 0,
    CONSTRAINT uk_config_group_page_code UNIQUE (page_id, code),
    CONSTRAINT fk_config_group_page FOREIGN KEY (page_id) REFERENCES config_page (id)
);

CREATE TABLE config_item (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id       BIGINT        NOT NULL,
    `key`          VARCHAR(256)  NOT NULL,
    name           VARCHAR(128)  NOT NULL,
    value          TEXT          NULL,
    default_value  TEXT          NULL,
    value_type     VARCHAR(32)   NOT NULL,
    component_type VARCHAR(32)   NOT NULL,
    options        TEXT          NULL,
    description    VARCHAR(512)  NULL,
    required       TINYINT(1)    NOT NULL DEFAULT 0,
    status         VARCHAR(32)   NOT NULL DEFAULT 'ACTIVE',
    sort           INT           NOT NULL DEFAULT 0,
    created_at     DATETIME      NOT NULL,
    updated_at     DATETIME      NOT NULL,
    deleted        TINYINT(1)    NOT NULL DEFAULT 0,
    CONSTRAINT uk_config_item_key UNIQUE (`key`),
    CONSTRAINT fk_config_item_group FOREIGN KEY (group_id) REFERENCES config_group (id)
);
```

要点：

- `deleted` 列：MyBatis-Plus `@TableLogic` 逻辑删除标记（0 未删除 / 1 已删除）
- `key` 为 SQL 保留字，使用反引号
- `value` / `default_value` / `options` 用 TEXT（可存 JSON / 长文本）
- 主键自增，外键建索引

## 3. 索引设计

| 索引 | 列 | 目的 |
| --- | --- | --- |
| uk_config_page_code | code | Page 编码唯一（业务契约） |
| uk_config_group_page_code | (page_id, code) | Group 分组唯一 |
| uk_config_item_key | `key` | Item 配置 Key 唯一（业务访问主键） |

## 4. 数据访问模式

V1 直接读 MySQL，不引入 Redis 缓存（需求书 §23.3 允许）。

高频读取路径：

- 按 key 查单条：`WHERE key = ? AND status = 'ACTIVE'`（走 uk_config_item_key）
- 按前缀批量：`WHERE key LIKE 'prefix.%' AND status = 'ACTIVE'`
- 按 keys 批量：`WHERE key IN (...)` 

> 大数据量下 LIKE 前缀需前缀索引，V1 配置表量小，暂不建。

## 5. 种子数据

对齐需求书 §14 订单配置示例 + §24 典型业务场景（订单/商品/物流）。

## 6. 关联

- 领域模型：`specs/v0.1/domain-model.md`
- API 设计：`specs/v0.1/api-design.md`
