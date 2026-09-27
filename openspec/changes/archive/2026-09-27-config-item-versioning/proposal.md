## Why

配置值一旦改动就丢失了旧值：改错了无法回滚，也无法追溯「这个配置以前是什么」。v0.1 只有当前值，没有历史。作为被多个业务系统依赖的稳定契约，配置的每次值变更都应当可追溯、可回滚。

## What Changes

- 新增配置项版本记录：每当配置项的 `value` / `defaultValue` 发生变更时，追加一条版本快照（**只记值变更**，name/description/status/sort 等变更不产生版本）。
- 版本快照记录 `value`、`defaultValue`、`valueType`，以及 `change_type`（CREATE / UPDATE / ROLLBACK / RESTORE）与时间；**不记录 operator**。
- 新增版本查询接口：列出某配置项的全部版本、查询单个版本详情。
- 新增回滚接口：把指定历史版本的值写回当前配置项，并追加一条 ROLLBACK 版本（append-only，不删除中间版本）。
- 新增恢复接口：恢复一个已软删除的配置项；恢复本身也会追加一条 RESTORE 版本（与 `harden-config-writes` 的 409 提示衔接）。
- **读路径与 SDK 契约不变**：业务读取仍只读当前值，版本历史仅管理端可见。

## Capabilities

### New Capabilities

- `config-item-versioning`: 配置项值变更的历史记录、查询、回滚，以及软删配置项的值级恢复。

### Modified Capabilities

<!-- 无：不修改既有能力的规格 -->

## Impact

- `dog-config-core`：新增版本实体 / Mapper / 版本写入与读取支撑。
- `dog-config-web`：新增版本查询与回滚/恢复端点；`ConfigAdminService` 接入版本写入。
- 数据库：新增 `config_item_version` 表；需同步 `db/schema.sql` 与 `doc/design/database-design.md`。
- 读路径、SDK 契约、缓存：无变更。
- 依赖前置：恢复端点依赖 `harden-config-writes` 提供的「删后同名 409」语义。
