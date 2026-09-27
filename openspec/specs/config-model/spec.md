# config-model Specification

## Purpose

定义 Page → Group → Item 三级配置模型、配置项字段语义、值/组件/状态枚举与软删除生命周期，作为配置管理与业务读取共同遵循的领域契约。

## Requirements

### Requirement: 三级配置模型与唯一标识

系统 SHALL 以 Page → Group → Item 组织配置：每个 Group 归属一个 Page，每个 Item 归属一个 Group。Page 的 `code`、同一 Page 下 Group 的 `code`、Item 的 `key` SHALL 各自唯一；发生重复时返回 CONFLICT（唯一性冲突）。唯一性 SHALL 覆盖已软删除的记录：与已软删除记录同名时 SHALL 返回 CONFLICT（唯一性冲突）并给出可识别的冲突提示，而不得抛出底层数据库唯一索引异常。

#### Scenario: 建立三级结构

- **WHEN** 依次创建一个 Page、其下的 Group、Group 下的 Item
- **THEN** 结构建立成功，Item 与其 Group、Page 的归属关系可查询

#### Scenario: Page code 重复被拒

- **WHEN** 创建与现有 Page 相同 `code` 的 Page
- **THEN** 返回 CONFLICT（唯一性冲突），且不新增记录

#### Scenario: 同一 Page 下 Group code 重复被拒

- **WHEN** 在同一 Page 下创建相同 `code` 的 Group
- **THEN** 返回 CONFLICT（唯一性冲突）

#### Scenario: Item key 全局重复被拒

- **WHEN** 创建与现有 Item 相同 `key` 的 Item
- **THEN** 返回 CONFLICT（唯一性冲突）

#### Scenario: 与已软删除记录同名被拒

- **WHEN** 创建 `code` / `key` 与某条已软删除记录相同的 Page / Group / Item
- **THEN** 返回 CONFLICT（唯一性冲突），并给出可识别的冲突提示，而不是数据库唯一索引异常

### Requirement: 配置项字段语义

每个配置项 SHALL 包含 `key`、`name`、`value`、`defaultValue`、`valueType`、`componentType`、`options`、`description`、`required`、`status`、`sort`。其中 `value` 与 `defaultValue` SHALL 以字符串原始形式存储并按 `valueType` 解释；`options` SHALL 以 JSON 字符串存储，供 SELECT / RADIO 使用；`componentType` SHALL 表达管理端的呈现方式。

#### Scenario: 配置项携带类型与组件信息

- **WHEN** 创建 `valueType=INTEGER`、`componentType=NUMBER` 的配置项
- **THEN** 该配置项被保存，并保留其值类型、组件类型与可选择项定义

### Requirement: 值类型与组件类型取值

系统 SHALL 支持值类型 STRING / INTEGER / LONG / DECIMAL / BOOLEAN / JSON，以及组件类型 INPUT / NUMBER / SWITCH / SELECT / RADIO / TEXTAREA。

#### Scenario: 使用受支持的类型创建配置

- **WHEN** 以本规格列出的值类型与组件类型的合法组合创建配置项
- **THEN** 创建成功

### Requirement: 配置状态与软删除生命周期

配置项的 `status` SHALL 取 ACTIVE 或 DISABLED。仅 ACTIVE 的配置项可被业务读取，DISABLED 的配置项对业务读取不可见。删除 SHALL 为软删除，被删除的记录 SHALL NOT 出现在默认的管理与读取结果中。

#### Scenario: 禁用的配置不可被读取

- **WHEN** 一个配置项 `status=DISABLED`，业务读取其 `key`
- **THEN** 该配置按不存在处理，不被返回

#### Scenario: 软删除后不可见

- **WHEN** 软删除一个配置项
- **THEN** 默认列表与业务读取均不再返回该配置项

### Requirement: 写入时按 valueType 校验配置值

系统 SHALL 在创建或更新配置项（含单独更新 value）时，校验 `value` 与 `defaultValue` 能否按该配置项的 `valueType` 解析；不能解析时拒绝写入并返回 BAD_REQUEST（参数校验失败）。校验与读取解析 SHALL 使用同一份类型规则。

#### Scenario: 合法值写入成功

- **WHEN** 以 `valueType=INTEGER`、`value="30"` 创建或更新配置项
- **THEN** 写入成功并返回该配置项

#### Scenario: 非法值写入被拒

- **WHEN** 以 `valueType=INTEGER`、`value="abc"` 创建或更新配置项
- **THEN** 返回 BAD_REQUEST（参数校验失败），且该配置项未被写入或修改

#### Scenario: 默认值非法写入被拒

- **WHEN** 以 `valueType=BOOLEAN`、`defaultValue="maybe"` 创建或更新配置项
- **THEN** 返回 BAD_REQUEST（参数校验失败），且该配置项未被写入或修改

#### Scenario: 值为空不影响校验

- **WHEN** 以 `valueType=INTEGER`、`value` 为空、`defaultValue="30"` 创建或更新配置项
- **THEN** 写入成功

### Requirement: 组件类型与选项合法性

系统 SHALL 在写入时校验 `componentType` 与 `valueType` 兼容，并校验 `options` 的合法性；不满足时返回 BAD_REQUEST（参数校验失败）。

#### Scenario: 组件类型与值类型不兼容被拒

- **WHEN** 以 `valueType=BOOLEAN`、`componentType=INPUT` 创建配置项
- **THEN** 返回 BAD_REQUEST（参数校验失败）

#### Scenario: 选项组件缺少或非法 options 被拒

- **WHEN** 以 `componentType=SELECT` 创建配置项，且 `options` 缺失或不是合法 JSON 数组
- **THEN** 返回 BAD_REQUEST（参数校验失败）

#### Scenario: 合法选项写入成功

- **WHEN** 以 `componentType=SELECT`、`valueType=STRING`、`options` 为合法 JSON 数组创建配置项
- **THEN** 写入成功

### Requirement: 必填约束

系统 SHALL 在写入时校验 `required` 约束：当配置项 `required` 为真时，`value` 与 `defaultValue` SHALL 至少有一个非空；否则返回 BAD_REQUEST（参数校验失败）。

#### Scenario: 必填但无任何取值被拒

- **WHEN** 以 `required=true`、`value` 与 `defaultValue` 均为空创建或更新配置项
- **THEN** 返回 BAD_REQUEST（参数校验失败）

#### Scenario: 必填但提供默认值通过

- **WHEN** 以 `required=true`、`value` 为空、`defaultValue` 非空创建或更新配置项
- **THEN** 写入成功

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
