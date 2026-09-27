## Purpose

为配置项的值提供版本历史、查询、回滚与值级恢复能力，使配置值的每一次变更都可追溯、可撤销，同时保持业务读取仍只依赖当前值。

## ADDED Requirements

### Requirement: 值变更产生版本记录

系统 SHALL 在配置项的值发生变更时追加一条版本记录。版本记录 SHALL 保存该时刻的 `value`、`defaultValue` 与 `valueType`，以及变更类型与时间。仅 `value` / `defaultValue` 变化的操作产生版本；仅 `name`、`description`、`status`、`sort` 等变化的操作 SHALL NOT 产生版本。版本号 SHALL 在同一配置项内从 1 开始递增，且不可被修改或删除。

#### Scenario: 创建配置项产生初始版本

- **WHEN** 创建一个配置项
- **THEN** 该配置项获得版本 1，变更类型为 CREATE

#### Scenario: 值变更追加新版本

- **WHEN** 更新一个已有配置项的 `value` 或 `defaultValue`
- **THEN** 追加一条版本，版本号在既有最大值上递增，变更类型为 UPDATE

#### Scenario: 仅非值字段变更不产生版本

- **WHEN** 仅修改配置项的 `name` / `description` / `status` / `sort`
- **THEN** 不产生新的版本记录

#### Scenario: 软删除不产生版本

- **WHEN** 软删除一个配置项
- **THEN** 不产生新的版本记录

### Requirement: 版本历史可查询

系统 SHALL 提供查询某配置项全部版本及单个版本详情的能力，并保持版本按版本号有序返回。查询不存在的配置项或版本 SHALL 返回 NOT_FOUND（资源不存在）。

#### Scenario: 列出配置项的版本历史

- **WHEN** 请求某配置项的版本列表
- **THEN** 返回其所有版本，按版本号有序

#### Scenario: 查询单个版本详情

- **WHEN** 请求某配置项的一个已存在版本
- **THEN** 返回该版本保存的值与元信息

#### Scenario: 查询不存在的版本

- **WHEN** 请求某配置项一个不存在的版本号
- **THEN** 返回 NOT_FOUND（资源不存在）

### Requirement: 回滚到指定历史版本

系统 SHALL 支持将配置项的当前值回滚到指定历史版本：把该版本的 `value` / `defaultValue` 写回当前配置项，并追加一条变更类型为 ROLLBACK 的新版本。回滚 SHALL NOT 删除或修改既有的中间版本。

#### Scenario: 回滚写入旧值并保留历史

- **WHEN** 请求把配置项回滚到版本 N
- **THEN** 当前值变为版本 N 的值，且追加一条 ROLLBACK 版本，版本 1..N 及之后的版本均保持不变

#### Scenario: 回滚到不存在版本

- **WHEN** 请求回滚到一个不存在的版本号
- **THEN** 返回 NOT_FOUND（资源不存在），且当前值不变

#### Scenario: 回滚后可再次读取

- **WHEN** 回滚完成后业务读取该配置项
- **THEN** 读取到版本 N 的值

### Requirement: 软删配置项的值级恢复

系统 SHALL 支持恢复一个已软删除的配置项，使其重新可被读取，并追加一条变更类型为 RESTORE 的版本。恢复 SHALL NOT 改变其既有版本历史。

#### Scenario: 恢复已删除配置项

- **WHEN** 请求恢复一个已软删除的配置项
- **THEN** 该配置项重新可被读取，并追加一条 RESTORE 版本

#### Scenario: 恢复不存在或未删除的配置项

- **WHEN** 请求恢复一个不存在或未被软删除的配置项
- **THEN** 返回 NOT_FOUND（资源不存在）

### Requirement: 版本能力不影响读取语义

版本历史的引入 SHALL NOT 改变业务读取行为：读取始终返回配置项的当前值，且沿用既有的 value → defaultValue → 调用方参数 / null 语义。

#### Scenario: 读取返回当前值

- **WHEN** 配置项存在多个历史版本后业务读取该配置项
- **THEN** 返回当前值，而不是任何历史版本值
