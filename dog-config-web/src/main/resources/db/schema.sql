-- Business Config 数据库结构
-- 对应规格：specs/v0.1/database-design.md
-- 兼容 MySQL 与 H2（MODE=MySQL）

CREATE TABLE IF NOT EXISTS config_page (
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

CREATE TABLE IF NOT EXISTS config_group (
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

CREATE TABLE IF NOT EXISTS config_item (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id       BIGINT        NOT NULL,
    `key`          VARCHAR(256)  NOT NULL,
    name           VARCHAR(128)  NOT NULL,
    `value`        TEXT          NULL,
    `default_value` TEXT         NULL,
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
