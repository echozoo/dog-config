# 需求确认 — Business Config v0.1

> Requirement 层（Why / What 的目标与边界）。
> 原始需求来源：`doc/product/v0.1/业务通用配置管理系统需求规格说明书_v1.0.md`（v1.0）
> 本文件记录需求确认时对需求书的澄清与冻结。

## 1. 需求来源

- 需求规格说明书 v1.0 已存在于仓库 `doc/product/v0.1/`，作为唯一需求来源
- 本次实现不新增/修改需求书内容，仅澄清落地细节

## 2. 确认结论（2026-08-18 探索阶段）

| # | 决策点 | 确认结论 | 依据 |
| --- | --- | --- | --- |
| 1 | 技术栈 | Java 17 + Spring Boot 3 | 需求书 §16 Java 示例 API |
| 2 | 数据访问层 | MyBatis-Plus | 用户选择 |
| 3 | 数据库 | 本地 MySQL（直接读写，V1 不引入 Redis） | 需求书 §23.3 + 用户选择 |
| 4 | 管理后台 | 静态页面（原生 HTML/JS，无前端构建） | 用户选择 |
| 5 | 业务读取通道 | 服务内 Bean + HTTP 双通道 | 需求书 §16/§17 + 用户选择 |
| 6 | 项目结构 | 多模块 sdk + core + web | 用户选择 |
| 7 | Options 存储 | JSON 字符串列 | 需求书 §13 + 用户选择 |
| 8 | 软删除 | MyBatis-Plus @TableLogic（deleted 列），status 保留业务态 | 需求书 §18 + 用户选择 |
| 9 | 包名 | com.echozoo.config | 用户选择 |
| 10 | sdk 独立性 | sdk 独立模块，只定义接口契约，不含实现 | 用户选择 |
| 11 | 默认值语义 | value → defaultValue → 调用方参数 / null | 需求书 §11 澄清（见下） |

## 3. 需求澄清记录

### 3.1 默认值语义（对需求书 §11 的澄清）

需求书 §11 描述「不存在配置 → 使用默认值」。经确认，最终语义：

```text
value 非空            → 使用 value
value 空，defaultValue 非空 → 使用 defaultValue（DB 集中兜底）
value 空，defaultValue 空  → 带参重载返回调用方传入参数；无参重载返回 null
```

- defaultValue 字段保留在 DB（集中管理），业务 SDK 提供带参重载（如 getInt(key, 30)）作为最后一层兜底

### 3.2 其他澄清

- 需求书 §30 的后续设计顺序（领域模型 → 数据库 → API → …）与本次 Spec/Design/Tasks 流程一致
- V1 明确不做项沿用需求书 §26，不新增

## 4. 边界（Out of Scope）

沿用需求书 §3 与 §26，V1 不实现：实时推送、Long Polling、MQ、Raft、服务发现、多租户、复杂权限、配置版本、审批、灰度、Redis 缓存。

## 5. 关联

- 项目规格：`openspec/specs/`
- 设计文档：`doc/design/`
