## Why

当前规格来源不唯一：`specs/v0.1/` 是一套手工维护的 Requirement → Spec → Design → Tasks 文档，`openspec/` 又在独立记录增量变更（`harden-config-writes`、`config-item-versioning`）。两者没有同步机制，而 `openspec/specs/` 至今为空——「canonical 规格在哪」不明确，v0.1 文档只能靠人工追赶变更（例如 `domain-model.md` 已被手工改到包含尚未归档的写校验行为）。既然已决定 openspec 取代 `specs/v0.1/`，需要把 v0.1 的行为规格正式迁入 openspec，形成唯一规格源，再删除旧目录。

## What Changes

- 将 `specs/v0.1/` 中描述**可观察行为**的内容，重整为 `openspec/specs/` 下的能力规格（`config-model` / `config-admin-api` / `config-read-api`），补齐 Purpose、SHALL 需求与 WHEN/THEN Scenario。
- 将 `specs/v0.1/` 中的**设计类内容**（database-design、project-structure、design-decisions）迁移到约定位置（见 design.md）。
- 删除 `specs/v0.1/` 目录，并同步 `README.md`、`openspec/config.yaml` 中所有指向 `specs/v0.1/` 的表述（README 已以 openspec 为 canonical）。
- 与在途变更协调：写路径校验/软删唯一/删除保护归 `config-write-integrity`，值版本/回滚/恢复归 `config-item-versioning`；本次基线能力**不重复**描述这些行为。
- **纯规格/文档迁移，不改变**运行时行为、HTTP API 与数据库结构。

## Capabilities

### New Capabilities

- `config-model`: 三级模型（Page / Group / Item）、枚举与值类型、读取解析语义（value → defaultValue → 调用方参数 / null）。
- `config-admin-api`: 管理端 Page / Group / Item 的 CRUD、启停与值更新行为，统一响应与错误码语义。
- `config-read-api`: 读取端单条 / 批量 keys / 前缀读取，以及类型化 SDK 的读取语义。

### Modified Capabilities

<!-- 无：openspec/specs 为空，本次不修改既有能力 -->

## Impact

- 文档：`specs/v0.1/` 删除并迁入 `openspec/specs/`；`openspec/config.yaml` 的 context 中「项目自身规格在 specs/v0.1/」需改为 openspec。
- 规格协调：基线能力避开 `config-write-integrity` / `config-item-versioning` 的行为，防止与在途变更重复定义。
- 代码 / API / 数据库：无变更。
- 需求来源：`doc/product/v0.1/业务通用配置管理系统需求规格说明书_v1.0.md` 与待迁移的 `specs/v0.1/*`。

## Non-goals

- 不修改任何运行时代码或数据库结构。
- 不迁移 `specs/v0.1/tasks.md`、`review.md` 等过程记录（属已完成的执行历史，处置方式见 design.md）。
- 不合并、改写 `harden-config-writes` / `config-item-versioning` 的能力规格。
- 不引入任何新业务能力。
