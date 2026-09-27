## Why

v0.1 的写路径不校验配置值：非法值（如把 `abc` 写进 INTEGER）在写入时无感，直到业务读取才抛异常。同时唯一性检查经 MyBatis-Plus `@TableLogic` 自动过滤软删行，删除某个 key 后再用同名 key 创建会绕过服务检查、直接撞数据库唯一索引抛出原始 SQL 异常。这些正确性缺口让写路径不可信。

## What Changes

- 写入校验：`value` / `defaultValue` 必须能按该 item 的 `valueType` 解析；`options` 必须是合法 JSON；`componentType` 必须与 `valueType` 兼容；`required` 为真时当前值或默认值至少一个非空；不合法返回 400。
- 抽出 core 内共享的值编解码器（`ValueTypeCodec`），使读路径解析与写路径校验使用同一份规则，消除漂移。
- 唯一性检查覆盖软删记录：创建 Page/Group/Item 时若命中已软删的同 `code` / `key`，返回语义明确的 409（而不是底层唯一索引异常）。
- 父级删除保护：删除存在未删子级的 Page / Group 时返回 409，需先清空子级。

## Capabilities

### New Capabilities

- `config-write-integrity`: 配置写路径的校验与生命周期完整性保证，覆盖值/选项/组件/必填校验、软删感知的唯一性、父级删除保护。

### Modified Capabilities

<!-- 无：openspec/specs 为空，本次不修改既有能力 -->

## Impact

- `dog-config-core`：新增值编解码器（校验 + 解析同源）；实体与 Mapper 结构不变。
- `dog-config-web`：`ConfigAdminService` 写入逻辑接入校验；DTO 校验与错误码（400/409）补充。
- 数据库：无 schema 变更。
- 读路径与 SDK 契约：无变更。
- 既有文档：`openspec/specs/config-read-api/spec.md` 的读取解析语义需在实现后同步说明校验侧约束。
