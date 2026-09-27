# 数据库设计 — Business Config v0.1

> 对应领域规格：`openspec/specs/config-model/spec.md`、`openspec/specs/config-read-api/spec.md`
> 本文件定义 MySQL 表结构、索引与数据访问模式（Design 层，How）。

## 1. ER 关系

```text
config_page
    │ 1:N
    ▼
config_group
    │ 1:N
    ▼
config_item
    │ 1:N
    ▼
config_item_version
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

CREATE TABLE config_item_version (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id        BIGINT        NOT NULL,
    version_no     INT           NOT NULL,
    `value`        TEXT          NULL,
    `default_value` TEXT         NULL,
    value_type     VARCHAR(32)   NULL,
    change_type    VARCHAR(32)   NOT NULL,
    created_at     DATETIME      NOT NULL,
    CONSTRAINT uk_config_item_version UNIQUE (item_id, version_no),
    CONSTRAINT fk_config_item_version_item FOREIGN KEY (item_id) REFERENCES config_item (id)
);
```

要点：

- `deleted` 列：MyBatis-Plus `@TableLogic` 逻辑删除标记（0 未删除 / 1 已删除）
- `key` 为 SQL 保留字，使用反引号
- `value` / `default_value` / `options` 用 TEXT（可存 JSON / 长文本）
- 主键自增，外键建索引
- `config_item_version` 为 append-only 版本历史：只在 `value` / `default_value` 变更时追加，无 `deleted` 列（历史不可删）、无 operator；`change_type` 取 CREATE / UPDATE / ROLLBACK / RESTORE

## 3. 索引设计

| 索引 | 列 | 目的 |
| --- | --- | --- |
| uk_config_page_code | code | Page 编码唯一（业务契约） |
| uk_config_group_page_code | (page_id, code) | Group 分组唯一 |
| uk_config_item_key | `key` | Item 配置 Key 唯一（业务访问主键） |
| uk_config_item_version | (item_id, version_no) | 配置项版本号在 item 内唯一、顺序递增 |

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

- 领域规格：`openspec/specs/config-model/spec.md`
- API 设计：`doc/design/api-design.md`

## 7. 换数据库（魔改数据访问层）

持久化基于 MyBatis-Plus，业务逻辑与数据库无关。切换到其他数据库只需改动以下位置，其余代码无需改动。

### 7.1 需要改动的位置

| # | 位置 | 要改什么 |
| --- | --- | --- |
| 1 | `dog-config-web/pom.xml` | 替换 `com.mysql:mysql-connector-j` 为目标库 JDBC 驱动 |
| 2 | `dog-config-web/src/main/resources/application.yml` | `spring.datasource.url` 与 `driver-class-name` |
| 3 | `db/schema.sql` | 建表 DDL 方言 |
| 4 | `db/data.sql` | 种子数据方言 |
| 5 | `ConfigPageMapper` / `ConfigGroupMapper` / `ConfigItemMapper` | 自定义 SQL |

### 7.2 MySQL 专属语法清单

- DDL（`db/schema.sql`）：`AUTO_INCREMENT`、`TINYINT(1)`、反引号（`` `key` `` / `` `value` `` / `` `default_value` ``）、`TEXT`、`DATETIME`
- 种子（`db/data.sql`）：`INSERT IGNORE`、`NOW()`
- 自定义 SQL（3 个 Mapper，共 5 个方法）：反引号与 `LIMIT 1`

### 7.3 保留字提示

`key` 与 `value` 是 SQL 保留字，本仓库用反引号规避；换库时需按目标库改用对应引用符（如 PostgreSQL 的双引号），或避免使用该列名。

### 7.4 排查方法

全局搜索**反引号**、`LIMIT`、`driver`、`AUTO_INCREMENT`，逐一按目标库方言替换；实体、Mapper 接口、Service 逻辑与读取语义均无需改动。
