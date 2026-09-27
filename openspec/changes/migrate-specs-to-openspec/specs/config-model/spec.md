## Purpose

定义 Page → Group → Item 三级配置模型、配置项字段语义、值/组件/状态枚举与软删除生命周期，作为配置管理与业务读取共同遵循的领域契约。

## ADDED Requirements

### Requirement: 三级配置模型与唯一标识

系统 SHALL 以 Page → Group → Item 组织配置：每个 Group 归属一个 Page，每个 Item 归属一个 Group。Page 的 `code`、同一 Page 下 Group 的 `code`、Item 的 `key` SHALL 各自唯一；发生重复时返回 CONFLICT（唯一性冲突）。

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
