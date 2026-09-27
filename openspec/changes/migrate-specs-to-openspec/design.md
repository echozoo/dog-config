## Context

见 proposal.md — Why。本次迁移涉及三类产物：行为规格、设计文档、流程记录。原文是 What/How 混编，且 `harden-config-writes` / `config-item-versioning` 两个在途变更已各自声明能力，迁移时需人工判定归属，避免归档时规格重复。

## Goals / Non-Goals

**Goals:**

- 让 `openspec/specs/` 成为唯一 canonical 的行为规格源。
- 设计文档归入 `doc/design/`，需求澄清归入 `doc/product/`。
- 删除 `specs/v0.1/`，消除双轨。

**Non-Goals:**

- 不改变运行时行为、HTTP API 或数据库结构。
- 不改写、合并或重定义在途变更的能力规格。
- 不迁移过程记录（`tasks.md` / `review.md`）。

## Decisions

**决策 1：按关注点拆成三个能力 —— `config-model` / `config-admin-api` / `config-read-api`**
- 理由：管理端与读取端行为不同，模型定义相对稳定；按关注点拆分后，后续变更可精确 MODIFIED 单个能力，冲突面小。
- 备选：单一 `config` spec（体积大、任何变更都碰它）；按 Page/Group/Item 拆（与「管理/读取」维度正交，不自然）。

**决策 2：基线能力不描述在途变更的行为**
- 理由：写路径校验、软删感知唯一性、父级删除保护归 `config-write-integrity`；值版本、回滚、恢复归 `config-item-versioning`。基线若重复，归档时会与两个 delta 撞车。
- 备选：把已落地的写校验并入 `config-model`（重复定义，拒绝）。

**决策 3：设计文档迁入 `doc/design/`，需求澄清迁入 `doc/product/v0.1/`**
- 映射：`database-design.md` / `api-design.md` / `project-structure.md` / `design-decisions.md` → `doc/design/`；`requirements.md` → `doc/product/v0.1/`；`README.md` 索引废弃。
- 理由：OpenSpec 只在变更内持有 `design.md`，没有持久设计目录；把持久 How 与需求来源分列 `doc/` 下，语义清晰且不与 schema 混淆。
- 备选：`openspec/design/`（OpenSpec 无此约定，易混淆）；不保留设计文档（丢失 How 参考）。

**决策 4：人工重写规格，不用脚本转换**
- 理由：原文 What/How 混编、多数条目缺 Scenario，需要按「可观察行为」判断归属并补 Scenario。
- 备选：脚本机械转换（无法保证语义与 Scenario 质量）。

**决策 5：过程记录随旧目录删除**
- 理由：`tasks.md` / `review.md` 是已完成的执行历史，git 已保留；OpenSpec 的 archive 只存变更，不承接过程文档。
- 备选：迁入 `openspec/changes/archive/`（位置语义不符）。

## Risks / Trade-offs

- [规格与实现漂移] 迁移后规格仍可能偏离代码 → 本变更逐条对照现有代码（`ConfigServiceImpl` / `ConfigAdminService` / 控制器）核对；后续靠 apply/archive 流程兜底。
- [与在途变更冲突] 迁移能力可能与 `harden-config-writes` / `config-item-versioning` 的 delta 重叠 → 决策 2 严格划界，基线不含其行为。
- [拆分丢失细节] 把 `api-design.md` 拆为 spec + design 可能遗漏字段表 → 完整搬迁原文到 `doc/design/api-design.md`，仅把行为提炼进 specs。
- [旧引用残留] 其他文件仍指向 `specs/v0.1/` → 任务含全局检索替换与最终校验。

## Migration Plan

1. 在本次变更中建立三个能力规格（delta）。
2. 将设计文档迁入 `doc/design/`、需求澄清迁入 `doc/product/v0.1/`。
3. 更新 `README.md`、`openspec/config.yaml`、`db/*.sql` 注释中的 `specs/v0.1/` 引用。
4. 删除 `specs/v0.1/`。
5. `openspec validate --strict` 与全局引用检索。

**Rollback**：本变更不触及代码与数据库，回滚即 `git revert` 或恢复 `specs/v0.1/`。

## Open Questions

无。
