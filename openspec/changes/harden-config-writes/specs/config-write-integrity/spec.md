## Purpose

保证配置写路径的合法性：非法配置值在写入时即被拒绝，唯一性与删除操作在软删除模型下语义明确，使业务系统读取到的配置始终可信。

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

### Requirement: 校验组件类型、选项与值类型的兼容性

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

### Requirement: 唯一性检查覆盖软删除记录

系统 SHALL 在创建 Page / Group / Item 时，将已软删除的同 `code` / `key` 记录视作占用；冲突时返回语义明确的 409，而不得因底层唯一索引抛出未处理的数据库异常。

#### Scenario: 与未删除记录冲突

- **WHEN** 创建与现有未删除 Page（或 Group、Item）相同 `code` / `key` 的记录
- **THEN** 返回 CONFLICT（唯一性冲突），且不新增记录

#### Scenario: 与已软删除记录冲突

- **WHEN** 创建 `code` / `key` 与某条已软删除记录相同的 Page / Group / Item
- **THEN** 返回 CONFLICT（唯一性冲突），并给出可识别的冲突提示，而不是数据库唯一索引异常

### Requirement: 父级删除保护

系统 SHALL 阻止删除仍有未删除子级的 Page / Group；存在未删除子级时返回 CONFLICT（唯一性冲突），要求先清空子级。

#### Scenario: 删除含未删 Group 的 Page 被拒

- **WHEN** 删除一个仍存在未删除 Group 的 Page
- **THEN** 返回 CONFLICT（唯一性冲突），且该 Page 未被删除

#### Scenario: 删除含未删 Item 的 Group 被拒

- **WHEN** 删除一个仍存在未删除 Item 的 Group
- **THEN** 返回 CONFLICT（唯一性冲突），且该 Group 未被删除

#### Scenario: 子级已全部软删除允许删除

- **WHEN** 删除一个其下子级均已软删除的 Page / Group
- **THEN** 删除成功
