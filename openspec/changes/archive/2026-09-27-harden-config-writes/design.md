## Context

见 `proposal.md — Why`。当前约束：

- 读路径解析逻辑位于 `ConfigServiceImpl`（core），写路径位于 `ConfigAdminService`（web），两者各自独立；web 依赖 core，因此 core 中共享代码对两条路径都可用。
- 唯一约束在数据库为 `code` / `(page_id, code)` / `key`，不包含 `deleted`；实体使用 MyBatis-Plus `@TableLogic`，框架会给查询自动注入 `deleted = 0`。
- 错误响应由 `GlobalExceptionHandler` 统一包装为 `ApiResponse`，业务错误码集中在 `ErrorCode`（`BAD_REQUEST=40000`、`CONFLICT=40900`、`NOT_FOUND=40400`）。

## Goals / Non-Goals

**Goals:**

- 让「非法配置值」在写入侧被拒绝，读取侧不再出现类型转换爆炸。
- 让软删除与唯一约束的交互语义明确：删后同名创建得到 `CONFLICT` 而非数据库异常。
- 防止误删仍有内容的 Page / Group。

**Non-Goals:**

- 不引入缓存、鉴权、审计（operator）。
- 不改变读语义（禁用父级仍不影响子级读取）。
- 不做级联删除、不做恢复端点（恢复由 `config-item-versioning` 承载）。
- 不修改数据库 schema。

## Decisions

### D1：core 内共享值编解码器，写校验与读解析同源

新增 core 组件（暂名 `ValueTypeCodec`），提供两个能力：

- `validate(ValueType, raw)`：判断能否按类型解析，非法抛 `IllegalArgumentException`（由既有全局处理映射为 `BAD_REQUEST`）。
- `parse(ValueType, raw)`：读取时的类型转换。

`ConfigServiceImpl.parseValue` 迁移到编解码器；`ConfigAdminService` 的 create/update/updateValue 在落库前调用 `validate`。

**备选**：在 DTO 上用 Bean Validation 注解——被否，因为值的类型由请求体中的 `valueType` 动态决定，静态注解无法表达。

### D2：componentType ↔ valueType 采用白名单

```
SWITCH           → BOOLEAN
NUMBER           → INTEGER | LONG | DECIMAL
INPUT            → STRING
TEXTAREA         → STRING | JSON
SELECT | RADIO   → STRING
```

不匹配即 `BAD_REQUEST`。该白名单与现有种子数据一致（`INTEGER+NUMBER`、`BOOLEAN+SWITCH`、`STRING+SELECT`），不会拒绝存量组合。

**备选**：全量笛卡尔校验——被否，容易误伤未来合理组合；只收紧语义明确的映射。

### D3：唯一性检查用自定义 SQL 绕过逻辑删除过滤

在三个 Mapper 上各加一个「包含已删」的查询方法（显式 `@Select`，不经过 MyBatis-Plus 逻辑删除注入），返回匹配记录及其 `deleted` 状态。服务层据此：

- 命中未删记录 → `CONFLICT`（现有行为）。
- 命中已删记录 → `CONFLICT`，消息区分「已被删除占用」，并提示可用恢复能力（恢复端点由版本 change 提供）。

**备选**：唯一索引加入 `deleted`（`deleted` 删除时置为 id）——被否，需自定义删除旁路 `@TableLogic`，且与后续「恢复」语义冲突。

### D4：required 语义 = 至少一个取值非空

`required = true` 时，`value` 与 `defaultValue` 至少一个非空，否则 `BAD_REQUEST`。不做「读取时强制」，避免影响读路径。

### D5：父级删除保护走服务层计数

删除 Page 前统计其下未删 Group 数，删除 Group 前统计未删 Item 数；大于 0 返回 `CONFLICT`。计数使用既有 Mapper 查询（自动过滤软删），语义正确：只有未删子级会阻止删除。

## Risks / Trade-offs

- [存量非法数据] 数据库里可能已有不满足新校验的旧值 → 校验只作用于新写入，读路径不变，旧数据不受影响。
- [白名单过严] 未来的合理组合可能被拒 → 白名单集中在一处，扩展成本低；必要时放开。
- [自定义 SQL 绕过逻辑删除] 若未来引入多租户等全局过滤，自定义 SQL 需同步 → 目前仅 3 处，集中注释说明。
- [恢复能力跨 change] 本 change 只保证冲突返回 `CONFLICT`；真正的恢复端点在后一 change，期间用户会看到「已被删除占用」的提示。

## Migration Plan

- 无 schema 变更，无数据迁移。发布新构建即可；回滚只需回退镜像/包。
- 上线后建议跑一次存量数据体检（只读脚本）确认是否有非法值，作为后续数据治理输入，不阻塞发布。
