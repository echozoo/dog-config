## ADDED Requirements

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

## MODIFIED Requirements

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
